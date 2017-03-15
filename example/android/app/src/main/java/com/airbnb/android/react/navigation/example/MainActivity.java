package com.airbnb.android.react.navigation.example;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.airbnb.android.react.navigation.ScreenCoordinatorLayout;
import com.airbnb.android.react.navigation.ReactAwareActivity;
import com.airbnb.android.react.navigation.ScreenCoordinator;
import com.airbnb.android.react.navigation.ScreenCoordinatorComponent;

public class MainActivity extends ReactAwareActivity implements ScreenCoordinatorComponent {
  private static final String TAG = MainActivity.class.getSimpleName();

  private ScreenCoordinator screenCoordinator;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ScreenCoordinatorLayout container = (ScreenCoordinatorLayout) findViewById(R.id.content);
    screenCoordinator = new ScreenCoordinator(this, container, savedInstanceState);

    if (savedInstanceState == null) {
      screenCoordinator.presentScreen(MainFragment.newInstance());
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
