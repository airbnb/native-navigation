package com.airbnb.android.react.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * This is a custom ViewGroup that draws the fragment that at the end of the back stack on top.
 *
 * Normally, Fragment exit transitions play on top of Fragment enter transitions both when
 * pushing and popping the back stack. This is an artifact of the way ViewGroup draws views by
 * default.
 *
 * When a FragmentTransaction is executed, the FragmentManger removes the current fragment view
 * and adds the new fragment view to the container. Normally when a view is removed, it won't be
 * drawn. However, when removing a View, if there is an animation currently playing on it, ViewGroup
 * will add it to a special list of disappearing views
 * (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/view/ViewGroup.java#4713)
 *
 * During dispatchDraw, ViewGroup draws disappearing views at the end
 * (https://android.googlesource.com/platform/frameworks/base/+/refs/heads/master/core/java/android/view/ViewGroup.java#3531)
 * This ignore the original z order of views and always draws exiting views on top.
 *
 * This behavior makes it impossible to do a modal animation where a new Fragment slides up
 * over the existing content because the existing content will be drawn on top of the new Fragment.
 *
 * However, there is no such problem when popping the modal because you want the exiting fragment
 * to draw on top.
 *
 * This ViewGroup looks at the back stack size when a view is added and reverses the drawing order
 * if the back stack grew since the last view was added.
 */
public class ScreenCoordinatorLayout extends FrameLayout {
  private List<DrawingOp> drawingOpPool = new ArrayList<>();
  private Stack<DrawingOp> drawingOps = new Stack<>();

  private int previousBackStackEntryCount = 0;
  private boolean reverseDrawing = false;

  private FragmentManager fragmentManager;

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

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    int backStackEntryCount = fragmentManager.getBackStackEntryCount();
    reverseDrawing = backStackEntryCount > previousBackStackEntryCount;
    previousBackStackEntryCount = backStackEntryCount;
    super.addView(child, index, params);
  }

  @Override
  public void removeView(View view) {
    super.removeView(view);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    while (!drawingOps.isEmpty()) {
      DrawingOp op = drawingOps.remove(drawingOps.size() - 1);
      op.draw();
      drawingOpPool.add(op);
    }
  }

  @Override
  protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
    if (reverseDrawing) {
      drawingOps.add(obtainDrawingOp().set(canvas, child, drawingTime));
      return true;
    } else {
      return super.drawChild(canvas, child, drawingTime);
    }
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
