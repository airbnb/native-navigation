package com.airbnb.android.react.navigation;

import com.airbnb.android.R;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.Map;

import static com.airbnb.android.react.navigation.ReactNativeUtils.VERSION_CONSTANT_KEY;

public class SharedElementGroupManager extends ViewGroupManager<ReactViewGroup> {

    private static final String REACT_CLASS = "NativeNavigationSharedElementGroup";

    private static final int VERSION = 1;

    @Override
    public Map<String, Object> getExportedViewConstants() {
        return MapBuilder.<String, Object> builder()
                .put(VERSION_CONSTANT_KEY, VERSION)
                .build();
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
