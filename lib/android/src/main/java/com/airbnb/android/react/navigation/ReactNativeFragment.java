package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.airbnb.android.R;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.PermissionListener;

import java.util.List;
import java.util.Locale;

import static com.airbnb.android.react.navigation.ReactNativeIntents.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.navigation.ReactNativeUtils.maybeEmitEvent;

public class ReactNativeFragment extends Fragment implements ReactInterface,
        ReactNativeFragmentViewGroup.KeyListener {

    private static final String TAG = ReactNativeFragment.class.getSimpleName();

    private DoubleTapReloadRecognizer mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();

    public static final String EXTRA_REACT_MODULE_NAME = "REACT_MODULE_NAME";

    public static final String EXTRA_REACT_PROPS = "REACT_PROPS";

    public static final String EXTRA_IS_MODAL = "IS_MODAL";

    private static final String ON_DISAPPEAR = "onDisappear";

    private static final String ON_APPEAR = "onAppear";

    private static final String ON_BACK_PRESS = "onBackPress";

    private static final String INSTANCE_ID_PROP = "nativeNavigationInstanceId";

    private static final String ON_BUTTON_PRESS = "onButtonPress";

    private static final String INITIAL_BAR_HEIGHT_PROP = "nativeNavigationInitialBarHeight";

    private static final int RENDER_TIMEOUT_IN_MS = 1700; // TODO(lmr): put this back down when done debugging

    // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
    private static int UUID = 1;

    private ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;

    private ReactInstanceManager reactInstanceManager = reactNavigationCoordinator.getReactInstanceManager();

    private final Runnable timeoutCallback = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "render timeout callback called");
            signalFirstRenderComplete();
        }
    };

    private String instanceId;

    private boolean isSharedElementTransition;

    private boolean isWaitingForRenderToFinish = false;

    private float barHeight;

    private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;

    private ReadableMap previousConfig = ConversionUtil.EMPTY_MAP;

    private ReadableMap renderedConfig = ConversionUtil.EMPTY_MAP;

    private ReactNativeFragmentViewGroup contentContainer;

    private ReactRootView reactRootView;

    //  private ReactInterfaceManager activityManager;
    private final Handler handler = new Handler();

    private PermissionListener permissionListener;

    private AppCompatActivity activity;

    private ReactToolbar toolbar;

    private View loadingView;

    public static ReactNativeFragment newInstance(String moduleName, @Nullable Bundle props) {
        ReactNativeFragment frag = new ReactNativeFragment();
        Bundle args = new BundleBuilder()
                .putString(ReactNativeIntents.EXTRA_MODULE_NAME, moduleName)
                .putBundle(ReactNativeIntents.EXTRA_PROPS, props)
                .toBundle();
        frag.setArguments(args);
        return frag;
    }

    static ReactNativeFragment newInstance(Bundle intentExtras) {
        ReactNativeFragment frag = new ReactNativeFragment();
        frag.setArguments(intentExtras);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (instanceId == null) {
            if (savedInstanceState == null) {
                String moduleName = getArguments().getString(ReactNativeIntents.EXTRA_MODULE_NAME);
                instanceId = String.format(Locale.ENGLISH, "%1s_fragment_%2$d", moduleName, UUID++);
            } else {
                instanceId = savedInstanceState.getString(INSTANCE_ID_PROP);
            }
        }

        setHasOptionsMenu(true);
        Log.d(TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        initReactNative();
    }

    private void initReactNative() {
        if (reactRootView != null || getView() == null) {
            return;
        }
        if (!isSuccessfullyInitialized()) {
            // TODO(lmr): need a different way of doing this
            // TODO(lmr): move to utils
            reactNavigationCoordinator.addInitializationListener(
                    new ReactInstanceManager.ReactInstanceEventListener() {
                        @Override
                        public void onReactContextInitialized(ReactContext context) {
                            reactInstanceManager.removeReactInstanceEventListener(this);
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    onAttachWithReactContext();
                                }
                            });
                        }
                    });
        } else {
            onAttachWithReactContext();
            // in this case, we end up waiting for the first render to complete
            // doing the transition. If this never happens for some reason, we are going to push
            // anyway in 250ms. The handler should get canceled + called sooner though (it's za race).
            isWaitingForRenderToFinish = true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "render timeout callback called");
                    startPostponedEnterTransition();
                }
            }, RENDER_TIMEOUT_IN_MS);
        }
//    activityManager = new ReactInterfaceManager(this);
        reactNavigationCoordinator.registerComponent(this, instanceId);
    }

    private void onAttachWithReactContext() {
        Log.d(TAG, "onCreateWithReactContext");
        if (getView() == null) {
            return;
        }
        loadingView.setVisibility(View.GONE);

        if (!isSuccessfullyInitialized()) {
            // TODO(lmr): should we make this configurable?
//      ReactNativeUtils.showAlertBecauseChecksFailed(getActivity(), null);
            return;
        }
        String moduleName = getArguments().getString(ReactNativeIntents.EXTRA_MODULE_NAME);
        Bundle props = getArguments().getBundle(ReactNativeIntents.EXTRA_PROPS);
        if (props == null) {
            props = new Bundle();
        }
        props.putString(INSTANCE_ID_PROP, instanceId);

        if (reactRootView == null) {
            ViewStub reactViewStub = (ViewStub) getView().findViewById(R.id.react_root_view_stub);
            reactRootView = (ReactRootView) reactViewStub.inflate();
        }

        getImplementation().reconcileNavigationProperties(
                this,
                getToolbar(),
                activity.getSupportActionBar(),
                ConversionUtil.EMPTY_MAP,
                renderedConfig,
                true
        );

        barHeight = getImplementation().getBarHeight(
                this,
                getToolbar(),
                activity.getSupportActionBar(),
                renderedConfig,
                true
        );

        reactRootView.startReactApplication(reactInstanceManager, moduleName, props);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        postponeEnterTransition();
        View v = inflater.inflate(R.layout.fragment_react_native, container, false);
        toolbar = (ReactToolbar) v.findViewById(R.id.toolbar);
        // TODO(lmr): should we make the "loading" XML configurable?
        loadingView = v.findViewById(R.id.loading_view);
        contentContainer = (ReactNativeFragmentViewGroup) v.findViewById(R.id.content_container);
        contentContainer.setKeyListener(this);
        activity = (AppCompatActivity) getActivity();

        // Set support action bar before setNavigationOnClickListener otherwise clicking back won't work on scratch RN application
        if (activity instanceof ReactActivity) {
            activity.setSupportActionBar(toolbar);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = ReactNativeFragment.this.getActivity();
                if (activity instanceof ScreenCoordinatorComponent) {
                    ((ScreenCoordinatorComponent) activity).getScreenCoordinator().onBackPressed();
                } else {
                    activity.onBackPressed();
                }
            }
        });

        // Set support action bar after setNavigationOnClickListener to allow override of the click listener in the activity for existing native apps
        if (!(activity instanceof ReactActivity)) {
            activity.setSupportActionBar(toolbar);
        }


        String moduleName = getArguments().getString(EXTRA_REACT_MODULE_NAME);
        Log.d(TAG, "onCreateView " + moduleName);

        initialConfig = reactNavigationCoordinator.getInitialConfigForModuleName(moduleName);
        // for reconciliation, we save this in "renderedConfig" until the real one comes down
        renderedConfig = initialConfig;

        if (initialConfig.hasKey("screenColor")) {
            int backgroundColor = initialConfig.getInt("screenColor");
            // TODO(lmr): do we need to create a style for this?...
//        if (backgroundColor == Color.TRANSPARENT) {
//            // This needs to happen before setContentView gets called
//            setTheme(R.style.Theme_Airbnb_ReactTranslucent);
//        }
        }

        return v;
    }

    @Override
    public void postponeEnterTransition() {
        super.postponeEnterTransition();
        Log.d(TAG, "postponeEnterTransition");
        getActivity().supportPostponeEnterTransition();
    }

    @Override
    public void startPostponedEnterTransition() {
        super.startPostponedEnterTransition();
        Log.d(TAG, "startPostponeEnterTransition");
        if (getActivity() != null) {
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//    activityManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_ID_PROP, instanceId);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (!enter) {
            // React Native will flush the UI cache as soon as we unmount it. This will cause the view to
            // disappear unless we delay it until after the fragment animation.
            if (transit == FragmentTransaction.TRANSIT_NONE && nextAnim == 0) {
                cleanUpAfterDestroyView();

            } else if (nextAnim != 0) {
                final Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cleanUpAfterDestroyView();
                    }
                });
                return anim;
            }
        }

        if (getActivity() instanceof ScreenCoordinatorComponent) {
            ScreenCoordinator screenCoordinator =
                    ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator();
            if (screenCoordinator != null) {
                // In some cases such as TabConfig, the screen may be loaded before there is a screen
                // coordinator but it doesn't live inside of any back stack and isn't visible.
                return screenCoordinator.onCreateAnimation(transit, enter, nextAnim);
            }
        }
        return null;
    }

    private void cleanUpAfterDestroyView() {
        // required to remove GlobalLayoutListener within reactRootView
        contentContainer.removeAllViews();

        if (reactRootView != null) {
            reactRootView.unmountReactApplication();
        }

        toolbar.setNavigationOnClickListener(null);

        contentContainer = null;
        reactRootView = null;
        toolbar = null;
        loadingView = null;
        activity = null;
        permissionListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.emitOnDisappear();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        updateBarHeightIfNeeded();
        emitOnAppear();
    }

    public void emitOnDisappear() {
        emitEvent(ON_DISAPPEAR, null);
    }

    public void emitOnAppear() {
        emitEvent(ON_APPEAR, null);
    }

    /**
     * we wanted to also emit onAppear event on pop (backbutton pressed),
     * so we can track screen reappearing after back
     */
    @Override
    public void onDetach() {
        super.onDetach();

        final FragmentManager fragmentManager = getFragmentManager();

        if (fragmentManager != null) {
            final List<Fragment> fragments = fragmentManager.getFragments();

            final int fragmentCount = fragments.size();

            if (fragmentCount > 0) {
                // the current fragment has already been removed from the fragment stack
                Fragment previousFragment = fragments.get(fragmentCount - 1);

                if (previousFragment != null && previousFragment instanceof ReactNativeFragment) {
                    ((ReactNativeFragment) previousFragment).emitOnAppear();
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
        reactNavigationCoordinator.unregisterComponent(instanceId);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (/* BuildConfig.DEBUG && */keyCode == KeyEvent.KEYCODE_MENU) {
            // TODO(lmr): disable this in prod
            reactInstanceManager.getDevSupportManager().showDevOptionsDialog();
            return true;
        }
        if (keyCode == 0) { // this is the "backtick"
            // TODO(lmr): disable this in prod
            reactInstanceManager.getDevSupportManager().showDevOptionsDialog();
            return true;
        }
        if (mDoubleTapReloadRecognizer.didDoubleTapR(keyCode, activity.getCurrentFocus())) {
            reactInstanceManager.getDevSupportManager().handleReloadJS();
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissionListener != null &&
                permissionListener.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            permissionListener = null;
        }
    }

    @Override
    public boolean isDismissible() {
        return reactNavigationCoordinator.getDismissCloseBehavior(this);
    }

    public void dismiss() {
        Intent intent = new Intent()
                .putExtra(EXTRA_IS_DISMISS, isDismissible());
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    public boolean isOnBackPressImplemented() {
        return renderedConfig.hasKey("overrideBackPressInJs")
                && renderedConfig.getBoolean("overrideBackPressInJs");
    }

    @Override
    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public ReactRootView getReactRootView() {
        return reactRootView;
    }

    @Override
    public ReactToolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void signalFirstRenderComplete() {
        Log.d(TAG, "signalFirstRenderComplete");
        startPostponedEnterTransition();
    }

    @Override
    public void notifySharedElementAddition() {
        Log.d(TAG, "notifySharedElementAddition");
        if (isWaitingForRenderToFinish && !ReactNativeUtils.isSharedElementTransition(getActivity())) {
            // if we are receiving a sharedElement and we have postponed the enter transition,
            // we want to cancel any existing handler and create a new one.
            // This is effectively debouncing the call.
            handler.removeCallbacksAndMessages(timeoutCallback);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    signalFirstRenderComplete();
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ReactToolbar toolbar = getToolbar();
        if (toolbar != null) {
            // 0 will prevent menu from getting inflated, since we are inflating manually
            toolbar.onCreateOptionsMenu(0, menu, inflater);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        getImplementation().prepareOptionsMenu(
                this,
                getToolbar(),
                null,
                menu,
                this.previousConfig,
                this.renderedConfig
        );
        super.onPrepareOptionsMenu(menu);
    }

    public void onBackPressed() {
        emitEvent(ON_BACK_PRESS, null);
    }

    private boolean isSuccessfullyInitialized() {
        return reactNavigationCoordinator.isSuccessfullyInitialized();
    }

    private NavigationImplementation getImplementation() {
        return reactNavigationCoordinator.getImplementation();
    }

    public void emitEvent(String eventName, Object object) {
        if (isSuccessfullyInitialized()) {
            String key =
                    String.format(Locale.ENGLISH, "NativeNavigationScreen.%s.%s", eventName, instanceId);
            maybeEmitEvent(reactInstanceManager.getCurrentReactContext(), key, object);
        }
    }

    private void reconcileNavigationProperties() {
        getImplementation().reconcileNavigationProperties(
                this,
                getToolbar(),
                null,
                this.previousConfig,
                this.renderedConfig,
                false
        );
    }

    @Override
    public void receiveNavigationProperties(ReadableMap properties) {
        this.previousConfig = this.renderedConfig;
        this.renderedConfig = ConversionUtil.combine(this.initialConfig, properties);
        reconcileNavigationProperties();
        updateBarHeightIfNeeded();
    }

    private void updateBarHeightIfNeeded() {
        float newHeight = getImplementation().getBarHeight(
                this,
                getToolbar(),
                activity.getSupportActionBar(),
                renderedConfig,
                false
        );
        if (newHeight != barHeight) {
            barHeight = newHeight;
            emitEvent("onBarHeightChanged", barHeight);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissions(String[] permissions, int requestCode,
                                   PermissionListener listener) {
        permissionListener = listener;
        requestPermissions(permissions, requestCode);
    }
}
