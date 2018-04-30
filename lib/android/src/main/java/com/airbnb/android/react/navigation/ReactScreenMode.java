package com.airbnb.android.react.navigation;

public enum ReactScreenMode {
    SCREEN(ReactNativeActivity.class, ReactNativeActivity.class),
    TABS(ReactNativeTabActivity.class),
    UNKNOWN(ReactNativeActivity.class, ReactNativeActivity.class);

    public static ReactScreenMode fromString(String s) {
        try {
            return ReactScreenMode.valueOf(s.toUpperCase());
        } catch (Exception e) {
            return ReactScreenMode.UNKNOWN;
        }
    }

    // TODO: merge these fields. They should be the same.
    private Class pushActivityClass;

    private Class presentActivityClass;

    ReactScreenMode(
            Class pushActivityClass
    ) {
        this(pushActivityClass, pushActivityClass);
    }

    ReactScreenMode(
            Class pushActivityClass,
            Class presentActivityClass
    ) {
        this.pushActivityClass = pushActivityClass;
        this.presentActivityClass = presentActivityClass;
    }

    public Class getPushActivityClass() {
        return this.pushActivityClass;
    }

    public Class getPresentActivityClass() {
        return this.presentActivityClass;
    }
}
