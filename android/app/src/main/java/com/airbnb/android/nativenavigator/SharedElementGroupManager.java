package com.airbnb.android.nativenavigator;

import com.airbnb.android.R;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.view.ReactViewGroup;

public class SharedElementGroupManager extends ViewGroupManager<ReactViewGroup> {
    private static final String REACT_CLASS = "AirbnbSharedElementGroup";

    private ReactNavigationCoordinator coordinator;

    SharedElementGroupManager(ReactNavigationCoordinator coordinator) {
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
        view.setTag(R.id.react_shared_element_group_id, id);
    }
}