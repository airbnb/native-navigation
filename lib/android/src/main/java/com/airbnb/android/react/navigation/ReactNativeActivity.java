package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import com.airbnb.android.utils.AndroidVersion;
import com.airbnb.android.utils.SimpleTransitionListener;
import com.airbnb.android.sharedelement.AutoSharedElementCallback;

import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.util.List;
import java.util.Locale;

import static com.airbnb.android.react.navigation.NavigatorModule.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.navigation.ReactNativeIntents.REACT_MODULE_NAME;
import static com.airbnb.android.react.navigation.ReactNativeIntents.REACT_PROPS;

public class ReactNativeActivity extends ReactAwareActivity implements ReactInterface, DefaultHardwareBackBtnHandler, PermissionAwareActivity {
    private static final int SHARED_ELEMENT_TARGET_API = VERSION_CODES.LOLLIPOP_MR1;
    /** We just need lollipop (not MR1) for the postponed slide in transition */
    private static final int WAITING_TRANSITION_TARGET_API = VERSION_CODES.LOLLIPOP;

    private static final String ON_LEFT_PRESS = "onLeftPress";
    private static final String ON_ENTER_TRANSITION_COMPLETE = "onEnterTransitionComplete";
    private static final String ON_BUTTON_PRESS = "onButtonPress";
    private static final String ON_LINK_PRESS = "onLinkPress";
    private static final String ON_DISAPPEAR = "onDisappear";
    private static final String ON_APPEAR = "onAppear";
    private static final String INSTANCE_ID_PROP = "nativeNavigationScreenInstanceId";
    private static final int RENDER_TIMEOUT_IN_MS = 700;
    private static final int FAKE_ENTER_TRANSITION_TIME_IN_MS = 500;
    private static final String TAG = "ReactNativeActivity";
    // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
    private static int UUID = 1;

    private String instanceId;
//    private List<MenuButton> menuButtons;
    private String link;
    private ReactInterfaceManager activityManager;
    private final Handler transitionHandler = new Handler();
    private boolean isWaitingForRenderToFinish = false;
    private boolean isSharedElementTransition = false;
    private @Nullable PermissionListener permissionListener;

    ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;
    ReactInstanceManager reactInstanceManager;

    ReactToolbarFacade toolbar;
    @Nullable ReactRootView reactRootView;

    public static Intent intentWithDismissFlag() {
        return new Intent().putExtra(EXTRA_IS_DISMISS, true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String moduleName = getIntent().getStringExtra(REACT_MODULE_NAME);

        int color = reactNavigationCoordinator.getBackgroundColorForModuleName(moduleName);
        // TODO(lmr): create a style for this...
//        if (color == Color.TRANSPARENT) {
//            // This needs to happen before setContentView gets called
//            setTheme(R.style.Theme_Airbnb_ReactTranslucent);
//        }
        setContentView(R.layout.activity_react_native);
        toolbar = findViewById(R.id.toolbar);
        setToolbar(toolbar);

        if (!isSuccessfullyInitialized()) {
            // TODO(lmr): move to utils
            reactInstanceManager.addReactInstanceEventListener(this::onCreateWithReactContext);
        } else {
            onCreateWithReactContext();
            setupTransition();
        }
    }

    private void onCreateWithReactContext() {
        if (supportIsDestroyed()) {
            return;
        }
        View loadingView = findViewById(R.id.loading_view);
        if (loadingView != null) {
            loadingView.setVisibility(View.GONE);
        }
        if (!isSuccessfullyInitialized()) {
            // TODO(lmr): should we do something like this?
//            ReactNativeUtils.showAlertBecauseChecksFailed(this, dialog -> finish());
            return;
        }

        String moduleName = getIntent().getStringExtra(REACT_MODULE_NAME);
        activityManager = new ReactInterfaceManager(this);

        instanceId = String.format(Locale.ENGLISH, "%1s_%2$d", moduleName, UUID++);
        reactNavigationCoordinator.registerComponent(this, instanceId);

//        @AirToolbar.Theme int toolbarTheme = reactNavigationCoordinator.getToolbarThemeForModuleName(moduleName);
//        toolbar.setTheme(toolbarTheme);

        Integer toolbarBgColor = reactNavigationCoordinator.getToolbarBackgroundColorForModuleName(moduleName);
        if (toolbarBgColor != null) {
            toolbar.setBackgroundColor(ContextCompat.getColor(this, toolbarBgColor));
        }
        Integer toolbarFgColor = reactNavigationCoordinator.getToolbarForegroundColorForModuleName(moduleName);
        if (toolbarFgColor != null) {
            toolbar.setForegroundColor(ContextCompat.getColor(this, toolbarFgColor));
        }

        if (reactRootView == null) {
            ViewStub reactViewStub = findById(this, R.id.react_root_view_stub);
            reactViewStub.setLayoutResource(R.layout.view_holder_react_root_view);
            reactRootView = (ReactRootView) reactViewStub.inflate();
        }

        Bundle props = getIntent().getBundleExtra(REACT_PROPS);
        if (props == null) {
            props = new Bundle();
        }
        props.putString(INSTANCE_ID_PROP, instanceId);

        int color = reactNavigationCoordinator.getBackgroundColorForModuleName(moduleName);
        reactRootView.setBackgroundColor(color);
        reactInstanceManager.startReactApplication(reactRootView, moduleName, props);
    }

    private void setupTransition() {
        // Shared element transitions have been unreliable on Lollipop < MR1.
        if (Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API && ReactNativeUtils.isSharedElementTransition(getIntent())) {
            setupSharedElementTransition();
        } else if (isSuccessfullyInitialized() && Build.VERSION.SDK_INT >= WAITING_TRANSITION_TARGET_API) {
            setupDefaultWaitingForRenderTransition();
        } else {
            // if we don't have the ability to use a `TransitionListener`, we do the poor man's approach of
            // just emitting the event after some amount of time has expired. :facepalm:
            transitionHandler.postDelayed(() -> emitEvent(ON_ENTER_TRANSITION_COMPLETE, null), FAKE_ENTER_TRANSITION_TIME_IN_MS);
        }

        // in this case, we end up waiting for the first render to complete
        // doing the transition. If this never happens for some reason, we are going to push
        // anyway in 250ms. The handler should get canceled + called sooner though (it's za race).
        isWaitingForRenderToFinish = true;
        transitionHandler.postDelayed(this::onFinishWaitingForRender, RENDER_TIMEOUT_IN_MS);
    }

    @TargetApi(SHARED_ELEMENT_TARGET_API)
    private void setupSharedElementTransition() {
        isSharedElementTransition = true;
        supportPostponeEnterTransition();

        // We are doing a shared element transition...
        setEnterSharedElementCallback(new AutoSharedElementCallback(this));

        attachEnterTransitionListener(getWindow().getEnterTransition());
    }

    @TargetApi(WAITING_TRANSITION_TARGET_API)
    private void attachEnterTransitionListener(Transition transition) {
        transition.addListener(new SimpleTransitionListener() {
            @Override
            public void onTransitionEnd(Transition transition) {
                emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
            }
        });
    }

    @TargetApi(WAITING_TRANSITION_TARGET_API)
    private void setEnterTransition(Transition transition) {
        attachEnterTransitionListener(transition);
        getWindow().setEnterTransition(transition);
    }

    @TargetApi(WAITING_TRANSITION_TARGET_API)
    private void setupDefaultWaitingForRenderTransition() {
        supportPostponeEnterTransition();
        setEnterTransition(makeSlideLeftAnimation());
    }

    @Override
    public void signalFirstRenderComplete() {
        // For some reason, this "signal" gets sent before the `transitionName` gets set on the shared
        // elements, so if we are doing a "Shared Element Transition", we want to keep waiting before
        // starting the enter transition.
        if (!isSharedElementTransition && isWaitingForRenderToFinish) {
            transitionHandler.post(this::onFinishWaitingForRender);
        }
    }

    public void notifySharedElementAddition() {
        if (isWaitingForRenderToFinish) {
            // if we are receiving a sharedElement and we have postponed the enter transition, we want to cancel any existing
            // handler and create a new one. (this is effectively debouncing the call).
            transitionHandler.removeCallbacksAndMessages(null);
            transitionHandler.post(this::onFinishWaitingForRender);
        }
    }

    private void onFinishWaitingForRender() {
        if (isWaitingForRenderToFinish && !supportIsDestroyed()) {
            isWaitingForRenderToFinish = false;
            supportStartPostponedEnterTransition();
        }
    }

    @Override
    public String getInstanceId() {
        return instanceId;
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
        }
//        else if (menuButtons != null) {
            // TODO(lmr): fix this
//            NavigatorModule.addButtonsToMenu(this, menu, menuButtons, (button, index) -> emitEvent(ON_BUTTON_PRESS, index));
//        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            emitEvent(ON_LEFT_PRESS, null);
            if (reactNavigationCoordinator.getDismissCloseBehavior(this)) {
                dismiss();
                return true; // consume the event
            } else {
                return super.onOptionsItemSelected(item);
            }
        }

        // it's the link
        emitEvent(ON_LINK_PRESS, null);
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (activityManager != null) {
            activityManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (reactNavigationCoordinator.getDismissCloseBehavior(this)) {
            dismiss();
        } else {
            reactInstanceManager.onBackPressed();
        }
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            reactInstanceManager.onHostPause(this);
        } catch (AssertionError ignored) {
            // Sometimes Android can call onPause() on an activity even though it's not resumed.
            // We've observed this behavior when you have "Don't keep activities" turned on, for
            // example, or when you change any setting that causes a similar behavior, like changing
            // the locale in the device settings. In this case, it should be safe to ignore it.
            Log.w(TAG, "Ignored AssertionError during onPause(). " +
                    "This Activity was probably killed in the background");
        }
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
        reactInstanceManager.onHostDestroy(this);
        reactNavigationCoordinator.unregisterComponent(instanceId);
        if (reactRootView != null) {
            reactRootView.unmountReactApplication();
        }
    }

//    @Override
//    public void setMenuButtons(List<MenuButton> buttons) {
//        menuButtons = buttons;
//        supportInvalidateOptionsMenu();
//    }

    @Override
    public void setLink(String link) {
        this.link = link;
        supportInvalidateOptionsMenu();
    }

    public ReactRootView getRootView() {
        return reactRootView;
    }

    @Override
    public ReactToolbarFacade getToolbar() {
        return toolbar;
    }

    @Override
    public ReactRootView getReactRootView() {
        return reactRootView;
    }

    @Override
    public boolean isDismissible() {
        return reactNavigationCoordinator.getDismissCloseBehavior(this) || !(this instanceof ReactNativeModalActivity);
    }

    private void emitEvent(String eventName, @Nullable Object object) {
        if (isSuccessfullyInitialized() && !supportIsDestroyed()) {
            String key = String.format(Locale.ENGLISH, "AirbnbNavigatorScene.%s.%s", eventName, instanceId);
            ReactNativeUtils.maybeEmitEvent((ReactContext) reactInstanceManager.getCurrentReactContext(), key, object);
        }
    }

    private void dismiss() {
        Intent intent = new Intent()
                .putExtra(EXTRA_IS_DISMISS, isDismissible());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    /** Returns a Slide up animation that excludes the status bar, navigation and toolbar because they look weird */
    @TargetApi(VERSION_CODES.LOLLIPOP)
    protected static Transition makeSlideUpAnimation() {
        return makeSlideAnimation(Gravity.BOTTOM);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public static Transition makeSlideLeftAnimation() {
        return makeSlideAnimation(Gravity.RIGHT);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    private static Transition makeSlideAnimation(int gravity) {
        Slide slide = new Slide(gravity);
        slide.excludeTarget(android.R.id.statusBarBackground, true);
        slide.excludeTarget(android.R.id.navigationBarBackground, true);
        slide.excludeTarget(R.id.toolbar, true);
        slide.setDuration(200);
        return slide;
    }

    @Override
    public ReactAwareActivityFacade getActivity() {
        return this;
    }

    protected boolean isSuccessfullyInitialized() {
        // TODO(lmr): move to utils
        return reactInstanceManager.isSuccessfullyInitialized();
    }

    @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
    protected boolean supportIsDestroyed() {
        return AndroidVersion.isAtLeastJellyBeanMR1() && isDestroyed();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(
            String[] permissions,
            int requestCode,
            PermissionListener listener) {
        permissionListener = listener;
        requestPermissions(permissions, requestCode);
    }

    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {
        if (permissionListener != null && permissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            permissionListener = null;
        }
    }
}
