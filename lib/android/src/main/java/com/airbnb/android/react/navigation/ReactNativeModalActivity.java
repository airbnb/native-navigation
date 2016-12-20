package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.ViewTreeObserver.OnPreDrawListener;

public class ReactNativeModalActivity extends ReactNativeActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (isSuccessfullyInitialized()) {
      if (AndroidVersion.isAtLeastLollipop()) {
        lollipopSetUpPostponedEnterTransition();
      } else {
        // Stick to the default slide up animation
        // TODO(lmr):
        // overridePendingTransition(R.anim.enter_bottom, 0);
      }
    }
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  private void lollipopSetUpPostponedEnterTransition() {
    // Delay the enter animation so we can wait for the Activity to be laid out
    // at least once, otherwise the animation might flash, making it look bad.
    // We can't delay simple "overridePendingTransition()" animations though, so
    // we need to use this custom Slide animation
    getWindow().setEnterTransition(makeSlideUpAnimation());
    supportPostponeEnterTransition();
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

  @Override
  public void finish() {
    super.finish();
    // same as onCreate()
    if (isSuccessfullyInitialized()) {
      // TODO(lmr):
      // overridePendingTransition(0, R.anim.exit_bottom);
    } else {
      overridePendingTransition(0, 0);
    }
  }
}
