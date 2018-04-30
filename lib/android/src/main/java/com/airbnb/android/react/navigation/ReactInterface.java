package com.airbnb.android.react.navigation;

import android.support.v4.app.FragmentActivity;

import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReadableMap;

public interface ReactInterface {

    // @formatter:off
  String getInstanceId();
  ReactRootView getReactRootView();
  ReactToolbar getToolbar();
  boolean isDismissible();
  void signalFirstRenderComplete();
  void notifySharedElementAddition();
  FragmentActivity getActivity();
  void emitEvent(String eventName, Object object);
  void receiveNavigationProperties(ReadableMap properties);
  void dismiss();
  // @formatter:on
}
