package com.airbnb.android.react.navigation;

import com.facebook.react.bridge.ReadableMap;

class ReactScreenConfig {

    ReadableMap initialConfig;

    boolean waitForRender;

    ReactScreenMode mode;

    static final ReactScreenConfig EMPTY = new ReactScreenConfig(
            ConversionUtil.EMPTY_MAP,
            true,
            ReactScreenMode.SCREEN
    );

    ReactScreenConfig(
            ReadableMap initialConfig,
            boolean waitForRender,
            ReactScreenMode mode
    ) {
        this.initialConfig = initialConfig;
        this.waitForRender = waitForRender;
        this.mode = mode;
    }

}
