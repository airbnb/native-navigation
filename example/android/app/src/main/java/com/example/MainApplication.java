package com.example;

import android.app.Application;

import android.util.Log;
import com.airbnb.android.react.navigation.NativeNavigationPackage;
import com.airbnb.android.react.navigation.ReactNavigationCoordinator;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.shell.MainReactPackage;

import java.util.Collections;
import java.util.List;

public class MainApplication extends Application {

  ReactInstanceManager manager;

  @Override
  public void onCreate() {
    super.onCreate();
    manager = ReactInstanceManager.builder()
            .setApplication(this)
            .setBundleAssetName("index.js") // file name to be used locally
            .setJSMainModuleName("example/index") // file name to be used for packager
            .addPackage(new MainReactPackage())
            .addPackage(new NativeNavigationPackage())
            .setUseDeveloperSupport(true)
            .setInitialLifecycleState(LifecycleState.BEFORE_RESUME)
            .build();

    Log.d("ReactNativeActivity", "application onCreate");

    ReactNavigationCoordinator.sharedInstance.injectReactInstanceManager(manager);

    manager.createReactContextInBackground();
  }
}
