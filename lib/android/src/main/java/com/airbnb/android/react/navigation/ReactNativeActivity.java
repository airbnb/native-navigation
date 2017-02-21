package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import com.airbnb.android.R;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;
import com.facebook.react.modules.core.PermissionAwareActivity;
import com.facebook.react.modules.core.PermissionListener;

import java.util.Locale;

import static com.airbnb.android.react.navigation.NavigatorModule.EXTRA_IS_DISMISS;

public class ReactNativeActivity extends ReactAwareActivity
    implements ReactInterface, DefaultHardwareBackBtnHandler, PermissionAwareActivity {
  private static final int SHARED_ELEMENT_TARGET_API = VERSION_CODES.LOLLIPOP_MR1;
  /** We just need lollipop (not MR1) for the postponed slide in transition */
  private static final int WAITING_TRANSITION_TARGET_API = VERSION_CODES.LOLLIPOP;
  private DoubleTapReloadRecognizer mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();

  public static final String REACT_MODULE_NAME = "REACT_MODULE_NAME";
  public static final String REACT_PROPS = "REACT_PROPS";

  private static final String ON_ENTER_TRANSITION_COMPLETE = "onEnterTransitionComplete";
  private static final String ON_DISAPPEAR = "onDisappear";
  private static final String ON_APPEAR = "onAppear";
  private static final String INSTANCE_ID_PROP = "nativeNavigationInstanceId";
  private static final String INITIAL_BAR_HEIGHT_PROP = "nativeNavigationInitialBarHeight";
  private static final int RENDER_TIMEOUT_IN_MS = 1700; // TODO(lmr): put this back down when done debugging
  private static final int FAKE_ENTER_TRANSITION_TIME_IN_MS = 500;
  private static final String TAG = "ReactNativeActivity";
  // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
  private static int UUID = 1;

  private String instanceId;
  private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap previousConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap renderedConfig = ConversionUtil.EMPTY_MAP;
  private ReactInterfaceManager activityManager;
  private final Handler transitionHandler = new Handler();
  private final Handler handler = new Handler();
  private boolean isWaitingForRenderToFinish = false;
  private boolean isSharedElementTransition = false;
  private PermissionListener permissionListener;

  private ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;
  private ReactInstanceManager reactInstanceManager = reactNavigationCoordinator.getReactInstanceManager();

  ReactToolbar toolbar;
  ReactRootView reactRootView;

  public static Intent intentWithDismissFlag() {
    return new Intent().putExtra(EXTRA_IS_DISMISS, true);
  }

  public static Intent intent(Context context, String moduleName, Bundle props, boolean isModal) {
    Class<? extends ReactNativeActivity> activityClass =
            isModal
                    ? ReactNativeModalActivity.class
                    : ReactNativeActivity.class;
    return new Intent(context, activityClass)
            .putExtra(REACT_MODULE_NAME, moduleName)
            .putExtra(REACT_PROPS, props);
  }

  public static Intent intent(Context context, String moduleName, Bundle props) {
    return intent(context, moduleName, props, false);
  }

  public static Intent intent(Context context, String moduleName, boolean isModal) {
    return intent(context, moduleName, null, isModal);
  }

  public static Intent intent(Context context, String moduleName) {
    return intent(context, moduleName, null, false);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    String moduleName = getIntent().getStringExtra(REACT_MODULE_NAME);

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
    setContentView(R.layout.activity_react_native);
    toolbar = (ReactToolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (!isSuccessfullyInitialized()) {
      Log.d(TAG, "onCreate: !successfully initialized");
      // TODO(lmr): move to utils
      reactInstanceManager.addReactInstanceEventListener(
          new ReactInstanceManager.ReactInstanceEventListener() {
            @Override
            public void onReactContextInitialized(ReactContext context) {
              Log.d(TAG, "onReactContextInitialized");
              reactInstanceManager.removeReactInstanceEventListener(this);
              handler.post(new Runnable() {
                @Override
                public void run() {
                  ReactNativeActivity.this.onCreateWithReactContext();
                }
              });
            }
          });
    } else {
      Log.d(TAG, "onCreate: successfully initialized!");
      onCreateWithReactContext();
      setupTransition();
    }
  }

  private void onCreateWithReactContext() {
    Log.d(TAG, "onCreateWithReactContext");
    if (supportIsDestroyed()) {
      Log.d(TAG, "onCreateWithReactContext: is destroyed");
      return;
    }
    View loadingView = findViewById(R.id.loading_view);
    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    }
    if (!isSuccessfullyInitialized()) {
      Log.d(TAG, "onCreateWithReactContext: is not fully initialized");
      // TODO(lmr): should we do something like this in OSS?
//            ReactNativeUtils.showAlertBecauseChecksFailed(this, dialog -> finish());
      return;
    }

    String moduleName = getIntent().getStringExtra(REACT_MODULE_NAME);
    activityManager = new ReactInterfaceManager(this);

    instanceId = String.format(Locale.ENGLISH, "%1s_%2$d", moduleName, UUID++);
    reactNavigationCoordinator.registerComponent(this, instanceId);

    if (reactRootView == null) {
      ViewStub reactViewStub = (ViewStub) findViewById(R.id.react_root_view_stub);
      reactViewStub.setLayoutResource(R.layout.view_holder_react_root_view);
      reactRootView = (ReactRootView) reactViewStub.inflate();
    }

    Bundle props = getIntent().getBundleExtra(REACT_PROPS);
    if (props == null) {
      props = new Bundle();
    }
    props.putString(INSTANCE_ID_PROP, instanceId);

    getImplementation().reconcileNavigationProperties(
        this,
        getToolbar(),
        getSupportActionBar(),
        ConversionUtil.EMPTY_MAP,
        renderedConfig,
        true
    );

    float barHeight = getImplementation().getBarHeight(
        this,
        getToolbar(),
        getSupportActionBar(),
        renderedConfig,
        true
    );

    props.putFloat(INITIAL_BAR_HEIGHT_PROP, barHeight);

    reactRootView.startReactApplication(reactInstanceManager, moduleName, props);
  }

  private void setupTransition() {
    Log.d(TAG, "setupTransition");
    if (initialConfig.hasKey("waitForRender") && !initialConfig.getBoolean("waitForRender")) {
      return;
    }
    // Shared element transitions have been unreliable on Lollipop < MR1.
    if (Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API && ReactNativeUtils.isSharedElementTransition(
        getIntent())) {
      Log.d(TAG, "setupTransition: sharedElementTransition");
      setupSharedElementTransition();
    } else if (isSuccessfullyInitialized() && Build.VERSION.SDK_INT >= WAITING_TRANSITION_TARGET_API) {
      Log.d(TAG, "setupTransition: waitingForRenderTransition");
      setupDefaultWaitingForRenderTransition();
    } else {
      Log.d(TAG, "setupTransition: postDelayed");
      // if we don't have the ability to use a `TransitionListener`, we do the poor man's approach of
      // just emitting the event after some amount of time has expired. :facepalm:
      handler.postDelayed(new Runnable() {
        @Override public void run() {
          if (!supportIsDestroyed()) {
            ReactNativeActivity.this.emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
          }
        }
      }, FAKE_ENTER_TRANSITION_TIME_IN_MS);
    }

    // in this case, we end up waiting for the first render to complete
    // doing the transition. If this never happens for some reason, we are going to push
    // anyway in 250ms. The handler should get canceled + called sooner though (it's za race).
    isWaitingForRenderToFinish = true;
    transitionHandler.postDelayed(new Runnable() {
      @Override public void run() {
        Log.d(TAG, "render timeout callback called");
        ReactNativeActivity.this.onFinishWaitingForRender();
      }
    }, RENDER_TIMEOUT_IN_MS);
  }

  @TargetApi(SHARED_ELEMENT_TARGET_API)
  private void setupSharedElementTransition() {
    isSharedElementTransition = true;
    Log.d(TAG, "supportPostponeEnterTransition");
    supportPostponeEnterTransition();

    // We are doing a shared element transition...
    setEnterSharedElementCallback(new AutoSharedElementCallback(this));

    attachEnterTransitionListener(getWindow().getEnterTransition());
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  private void attachEnterTransitionListener(Transition transition) {
    transition.addListener(new SimpleTransitionListener() {
      @Override public void onTransitionEnd(Transition transition) {
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
    Log.d(TAG, "supportPostponeEnterTransition");
    // TODO(lmr): it seems like this isn't actually quite working on the first push.
    supportPostponeEnterTransition();
    setEnterTransition(makeSlideLeftAnimation());
  }

  @Override public void signalFirstRenderComplete() {
    Log.d(TAG, "signalFirstRenderComplete");
    // For some reason, this "signal" gets sent before the `transitionName` gets set on the shared
    // elements, so if we are doing a "Shared Element Transition", we want to keep waiting before
    // starting the enter transition.
    if (!isSharedElementTransition && isWaitingForRenderToFinish) {
      transitionHandler.removeCallbacksAndMessages(null);
      transitionHandler.post(new Runnable() {
        @Override public void run() {
          Log.d(TAG, "signalFirstRenderComplete: onRun");
          ReactNativeActivity.this.onFinishWaitingForRender();
        }
      });
    }
  }

  public void notifySharedElementAddition() {
    Log.d(TAG, "notifySharedElementAddition");
    if (isWaitingForRenderToFinish) {
      // if we are receiving a sharedElement and we have postponed the enter transition, we want to cancel any existing
      // handler and create a new one. (this is effectively debouncing the call).
      transitionHandler.removeCallbacksAndMessages(null);
      transitionHandler.post(new Runnable() {
        @Override public void run() {
          ReactNativeActivity.this.onFinishWaitingForRender();
        }
      });
    }
  }

  @Override public FragmentActivity getActivity() {
    return this;
  }

  private void onFinishWaitingForRender() {
    Log.d(TAG, "onFinishWaitingForRender");
    if (isWaitingForRenderToFinish && !supportIsDestroyed()) {
      isWaitingForRenderToFinish = false;
      Log.d(TAG, "onFinishWaitingForRender: supportStartPostponedEnterTransition");
      scheduleStartPostponedTransition();
    }
  }

  @Override public String getInstanceId() {
    return instanceId;
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    Log.d(TAG, "onCreateOptionsMenu");
    if (toolbar != null) {
      // 0 will prevent menu from getting inflated, since we are inflating manually
      toolbar.onCreateOptionsMenu(0, menu, getMenuInflater());
    }
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    Log.d(TAG, "onPrepareOptionsMenu");
    getImplementation().prepareOptionsMenu(
        this,
        toolbar,
        getSupportActionBar(),
        menu,
        this.previousConfig,
        this.renderedConfig
    );
    return super.onPrepareOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    Log.d(TAG, "onOptionsItemSelected");
    return getImplementation().onOptionsItemSelected(
        this,
        toolbar,
        getSupportActionBar(),
        item,
        this.renderedConfig
    );
  }

  @Override
  public boolean onSupportNavigateUp() {
    Log.d(TAG, "onSupportNavigateUp");
    return super.onSupportNavigateUp();
  }

  @Override
  public boolean onMenuOpened(int featureId, Menu menu) {
    Log.d(TAG, "onMenuOpened");
    return super.onMenuOpened(featureId, menu);
  }

  @Override
  public void onLowMemory() {
    Log.d(TAG, "onLowMemory");
    super.onLowMemory();
  }

  @Override
  public void onActivityReenter(int resultCode, Intent data) {
    Log.d(TAG, "onActivityReenter");
    super.onActivityReenter(resultCode, data);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d(TAG, "onActivityResult");
    super.onActivityResult(requestCode, resultCode, data);
    if (activityManager != null) {
      activityManager.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override public void onBackPressed() {
    Log.d(TAG, "onBackPressed");
    if (reactNavigationCoordinator.getDismissCloseBehavior(this)) {
      dismiss();
    } else {
      super.onBackPressed();
//      reactInstanceManager.onBackPressed();
    }
  }

  @Override public void invokeDefaultOnBackPressed() {
    super.onBackPressed();
  }

  @Override protected void onPause() {
    Log.d(TAG, "onPause");
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

  @Override protected void onResume() {
    Log.d(TAG, "onResume");
    super.onResume();
    // TODO(lmr): i wonder if we really actually want to do this...
    reactInstanceManager.onHostResume(this, this);
    // TODO(lmr): onResume might not actually be the right place to do this, since we are
    // postponing the enter transition, this gets called at the wrong time technically.
    emitEvent(ON_APPEAR, null);
  }

  @Override protected void onDestroy() {
    Log.d(TAG, "onDestroy");
    // TODO(lmr): should super.onDestroy go after or before we unmount?
    super.onDestroy();
    // TODO(lmr): i wonder if we really actually want to do this...
    reactInstanceManager.onHostDestroy(this);
    reactNavigationCoordinator.unregisterComponent(instanceId);
    if (reactRootView != null) {
      reactRootView.unmountReactApplication();
    }
  }

  public ReactRootView getRootView() {
    return reactRootView;
  }

  @Override public ReactToolbar getToolbar() {
    return toolbar;
  }

  @Override public ReactRootView getReactRootView() {
    return reactRootView;
  }

  @Override public boolean isDismissible() {
    return reactNavigationCoordinator.getDismissCloseBehavior(
        this) || !(this instanceof ReactNativeModalActivity);
  }

  public void emitEvent(String eventName, Object object) {
    if (isSuccessfullyInitialized() && !supportIsDestroyed()) {
      String key =
          String.format(Locale.ENGLISH, "NativeNavigationScreen.%s.%s", eventName, instanceId);
      Log.d(TAG, key);
      ReactNativeUtils.maybeEmitEvent((ReactContext) reactInstanceManager.getCurrentReactContext(),
          key, object);
    }
  }

  @Override
  public void receiveNavigationProperties(ReadableMap properties) {
    Log.d(TAG, "receiveNavigationProperties");
    this.previousConfig = this.renderedConfig;
    this.renderedConfig = ConversionUtil.combine(this.initialConfig, properties);
    reconcileNavigationProperties();
  }

  private void reconcileNavigationProperties() {
    Log.d(TAG, "reconcileNavigationProperties");
    getImplementation().reconcileNavigationProperties(
        this,
        toolbar,
        getSupportActionBar(),
        this.previousConfig,
        this.renderedConfig,
        false
    );
  }

  public void dismiss() {
    Intent intent = new Intent()
        .putExtra(EXTRA_IS_DISMISS, isDismissible());
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  /**
   * Returns a Slide up animation that excludes the status bar, navigation and toolbar because they
   * look weird
   */
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

  protected boolean isSuccessfullyInitialized() {
    return reactNavigationCoordinator.isSuccessfullyInitialized();
  }

  private NavigationImplementation getImplementation() {
    return reactNavigationCoordinator.getImplementation();
  }

  @TargetApi(VERSION_CODES.JELLY_BEAN_MR1)
  protected boolean supportIsDestroyed() {
    return AndroidVersion.isAtLeastJellyBeanMR1() && isDestroyed();
  }

  @TargetApi(Build.VERSION_CODES.M)
  public void requestPermissions(String[] permissions, int requestCode,
      PermissionListener listener) {
    permissionListener = listener;
    requestPermissions(permissions, requestCode);
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
    if (mDoubleTapReloadRecognizer.didDoubleTapR(keyCode, getCurrentFocus())) {
      reactInstanceManager.getDevSupportManager().handleReloadJS();
    }

    return super.onKeyUp(keyCode, event);
  }

  public void onRequestPermissionsResult(int requestCode, String[] permissions,
      int[] grantResults) {
    if (permissionListener != null && permissionListener.onRequestPermissionsResult(requestCode,
        permissions, grantResults)) {
      permissionListener = null;
    }
  }
}
