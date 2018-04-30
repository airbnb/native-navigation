package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import android.transition.Transition;
import android.transition.Transition.TransitionListener;

@TargetApi(VERSION_CODES.KITKAT)
public class SimpleTransitionListener implements TransitionListener {

    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
    }

    @Override
    public void onTransitionCancel(Transition transition) {
    }

    @Override
    public void onTransitionPause(Transition transition) {
    }

    @Override
    public void onTransitionResume(Transition transition) {
    }
}
