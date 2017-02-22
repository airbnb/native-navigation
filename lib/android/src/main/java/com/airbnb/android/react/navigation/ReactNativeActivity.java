package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;

import com.airbnb.android.R;
import com.facebook.react.bridge.ReadableMap;

public class ReactNativeActivity extends ReactAwareActivity {
  private static final String TAG = ReactNativeActivity.class.getSimpleName();
  private static final int SHARED_ELEMENT_TARGET_API = Build.VERSION_CODES.LOLLIPOP_MR1;
  /** We just need lollipop (not MR1) for the postponed slide in transition */
  static final int WAITING_TRANSITION_TARGET_API = Build.VERSION_CODES.LOLLIPOP;
  private static final String ON_ENTER_TRANSITION_COMPLETE = "onEnterTransitionComplete";
  private static final int FAKE_ENTER_TRANSITION_TIME_MS = 500;

  private final Handler handler = new Handler();
  private ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;
  private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;
  private ReactNativeFragment fragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    String moduleName = getIntent().getStringExtra(ReactNativeIntents.EXTRA_MODULE_NAME);
    initialConfig = reactNavigationCoordinator.getInitialConfigForModuleName(moduleName);

    setContentView(R.layout.activity_react_native);
    fragment = ReactNativeFragment.newInstance(getIntent().getExtras());
    getSupportFragmentManager().beginTransaction()
            .setAllowOptimization(true)
            .add(R.id.content, fragment)
            .commitNow();
    getSupportFragmentManager().executePendingTransactions();
    supportPostponeEnterTransition();
    setupTransition();
  }

  private void setupTransition() {
    Log.d(TAG, "setupTransition");
    if (initialConfig.hasKey("waitForRender") && !initialConfig.getBoolean("waitForRender")) {
      return;
    }
    // Shared element transitions have been unreliable on Lollipop < MR1.
    if (Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API &&
            ReactNativeUtils.isSharedElementTransition(getIntent().getExtras())) {
      Log.d(TAG, "setupTransition: sharedElementTransition");
      setupSharedElementTransition();
    } else if (isSuccessfullyInitialized() && Build.VERSION.SDK_INT >= WAITING_TRANSITION_TARGET_API) {
      Log.d(TAG, "setupTransition: waitingForRenderTransition");
      supportPostponeEnterTransition();
      getDefaultTransition();
    } else {
      Log.d(TAG, "setupTransition: postDelayed");
      // if we don't have the ability to use a `TransitionListener`, we do the poor man's approach of
      // just emitting the event after some amount of time has expired. :facepalm:
      handler.postDelayed(new Runnable() {
        @Override public void run() {
          if (!supportIsDestroyed()) {
            fragment.emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
          }
        }
      }, FAKE_ENTER_TRANSITION_TIME_MS);
    }
  }

  @TargetApi(SHARED_ELEMENT_TARGET_API)
  private void setupSharedElementTransition() {
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
        fragment.emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
      }
    });
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  private void setEnterTransition(Transition transition) {
    attachEnterTransitionListener(transition);
    getWindow().setEnterTransition(transition);
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  void getDefaultTransition() {
    Log.d(TAG, "supportPostponeEnterTransition");
    // TODO(lmr): it seems like this isn't actually quite working on the first push.
    supportPostponeEnterTransition();
    setEnterTransition(makeSlideLeftAnimation());
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Transition makeSlideLeftAnimation() {
    return makeSlideAnimation(Gravity.RIGHT);
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  static Transition makeSlideAnimation(int gravity) {
    Slide slide = new Slide(gravity);
    slide.excludeTarget(android.R.id.statusBarBackground, true);
    slide.excludeTarget(android.R.id.navigationBarBackground, true);
    slide.excludeTarget(R.id.toolbar, true);
    slide.setDuration(200);
    return slide;
  }
}
