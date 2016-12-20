package com.airbnb.android.react.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

public final class ReactNativeIntents {
  public static final String REACT_MODULE_NAME = "REACT_MODULE_NAME";
  public static final String REACT_PROPS = "REACT_PROPS";

  public static Bundle intentExtras(String moduleName, @Nullable Bundle props) {
    return new BundleBuilder()
        .putString(REACT_MODULE_NAME, moduleName)
        .putBundle(REACT_PROPS, props)
        .toBundle();
  }

  public static Intent intent(Context context, String moduleName) {
    return intent(context, moduleName, null);
  }

  public static Intent intent(Context context, String moduleName, @Nullable Bundle props) {
    return new Intent(context, ReactNativeActivity.class)
        .putExtras(intentExtras(moduleName, props));
  }

  public static Intent intentForPortraitMode(Context context, String moduleName,
      @Nullable Bundle props) {
    return new Intent(context, ReactNativeActivity.class)
        .putExtras(intentExtras(moduleName, props));
  }

  public static Intent modalIntent(Context context, String moduleName, @Nullable Bundle props) {
    return new Intent(context, ReactNativeModalActivity.class)
        .putExtras(intentExtras(moduleName, props));
  }
}
