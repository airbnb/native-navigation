package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactNavigationCoordinator {
    public static ReactNavigationCoordinator sharedInstance = new ReactNavigationCoordinator();

    private ReactNavigationCoordinator() {
    }

    public void injectExposedActivities(List<ReactExposedActivityParams> exposedActivities) {
        // TODO(lmr): would it make sense to warn or throw here if it's already set?
        this.exposedActivities = exposedActivities;
    }

    /**
     * NOTE(lmr): In the future, we would like to replace this with an annotation parser that generates this map based off of the
     * `ReactExposedActivity` annotations. For now, this should work well enough in the interim.
     */
    private List<ReactExposedActivityParams> exposedActivities;
    private final Map<String, WeakReference<ReactInterface>> componentsMap = new HashMap<>();
    private final Map<String, Integer> sceneBackgroundColorMap = new HashMap<>();
    private final Map<String, Integer> sceneToolbarForegroundColorMap = new HashMap<>();
    private final Map<String, Integer> sceneToolbarBackgroundColorMap = new HashMap<>();
    private final Map<String, Integer> sceneToolbarThemeMap = new HashMap<>();
    private final Map<String, Boolean> dismissCloseBehaviorMap = new HashMap<>();

    public void registerComponent(ReactInterface component, String name) {
        componentsMap.put(name, new WeakReference<>(component));
    }

    public void unregisterComponent(String name) {
        componentsMap.remove(name);
    }

    /**
     * Returns an {@link Intent} used for launching an {@link Activity} exposed to React Native flows based on the provided {@code key}. Will pass the
     * provided {@code arguments} as {@link Intent} extras. Activities should have been previously registered via {@code exposedActivities} in the
     * {@link ReactNavigationCoordinator} constructor.
     *
     * @see ReactExposedActivityParams#toIntent(Context, ReadableMap)
     */
    Intent intentForKey(Context context, String key, ReadableMap arguments) {
        Optional<Intent> intent = FluentIterable.from(exposedActivities)
                .firstMatch(a -> a.key().equals(key))
                .transform(a -> a.toIntent(context, arguments));
        if (!intent.isPresent()) {
            throw new IllegalArgumentException(String.format("Tried to push Activity with key '%s', but it could not be found", key));
        }
        return intent.get();
    }

    @Nullable
    ReactAwareActivityFacade activityFromId(String id) {
        WeakReference<ReactInterface> ref = componentsMap.get(id);
        return ref == null ? null : ref.get().getActivity();
    }

    @Nullable
    ReactInterface componentFromId(String id) {
        WeakReference<ReactInterface> ref = componentsMap.get(id);
        return ref == null ? null : ref.get();
    }

    // If set to true, the Activity will be dismissed when its Toolbar NavigationIcon (home button) is clicked,
    // instead of performing the default behavior (finish)
    public void setDismissCloseBehavior(String id, boolean dismissClose) {
        dismissCloseBehaviorMap.put(id, dismissClose);
    }

    public boolean getDismissCloseBehavior(ReactInterface reactInterface) {
        String id = reactInterface.getInstanceId();
        Boolean dismissClose = dismissCloseBehaviorMap.get(id);
        return dismissClose != null && dismissClose;
    }

    public void setBackgroundColorForModuleName(String sceneName, Integer color) {
        sceneBackgroundColorMap.put(sceneName, color);
    }

    public int getBackgroundColorForModuleName(String sceneName) {
        if (sceneBackgroundColorMap.containsKey(sceneName)) {
            return sceneBackgroundColorMap.get(sceneName);
        } else {
            return Color.WHITE;
        }
    }

    // TODO(lmr):
//    public void setToolbarThemeForModuleName(String sceneName, Integer theme) {
//        sceneToolbarThemeMap.put(sceneName, theme);
//    }
//
//    public @AirToolbar.Theme int getToolbarThemeForModuleName(String sceneName) {
//        if (sceneToolbarThemeMap.containsKey(sceneName)) {
//            @AirToolbar.Theme int result = sceneToolbarThemeMap.get(sceneName);
//            return result;
//        } else {
//            return AirToolbar.THEME_TRANSPARENT_DARK_FOREGROUND;
//        }
//    }

    public void setToolbarForegroundColorForModuleName(String sceneName, Integer color) {
        sceneToolbarForegroundColorMap.put(sceneName, color);
    }

    public Integer getToolbarForegroundColorForModuleName(String sceneName) {
        return sceneToolbarForegroundColorMap.get(sceneName);
    }

    public void setToolbarBackgroundColorForModuleName(String sceneName, Integer color) {
        sceneToolbarBackgroundColorMap.put(sceneName, color);
    }

    public Integer getToolbarBackgroundColorForModuleName(String sceneName) {
        return sceneToolbarBackgroundColorMap.get(sceneName);
    }
}
