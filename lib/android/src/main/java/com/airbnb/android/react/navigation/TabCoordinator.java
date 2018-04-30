package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.LongSparseArray;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class TabCoordinator {

    private static final String TAG = TabCoordinator.class.getSimpleName();

    private final LongSparseArray<ScreenCoordinator> screenCoordinators = new LongSparseArray<>();

    private final AppCompatActivity activity;

    private final ScreenCoordinatorLayout container;

    private Integer currentTabId = null;

    public TabCoordinator(AppCompatActivity activity, ScreenCoordinatorLayout container,
                          @Nullable Bundle savedInstanceState) {
        this.activity = activity;
        this.container = container;
    }

    public void showTab(Fragment startingFragment, int id) {
        if (currentTabId != null) {
            if (id == currentTabId) {
                // TODO: add support for other behavior here such as reset the tab stack.
                return;
            }
            ScreenCoordinator coordinator = screenCoordinators.get(currentTabId);
            coordinator.dismissAll();
        }
        currentTabId = id;
        ScreenCoordinator coordinator = screenCoordinators.get(id);
        if (coordinator == null) {
            coordinator = new ScreenCoordinator(activity, container, null);
            screenCoordinators.put(id, coordinator);
        }
        coordinator.presentScreen(startingFragment, ScreenCoordinator.PresentAnimation.Fade, null);
        Log.d(TAG, toString());
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TabCoordinator={");
        int size = screenCoordinators.size();
        for (int i = 0; i < size; i++) {
            long id = screenCoordinators.keyAt(i);
            ScreenCoordinator coordinator = screenCoordinators.valueAt(i);
            sb.append('\n').append(id).append(": ").append(coordinator);
        }
        sb.append("\n}");
        return sb.toString();
    }
}
