package com.airbnb.android.nativenavigator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.List;
import java.util.Map;

import rx.functions.Action1;

// TODO(lmr): remove
import static com.airbnb.n2.components.AirToolbar.NAVIGATION_ICON_BACK;
import static com.airbnb.n2.components.AirToolbar.NAVIGATION_ICON_MENU;
import static com.airbnb.n2.components.AirToolbar.NAVIGATION_ICON_NONE;
import static com.airbnb.n2.components.AirToolbar.NAVIGATION_ICON_X;
import static com.airbnb.n2.components.AirToolbar.THEME_OPAQUE;
import static com.airbnb.n2.components.AirToolbar.THEME_TRANSPARENT_DARK_FOREGROUND;
import static com.airbnb.n2.components.AirToolbar.THEME_TRANSPARENT_LIGHT_FOREGROUND;

class NavigatorModule extends ReactContextBaseJavaModule {
    private static final int VERSION = 1;
    static final String EXTRA_PAYLOAD = "payload";
    static final String EXTRA_CODE = "code";
    static final String EXTRA_IS_DISMISS = "isDismiss";
    private static final String CLOSE_BEHAVIOR_DISMISS = "dismiss";
    private final static Map<String, Integer> LEFT_ICON_MAP = ImmutableMap.of(
            "close", NAVIGATION_ICON_X,
            "menu", NAVIGATION_ICON_MENU,
            "none", NAVIGATION_ICON_NONE,
            "nav-left", NAVIGATION_ICON_BACK);

    private final static Map<String, MenuButton> BUTTON_MAP = new Builder<String, MenuButton>()
            .put("map", new MenuButton(R.drawable.n2_ic_map, "Map"))
            .put("filters", new MenuButton(R.drawable.n2_ic_filters, "Filters"))
            .put("share", new MenuButton(R.drawable.n2_ic_share, "Share"))
            .put("invite", new MenuButton(R.drawable.n2_ic_invite_friends, "Invite Friends"))
            .put("heart", new MenuButton(R.drawable.heart, "Favorite"))
            .put("heart-alt", new MenuButton(R.drawable.heart_alt, "Favorite"))
            .put("more", new MenuButton(R.drawable.icon_more, "More"))
            .build();

    private static final Map<String, Integer> BACKGROUND_COLOR_MAP = ImmutableMap.of(
            "celebratory", R.color.n2_rausch,
            "valid", R.color.n2_babu,
            "invalid", R.color.n2_arches,
            "unvalidated", R.color.n2_babu,
            "white", R.color.white);

    private static final Map<String, Integer> FOREGROUND_COLOR_MAP = ImmutableMap.of(
            "celebratory", R.color.n2_action_bar_foreground_light,
            "valid", R.color.n2_action_bar_foreground_light,
            "invalid", R.color.n2_action_bar_foreground_light,
            "unvalidated", R.color.n2_action_bar_foreground_light,
            "white", R.color.n2_action_bar_foreground_dark);

    private static final Map<String, Integer> THEME_MAP = ImmutableMap.of(
            "opaque", THEME_OPAQUE,
            "transparent-dark", THEME_TRANSPARENT_DARK_FOREGROUND,
            "transparent-light", THEME_TRANSPARENT_LIGHT_FOREGROUND);

    private final ReactNavigationCoordinator coordinator;

    NavigatorModule(ReactApplicationContext reactContext, ReactNavigationCoordinator coordinator) {
        super(reactContext);
        this.coordinator = coordinator;
    }

    @Override
    public String getName() {
        return "NativeNavigatorModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        return ImmutableMap.of(
                "VERSION", VERSION);
    }

    @ReactMethod
    public void setTitle(String title, String id) {
        ReactNativeActivity activity = coordinator.activityFromId(id);
        withToolbar(activity, toolbar -> toolbar.setTitle(title));
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void setLink(String link, String id) {
        ReactNativeActivity activity = coordinator.activityFromId(id);
        //noinspection ConstantConditions
        withToolbar(activity, toolbar -> activity.setLink(link));
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void setLeftIcon(String leftIcon, String id) {
        @NavigationIcon int icon = leftIcon != null ? LEFT_ICON_MAP.get(leftIcon) : NAVIGATION_ICON_BACK;
        ReactNativeActivity activity = coordinator.activityFromId(id);
        withToolbar(activity, toolbar -> toolbar.setNavigationIcon(icon));
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void setButtons(ReadableArray buttons, String id) {
        List<String> btns = ConversionUtil.toStringArray(buttons);
        List<MenuButton> menuButtons = FluentIterable.from(btns).transform(BUTTON_MAP::get).toList();
        ReactNativeActivity activity = coordinator.activityFromId(id);
        //noinspection ConstantConditions
        withToolbar(activity, toolbar -> activity.setMenuButtons(menuButtons));
    }

    @ReactMethod
    public void setBackgroundColor(String color, String id) {
        @ColorRes int colorId = color != null ? BACKGROUND_COLOR_MAP.get(color) : BACKGROUND_COLOR_MAP.get("white");
        @ColorRes int fgColorId = color != null ? FOREGROUND_COLOR_MAP.get(color) : FOREGROUND_COLOR_MAP.get("white");
        ReactNativeActivity activity = coordinator.activityFromId(id);
        withToolbar(activity, toolbar -> {
            //noinspection ConstantConditions
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, colorId));
            toolbar.setForegroundColor(ContextCompat.getColor(activity, fgColorId));
        });
    }

    @ReactMethod
    public void setTheme(String theme, String id) {
        @Theme int themeInt = theme != null ? THEME_MAP.get(theme) : THEME_OPAQUE;
        ReactNativeActivity activity = coordinator.activityFromId(id);
        withToolbar(activity, toolbar -> toolbar.setTheme(themeInt));
    }

    /** Sets whether or not the Toolbar "home" button is visible */
    @SuppressWarnings("unused")
    @ReactMethod
    public void setLeadingButtonVisible(boolean leadingButtonVisible, String id) {
        ReactNativeActivity activity = coordinator.activityFromId(id);
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowHomeEnabled(leadingButtonVisible);
            }
        }
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @ReactMethod
    public void setCloseBehavior(String closeBehavior, String id) {
        if (CLOSE_BEHAVIOR_DISMISS.equals(closeBehavior)) {
            ReactNativeActivity activity = getActivity();
            if (activity != null) {
                activity.dismissOnFinish();
            }
        }
    }

    @Nullable
    private ReactNativeActivity getActivity() {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return null;
        }
        if (!(activity instanceof ReactNativeActivity)) {
            throw new IllegalStateException("React Native tried to start an activity while it was not the active activity.");
        }
        return (ReactNativeActivity) activity;
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void push(String screenName, ReadableMap props, ReadableMap options, Promise promise) {
        ReactNativeActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        Bundle propsBundle = ConversionUtil.toBundle(props);
        Intent intent = ReactNativeActivity.intent(getReactApplicationContext(), screenName, propsBundle);
        activity.startActivityWithPromise(intent, promise, options);
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @ReactMethod
    public void pushNative(String name, ReadableMap props, @Nullable ReadableMap options, Promise promise) {
        ReactNativeActivity activity = getActivity();
        if (activity != null) {
            Intent intent = coordinator.intentForKey(activity, name, props);
            activity.startActivityWithPromise(intent, promise, options);
        }
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void present(String screenName, ReadableMap props, ReadableMap options, Promise promise) {
        ReactNativeActivity activity = getActivity();
        if (activity != null) {
            Bundle propsBundle = ConversionUtil.toBundle(props);
            Intent intent = ReactNativeModalActivity.intent(getReactApplicationContext(), screenName, propsBundle);
            activity.startActivityWithPromise(intent, promise, options);
        }
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @ReactMethod
    public void presentNative(String name, ReadableMap props, @Nullable ReadableMap options, Promise promise) {
        ReactNativeActivity activity = getActivity();
        if (activity != null) {
            Intent intent = coordinator.intentForKey(activity, name, props);
            activity.startActivityWithPromise(intent, promise, options);
        }
    }

    @ReactMethod
    public void dismiss(ReadableMap payload, @SuppressWarnings("UnusedParameters") boolean animated) {
        ReactNativeActivity activity = getActivity();
        if (activity != null) {
            activity.dismiss(payload);
        }
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void pop(ReadableMap payload, boolean animated) {
        ReactNativeActivity activity = getActivity();
        if (activity != null) {
            activity.pop(payload);
        }
    }

    private interface ToolbarModifier extends Action1<Toolbar> {
    }

    private void withToolbar(@Nullable ReactNativeActivity activity, ToolbarModifier modifier) {
        if (activity != null) {
            activity.runOnUiThread(() -> {
                Toolbar toolbar = activity.getToolbar();
                if (toolbar != null) {
                    modifier.call(toolbar);
                }
            });
        }
    }
}