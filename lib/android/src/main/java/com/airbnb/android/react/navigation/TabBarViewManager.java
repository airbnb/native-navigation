package com.airbnb.android.react.navigation;

import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

public class TabBarViewManager extends ViewGroupManager<TabBarView> {

    private static final String TAG = "TabBarViewManager";

    @Override
    public String getName() {
        return "NativeNavigationTabBarView";
    }

    @Override
    protected TabBarView createViewInstance(ThemedReactContext reactContext) {
        return new TabBarView(reactContext, null);
    }

    @ReactProp(name = "config")
    public void setConfig(TabBarView view, ReadableMap config) {
        Log.d(TAG, "setConfig");
        view.setConfig(config);
    }
}
