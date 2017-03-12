package com.airbnb.android.react.navigation.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import com.airbnb.android.react.navigation.*;

public class MainActivity extends ReactAwareActivity implements ScreenCoordinatorComponent {

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
      screenCoordinator.presentScreen(MainFragment.newInstance());
//      screenCoordinator.presentScreen("ScreenOne");
    }
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
