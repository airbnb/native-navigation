package com.airbnb.android.nativenavigator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReadableMap;
import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReactNavigationCoordinator {
    /**
     * NOTE(lmr): In the future, we would like to replace this with an annotation parser that generates this map based off of the
     * `ReactExposedActivity` annotations. For now, this should work well enough in the interim.
     */
    private final List<ReactExposedActivityParams> exposedActivities;
    private final Map<String, WeakReference<ReactNativeActivity>> activityMap = new HashMap<>();

    public ReactNavigationCoordinator(List<ReactExposedActivityParams> exposedActivities) {
        this.exposedActivities = exposedActivities;
    }

    void registerActivity(ReactNativeActivity activity, String name) {
        activityMap.put(name, new WeakReference<>(activity));
    }

    void unregisterActivity(String name) {
        activityMap.remove(name);
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
    ReactNativeActivity activityFromId(String id) {
        WeakReference<ReactNativeActivity> ref = activityMap.get(id);
        return ref == null ? null : ref.get();
    }
}