package com.airbnb.android.react.navigation;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.facebook.react.views.toolbar.DrawableWithIntrinsicSize;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.PixelUtil;

// TODO(lmr): we might want to make this an abstract class and have a default implementation
public class ReactToolbar extends Toolbar {

//  private static final String PROP_ACTION_ICON = "icon";
//  private static final String PROP_ACTION_SHOW = "show";
//  private static final String PROP_ACTION_SHOW_WITH_TEXT = "showWithText";
  private static final String TAG = "ReactToolbar";

  private static final String PROP_ICON_URI = "uri";
  private static final String PROP_ICON_WIDTH = "width";
  private static final String PROP_ICON_HEIGHT = "height";

  private final DraweeHolder mLogoHolder;
  private final DraweeHolder mNavIconHolder;
  private final DraweeHolder mOverflowIconHolder;
  private final MultiDraweeHolder<GenericDraweeHierarchy> mActionsHolder =
      new MultiDraweeHolder<>();

  private IconControllerListener mLogoControllerListener;
  private IconControllerListener mNavIconControllerListener;
  private IconControllerListener mOverflowIconControllerListener;

  private int foregroundColor;

  /**
   * Attaches specific icon width & height to a BaseControllerListener which will be used to
   * create the Drawable
   */
  public abstract class IconControllerListener extends BaseControllerListener<ImageInfo> {

    private final DraweeHolder mHolder;

    private IconImageInfo mIconImageInfo;

    public IconControllerListener(DraweeHolder holder) {
      mHolder = holder;
    }

    public void setIconImageInfo(IconImageInfo iconImageInfo) {
      mIconImageInfo = iconImageInfo;
    }

    @Override
    public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
      super.onFinalImageSet(id, imageInfo, animatable);

      final ImageInfo info = mIconImageInfo != null ? mIconImageInfo : imageInfo;
      setDrawable(new DrawableWithIntrinsicSize(mHolder.getTopLevelDrawable(), info));
    }

    protected abstract void setDrawable(Drawable d);

  }

  public class ActionIconControllerListener extends IconControllerListener {
    private final MenuItem mItem;

    ActionIconControllerListener(MenuItem item, DraweeHolder holder) {
      super(holder);
      mItem = item;
    }

    @Override
    protected void setDrawable(Drawable d) {
      mItem.setIcon(d);
    }
  }

  /**
   * Simple implementation of ImageInfo, only providing width & height
   */
  private static class IconImageInfo implements ImageInfo {

    private int mWidth;
    private int mHeight;

    public IconImageInfo(int width, int height) {
      mWidth = width;
      mHeight = height;
    }

    @Override
    public int getWidth() {
      return mWidth;
    }

    @Override
    public int getHeight() {
      return mHeight;
    }

    @Override
    public QualityInfo getQualityInfo() {
      return null;
    }

  }


  public ReactToolbar(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
    mLogoHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    mNavIconHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    mOverflowIconHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    init(context);
  }

  public ReactToolbar(Context context) {
    super(context);
    mLogoHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    mNavIconHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    mOverflowIconHolder = DraweeHolder.create(createDraweeHierarchy(), context);
    init(context);
  }

  private void init(Context context) {


    mLogoControllerListener = new IconControllerListener(mLogoHolder) {
      @Override
      protected void setDrawable(Drawable d) {
        setLogo(d);
      }
    };

    mNavIconControllerListener = new IconControllerListener(mNavIconHolder) {
      @Override
      protected void setDrawable(Drawable d) {
        setNavigationIcon(d);
      }
    };

    mOverflowIconControllerListener = new IconControllerListener(mOverflowIconHolder) {
      @Override
      protected void setDrawable(Drawable d) {
        setOverflowIcon(d);
      }
    };

  }

//  private final Runnable mLayoutRunnable = new Runnable() {
//    @Override
//    public void run() {
//      measure(
//          MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.EXACTLY),
//          MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.EXACTLY));
//      layout(getLeft(), getTop(), getRight(), getBottom());
//    }
//  };
//
//  @Override
//  public void requestLayout() {
//    super.requestLayout();
//
//    // The toolbar relies on a measure + layout pass happening after it calls requestLayout().
//    // Without this, certain calls (e.g. setLogo) only take effect after a second invalidation.
//    post(mLayoutRunnable);
//  }

  @Override
  public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    detachDraweeHolders();
  }

  @Override
  public void onStartTemporaryDetach() {
    super.onStartTemporaryDetach();
    detachDraweeHolders();
  }

  @Override
  public void onAttachedToWindow() {
    super.onAttachedToWindow();
    attachDraweeHolders();
  }

  @Override
  public void onFinishTemporaryDetach() {
    super.onFinishTemporaryDetach();
    attachDraweeHolders();
  }

  private void detachDraweeHolders() {
    mLogoHolder.onDetach();
    mNavIconHolder.onDetach();
    mOverflowIconHolder.onDetach();
    mActionsHolder.onDetach();
  }

  private void attachDraweeHolders() {
    mLogoHolder.onAttach();
    mNavIconHolder.onAttach();
    mOverflowIconHolder.onAttach();
    mActionsHolder.onAttach();
  }

  /* package */ void setLogoSource(ReadableMap source) {
    setIconSource(source, mLogoControllerListener, mLogoHolder);
  }

  /* package */ void setNavIconSource(ReadableMap source) {
    setIconSource(source, mNavIconControllerListener, mNavIconHolder);
  }

  /* package */ void setOverflowIconSource(ReadableMap source) {
    setIconSource(source, mOverflowIconControllerListener, mOverflowIconHolder);
  }

  /* package */ void setRightButtons(Menu menu, ReadableArray buttons, final ReactInterface component) {
    Log.d(TAG, "");
    mActionsHolder.clear();
    for (int i = 0; i < buttons.size(); i++) {
      ReadableMap button = buttons.getMap(i);

      MenuItem item = menu.add(Menu.NONE, Menu.NONE, i, button.getString("title"));

      if (button.hasKey("image")) {
        setMenuItemIcon(item, button.getMap("image"));
      }
      item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      final Object data = i;
      item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          component.emitEvent("onRightPress", data);
          return false;
        }
      });
    }
  }

  private void setMenuItemIcon(final MenuItem item, ReadableMap iconSource) {

    DraweeHolder<GenericDraweeHierarchy> holder =
        DraweeHolder.create(createDraweeHierarchy(), getContext());
    ActionIconControllerListener controllerListener = new ActionIconControllerListener(item, holder);
    controllerListener.setIconImageInfo(getIconImageInfo(iconSource));

    setIconSource(iconSource, controllerListener, holder);

    mActionsHolder.add(holder);

  }

  /**
   * Sets an icon for a specific icon source. If the uri indicates an icon
   * to be somewhere remote (http/https) or on the local filesystem, it uses fresco to load it.
   * Otherwise it loads the Drawable from the Resources and directly returns it via a callback
   */
  private void setIconSource(ReadableMap source, IconControllerListener controllerListener, DraweeHolder holder) {

    String uri = source != null ? source.getString(PROP_ICON_URI) : null;

    if (uri == null) {
      controllerListener.setIconImageInfo(null);
      controllerListener.setDrawable(null);
    } else if (uri.startsWith("http://") || uri.startsWith("https://") || uri.startsWith("file://")) {
      controllerListener.setIconImageInfo(getIconImageInfo(source));
      DraweeController controller = Fresco.newDraweeControllerBuilder()
          .setUri(Uri.parse(uri))
          .setControllerListener(controllerListener)
          .setOldController(holder.getController())
          .build();
      holder.setController(controller);
      holder.getTopLevelDrawable().setVisible(true, true);
    } else {
      controllerListener.setDrawable(getDrawableByName(uri));
    }

  }

  private GenericDraweeHierarchy createDraweeHierarchy() {
    return new GenericDraweeHierarchyBuilder(getResources())
        .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
        .setFadeDuration(0)
        .build();
  }

  private int getDrawableResourceByName(String name) {
    return getResources().getIdentifier(
        name,
        "drawable",
        getContext().getPackageName());
  }

  private Drawable getDrawableByName(String name) {
    int drawableResId = getDrawableResourceByName(name);
    if (drawableResId != 0) {
      return getResources().getDrawable(getDrawableResourceByName(name));
    } else {
      return null;
    }
  }

  private IconImageInfo getIconImageInfo(ReadableMap source) {
    if (source.hasKey(PROP_ICON_WIDTH) && source.hasKey(PROP_ICON_HEIGHT)) {
      final int width = Math.round(PixelUtil.toPixelFromDIP(source.getInt(PROP_ICON_WIDTH)));
      final int height = Math.round(PixelUtil.toPixelFromDIP(source.getInt(PROP_ICON_HEIGHT)));
      return new IconImageInfo(width, height);
    } else {
      return null;
    }
  }

  public boolean onCreateOptionsMenu(int menuRes, Menu menu, MenuInflater inflater) {
    menu.clear();

    if (menuRes != 0) {
      inflater.inflate(menuRes, menu);
//      refreshForegroundColor();
    }

    return true;
  }

  private void refreshForegroundColor() {
    setForegroundColor(foregroundColor);
  }

  public void setForegroundColor(int color) {
    if (color == 0) {
      return;
    }
    foregroundColor = color;
//    setTitleTextColor(color);
//    setSubtitleTextColor(color);
  }

}
