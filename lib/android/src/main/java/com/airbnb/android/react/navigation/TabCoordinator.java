package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

public class TabCoordinator {

  private final LongSparseArray<ScreenCoordinator> screenCoordinators = new LongSparseArray<>();
  private final AppCompatActivity activity;
  private final ViewGroup container;

  private Integer currentTabId = null;

  public TabCoordinator(AppCompatActivity activity, ViewGroup container,
          @Nullable Bundle savedInstanceState) {
    this.activity = activity;
    this.container = container;
  }

  public void showTab(Fragment startingFragment, int id) {
    if (currentTabId != null) {
      ScreenCoordinator coordinator = screenCoordinators.get(currentTabId);
      coordinator.dismissAll();
    }
    currentTabId = id;
    ScreenCoordinator coordinator = screenCoordinators.get(id);
    if (coordinator == null) {
      coordinator = new ScreenCoordinator(activity, container, null);
      screenCoordinators.put(id, coordinator);
    }
    coordinator.showTab(startingFragment, id);
  }

  @Nullable
  public ScreenCoordinator getCurrentScreenCoordinator() {
    if (currentTabId == null)
      return null;
    return screenCoordinators.get(currentTabId);
  }

  public boolean onBackPressed() {
    if (currentTabId == null) {
      return false;
    }
    screenCoordinators.get(currentTabId).pop();
    return true;
  }
}