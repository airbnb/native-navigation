package com.example;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;

import java.util.Collections;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {
  private final ReactNativeHost reactNativeHost = new ReactNativeHost(this) {
    @Override protected boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    protected String getJSMainModuleName() {
      return "example/index";
    }

    @Override protected List<ReactPackage> getPackages() {
      return Collections.<ReactPackage>singletonList(new MainReactPackage());
    }
  };

  @Override public ReactNativeHost getReactNativeHost() {
    return reactNativeHost;
  }
}
