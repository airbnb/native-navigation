package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.BatteryManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.airbnb.android.R;

import java.util.List;
import java.util.Stack;

public final class ViewUtils {

    private static Rect screenRect = null;

    /**
     * Gets the screen bounds of a view. This should be more accurate than
     * getScreenLocationMinusStatusBar since it doesn't need to estimate the status bar height. Try to
     * use this method if possible.
     *
     * @return Rect the view Rect(top, left, width, height) bounds on screen
     */
    public static Rect getViewBounds(View view) {
        Rect loc = new Rect();
        int[] coords = new int[2];
        view.getLocationOnScreen(coords);
        loc.set(coords[0], coords[1], coords[0] + view.getWidth(), coords[1] + view.getHeight());
        return loc;
    }

    public static Rect getViewRect(View view) {
        return new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    public static Rect getViewRectWithMargins(View view) {
        return new Rect(
                view.getLeft() - getLeftMargin(view),
                view.getTop() - getTopMargin(view),
                view.getRight() + getRightMargin(view),
                view.getBottom() + getBottomMargin(view));
    }

    /**
     * Helper to create a layout inflater and inflate a view into a parent layout.
     */
    public static View inflate(ViewGroup parent, /*@LayoutRes*/ int layout, boolean attach) {
        return LayoutInflater.from(parent.getContext()).inflate(layout, parent, attach);
    }

    public static void setVisibleIf(/*@Nullable */View view, boolean visibleIfTrue) {
        if (view == null) {
            return;
        }
        view.setVisibility(visibleIfTrue ? View.VISIBLE : View.GONE);
    }

    public static void setGoneIf(/*@Nullable*/ View view, boolean goneIfTrue) {
        if (view == null) {
            return;
        }
        view.setVisibility(goneIfTrue ? View.GONE : View.VISIBLE);
    }

    public static void setInvisibleIf(/*@Nullable*/ View view, boolean invisibleIfTrue) {
        if (view == null) {
            return;
        }
        view.setVisibility(invisibleIfTrue ? View.INVISIBLE : View.VISIBLE);
    }

    public static boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public static void bindOptionalTextView(TextView textView, CharSequence text) {
        setVisibleIf(textView, !TextUtils.isEmpty(text));
        textView.setText(text);
    }

    public static void bindOptionalTextView(TextView textView, /*@StringRes*/ int textResId) {
        setVisibleIf(textView, textResId > 0);
        textView.setText(textResId);
    }

    public static void setEnabledIf(View view, boolean enabledIfTrue) {
        view.setEnabled(enabledIfTrue);
    }

    public static Point getScreenSize(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return new Point(metrics.widthPixels, metrics.heightPixels);
    }

    public static int getScreenWidth(Context context) {
        return ViewUtils.getScreenSize(context).x;
    }

    public static float getViewPercentageOnScreen(View view) {
        if (screenRect == null) {
            Point screenSize = getScreenSize(view.getContext());
            screenRect = new Rect(0, 0, screenSize.x, screenSize.y);
        }

        int[] locs = new int[2];
        view.getLocationInWindow(locs);

        return getRectOverlapPercentage(
                new Rect(locs[0], locs[1], locs[0] + view.getWidth(), locs[1] + view.getHeight()),
                screenRect);
    }

    /**
     * Returns the percentage of rect1 that is inside rect2.
     */
    public static float getRectOverlapPercentage(Rect rect1, Rect rect2) {
        float intersectionSurfaceArea =
                Math.max(0, Math.min(rect1.right, rect2.right) - Math.max(rect1.left, rect2.left)) *
                        Math.max(0, Math.min(rect1.bottom, rect2.bottom) - Math.max(rect1.top, rect2.top));
        float surfaceArea1 = rect1.width() * rect1.height();
        return intersectionSurfaceArea / surfaceArea1;
    }

    /*@Nullable*/
    public static View getMostVisibleView(List<View> views) {
        // If there are multiple partial matches, we will map the one that is the most visible.
        View bestMatch = null;
        float bestMatchPercentageOnScreen = 0;
        if (!views.isEmpty()) {
            for (View pm : views) {
                float percentageOnScreen = ViewUtils.getViewPercentageOnScreen(pm);
                if (percentageOnScreen > bestMatchPercentageOnScreen) {
                    bestMatchPercentageOnScreen = percentageOnScreen;
                    bestMatch = pm;
                }
            }
            return bestMatch;
        }
        return null;
    }

    public static int getScreenHeight(Context context) {
        return ViewUtils.getScreenSize(context).y;
    }

    public static int clamp(int number, int min, int max) {
        return Math.max(Math.min(number, max), min);
    }

    public static float clamp(float number, float min, float max) {
        return Math.max(Math.min(number, max), min);
    }

    /**
     * Trim a CharSequence (consisting of characters) in the same matter as {@link String#trim()}
     */
    public static CharSequence trim(CharSequence input) {
        int start = 0;
        int end = input.length() - 1;

        while (start <= end && input.charAt(start) <= ' ') {
            start++;
        }

        while (end >= start && input.charAt(end) <= ' ') {
            end--;
        }

        if (start > end) {
            return "";
        }

        return input.subSequence(start, end + 1);
    }

    /**
     * Simpler version of {@link View#findViewById(int)} which infers the target type.
     */
    @SuppressWarnings({"unchecked", "UnusedDeclaration"}) // Checked by runtime cast. Public API.
    public static <T extends View> T findById(View view, int id) {
        return (T) view.findViewById(id);
    }

    /**
     * Simpler version of {@link Activity#findViewById(int)} which infers the target type.
     */
    @SuppressWarnings({"unchecked", "UnusedDeclaration"}) // Checked by runtime cast. Public API.
    public static <T extends View> T findById(Activity activity, int id) {
        return (T) activity.findViewById(id);
    }

    public static void showSoftKeyboard(Context context, EditText editText) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(editText, 0);
        }
    }

    public static void hideSoftKeyboard(Context context, EditText editText) {
        InputMethodManager imm =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    public static void setDimension(View view, int widthPx, int heightPx) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = widthPx;
        params.height = heightPx;
        view.setLayoutParams(params);
    }

    public static void setPaddingBottom(View view, int px) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), px);
    }

    public static void setPadding(View view, int px) {
        view.setPadding(px, px, px, px);
    }

    public static void setPaddingDimen(View view, /*@DimenRes*/ int dimenRes) {
        int px = view.getResources().getDimensionPixelOffset(dimenRes);
        setPadding(view, px);
    }

    public static void setPaddingLeft(View view, int px) {
        view.setPadding(px, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
    }

    public static void setPaddingRight(View view, int px) {
        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(), px, view.getPaddingBottom());
    }

    public static void setPaddingTop(View view, int px) {
        view.setPadding(view.getPaddingLeft(), px, view.getPaddingRight(), view.getPaddingBottom());
    }

    public static int getLeftMargin(View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).leftMargin;
    }

    public static int getTopMargin(View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).topMargin;
    }

    public static int getRightMargin(View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).rightMargin;
    }

    public static int getBottomMargin(View view) {
        return ((ViewGroup.MarginLayoutParams) view.getLayoutParams()).bottomMargin;
    }

    public static void setPaddingDP(View view, float dp) {
        setPadding(view, dpToPx(view.getContext(), dp));
    }

    /**
     * Returns the measured width + margins.
     */
    public static int getTotalMeasuredWidth(View view) {
        return getLeftMargin(view) + view.getMeasuredWidth() + getRightMargin(view);
    }

    /**
     * Returns the height + margins.
     */
    public static int getTotalHeight(View view) {
        return getTopMargin(view) + view.getHeight() + getBottomMargin(view);
    }

    public static int dpToPx(final Context context, final float dp) {
        // Took from http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }

    /**
     * @return the asset for `selectableItemBackgroundBorderlessResource` from the app theme
     */
    public static int getSelectableItemBackgroundBorderlessResource(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, outValue, true);
        return outValue.resourceId;
    }

    /* This is a slightly modified View/ViewGroup#findNamedViews().
     * findNamedViews() is used by the framework to traverse all children and create a map
     * of all children with transition names. However, that method is @hide. Thanks Google.
     * However, we can actually create a slightly more efficient version where we can start
     * with a specific child, look for a specific transition name, and just return that.
     * This works better in cases where you are only looking for a single view, not an arbitrary
     * number of transition views.
     */
    /*@Nullable*/
    public static View findTransitionView(View view, String transitionName) {
        if (transitionName.equals(ViewCompat.getTransitionName(view))) {
            return view;
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = vg.getChildCount() - 1; i >= 0; i--) {
                View transitionView = findTransitionView(vg.getChildAt(i), transitionName);
                if (transitionView != null) {
                    return transitionView;
                }
            }
        }

        return null;
    }

    public static void findTransitionViews(View view, List<Pair<View, String>> transitionViews) {
        String transitionName = ViewCompat.getTransitionName(view);
        if (!TextUtils.isEmpty(transitionName) && view.getVisibility() == View.VISIBLE && isOnScreen(
                view)) {
            transitionViews.add(Pair.create(view, transitionName));
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
                findTransitionViews(viewGroup.getChildAt(i), transitionViews);
            }
        }
    }

    /**
     * Breadth-first search of a view hierarchy that returns the first element with a matching tag.
     *
     * @param root   - The view to start traversing (it is also checked for a matching tag)
     * @param key    - The tag key
     * @param object - The object to test for
     *
     * @return - The first matching view
     */
    @Nullable
    static ViewGroup findViewGroupWithTag(ViewGroup root, int key, Object object) {
        Stack<ViewGroup> stack = new Stack<>();
        stack.push(root);

        while (!stack.empty()) {
            ViewGroup view = stack.pop();
            Object tag = view.getTag(key);

            if (object.equals(tag)) {
                return view;
            }

            int childCount = view.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View child = view.getChildAt(i);

                if (child instanceof ViewGroup) {
                    stack.push((ViewGroup) child);
                }
            }
        }
        return null;
    }

    public static boolean isOnScreen(View view) {
        View parent = (View) view.getParent();
        return view.getRight() > 0 && view.getLeft() < parent.getWidth() &&
                view.getBottom() > 0 && view.getTop() < parent.getHeight();
    }

    public static int getResource(Context context, /*@AttrRes*/ int attributeRes) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        return outValue.resourceId;
    }

    /**
     * http://stackoverflow.com/questions/15746709/get-battery-level-only-once-using-android-sdk
     */
    /*@FloatRange(from=0f, to=100f)*/
    public static float getBatteryLevel(Context context) {
        Intent batteryIntent =
                context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent == null) {
            return 50.0f;
        }
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Just in case.
        if (level == -1 || scale == -1 || scale == 0f) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    /**
     * https://developer.android.com/training/monitoring-device-state/battery-monitoring.html
     */
    public static int getBatteryState(Context context) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        if (batteryStatus == null) {
            return BatteryManager.BATTERY_STATUS_UNKNOWN;
        }
        return batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
    }

    public static boolean isAtLeastLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isLollipopMr1() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1;
    }

    public static boolean isAtLeastKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
