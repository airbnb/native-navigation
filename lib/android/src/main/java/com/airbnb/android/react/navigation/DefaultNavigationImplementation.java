package com.airbnb.android.react.navigation;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.FrameLayout.LayoutParams;

import com.airbnb.android.R;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;

public class DefaultNavigationImplementation implements NavigationImplementation {

    private static final String TAG = "DefaultImplementation";

    class Defaults {

        int foregroundColor;

        int screenColor;

        int backgroundColor;

        int statusBarColor;

        boolean statusBarTranslucent;

        float elevation;

        float alpha;

        Drawable overflowIconSource;

        boolean displayHomeAsUp;

        boolean homeButtonEnabled;

        boolean showHome;

        boolean showTitle;

        boolean showCustom;

        boolean useLogo;

        boolean useShowHideAnimation;

        boolean hideOnScroll;

        int hideOffset;

        int textAlignment;
    }

    private final Defaults defaults;

    public DefaultNavigationImplementation(Defaults defaults) {
        this.defaults = defaults;
    }

    public DefaultNavigationImplementation() {
        defaults = new Defaults();
        defaults.foregroundColor = Color.BLACK;
        defaults.screenColor = Color.WHITE;
        defaults.statusBarColor = Color.TRANSPARENT;
        defaults.statusBarTranslucent = false;
        defaults.elevation = 4.0f;
        defaults.alpha = 1.0f;
        defaults.overflowIconSource = null;
        defaults.displayHomeAsUp = true;
        defaults.homeButtonEnabled = true;
        defaults.showHome = true;
        defaults.showTitle = false;
        defaults.showCustom = false;
        defaults.useLogo = false;
        defaults.useShowHideAnimation = false;
        defaults.hideOnScroll = false;
        defaults.hideOffset = 0;
        defaults.textAlignment = View.TEXT_DIRECTION_LTR; // Do we want this or view_start?
    }

    private static int TextAlignmentFromString(String s) {
        switch (s) {
            case "center":
                return View.TEXT_ALIGNMENT_CENTER;
            case "right":
                return View.TEXT_ALIGNMENT_VIEW_END;
            case "left":
            default:
                return View.TEXT_ALIGNMENT_VIEW_START;
        }
    }

    public float getBarHeight(
            ReactInterface component,
            ReactToolbar toolbar,
            ActionBar actionBar,
            ReadableMap config,
            boolean firstCall
    ) {

        Activity activity = component.getActivity();
        TypedValue typedValue = new TypedValue();

        int attributeResourceId = android.R.attr.actionBarSize;
        if (activity instanceof AppCompatActivity) {
            attributeResourceId = R.attr.actionBarSize;
        }

        if (activity.getTheme().resolveAttribute(attributeResourceId, typedValue, true)) {
            float px = TypedValue.complexToDimension(typedValue.data, activity.getResources().getDisplayMetrics());
            float pixelDensity = Resources.getSystem().getDisplayMetrics().density;
            return px / pixelDensity;
        }
        // if we've made it here, we need to guess...
        return activity.getResources().getDimensionPixelSize(R.dimen.abc_action_bar_default_height_material);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void reconcileStatusBarStyleOnM(
            Activity activity,
            ReadableMap prev,
            ReadableMap next,
            boolean firstCall
    ) {
        if (firstCall || stringHasChanged("statusBarStyle", prev, next)) {
            View decorView = activity.getWindow().getDecorView();
            if (next.hasKey("statusBarStyle")) {
                String style = next.getString("statusBarStyle");
                decorView.setSystemUiVisibility(
                        style.equals("default") ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0);
            } else {
                decorView.setSystemUiVisibility(0);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void reconcileStatusBarStyleOnLollipop(
            final Activity activity,
            ReadableMap prev,
            ReadableMap next,
            boolean firstCall
    ) {
        if (firstCall || numberHasChanged("statusBarColor", prev, next)) {
            boolean animated = false;
            if (next.hasKey("statusBarAnimation")) {
                animated = !("none".equals(next.getString("statusBarAnimation")));
            }

            if (next.hasKey("statusBarColor")) {
                Integer color = next.getInt("statusBarColor");

                if (animated) {
                    int curColor = activity.getWindow().getStatusBarColor();
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(
                            new ArgbEvaluator(), curColor, color);

                    colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animator) {
                            activity.getWindow().setStatusBarColor((Integer) animator.getAnimatedValue());
                        }
                    });
                    colorAnimation
                            .setDuration(300)
                            .setStartDelay(0);
                    colorAnimation.start();
                } else {
                    activity.getWindow().setStatusBarColor(color);
                }
            }
        }

        if (firstCall || boolHasChanged("statusBarTranslucent", prev, next)) {
            boolean translucent = defaults.statusBarTranslucent;
            if (next.hasKey("statusBarTranslucent")) {
                translucent = next.getBoolean("statusBarTranslucent");
            }
            View decorView = activity.getWindow().getDecorView();
            // If the status bar is translucent hook into the window insets calculations
            // and consume all the top insets so no padding will be added under the status bar.
            if (translucent) {
                decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                        WindowInsets defaultInsets = v.onApplyWindowInsets(insets);
                        return defaultInsets.replaceSystemWindowInsets(
                                defaultInsets.getSystemWindowInsetLeft(),
                                0,
                                defaultInsets.getSystemWindowInsetRight(),
                                defaultInsets.getSystemWindowInsetBottom());
                    }
                });
                ViewCompat.requestApplyInsets(decorView);
            } else {
//        decorView.setOnApplyWindowInsetsListener(null);
            }

//      ViewCompat.requestApplyInsets(decorView);
        }
    }

    private void reconcileStatusBarStyle(
            Activity activity,
            ReadableMap prev,
            ReadableMap next,
            boolean firstCall
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            reconcileStatusBarStyleOnM(
                    activity,
                    prev,
                    next,
                    firstCall
            );
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            reconcileStatusBarStyleOnLollipop(
                    activity,
                    prev,
                    next,
                    firstCall
            );
        }

        if (firstCall || boolHasChanged("statusBarHidden", prev, next)) {
            boolean hidden = false;
            if (next.hasKey("statusBarHidden")) {
                hidden = next.getBoolean("statusBarHidden");
            }
            if (hidden) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            } else {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    private static int getStatusBarHeight(ReactInterface component) {
        int resourceId = component.getActivity().getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            return component.getActivity().getResources().getDimensionPixelSize(resourceId);
        }

        return 0;
    }

    // NOTE(lmr):
    // The problem we have now is that we don't know when a "default" is different
    // than the system default, so those properties start off out of sync...
    public void reconcileNavigationProperties(
            ReactInterface component,
            ReactToolbar toolbar,
            ActionBar bar,
            ReadableMap prev,
            ReadableMap next,
            boolean firstCall
    ) {
        Log.d(TAG, "reconcileNavigationProperties");

        Integer foregroundColor = defaults.foregroundColor;

        if (next.hasKey("foregroundColor")) {
            foregroundColor = next.getInt("foregroundColor");
        }

        if (stringHasChanged("title", prev, next) || stringHasChanged("titleFontName", prev, next)) {
            if (next.hasKey("title")) {
                String title = next.getString("title");

                if (next.hasKey("titleFontName")) {
                    String titleFontName = next.getString("titleFontName");
                    SpannableString titleWithFont = new SpannableString(title);
                    titleWithFont.setSpan(new TypefaceSpan(component.getActivity(), titleFontName, 0), 0, titleWithFont.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    toolbar.setTitle(titleWithFont);
                } else {
                    toolbar.setTitle(title);
                }
            } else {
                toolbar.setTitle(null);
            }
        }

        if (firstCall || numberHasChanged("titleColor", prev, next)) {
            final int titleColor = next.hasKey("titleColor") ? next.getInt("titleColor") : foregroundColor;

            toolbar.setTitleTextColor(titleColor);
        }

        if (firstCall || numberHasChanged("foregroundColor", prev, next)) {
            toolbar.setTitleTextColor(foregroundColor);

            Drawable navigationIcon = toolbar.getNavigationIcon();
            if (navigationIcon != null)
                navigationIcon.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP);

            Drawable menuButton = toolbar.getOverflowIcon();
            if (menuButton != null)
                menuButton.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_ATOP);
        }

        if (stringHasChanged("subtitle", prev, next)) {
            if (next.hasKey("subtitle")) {
                String subtitle = next.getString("subtitle");
                toolbar.setSubtitle(subtitle);
            } else {
                toolbar.setSubtitle(null);
            }
        }

        if (firstCall || numberHasChanged("subtitleColor", prev, next)) {
            if (next.hasKey("subtitleColor")) {
                Integer subtitleColor = next.getInt("subtitleColor");
                toolbar.setSubtitleTextColor(subtitleColor);
            } else {
                toolbar.setSubtitleTextColor(foregroundColor);
            }
        }

        if (stringHasChanged("windowTitle", prev, next)) {
            if (next.hasKey("windowTitle")) {
                String windowTitle = next.getString("windowTitle");
                bar.setWindowTitle(windowTitle);
            } else {
                bar.setWindowTitle(null);
            }
        }

        if (firstCall || numberHasChanged("screenColor", prev, next)) {
            if (next.hasKey("screenColor")) {
                // this is the screen background color
                Integer screenColor = next.getInt("screenColor");
                component.getReactRootView().setBackgroundColor(screenColor);
            } else {
                component.getReactRootView().setBackgroundColor(defaults.screenColor);
            }
        }

        if (firstCall || numberHasChanged("backgroundColor", prev, next)) {
            if (next.hasKey("backgroundColor")) {
                final Integer backgroundColor = next.getInt("backgroundColor");
                toolbar.setBackgroundColor(backgroundColor);

                final boolean isToolbarTransparent = Color.alpha(backgroundColor) == 0;

                if (isToolbarTransparent) {
                    LayoutParams reactViewLayout = (LayoutParams) component.getReactRootView().getLayoutParams();
                    reactViewLayout.setMargins(0, 0, 0, 0);
                    component.getReactRootView().setLayoutParams(reactViewLayout);

                    final boolean isStatusBarTranslucent = next.hasKey("statusBarTranslucent") && next.getBoolean("statusBarTranslucent");

                    if (isStatusBarTranslucent) {
                        LayoutParams toolbarLayout = (LayoutParams) toolbar.getLayoutParams();

                        toolbarLayout.setMargins(0, getStatusBarHeight(component), 0, 0);
                        toolbar.setLayoutParams(toolbarLayout);
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (firstCall || numberHasChanged("elevation", prev, next)) {
                if (next.hasKey("elevation")) {
                    Double elevation = next.getDouble("elevation");
                    toolbar.setElevation(elevation.floatValue());
                } else {
                    toolbar.setElevation(defaults.elevation);
                }
            }
        }

        if (firstCall || numberHasChanged("alpha", prev, next)) {
            if (next.hasKey("alpha")) {
                Double alpha = next.getDouble("alpha");
                toolbar.setAlpha(alpha.floatValue());
            } else {
                toolbar.setAlpha(defaults.alpha);
            }
        }

        if (firstCall || mapHasChanged("navIcon", prev, next)) {
            if (next.hasKey("navIcon")) {
                toolbar.setNavIconSource(next.getMap("navIcon"));
            } else {
//        toolbar.setNavigationIcon(android.R.drawable.ic_launchaer/*R.drawable.abc_ic_ab_back_mtrl_am_alpha*/);
            }
        }

        if (firstCall || mapHasChanged("logo", prev, next)) {
            if (next.hasKey("logo")) {
                toolbar.setLogoSource(next.getMap("logo"));
            } else {

            }
        }

        if (firstCall || mapHasChanged("overflowIcon", prev, next)) {
            if (next.hasKey("overflowIcon")) {
                toolbar.setOverflowIconSource(next.getMap("overflowIcon"));
            } else {

            }
        }

        if (firstCall || numberHasChanged("contentInsetStartWithNavigation", prev, next)) {
            if (next.hasKey("contentInsetStartWithNavigation")) {
                toolbar.setContentInsetStartWithNavigation(next.getInt("contentInsetStartWithNavigation"));
            } else {

            }
        }

        if (firstCall || boolHasChanged("displayHomeAsUp", prev, next)) {
//      if (next.hasKey("displayHomeAsUp")) {
//        boolean displayHomeAsUp = next.getBoolean("displayHomeAsUp");
//        if (displayHomeAsUp) {
//          toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
//        } else {
//          toolbar.setNavigationIcon(null);
//        }
        } else {
//        bar.setDisplayHomeAsUpEnabled(defaults.displayHomeAsUp);
//        if (defaults.displayHomeAsUp) {
//          toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material);
//        } else {
//          toolbar.setNavigationIcon(null);
//        }
//      }
        }

        if (firstCall || boolHasChanged("hidden", prev, next)) {
            if (next.hasKey("hidden")) {
                boolean hidden = next.getBoolean("hidden");
                if (hidden) {
                    toolbar.setVisibility(View.GONE);
                } else {
                    toolbar.setVisibility(View.VISIBLE);
                }
            }
        }

        if (bar != null) {

            // ActionBar only properties...

            if (firstCall || boolHasChanged("hidden", prev, next)) {
                boolean hidden = false;

                if (next.hasKey("hidden")) {
                    hidden = next.getBoolean("hidden");
                }

                if (hidden && bar.isShowing()) {
                    bar.hide();
                } else if (!hidden && !bar.isShowing()) {
                    bar.show();
                }
            }

            if (firstCall || boolHasChanged("displayHomeAsUp", prev, next)) {
                if (next.hasKey("displayHomeAsUp")) {
                    boolean displayHomeAsUp = next.getBoolean("displayHomeAsUp");
                    bar.setDisplayHomeAsUpEnabled(displayHomeAsUp);
                } else {
                    bar.setDisplayHomeAsUpEnabled(defaults.displayHomeAsUp);
                }
            }

            if (firstCall || boolHasChanged("homeButtonEnabled", prev, next)) {
                if (next.hasKey("homeButtonEnabled")) {
                    boolean homeButtonEnabled = next.getBoolean("homeButtonEnabled");
                    bar.setHomeButtonEnabled(homeButtonEnabled);
                } else {

                }
            }

            if (firstCall || boolHasChanged("showHome", prev, next)) {
                if (next.hasKey("showHome")) {
                    boolean showHome = next.getBoolean("showHome");
                    bar.setDisplayShowHomeEnabled(showHome);
                } else {
                    bar.setDisplayShowHomeEnabled(defaults.showHome);
                }
            }

            if (firstCall || boolHasChanged("showTitle", prev, next)) {
                if (next.hasKey("showTitle")) {
                    boolean showTitle = next.getBoolean("showTitle");
                    bar.setDisplayShowTitleEnabled(showTitle);
                } else {
                    bar.setDisplayShowTitleEnabled(defaults.showTitle);
                }
            }

            if (firstCall || boolHasChanged("showCustom", prev, next)) {
                if (next.hasKey("showCustom")) {
                    boolean showCustom = next.getBoolean("showCustom");
                    bar.setDisplayShowCustomEnabled(showCustom);
                } else {
                    bar.setDisplayShowCustomEnabled(defaults.showCustom);
                }
            }

            if (firstCall || boolHasChanged("useLogo", prev, next)) {
                if (next.hasKey("useLogo")) {
                    boolean useLogo = next.getBoolean("useLogo");
                    bar.setDisplayUseLogoEnabled(useLogo);
                } else {
                    bar.setDisplayUseLogoEnabled(defaults.useLogo);
                }
            }

            if (firstCall || boolHasChanged("useShowHideAnimation", prev, next)) {
                if (next.hasKey("useShowHideAnimation")) {
                    boolean useShowHideAnimation = next.getBoolean("useShowHideAnimation");
                    bar.setShowHideAnimationEnabled(useShowHideAnimation);
                } else {
                    bar.setShowHideAnimationEnabled(defaults.useShowHideAnimation);
                }
            }

            if (firstCall || boolHasChanged("hideOnScroll", prev, next)) {
                if (next.hasKey("hideOnScroll")) {
                    boolean hideOnScroll = next.getBoolean("hideOnScroll");
                    bar.setHideOnContentScrollEnabled(hideOnScroll);
                } else {
                    bar.setHideOnContentScrollEnabled(defaults.hideOnScroll);
                }
            }

            if (firstCall || numberHasChanged("hideOffset", prev, next)) {
                if (next.hasKey("hideOffset")) {
                    int hideOffset = next.getInt("hideOffset");
                    bar.setHideOffset(hideOffset);
                } else {
                    bar.setHideOffset(defaults.hideOffset);
                }
            }
        }

        reconcileStatusBarStyle(
                component.getActivity(),
                prev,
                next,
                firstCall
        );

        // TODO(lmr): this doesnt appear to work like i think it should.
//    if (firstCall || stringHasChanged("textAlign", prev, next)) {
//      if (next.hasKey("textAlign")) {
//        String textAlign = next.getString("textAlign");
//        int alignment = TextAlignmentFromString(textAlign);
//        toolbar.setTextAlignment(alignment);
//      } else {
//        toolbar.setTextAlignment(defaults.textAlignment);
//      }
//    }

//    bar.setHomeAsUpIndicator(drawable); // TODO
//    toolbar.setForeground(drawable);
//    toolbar.setCameraDistance(0.1);
//    toolbar.setBackgroundTintMode(PorterDuff.Mode.CLEAR);
//    toolbar.setForegroundTintMode(PorterDuff.Mode.DARKEN);

        // we are just going to *always* invalidate this menu when we
        // reconcile, and handle everything in `prepareOptionsMenu`.
        component.getActivity().supportInvalidateOptionsMenu();
    }

    public void prepareOptionsMenu(
            final ReactInterface component,
            ReactToolbar toolbar,
            ActionBar bar,
            Menu menu,
            ReadableMap prev,
            ReadableMap next
    ) {
        Log.d(TAG, "prepareOptionsMenu");

        if (arrayHasChanged("rightButtons", prev, next)) {
            if (next.hasKey("rightButtons")) {
                ReadableArray buttons = next.getArray("rightButtons");
                menu.clear();
                toolbar.setRightButtons(menu, buttons, component);
            } else {
                menu.clear();
            }
        }
        if (stringHasChanged("rightTitle", prev, next)) {
            menu.clear();
            if (next.hasKey("rightTitle")) {
                String rightTitle = next.getString("rightTitle");
                MenuItem item = menu.add(rightTitle);
                item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        component.emitEvent("onRightPress", null);
                        return true;
                    }
                });
            }
        }
        if (mapHasChanged("rightImage", prev, next)) {
            // TODO(lmr):
        }
    }

    public boolean onOptionsItemSelected(
            final ReactInterface component,
            ReactToolbar toolbar,
            ActionBar bar,
            MenuItem item,
            ReadableMap properties
    ) {
        // TODO(lmr): we need to make this possible somehow
//    if (item.getItemId() == android.R.id.home) {
//      component.emitEvent(ON_LEFT_PRESS, null);
//      if (reactNavigationCoordinator.getDismissCloseBehavior(component)) {
//        component.dismiss();
//        return true; // consume the event
//      } else {
//        return super.onOptionsItemSelected(item);
//      }
//    }
        if (item.getItemId() == android.R.id.home) {
//      component.dismiss();
            // set result here?
            // do we ever want to dismiss? do we want "closeBehavior"?
            component.getActivity().finish();
            return false;
        }

//    component.emitEvent("onRightPress", null);
        return true;
    }

    public void makeTabItem(
            ReactBottomNavigation bottomNavigation,
            Menu menu,
            int index,
            Integer itemId,
            ReadableMap config
    ) {

        Log.d(TAG, "makeTabItem");

        MenuItem item = menu.add(
                Menu.NONE,
                itemId,
                Menu.NONE,
                config.getString("title")
        );

        if (config.hasKey("image")) {
            bottomNavigation.setMenuItemIcon(item, config.getMap("image"));
        } else {
            // TODO(lmr): this probably isn't the best default.
            item.setIcon(android.R.drawable.btn_radio);
        }

        if (config.hasKey("enabled")) {
            boolean enabled = config.getBoolean("enabled");
            item.setEnabled(enabled);
        }

        // not sure if we want/need to set anything on the itemview itself. hacky.
//    BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigation.getChildAt(0);
//    BottomNavigationItemView itemView = (BottomNavigationItemView)menuView.getChildAt(index);
    }

    private static ColorStateList colorStatesFromPrefix(String prefix, ReadableMap props, int defaultColor) {

        String active = String.format("%sActiveColor", prefix);
        String selected = String.format("%sSelectedColor", prefix);
        String normal = String.format("%sColor", prefix);
        String disabled = String.format("%sDisabledColor", prefix);

        int normalColor = props.hasKey(normal) ? props.getInt(normal) : defaultColor;
        int selectedColor = props.hasKey(selected) ? props.getInt(selected) : normalColor;
        int activeColor = props.hasKey(active) ? props.getInt(active) : selectedColor;
        int disabledColor = props.hasKey(disabled) ? props.getInt(disabled) : normalColor;


        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_pressed},
                        new int[]{android.R.attr.state_checked},
                        new int[]{android.R.attr.state_enabled},
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{} // this should be empty to make default color as we want
                },
                new int[]{
                        activeColor,
                        selectedColor,
                        normalColor,
                        disabledColor,
                        normalColor
                }
        );
    }

    public void reconcileTabBarProperties(
            ReactBottomNavigation bottomNavigation,
            Menu menu,
            ReadableMap prev,
            ReadableMap next
    ) {

        // TODO(lmr):
//    bottomNavigation.setForegroundTintMode(mode);
//    bottomNavigation.setBackgroundTintMode(mode);
//    bottomNavigation.setBackgroundTintMode(PorterDuff.Mode.DARKEN);

        if (boolHasChanged("enabled", prev, next)) {
            if (next.hasKey("enabled")) {
                bottomNavigation.setEnabled(next.getBoolean("enabled"));
            } else {
                bottomNavigation.setEnabled(true);
            }
        }

        if (mapHasChanged("backgroundImage", prev, next)) {
            if (next.hasKey("backgroundImage")) {
                bottomNavigation.setBackgroundSource(next.getMap("backgroundImage"));
            } else {
                // ???
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (numberHasChanged("elevation", prev, next)) {
                if (next.hasKey("elevation")) {
                    bottomNavigation.setElevation((float) next.getDouble("elevation"));
                } else {
                    bottomNavigation.setElevation(defaults.elevation);
                }
            }
        }

        bottomNavigation.setItemIconTintList(colorStatesFromPrefix("itemIcon", next, Color.BLACK));
        bottomNavigation.setItemTextColor(colorStatesFromPrefix("itemText", next, Color.BLACK));

        // TODO(lmr): backgroundTintList doesn't seem to have an effect.
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//      bottomNavigation.setBackgroundTintList(colorStatesFromPrefix("background", next, Color.GRAY));
//    } else
        if (numberHasChanged("backgroundColor", prev, next)) {
            if (next.hasKey("backgroundColor")) {
                bottomNavigation.setBackgroundColor(next.getInt("backgroundColor"));
            } else {
                bottomNavigation.setBackgroundColor(Color.GRAY);
            }
        }
    }


    private static boolean shouldBail(String key, ReadableType type, ReadableMap prev, ReadableMap next) {
        boolean inNext = next.hasKey(key);
        boolean inPrev = prev.hasKey(key);

        if (!inNext && !inPrev)
            return true;
        if (inNext && next.getType(key) != type) {
            // we should bail if it's in next and not the expected type
            return true;
        }
        if (inPrev && prev.getType(key) != type) {
            return true;
        }

        return false;
    }

    protected static boolean boolHasChanged(
            String key,
            ReadableMap prev,
            ReadableMap next
    ) {
        if (shouldBail(key, ReadableType.Boolean, prev, next)) {
            return false;
        }

        return next.hasKey(key) != prev.hasKey(key) ||
                next.getBoolean(key) != prev.getBoolean(key);
    }

    protected static boolean stringHasChanged(
            String key,
            ReadableMap prev,
            ReadableMap next
    ) {

        if (shouldBail(key, ReadableType.String, prev, next)) {
            return false;
        }

        return next.hasKey(key) != prev.hasKey(key) ||
                !next.getString(key).equals(prev.getString(key));
    }

    protected static boolean numberHasChanged(
            String key,
            ReadableMap prev,
            ReadableMap next
    ) {
        if (shouldBail(key, ReadableType.Number, prev, next)) {
            return false;
        }

        return next.hasKey(key) != prev.hasKey(key) ||
                next.getDouble(key) != prev.getDouble(key);
    }

    protected static boolean mapHasChanged(
            String key,
            ReadableMap prev,
            ReadableMap next
    ) {

        if (shouldBail(key, ReadableType.Map, prev, next)) {
            return false;
        }

        return next.hasKey(key) != prev.hasKey(key) ||
                !mapEqual(next.getMap(key), prev.getMap(key));
    }

    protected static boolean arrayHasChanged(
            String key,
            ReadableMap prev,
            ReadableMap next
    ) {

        if (shouldBail(key, ReadableType.Array, prev, next)) {
            return false;
        }

        return next.hasKey(key) != prev.hasKey(key) ||
                !arrayEqual(next.getArray(key), prev.getArray(key));
    }

    private static boolean mapEqual(
            ReadableMap a,
            ReadableMap b
    ) {
        ReadableMapKeySetIterator iterator = b.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            if (!a.hasKey(key))
                return false;
            ReadableType type = b.getType(key);
            if (type != a.getType(key))
                return false;
            switch (type) {
                case Null:
                    break;
                case Boolean:
                    if (a.getBoolean(key) != b.getBoolean(key))
                        return false;
                    break;
                case Number:
                    if (a.getDouble(key) != b.getDouble(key))
                        return false;
                    break;
                case String:
                    if (!a.getString(key).equals(b.getString(key)))
                        return false;
                    break;
                case Map:
                    if (!mapEqual(a.getMap(key), b.getMap(key)))
                        return false;
                    break;
                case Array:
                    if (!arrayEqual(a.getArray(key), b.getArray(key)))
                        return false;
                    break;
                default:
                    Log.e(TAG, "Could not convert object with key: " + key + ".");
            }
        }
        return true;
    }

    private static boolean arrayEqual(
            ReadableArray a,
            ReadableArray b
    ) {
        if (b.size() != a.size())
            return false;

        for (int i = 0; i < a.size(); i++) {
            ReadableType type = a.getType(i);
            if (type != b.getType(i))
                return false;
            switch (type) {
                case Null:
                    break;
                case Boolean:
                    if (b.getBoolean(i) != a.getBoolean(i))
                        return false;
                    break;
                case Number:
                    if (b.getDouble(i) != a.getDouble(i))
                        return false;
                    break;
                case String:
                    if (!b.getString(i).equals(a.getString(i)))
                        return false;
                    break;
                case Map:
                    if (!mapEqual(a.getMap(i), b.getMap(i)))
                        return false;
                    break;
                case Array:
                    if (!arrayEqual(a.getArray(i), b.getArray(i)))
                        return false;
                    break;
                default:
                    Log.e(TAG, "Could not compare object with index: " + i + ".");
            }
        }
        return true;
    }
}
