package com.airbnb.android.react.navigation;

import android.support.v4.view.ViewCompat;
import android.view.View;

import com.airbnb.android.R;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.views.image.ReactImageView;
import com.facebook.react.views.view.ReactViewGroup;

import java.util.Map;

import static com.airbnb.android.react.navigation.ReactNativeUtils.VERSION_CONSTANT_KEY;

public class SharedElementViewManager extends ViewGroupManager<ReactViewGroup> {

    private static final String REACT_CLASS = "NativeNavigationSharedElement";

    private static final int VERSION = 1;

    private final ReactNavigationCoordinator coordinator;

    SharedElementViewManager(ReactNavigationCoordinator coordinator) {
        this.coordinator = coordinator;
    }

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
        view.setTag(R.id.react_shared_element_transition_name, id);
    }

    @ReactProp(name = "nativeNavigationInstanceId")
    public void setInstanceId(ReactViewGroup view, String instanceId) {
        view.setTag(R.id.react_shared_element_screen_instance_id, instanceId);
    }

    @Override
    public void addView(ReactViewGroup parent, View child, int index) {
        String transitionName = (String) parent.getTag(R.id.react_shared_element_transition_name);
        String instanceId = (String) parent.getTag(R.id.react_shared_element_screen_instance_id);
        ReactInterface component = coordinator.componentFromId(instanceId);

        if (child instanceof ReactImageView) {
            ReactImageView iv = (ReactImageView) child;
            // TODO(lmr): do something to wait for image to load
        }

        ViewCompat.setTransitionName(child, transitionName);
        parent.addView(child, index);

        if (component != null) {
            component.notifySharedElementAddition();
        }
    }
}
