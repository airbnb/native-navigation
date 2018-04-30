package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

final class ActivityUtils {

    private ActivityUtils() {
    }

    static boolean hasActivityStopped(Activity activity) {
        return activity.getWindow() == null || activity.isFinishing();
    }

    private static int getAndroidDimension(Context context, String resourceIdName) {
        Resources resources = context.getResources();
        try {
            int id = resources.getIdentifier(resourceIdName, "dimen", "android");
            return id > 0 ? resources.getDimensionPixelSize(id) : 0;
        } catch (Resources.NotFoundException exception) {
            return 0;
        }
    }

    private static int getStatusBarHeight(Activity activity) {
        Rect rectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        if (rectangle.top > 0) {
            return rectangle.top;
        }
        return getAndroidDimension(activity, "status_bar_height");
    }

    /**
     * Returns the height of the standard status bar and action bar height. Should be called AFTER the
     * initial view layout pass (e.g. wrapped in a post() call).
     */
    static int getStatusBarActionBarHeight(AppCompatActivity activity) {
        return getStatusBarHeight(activity) + activity.getSupportActionBar().getHeight();
    }

    static boolean hasTranslucentStatusBar(Window window) {
        if (AndroidVersion.isAtLeastKitKat()) {
            int flags = window.getAttributes().flags;
            return (flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS) ==
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }
        return false;
    }

    static int getNavBarHeight(Context context) {
        if (ActivityUtils.isPortraitMode(context)) {
            return getAndroidDimension(context, "navigation_bar_height");
        }
        return getAndroidDimension(context, "navigation_bar_height_landscape");
    }

    private static boolean isPortraitMode(Context context) {
        Point point = ViewUtils.getScreenSize(context);
        return point.x < point.y;
    }

    static boolean isLandscapeMode(Context context) {
        return !isPortraitMode(context);
    }
}

