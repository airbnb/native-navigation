package com.airbnb.android.react.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is a custom ViewGroup that draws the fragment that at the end of the back stack on top.
 * <p>
 * Normally, Fragment exit transitions play on top of Fragment enter transitions both when
 * pushing and popping the back stack. This is an artifact of the way ViewGroup draws views by
 * default.
 * <p>
 * When a FragmentTransaction is executed, the FragmentManger removes the current fragment view
 * and adds the new fragment view to the container. Normally when a view is removed, it won't be
 * drawn. However, when removing a View, if there is an animation currently playing on it, ViewGroup
 * will add it to a special list of disappearing views
 * (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/view/ViewGroup.java#4713)
 * <p>
 * During dispatchDraw, ViewGroup draws disappearing views at the end
 * (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/view/ViewGroup.java#3531)
 * This ignore the original z order of views and always draws exiting views on top.
 * <p>
 * This behavior makes it impossible to do a modal animation where a new Fragment slides up
 * over the existing content because the existing content will be drawn on top of the new Fragment.
 * <p>
 * However, there is no such problem when popping the modal because you want the exiting fragment
 * to draw on top.
 * <p>
 * This ViewGroup looks at the back stack size when a view is added and reverses the drawing order
 * if the back stack grew since the last view was added.
 */
public class ScreenCoordinatorLayout extends FrameLayout {

    private final List<DrawingOp> drawingOpPool = new ArrayList<>();

    private final List<DrawingOp> drawingOps = new ArrayList<>();

    private FragmentManager fragmentManager;

    private boolean reverseLastTwoChildren = false;

    private boolean isDetachingCurrentScreen = false;

    private int previousChildrenCount = 0;

    public ScreenCoordinatorLayout(@NonNull Context context) {
        super(context);
    }

    public ScreenCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScreenCoordinatorLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void willDetachCurrentScreen() {
        isDetachingCurrentScreen = true;
    }

    @Override
    public void removeView(final View view) {
        if (isDetachingCurrentScreen) {
            isDetachingCurrentScreen = false;
            reverseLastTwoChildren = true;
        }

        super.removeView(view);
    }

    private void drawAndRelease(int index) {
        DrawingOp op = drawingOps.remove(index);
        op.draw();
        drawingOpPool.add(op);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // check the view removal is completed (by comparing the previous children count)
        if (drawingOps.size() < previousChildrenCount) {
            reverseLastTwoChildren = false;
        }
        previousChildrenCount = drawingOps.size();

        if (reverseLastTwoChildren && drawingOps.size() >= 2) {
            Collections.swap(drawingOps, drawingOps.size() - 1, drawingOps.size() - 2);
        }

        while (!drawingOps.isEmpty()) {
            drawAndRelease(0);
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        drawingOps.add(obtainDrawingOp().set(canvas, child, drawingTime));
        return true;
    }

    private void performDraw(DrawingOp op) {
        super.drawChild(op.canvas, op.child, op.drawingTime);
    }

    private DrawingOp obtainDrawingOp() {
        if (drawingOpPool.isEmpty()) {
            return new DrawingOp();
        }
        return drawingOpPool.remove(drawingOpPool.size() - 1);
    }

    private final class DrawingOp {

        private Canvas canvas;

        private View child;

        private long drawingTime;

        DrawingOp set(Canvas canvas, View child, long drawingTime) {
            this.canvas = canvas;
            this.child = child;
            this.drawingTime = drawingTime;
            return this;
        }

        void draw() {
            performDraw(this);
            canvas = null;
            child = null;
            drawingTime = 0;
        }
    }
}
