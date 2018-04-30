package com.airbnb.android.react.navigation;

import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

public class TabViewManager extends SimpleViewManager<TabView> {

    private static final String TAG = "TabViewManager";

    @Override
    public String getName() {
        return "NativeNavigationTabView";
    }

    @Override
    protected TabView createViewInstance(ThemedReactContext reactContext) {
        Log.d(TAG, "createViewInstance");
        return new TabView(reactContext, null);
    }

    @ReactProp(name = "route")
    public void setRoute(TabView view, String route) {
        Log.d(TAG, "setRoute");
        view.setRoute(route);
    }

    @ReactProp(name = "props")
    public void setProps(TabView view, ReadableMap props) {
        Log.d(TAG, "setProps");
        view.setProps(props);
    }

    @ReactProp(name = "config")
    public void setConfig(TabView view, ReadableMap config) {
        Log.d(TAG, "setConfig");
        view.setConfig(config);
    }
}
