package com.airbnb.android.react.navigation;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.facebook.react.bridge.Promise;

import java.util.Stack;

class BackStack {

  private final Stack<Fragment> fragments = new Stack<>();
  private final String tag;
  private final ScreenCoordinator.PresentAnimation animation;
  private final Promise promise;

  BackStack(String tag, ScreenCoordinator.PresentAnimation animation, Promise promise) {
    this.tag = tag;
    this.animation = animation;
    this.promise = promise;
  }

  String getTag() {
    return tag;
  }

  ScreenCoordinator.PresentAnimation getAnimation() {
    return animation;
  }

  Promise getPromise() {
    return promise;
  }

  @Nullable
  Fragment peekFragment() {
    if (fragments.isEmpty()) {
      return null;
    }
    return fragments.peek();
  }

  void pushFragment(Fragment fragment) {
    fragments.push(fragment);
  }

  Fragment popFragment() {
    if (fragments.isEmpty()) {
      throw new IllegalStateException("Cannot pop empty stack.");
    }
    return fragments.remove(fragments.size() - 1);
  }

  int getSize() {
    return fragments.size();
  }

  @Override
  public String toString() {
    return "BackStack{" + ", tag='" + tag +
            ", size=" + fragments.size() +
            ", animation=" + animation +
            ", promise?=" + (promise != null) +
            '}';
  }
}
