package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentActivity;

import com.airbnb.android.R;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

public final class ReactNativeUtils {

    static final String VERSION_CONSTANT_KEY = "VERSION";

    private static final String IS_SHARED_ELEMENT_TRANSITION = "isSharedElementTransition";

    private static final int SHARED_ELEMENT_TARGET_API = Build.VERSION_CODES.LOLLIPOP_MR1;

    private ReactNativeUtils() {
    }

    public static void pushScreen(Context context, String moduleName) {
        pushScreen(context, moduleName, null);
    }

    public static void pushScreen(Context context, String moduleName, @Nullable Bundle props) {
        Bundle options = ActivityOptionsCompat
                .makeCustomAnimation(context, R.anim.slide_in_right, R.anim.delay)
                .toBundle();
        showScreen(context, moduleName, props, options, false);
    }

    public static void presentScreen(Context context, String moduleName) {
        presentScreen(context, moduleName, null);
    }

    public static void presentScreen(Activity context, String moduleName, @Nullable Bundle props) {
        Bundle options = ActivityOptionsCompat
                .makeCustomAnimation(context, R.anim.slide_up, R.anim.delay)
                .toBundle();
        Intent intent = intent(context, moduleName, props, true);
        context.startActivity(intent, options);
    }

    public static void presentScreen(Context context, String moduleName, @Nullable Bundle props) {
        Bundle options = ActivityOptionsCompat
                .makeCustomAnimation(context, R.anim.slide_up, R.anim.delay)
                .toBundle();
        showScreen(context, moduleName, props, options, true);
    }

    private static void showScreen(
            Context context, String moduleName, Bundle props, Bundle options, boolean isModal) {
        Intent intent = intent(context, moduleName, props, isModal);
        context.startActivity(intent, options);
    }

    // TODO: delete this?
    public static Intent intentWithDismissFlag() {
        return new Intent().putExtra(ReactNativeIntents.EXTRA_IS_DISMISS, true);
    }

    private static Intent intent(Context context, String moduleName, Bundle props, boolean isModal) {
        return new Intent(context, ReactModalActivity.class)
                .putExtra(ReactNativeFragment.EXTRA_IS_MODAL, isModal)
                .putExtra(ReactNativeFragment.EXTRA_REACT_MODULE_NAME, moduleName)
                .putExtra(ReactNativeFragment.EXTRA_REACT_PROPS, props);
    }

    /**
     * Emits a JS event with the provided name and data if the rect context is initialized
     */
    static void maybeEmitEvent(ReactContext context, String name, Object data) {
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

    /**
     * Returns true if the provided intent will launch a ReactNative Activity, false otherwise.
     */
    static boolean isReactNativeIntent(Intent intent) {
        String className = intent.getComponent().getClassName();
        return ReactNativeActivity.class.getName().equals(className);
    }

    static boolean isSharedElementTransition(Activity activity) {
        return isSharedElementTransition(activity.getIntent().getExtras());
    }

    static boolean isSharedElementTransition(Intent intent) {
        return isSharedElementTransition(intent.getExtras());
    }

    static boolean isSharedElementTransition(@Nullable Bundle args) {
        return Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API && args != null &&
                args.getBoolean(IS_SHARED_ELEMENT_TRANSITION, false);
    }

    static void setIsSharedElementTransition(Intent intent) {
        intent.putExtra(IS_SHARED_ELEMENT_TRANSITION, true);
    }

//  static boolean isSuccessfullyInitialized(ReactInstanceManager reactInstanceManager) {
//    // TODO
//    return false;
//  }

    static void showAlertBecauseChecksFailed(FragmentActivity activity, Object o) {
        // TODO
    }
}
