package com.airbnb.android.react.navigation;

import android.support.v4.app.FragmentActivity;

import com.facebook.react.ReactRootView;

public interface ReactInterface {
  // @formatter:off
  String getInstanceId();
  ReactRootView getReactRootView();
  ReactToolbar getToolbar();
  boolean isDismissible();
  void signalFirstRenderComplete();
  void setLink(String link);
  void notifySharedElementAddition();
  FragmentActivity getActivity();
  // @formatter:on
}
