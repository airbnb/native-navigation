package com.airbnb.android.react.navigation;

import com.facebook.react.ReactRootView;

public interface ReactInterface {
  // @formatter:off
  String getInstanceId();
  ReactAwareActivityFacade getActivity();
  ReactRootView getReactRootView();
  ReactToolbar getToolbar();
  boolean isDismissible();
  void signalFirstRenderComplete();
  //    void setMenuButtons(List<MenuButton> menuButtons);
  void setLink(String link);
  void notifySharedElementAddition();
  // @formatter:on
}
