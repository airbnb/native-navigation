package com.airbnb.android.react.navigation;

import android.content.Intent;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

public final class ReactNativeUtils {
  static final String VERSION_CONSTANT_KEY = "VERSION";
  private static final String IS_SHARED_ELEMENT_TRANSITION = "isSharedElementTransition";

  private ReactNativeUtils() {
  }

  /** Emits a JS event with the provided name and data if the rect context is initialized */
  static void maybeEmitEvent(@Nullable ReactContext context, String name, @Nullable Object data) {
    if (context == null) {
      throw new IllegalArgumentException(
          String.format("reactContext is null (calling event: %s)", name));
    }
    if (context.hasActiveCatalystInstance()) {
      try {
        context.getJSModule(RCTDeviceEventEmitter.class).emit(name, data);
      } catch (RuntimeException e) {
        // the JS bundle hasn't finished executing, so this call is going to be lost.
        // In the future, we could maybe set something up to queue the call, and then pass them through once
        // the bundle has finished getting parsed, but for now I am going to just swallow the error.
      }
    }
  }

  /** Returns true if the provided intent will launch a ReactNative Activity, false otherwise. */
  static boolean isReactNativeIntent(Intent intent) {
    String className = intent.getComponent().getClassName();
    return ReactNativeModalActivity.class.getName().equals(className)
        || ReactNativeActivity.class.getName().equals(className);
  }

  static boolean isSharedElementTransition(Intent intent) {
    return intent.getBooleanExtra(IS_SHARED_ELEMENT_TRANSITION, false);
  }

  static void setIsSharedElementTransition(Intent intent, boolean value) {
    intent.putExtra(IS_SHARED_ELEMENT_TRANSITION, value);
  }
}
