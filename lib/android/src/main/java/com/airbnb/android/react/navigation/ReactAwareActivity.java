package com.airbnb.android.react.navigation;

import android.support.v7.app.AppCompatActivity;
import android.view.ViewTreeObserver;

public abstract class ReactAwareActivity extends AppCompatActivity implements ReactAwareActivityFacade {
  protected boolean hasCustomEnterTransition() {
    return true;
  }

  /**
   * Schedules the shared element transition to be started immediately after the shared element has been measured and laid out within the activity's
   * view hierarchy. Some common places where it might make sense to call this method are:
   * <p>
   * (1) Inside a Fragment's onCreateView() method (if the shared element lives inside a Fragment hosted by the called Activity).
   * <p>
   * (2) Inside a Glide Callback object (if you need to wait for Glide to asynchronously load/scale a bitmap before the transition can begin).
   */
  public void scheduleStartPostponedTransition() {
    getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(
        new ViewTreeObserver.OnPreDrawListener() {
          @Override
          public boolean onPreDraw() {
            getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
            supportStartPostponedEnterTransition();
            return true;
          }
        });
  }
}
