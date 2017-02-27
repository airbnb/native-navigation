package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.ViewGroup;

import com.airbnb.android.R;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReadableMap;

public final class ReactNativeIntents {
  static final String EXTRA_MODULE_NAME = "REACT_MODULE_NAME";
  static final String EXTRA_PROPS = "REACT_PROPS";
  private static final String EXTRA_IS_MODAL = "REACT_IS_MODAL";
  private static final String SHARED_ELEMENT_TRANSITION_GROUP_OPTION = "transitionGroup";

  private ReactNativeIntents() {
  }

  @SuppressWarnings({"WeakerAccess", "unused"})
  public static void pushScreen(Activity activity, String moduleName) {
    pushScreen(activity, moduleName, null);
  }

  @SuppressWarnings("WeakerAccess")
  public static void pushScreen(Activity activity, String moduleName, @Nullable Bundle props) {
    // TODO: right now this is the same as presentScreen but eventually it should just do
    // a fragment transaction
    Intent intent = presentIntent(activity, moduleName, props, false);
    //noinspection unchecked
    Bundle options = ActivityOptionsCompat
            .makeSceneTransitionAnimation(activity)
            .toBundle();
    activity.startActivity(intent, options);
  }

  @SuppressWarnings("WeakerAccess")
  public static void presentScreen(Activity context, String moduleName) {
    presentScreen(context, moduleName, null);
  }


  @SuppressWarnings("WeakerAccess")
  public static void presentScreen(Activity activity, String moduleName, @Nullable Bundle props) {
    Intent intent = presentIntent(activity, moduleName, props, false);
    //noinspection unchecked
    Bundle options = ActivityOptionsCompat
            .makeSceneTransitionAnimation(activity)
            .toBundle();
    activity.startActivity(intent, options);
  }

  @SuppressWarnings({"WeakerAccess", "unused"})
  public static void presentModal(Activity activity, String moduleName) {
    presentModal(activity, moduleName, null);
  }

  @SuppressWarnings("WeakerAccess")
  public static void presentModal(Activity activity, String moduleName, @Nullable Bundle props) {
    Intent intent = presentIntent(activity, moduleName, props, false);
    //noinspection unchecked
    Bundle options = ActivityOptionsCompat
            .makeSceneTransitionAnimation(activity)
            .toBundle();
    activity.startActivity(intent, options);
  }

  @Nullable
  static Bundle getSharedElementOptionsBundle(
          Activity activity, Intent intent, ReadableMap props) {
    if (props != null && props.hasKey(
            SHARED_ELEMENT_TRANSITION_GROUP_OPTION) && activity instanceof ReactInterface) {
      // TODO: 10/6/16 emily this doesn't work for ReactNativeFragment
      ReactRootView reactRootView = ((ReactInterface) activity).getReactRootView();
      ViewGroup transitionGroup = ViewUtils.findViewGroupWithTag(
              reactRootView,
              R.id.react_shared_element_group_id,
              props.getString(SHARED_ELEMENT_TRANSITION_GROUP_OPTION));
      if (transitionGroup != null) {
        ReactNativeUtils.setIsSharedElementTransition(intent, true);
        return  AutoSharedElementCallback.getActivityOptionsBundle(activity, transitionGroup);
      }
    }
    return null;
  }

  static Intent pushIntent(Context context, String moduleName, @Nullable Bundle props) {
    return new Intent(context, ReactNativeActivity.class)
            .putExtras(intentExtras(moduleName, props, false));
  }

  static Intent presentIntent(Context context, String moduleName,
          @Nullable Bundle props, boolean isModal) {
    Class<?> intentClass = isModal ? ReactNativeModalActivity.class : ReactNativeActivity.class;
    return new Intent(context, intentClass)
            .putExtras(intentExtras(moduleName, props, isModal));
  }


  private static Bundle intentExtras(String moduleName, @Nullable Bundle props, boolean isModal) {
    return new BundleBuilder()
            .putString(EXTRA_MODULE_NAME, moduleName)
            .putBundle(EXTRA_PROPS, props)
            .putBoolean(EXTRA_IS_MODAL, isModal)
            .toBundle();
  }
}
