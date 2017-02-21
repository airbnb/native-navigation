package com.airbnb.android.react.navigation;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

final class AndroidVersion {
  static boolean isAtLeastJellyBeanMR1() {
    return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;
  }

  static boolean isAtLeastJellyBeanMR2() {
    return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;
  }

  static boolean isAtLeastKitKat() {
    return VERSION.SDK_INT >= VERSION_CODES.KITKAT;
  }

  static boolean isAtLeastLollipop() {
    return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;
  }

  static boolean isAtLeastMarshmallow() {
    return VERSION.SDK_INT >= VERSION_CODES.M;
  }

  static boolean isAtLeastLollipopMR1() {
    return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1;
  }

  static boolean isJellyBean() {
    return VERSION.SDK_INT == VERSION_CODES.JELLY_BEAN;
  }

  static boolean isAtLeastNougat() {
    return VERSION.SDK_INT >= VERSION_CODES.N;
  }

  private AndroidVersion() {
    // Prevent users from instantiating this class.
  }
}
