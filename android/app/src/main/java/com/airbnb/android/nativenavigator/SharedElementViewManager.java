package com.airbnb.android.nativenavigator;

import android.support.v4.view.ViewCompat;
import android.view.View;

import com.airbnb.android.R;
import com.airbnb.android.views.ReactAirImageView;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.view.ReactViewGroup;

public class SharedElementViewManager extends ViewGroupManager<ReactViewGroup> {
    private static final String REACT_CLASS = "AirbnbSharedElement";

    private ReactNavigationCoordinator coordinator;

    SharedElementViewManager(ReactNavigationCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public ReactViewGroup createViewInstance(ThemedReactContext context) {
        return new ReactViewGroup(context);
    }

    @ReactProp(name = "id")
    public void setIdentifier(ReactViewGroup view, String id) {
        view.setTag(R.id.react_shared_element_tranition_name, id);
    }

    @ReactProp(name = "airbnbInstanceId")
    public void setAirbnbInstanceId(ReactViewGroup view, String airbnbInstanceId) {
        view.setTag(R.id.react_shared_element_screen_instance_id, airbnbInstanceId);
    }

    @Override
    public void addView(ReactViewGroup parent, View child, int index) {
        String transitionName = (String) parent.getTag(R.id.react_shared_element_tranition_name);
        String airbnbInstanceId = (String) parent.getTag(R.id.react_shared_element_screen_instance_id);
        ReactNativeActivity activity = coordinator.activityFromId(airbnbInstanceId);

        if (child instanceof ReactAirImageView) {
            ReactAirImageView iv = (ReactAirImageView) child;
            // TODO(lmr): do something to wait for image to load
        }

        ViewCompat.setTransitionName(child, transitionName);
        parent.addView(child, index);

        if (activity != null) {
            activity.notifySharedElementAddition();
        }
    }
}