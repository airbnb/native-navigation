package com.airbnb.android.react.navigation;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class NativeNavigationPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.<NativeModule> singletonList(
                new NavigatorModule(reactContext, ReactNavigationCoordinator.sharedInstance));
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager> asList(
                new SharedElementGroupManager(),
                new SharedElementViewManager(ReactNavigationCoordinator.sharedInstance),
                new TabBarViewManager(),
                new TabViewManager()
        );
    }
}
