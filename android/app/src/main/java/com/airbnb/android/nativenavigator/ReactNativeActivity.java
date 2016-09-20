package com.airbnb.android.nativenavigator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.airbnb.android.AirbnbApplication;
import com.airbnb.android.BugsnagWrapper;
import com.airbnb.android.BuildConfig;
import com.airbnb.android.R;
import com.airbnb.android.R2;
import com.airbnb.android.activities.AirActivity;
import com.airbnb.n2.ViewLibUtils;
import com.airbnb.n2.components.AirToolbar;
import com.airbnb.n2.transition.AutoSharedElementCallback;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.State;

import static com.airbnb.android.react.NavigatorModule.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.NavigatorModule.EXTRA_PAYLOAD;
import static com.airbnb.android.utils.AnimationUtils.makeSlideLeftAnimation;
import static com.airbnb.android.react.ReactNativeUtils.REACT_MODULE_NAME;
import static com.airbnb.android.react.ReactNativeUtils.REACT_PROPS;
import static com.airbnb.android.react.ReactNativeUtils.maybeEmitEvent;

// TODO(lmr): we need to figure out how to do the base activity "currently active" accounting that we do in AirActivity

public class ReactNativeActivity extends Activity implements DefaultHardwareBackBtnHandler {
    /**
     * Target >= 5.1 because of https://app.bugsnag.com/airbnb/android-1/errors/576174ba26963cde6fd02002?filters[error.status][]=in%20progress&filters[event.severity][]=error&filters[error.assigned_to][]=me&pivot_tab=event
     * http://stackoverflow.com/questions/34658911/entertransitioncoordinator-causes-npe-in-android-5-0
     */
    private static final int SHARED_ELEMENT_TARGET_API = VERSION_CODES.LOLLIPOP_MR1;
    /**
     * We just need lollipop (not MR1) for the postponed slide in transition
     */
    private static final int WAITING_TRANSITION_TARGET_API = VERSION_CODES.LOLLIPOP;
    private static final String RESULT_CODE = "resultCode";
    private static final String ON_LEFT_PRESS = "onLeftPress";
    private static final String ON_BUTTON_PRESS = "onButtonPress";
    private static final String ON_LINK_PRESS = "onLinkPress";
    private static final String ON_DISAPPEAR = "onDisappear";
    private static final String ON_APPEAR = "onAppear";
    private static final String SCENE_INSTANCE_ID_PROP = "sceneInstanceId";
    private static final String TAG = "ReactNativeActivity";
    private static final String SHARED_ELEMENT_TRANSITION_GROUP_OPTION = "transitionGroup";
    private static final int SHARED_ELEMENT_TIMEOUT_IN_MS = 250;
    // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
    private static int UUID = 1;

    private String instanceId;
    private List<MenuButton> menuButtons;
    private String link;
    private ReactNativeActivityManager activityManager;
    private final Handler transitionHandler = new Handler();
    private boolean isWaitingForSharedElements = false;

    @BindView(R2.id.toolbar) Toolbar toolbar;
    @BindView(R2.id.reactRootView) ReactRootView reactRootView;

    // TODO(lmr): need way to get instance manager
    @Inject ReactInstanceManager reactInstanceManager;

    // If set to true, this Activity will be dismissed when its Toolbar NavigationIcon (home button) is clicked,
    // instead of performing the default behavior (finish)
    @State boolean dismissOnFinish;

    public static Intent intent(Context context, String moduleName) {
        return intent(context, moduleName, null);
    }

    public static Intent intent(Context context, String moduleName, Bundle props) {
        return new Intent(context, ReactNativeActivity.class)
                .putExtras(ReactNativeUtils.intentExtras(moduleName, props));
    }

    public static Intent intentWithDismissFlag() {
        return new Intent().putExtra(EXTRA_IS_DISMISS, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AirbnbApplication.get(this).component().inject(this);

        if (!performChecks()) {
            return;
        }

        activityManager = new ReactNativeActivityManager(this);
        Intent intent = getIntent();
        setContentView(R.layout.activity_react_native);
        ButterKnife.bind(this);

        String moduleName = intent.getStringExtra(REACT_MODULE_NAME);
        Bundle props = intent.getBundleExtra(REACT_PROPS);
        if (props == null) {
            props = new Bundle();
        }
        instanceId = String.format(Locale.ENGLISH, "%1s_%2$d", moduleName, UUID++);
        props.putString(SCENE_INSTANCE_ID_PROP, instanceId);

        reactNavigationCoordinator.registerActivity(this, instanceId);

        // TODO(lmr): compat?
        setToolbar(toolbar);

        reactRootView.startReactApplication(reactInstanceManager, moduleName, props);

        setupTransition(intent);
    }

    public void notifySharedElementAddition() {
        if (isWaitingForSharedElements) {
            // if we are receiving a sharedElement and we have postponed the enter transition, we want to cancel any existing
            // handler and create a new one. (this is effectively debouncing the call).
            transitionHandler.removeCallbacksAndMessages(null);
            transitionHandler.post(() -> {
                isWaitingForSharedElements = false;
                supportStartPostponedEnterTransition();
            });
        }
    }

    private void setupTransition(Intent intent) {
        // Shared element transitions have been unreliable on Lollipop < MR1.
        if (Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API && ReactNativeUtils.isSharedElementTransition(intent)) {
            setupSharedElementTransition();
        } else if (isSuccessfullyInitialized() && Build.VERSION.SDK_INT >= WAITING_TRANSITION_TARGET_API) {
            setupDefaultWaitingForRenderTransition();
        }
    }

    @TargetApi(SHARED_ELEMENT_TARGET_API)
    private void setupSharedElementTransition() {
        supportPostponeEnterTransition();

        // We are doing a shared element transition...
        setEnterSharedElementCallback(new AutoSharedElementCallback(this));

        // in this case, we end up waiting for shared elements to get rendered before
        // doing the transition. If this never happens for some reason, we are going to push
        // anyway in 250ms. The handler should get canceled + called sooner though (it's a race).
        isWaitingForSharedElements = true;
        transitionHandler.postDelayed(() -> {
            isWaitingForSharedElements = false;
            supportStartPostponedEnterTransition();
        }, SHARED_ELEMENT_TIMEOUT_IN_MS);
    }

    @TargetApi(WAITING_TRANSITION_TARGET_API)
    private void setupDefaultWaitingForRenderTransition() {
        supportPostponeEnterTransition();

        getWindow().setEnterTransition(makeSlideLeftAnimation());

        // Delay the enter animation so we can wait for the Activity to be laid out
        // at least once, otherwise the animation might flash, making it look bad.
        // We can't delay simple "overridePendingTransition()" animations though, so
        // we need to use this custom Slide animation
        reactRootView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                reactRootView.getViewTreeObserver().removeOnPreDrawListener(this);
                // Now we can finally start the enter transition
                supportStartPostponedEnterTransition();
                return true;
            }
        });
    }

    /**
     * Returns whether or not React Native has been correctly initialized. If not, finishes the Activity and displays a message to the user.
     */
    private boolean performChecks() {
        if (isSuccessfullyInitialized()) {
            // We're good
            return true;
        }
        // React Native failed to initialize. There's not much we can do about it.
        // Just notify the user and close the Activity.
        // TODO(lmr): better handling here?
        return false;
    }

    protected boolean isSuccessfullyInitialized() {
        return reactInstanceManager.getCurrentReactContext() != null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (toolbar != null) {
            // 0 will prevent menu from getting inflated, since we are inflating manually
            toolbar.onCreateOptionsMenu(0, menu, getMenuInflater());
            createOptionsMenu(menu);
        }
        return true;
    }

    private void createOptionsMenu(Menu menu) {
        if (link != null) {
            menu.add(link).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else if (menuButtons != null) {
            for (MenuButton button : menuButtons) {
                menu.add(button.title)
                        .setIcon(button.icon)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            emitEvent(ON_LEFT_PRESS, null);
            if (!dismissOnFinish) {
                return super.onOptionsItemSelected(item);
            } else {
                dismiss();
                // Return true to consume the event here
                return true;
            }
        }
        if (item.getIcon() != null) {
            // it's one of the buttons
            Menu menu = getToolbar().getMenu();
            // DLS requires that only 0-2 buttons are in the menu
            int index = menu.getItem(0) == item ? 0 : 1;
            emitEvent(ON_BUTTON_PRESS, index);
        } else {
            // it's the link
            emitEvent(ON_LINK_PRESS, null);
        }
        return false;
    }

    public void startActivityWithPromise(Intent intent, Promise promise, @Nullable ReadableMap options) {
        Bundle optionsBundle = null;
        if (options != null) {
            if (options.hasKey(SHARED_ELEMENT_TRANSITION_GROUP_OPTION)) {
                ViewGroup transitionGroup = ViewLibUtils.findViewGroupWithTag(
                        reactRootView,
                        R.id.react_shared_element_group_id,
                        options.getString(SHARED_ELEMENT_TRANSITION_GROUP_OPTION));
                if (transitionGroup != null) {
                    ReactNativeUtils.setIsSharedElementTransition(intent, true);
                    optionsBundle = AutoSharedElementCallback.getActivityOptionsBundle(this, transitionGroup);
                }
            }
        }
        activityManager.startActivityWithPromise(intent, promise, optionsBundle);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        activityManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_MENU) {
            reactInstanceManager.showDevOptionsDialog();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (!dismissOnFinish) {
            reactInstanceManager.onBackPressed();
        } else {
            dismiss();
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reactInstanceManager.onHostPause();
        emitEvent(ON_DISAPPEAR, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reactInstanceManager.onHostResume(this, this);
        emitEvent(ON_APPEAR, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        reactNavigationCoordinator.unregisterActivity(instanceId);
        if (reactRootView != null) {
            reactRootView.unmountReactApplication();
        }
    }

    public void setMenuButtons(List<MenuButton> buttons) {
        menuButtons = buttons;
        supportInvalidateOptionsMenu();
    }

    public void setLink(String link) {
        this.link = link;
        supportInvalidateOptionsMenu();
    }

    public ReactRootView getRootView() {
        return reactRootView;
    }

    public AirToolbar getToolbar() {
        return toolbar;
    }

    private void emitEvent(String eventName, Object object) {
        if (isSuccessfullyInitialized()) {
            String key = String.format(Locale.ENGLISH, "NativeNavigatorScene.%s.%s", eventName, instanceId);
            maybeEmitEvent(reactInstanceManager.getCurrentReactContext(), key, object);
        }
    }

    public void dismissOnFinish() {
        dismissOnFinish = true;
    }

    void dismiss() {
        dismiss(null);
    }

    void dismiss(@Nullable ReadableMap payload) {
        Intent intent = new Intent()
                .putExtra(EXTRA_IS_DISMISS, isDismissable())
                .putExtra(EXTRA_PAYLOAD, payloadToMap(payload));
        setResult(getResultCodeFromPayload(payload), intent);
        finish();
    }

    private boolean isDismissable() {
        return dismissOnFinish || ReactNativeActivityManager.isDismissable(this);
    }

    void pop(@Nullable ReadableMap payload) {
        Intent intent = new Intent().putExtra(EXTRA_PAYLOAD, payloadToMap(payload));
        setResult(getResultCodeFromPayload(payload), intent);
        finish();
    }

    private static HashMap<String, Object> payloadToMap(@Nullable ReadableMap payload) {
        return payload == null
                ? Maps.newHashMap()
                : Maps.newHashMap(ConversionUtil.toMap(payload));
    }

    /**
     * Returns the Activity result_code from the ReadableMap payload or RESULT_OK if none found.
     * <p>
     * Throws IllegalArgumentException if the resultCode is not a number.
     */
    private static int getResultCodeFromPayload(@Nullable ReadableMap payload) {
        if (payload == null) {
            return RESULT_OK;
        }
        if (!payload.hasKey(RESULT_CODE)) {
            return RESULT_OK;
        }
        if (payload.getType(RESULT_CODE) != ReadableType.Number) {
            throw new IllegalArgumentException("Found non-integer resultCode.");
        }
        return payload.getInt(RESULT_CODE);
    }
}
