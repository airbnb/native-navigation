package com.airbnb.android.react.navigation;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

public final class AndroidVersion {

    public static boolean isAtLeastJellyBeanMR1() {
        return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isAtLeastJellyBeanMR2() {
        return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean isAtLeastKitKat() {
        return VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    public static boolean isAtLeastLollipop() {
        return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
    }

    public static boolean isAtLeastMarshmallow() {
        return VERSION.SDK_INT >= VERSION_CODES.M;
    }

    public static boolean isAtLeastLollipopMR1() {
        return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean isJellyBean() {
        return VERSION.SDK_INT == VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isAtLeastNougat() {
        return VERSION.SDK_INT >= VERSION_CODES.N;
    }

    private AndroidVersion() {
        // Prevent users from instantiating this class.
    }
}
