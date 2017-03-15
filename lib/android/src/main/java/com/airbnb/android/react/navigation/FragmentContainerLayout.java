package com.airbnb.android.react.navigation;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.LongSparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FragmentContainerLayout extends FrameLayout {
  public static final int TAG_BACK_STACK_INDEX = "back_stack_index".hashCode();

  private final LongSparseArray<Integer> viewsAdded = new LongSparseArray<>();
  private List<DrawingOp> drawingOpPool = new ArrayList<>();
  private List<DrawingOp> drawingOps = new ArrayList<>();

  private int viewAddedCount = 0;

  private final Comparator<DrawingOp> opComparator = new Comparator<DrawingOp>() {
    @Override
    public int compare(DrawingOp op1, DrawingOp op2) {
      Object op1Tag = op1.child.getTag(TAG_BACK_STACK_INDEX);
      Object op2Tag = op2.child.getTag(TAG_BACK_STACK_INDEX);
      if (op1Tag == null || op2Tag == null) {
        throw new IllegalStateException("There is not record of a drawn view.");
      }
      int op1Count = (int) op1Tag;
      int op2Count = (int) op2Tag;
      return (op1Count < op2Count) ? -1 : ((op1Count == op2Count) ? 0 : 1);
    }
  };

  public FragmentContainerLayout(@NonNull Context context) {
    super(context);
  }

  public FragmentContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public FragmentContainerLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (child.getTag(TAG_BACK_STACK_INDEX) == null) {
      throw new IllegalArgumentException("You must specify TAG_BACK_STACK_INDEX");
    }
    super.addView(child, index, params);
    viewsAdded.put(child.hashCode(), viewAddedCount++);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    Collections.sort(drawingOps, opComparator);

    while (!drawingOps.isEmpty()) {
      DrawingOp op = drawingOps.remove(drawingOps.size() - 1);
      op.draw();
      drawingOpPool.add(op);
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