package com.airbnb.android.react.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.View;

import com.facebook.react.bridge.ReadableMap;

/**
 * This view is used as a data structure to hold configuration for a Tab in a
 * ReactNativeTabActivity, and doesn't actually render anything that will be visible
 * to the user.
 */
public class TabView extends View {

    private String route;

    private String title;

    private ReadableMap prevConfig;

    private ReadableMap renderedConfig;

    private Bundle props;

    private Fragment fragment;

    public TabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setVisibility(View.GONE);
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public void setProps(ReadableMap props) {
        this.props = ConversionUtil.toBundle(props);
    }

    public void setConfig(ReadableMap config) {
        this.prevConfig = this.renderedConfig;
        this.renderedConfig = config;
    }

    public ReadableMap getPrevConfig() {
        return prevConfig;
    }

    public ReadableMap getRenderedConfig() {
        return renderedConfig;
    }

    public Fragment getFragment() {
        if (fragment == null) {
            fragment = instantiateFragment();
        }
        return fragment;
    }

    public String getRoute() {
        return route;
    }

    private ReactNativeFragment instantiateFragment() {
        return ReactNativeFragment.newInstance(route, props);
    }
}
