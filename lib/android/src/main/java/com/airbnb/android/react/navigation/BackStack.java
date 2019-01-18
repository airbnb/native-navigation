package com.airbnb.android.react.navigation;

import com.facebook.react.bridge.Promise;

class BackStack {

    private final String tag;

    private final ScreenCoordinator.PresentAnimation animation;

    private final Promise promise;

    private int fragmentCount = 0;

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

    void pushFragment() {
        fragmentCount++;
    }

    void popFragment() {
        fragmentCount--;
    }

    int getSize() {
        return fragmentCount;
    }

    boolean isEmpty() {
        return getSize() == 0;
    }

    @Override
    public String toString() {
        return "BackStack{" + ", tag='" + tag +
                ", size=" + fragmentCount +
                ", animation=" + animation +
                ", promise?=" + (promise != null) +
                '}';
    }
}
