package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.transition.Transition;
import android.util.Log;
import com.airbnb.android.R;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

import java.util.Locale;

import static com.airbnb.android.react.navigation.ReactNativeIntents.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.navigation.ReactNativeIntents.INITIAL_BAR_HEIGHT_PROP;
import static com.airbnb.android.react.navigation.ReactNativeActivity.makeSlideLeftAnimation;
import static com.airbnb.android.react.navigation.ReactNativeUtils.maybeEmitEvent;

public class ReactNativeFragment extends Fragment implements ReactInterface,
    DefaultHardwareBackBtnHandler {

  private static final String TAG = "ReactNativeFragment";
  private static final String ON_DISAPPEAR = "onDisappear";
  private static final String ON_APPEAR = "onAppear";
  private static final String INSTANCE_ID_PROP = "nativeNavigationInstanceId";
  private static final String ON_BUTTON_PRESS = "onButtonPress";
  private static final String ON_LINK_PRESS = "onLinkPress";

  private static final int SHARED_ELEMENT_TARGET_API = Build.VERSION_CODES.LOLLIPOP_MR1;
  /** We just need lollipop (not MR1) for the postponed slide in transition */
  private static final int WAITING_TRANSITION_TARGET_API = Build.VERSION_CODES.LOLLIPOP;

  private static final String ON_ENTER_TRANSITION_COMPLETE = "onEnterTransitionComplete";
  private static final int RENDER_TIMEOUT_IN_MS = 1700; // TODO(lmr): put this back down when done debugging
  private static final int FAKE_ENTER_TRANSITION_TIME_IN_MS = 500;

  // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
  private static int UUID = 1;

  ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;
  ReactInstanceManager reactInstanceManager = reactNavigationCoordinator.getReactInstanceManager();

  private String instanceId;
  private float barHeight;
  private ReactInterfaceManager activityManager;
  private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap previousConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap renderedConfig = ConversionUtil.EMPTY_MAP;
  private ReactRootView reactRootView;
  private final Handler transitionHandler = new Handler();
  private final Handler handler = new Handler();
  private boolean isWaitingForRenderToFinish = false;
  private boolean isSharedElementTransition = false;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    Log.d(TAG, "onCreate");
    setupTransition();
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    Log.d(TAG, "onActivityCreated");
    initReactNative();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    Log.d(TAG, "onCreateView");
    View v = inflater.inflate(R.layout.fragment_react_native, container, false);
    ReactToolbar toolbar = (ReactToolbar) v.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);
    initReactNative();
    return v;
  }

  private void initReactNative() {
    if (reactRootView != null || getView() == null) {
      return;
    }
    if (!isSuccessfullyInitialized()) {
      // TODO(lmr): need a different way of doing this
      // TODO(lmr): move to utils
      reactInstanceManager.addReactInstanceEventListener(
          new ReactInstanceManager.ReactInstanceEventListener() {
            @Override public void onReactContextInitialized(ReactContext context) {
              onCreateWithReactContext();
            }
          });
    } else {
      onCreateWithReactContext();
    }
    activityManager = new ReactInterfaceManager(this);
    reactNavigationCoordinator.registerComponent(this, instanceId);
  }

  private void onCreateWithReactContext() {
    Log.d(TAG, "onCreateWithReactContext");
    if (getView() == null) {
      return;
    }
    // TODO(lmr): should we make the "loading" XML configurable?
    View loadingView = getView().findViewById(R.id.loading_view);
    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    } else {
      // TODO(lmr): this shouldn't happen...
    }

    if (!isSuccessfullyInitialized()) {
      // TODO(lmr): should we make this configurable?
//      ReactNativeUtils.showAlertBecauseChecksFailed(getActivity(), null);
      return;
    }
    String moduleName = getArguments().getString(ReactNativeIntents.EXTRA_MODULE_NAME);
    instanceId = String.format(Locale.ENGLISH, "%1s_fragment_%2$d", moduleName, UUID++);
    Bundle props = getArguments().getBundle(ReactNativeIntents.EXTRA_PROPS);
    if (props == null) {
      props = new Bundle();
    }
    props.putString(INSTANCE_ID_PROP, instanceId);

    if (reactRootView == null) {
      ViewStub reactViewStub = (ViewStub) getView().findViewById(R.id.react_root_view_stub);
      reactViewStub.setLayoutResource(R.layout.view_holder_react_root_view);
      reactRootView = (ReactRootView) reactViewStub.inflate();
    }
    Log.d(TAG, "startReactApplication");


    barHeight = getImplementation().getBarHeight(
        this,
        getToolbar(),
        ((AppCompatActivity)getActivity()).getSupportActionBar(),
        renderedConfig,
        true
    );

    props.putFloat(INITIAL_BAR_HEIGHT_PROP, barHeight);

    reactRootView.startReactApplication(reactInstanceManager, moduleName, props);
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    activityManager.onActivityResult(requestCode, resultCode, data);
  }

  @Override public void invokeDefaultOnBackPressed() {
    getActivity().onBackPressed();
  }

  @Override public void onPause() {
    super.onPause();
    reactInstanceManager.onHostPause(getActivity());
    emitEvent(ON_DISAPPEAR, null);
  }

  @Override public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume");
    reactInstanceManager.onHostResume(getActivity(), this);
    emitEvent(ON_APPEAR, null);
  }

  @Override public void onDestroyView() {
    Log.d(TAG, "onDestroyView");
    super.onDestroyView();
    reactNavigationCoordinator.unregisterComponent(instanceId);
    if (reactRootView != null) {
      reactRootView.unmountReactApplication();
      reactRootView = null;
    }
  }

  @Override public boolean isDismissible() {
    return reactNavigationCoordinator.getDismissCloseBehavior(this);
  }

  public void dismiss() {
    Intent intent = new Intent()
        .putExtra(EXTRA_IS_DISMISS, isDismissible());
    getActivity().setResult(Activity.RESULT_OK, intent);
    getActivity().finish();
  }

  @Override public String getInstanceId() {
    return instanceId;
  }

  @Override public ReactRootView getReactRootView() {
    return reactRootView;
  }

  @Override public ReactToolbar getToolbar() {
    // TODO
    return null;
  }

  @TargetApi(SHARED_ELEMENT_TARGET_API)
  private void setupSharedElementTransition() {
    isSharedElementTransition = true;
    Log.d(TAG, "supportPostponeEnterTransition");
    postponeEnterTransition();
//    supportPostponeEnterTransition();

    // We are doing a shared element transition...
//    setEnterSharedElementCallback(new AutoSharedElementCallback(this));
//
//
//    attachEnterTransitionListener((Transition) getEnterTransition());
  }

  private void setupTransition() {
    Log.d(TAG, "setupTransition");
    if (initialConfig.hasKey("waitForRender") && !initialConfig.getBoolean("waitForRender")) {
      return;
    }
    // Shared element transitions have been unreliable on Lollipop < MR1.
    if (Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API && false) {
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
          if (!isDetached()) {
            ReactNativeFragment.this.emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
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
        ReactNativeFragment.this.onFinishWaitingForRender();
      }
    }, RENDER_TIMEOUT_IN_MS);
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
  private void setEnterTransitionAndListen(Transition transition) {
//    attachEnterTransitionListener(transition);
//    setEnterTransition(transition);
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  private void setupDefaultWaitingForRenderTransition() {
    Log.d(TAG, "supportPostponeEnterTransition");
    // TODO(lmr): it seems like this isn't actually quite working on the first push.
    postponeEnterTransition();
    setEnterTransitionAndListen(makeSlideLeftAnimation());
  }

  @Override public void signalFirstRenderComplete() {
    // For some reason, this "signal" gets sent before the `transitionName` gets set on the shared
    // elements, so if we are doing a "Shared Element Transition", we want to keep waiting before
    // starting the enter transition.
    if (!isSharedElementTransition && isWaitingForRenderToFinish) {
      transitionHandler.removeCallbacksAndMessages(null);
      transitionHandler.post(new Runnable() {
        @Override public void run() {
          Log.d(TAG, "signalFirstRenderComplete: onRun");
          ReactNativeFragment.this.onFinishWaitingForRender();
        }
      });
    }
  }

  private void onFinishWaitingForRender() {
    Log.d(TAG, "onFinishWaitingForRender");
    if (isWaitingForRenderToFinish && !isDetached()) {
      isWaitingForRenderToFinish = false;
      Log.d(TAG, "onFinishWaitingForRender: supportStartPostponedEnterTransition");
      startPostponedEnterTransition();
    }
  }

  @Override public void notifySharedElementAddition() {
    // TODO: shared element transitions probably not quite supported with RN fragments just yet.
//    if (isWaitingForRenderToFinish) {
//      // if we are receiving a sharedElement and we have postponed the enter transition, we want to cancel any existing
//      // handler and create a new one. (this is effectively debouncing the call).
//      transitionHandler.removeCallbacksAndMessages(null);
//      transitionHandler.post(new Runnable() {
//        @Override public void run() {
//          ReactNativeActivity.this.onFinishWaitingForRender();
//        }
//      });
//    }
  }

  @Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // it's the link
    emitEvent(ON_LINK_PRESS, null);
    return false;
  }

  protected boolean isSuccessfullyInitialized() {
    return reactNavigationCoordinator.isSuccessfullyInitialized();
  }

  private NavigationImplementation getImplementation() {
    return reactNavigationCoordinator.getImplementation();
  }

  public void emitEvent(String eventName, Object object) {
    if (isSuccessfullyInitialized()) {
      String key =
          String.format(Locale.ENGLISH, "AirbnbNavigatorScreen.%s.%s", eventName, instanceId);
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
    Log.d(TAG, "receiveNavigationProperties");
//    this.previousConfig = this.renderedConfig;
//    this.renderedConfig = ConversionUtil.combine(this.initialConfig, properties);
//    reconcileNavigationProperties();
    Activity activity = getActivity();
    if (activity instanceof ReactInterface) {
      ((ReactInterface)activity).receiveNavigationProperties(properties);
    }
  }
}
