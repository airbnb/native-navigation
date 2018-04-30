package com.airbnb.android.react.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.react.bridge.ReadableMap;

/**
 * This view is used as a data structure to hold configuration for a TabBar in a
 * ReactNativeTabActivity, and doesn't actually render anything that will be visible
 * to the user.
 */
public class TabBarView extends ViewGroup {

    private ReadableMap prevConfig = ConversionUtil.EMPTY_MAP;

    private ReadableMap renderedConfig = ConversionUtil.EMPTY_MAP;

    public TabBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public void setConfig(ReadableMap config) {
        this.prevConfig = this.renderedConfig;
        this.renderedConfig = config;
    }

    public ReadableMap getConfig() {
        return renderedConfig;
    }

}
