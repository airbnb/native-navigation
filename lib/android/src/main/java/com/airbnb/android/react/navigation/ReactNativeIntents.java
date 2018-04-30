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

    static final String EXTRA_CODE = "code";

    static final String EXTRA_IS_DISMISS = "isDismiss";

    static final String INSTANCE_ID_PROP = "nativeNavigationInstanceId";

    static final String INITIAL_BAR_HEIGHT_PROP = "nativeNavigationInitialBarHeight";

    private static final String SHARED_ELEMENT_TRANSITION_GROUP_OPTION = "transitionGroup";

    private static ReactNavigationCoordinator coordinator = ReactNavigationCoordinator.sharedInstance;

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
        Intent intent = pushIntent(activity, moduleName, props);
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
        Intent intent = presentIntent(activity, moduleName, props);
        //noinspection unchecked
        Bundle options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity)
                .toBundle();
        activity.startActivity(intent, options);
    }

    static Bundle getSharedElementOptionsBundle(
            Activity activity, Intent intent, @Nullable ReadableMap options) {
        ViewGroup transitionGroup = null;
        if (activity instanceof ReactInterface && options != null &&
                options.hasKey(SHARED_ELEMENT_TRANSITION_GROUP_OPTION)) {
            ReactRootView reactRootView = ((ReactInterface) activity).getReactRootView();
            transitionGroup = ViewUtils.findViewGroupWithTag(
                    reactRootView,
                    R.id.react_shared_element_group_id,
                    options.getString(SHARED_ELEMENT_TRANSITION_GROUP_OPTION));
        }

        if (transitionGroup == null) {
            // Even though there is no transition group, we want the activity options to include a scene
            // transition so that we can postpone the enter transition.
            //noinspection unchecked
            return ActivityOptionsCompat.makeSceneTransitionAnimation(activity).toBundle();
        } else {
            ReactNativeUtils.setIsSharedElementTransition(intent);
            return AutoSharedElementCallback.getActivityOptionsBundle(activity, transitionGroup);
        }
    }

    static Intent pushIntent(Context context, String moduleName, @Nullable Bundle props) {
        Class destClass = coordinator.getOrDefault(moduleName).mode.getPushActivityClass();
        return new Intent(context, destClass)
                .putExtras(intentExtras(moduleName, props));
    }

    static Intent presentIntent(
            Context context, String moduleName, @Nullable Bundle props) {
        Class destClass = coordinator.getOrDefault(moduleName).mode.getPresentActivityClass();
        return new Intent(context, destClass)
                .putExtras(intentExtras(moduleName, props));
    }

    private static Bundle intentExtras(String moduleName, @Nullable Bundle props) {
        return new BundleBuilder()
                .putString(EXTRA_MODULE_NAME, moduleName)
                .putBundle(EXTRA_PROPS, props)
                .toBundle();
    }
}
