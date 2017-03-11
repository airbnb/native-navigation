package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public abstract class ReactActivity extends ReactAwareActivity implements ScreenCoordinatorComponent {
  private static final String TAG = ReactActivity.class.getSimpleName();

  private ScreenCoordinator screenCoordinator;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FrameLayout container = new FrameLayout(this);
    container.setLayoutParams(
        new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    );
    container.setForegroundGravity(Gravity.CENTER);
    setContentView(container);

    screenCoordinator = new ScreenCoordinator(this, container, savedInstanceState);

    if (savedInstanceState == null) {
      screenCoordinator.presentScreen(
          getInitialScreenName(),
          getInitialScreenProps(),
          getInitialScreenOptions(),
          null
      );
    }
  }

  protected @Nullable String getInitialScreenName() {
    return null;
  }

  protected @Nullable Bundle getInitialScreenProps() {
    return null;
  }

  protected @Nullable Bundle getInitialScreenOptions() {
    return null;
  }

  @Override
  public ScreenCoordinator getScreenCoordinator() {
    return screenCoordinator;
  }

  @Override
  public void onBackPressed() {
    screenCoordinator.onBackPressed();
  }
}
