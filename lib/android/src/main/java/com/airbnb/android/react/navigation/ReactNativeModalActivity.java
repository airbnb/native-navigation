package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.Transition;
import android.view.Gravity;

public class ReactNativeModalActivity extends ReactNativeActivity {

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  @Override
  void getDefaultTransition() {
    getWindow().setEnterTransition(makeSlideUpAnimation());
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private static Transition makeSlideUpAnimation() {
    return makeSlideAnimation(Gravity.BOTTOM);
  }
}
