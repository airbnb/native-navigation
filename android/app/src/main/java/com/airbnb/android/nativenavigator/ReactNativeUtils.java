package com.airbnb.android.nativenavigator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class ReactNativeUtils {
    public static final String REACT_MODULE_NAME = "REACT_MODULE_NAME";
    public static final String REACT_PROPS = "REACT_PROPS";
    private static final String IS_SHARED_ELEMENT_TRANSITION = "isSharedElementTransition";

    private ReactNativeUtils() {
    }


    public static Bundle intentExtras(String moduleName, Bundle props) {
        return new BundleBuilder()
                .putString(REACT_MODULE_NAME, moduleName)
                .putBundle(REACT_PROPS, props)
                .toBundle();
    }

    public static long getLongFromBundle(Bundle bundle, String key) {
        long userId = bundle.getLong(key, -1);
        if (userId != -1) {
            return userId;
        }
        // React Native always sends numbers as double by default,
        // so we need to check for that as well
        return (long) bundle.getDouble(key, -1);
    }

    /** Emits a JS event with the provided name and data if the rect context is initialized */
    public static void maybeEmitEvent(ReactContext context, String name, @Nullable Object data) {
        if (context == null) {
//            BugsnagWrapper.throwOrNotify(new IllegalArgumentException(String.format("reactContext is null (calling event: %s)", name)));
            return;
        }
        if (context.hasActiveCatalystInstance()) {
            context.getJSModule(RCTDeviceEventEmitter.class).emit(name, data);
        }
    }

    /** Returns true if the provided intent will launch a ReactNative Activity, false otherwise. */
    public static boolean isReactNativeIntent(Intent intent) {
        String className = intent.getComponent().getClassName();
        return ReactNativeModalActivity.class.getName().equals(className)
                || ReactNativeActivity.class.getName().equals(className);
    }

    public static boolean isSharedElementTransition(Intent intent) {
        return intent.getBooleanExtra(IS_SHARED_ELEMENT_TRANSITION, false);
    }

    public static void setIsSharedElementTransition(Intent intent, boolean value) {
        intent.putExtra(IS_SHARED_ELEMENT_TRANSITION, value);
    }
}
