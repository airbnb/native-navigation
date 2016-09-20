package com.airbnb.android.react.navigation;

import com.facebook.react.ReactRootView;

import java.util.List;

public interface ReactInterface {
    String getInstanceId();
    ReactAwareActivityFacade getActivity();
    ReactRootView getReactRootView();
    ReactToolbarFacade getToolbar();
    boolean isDismissible();
    void signalFirstRenderComplete();
//    void setMenuButtons(List<MenuButton> menuButtons);
    void setLink(String link);
    void notifySharedElementAddition();
}
