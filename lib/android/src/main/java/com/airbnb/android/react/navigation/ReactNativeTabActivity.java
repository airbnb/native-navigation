package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.airbnb.android.R;
import com.facebook.react.bridge.ReadableMap;

import java.util.Map;
import java.util.Stack;

public class ReactNativeTabActivity extends ReactAwareActivity
        implements ScreenCoordinatorComponent, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = ReactNativeTabActivity.class.getSimpleName();

    private ViewGroup.OnHierarchyChangeListener reactViewChangeListener = new ViewGroup.OnHierarchyChangeListener() {
        @Override
        public void onChildViewAdded(View parent, View child) {
            Log.d(TAG, "onChildViewAdded");
            if (child instanceof ViewGroup) {
                Log.d(TAG, "onChildViewAdded: adding child listener");
                // onChildViewAdded is a shallow listener, so we want to recursively listen
                // to all children that are ViewGroups as well. For a tab scene, the view
                // hierarchy should not be very deep, so this seems okay to me. We should be
                // careful though.
                ((ViewGroup) child).setOnHierarchyChangeListener(reactViewChangeListener);
            }
            debouncedRefreshTabs();
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            Log.d(TAG, "onChildViewRemoved");
            // TODO(lmr): is there any reason we would need to clean up the onHierarchyChangeListener here?
            debouncedRefreshTabs();
        }
    };

    private TabCoordinator tabCoordinator;

    private ReactBottomNavigation bottomNavigationView;

    private ViewGroup tabConfigContainer;

    private boolean tabViewsIsDirty = false;

    private Map<Integer, TabView> tabViews = new ArrayMap<>();

    private ReadableMap prevTabBarConfig = ConversionUtil.EMPTY_MAP;

    private ReadableMap renderedTabBarConfig = ConversionUtil.EMPTY_MAP;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_2);
        bottomNavigationView = (ReactBottomNavigation) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        tabConfigContainer = (ViewGroup) findViewById(R.id.tab_config_container);
        tabConfigContainer.setOnHierarchyChangeListener(reactViewChangeListener);
        ScreenCoordinatorLayout container = (ScreenCoordinatorLayout) findViewById(R.id.content);
        tabCoordinator = new TabCoordinator(this, container, savedInstanceState);

        ReactNativeFragment tabConfigFragment = ReactNativeFragment.newInstance("TabScreen", null);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.tab_config_container, tabConfigFragment)
                .commitNow();
    }

    @Override
    public ScreenCoordinator getScreenCoordinator() {
        return tabCoordinator.getCurrentScreenCoordinator();
    }

    @Override
    public void onBackPressed() {
        if (!tabCoordinator.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void debouncedRefreshTabs() {
        if (tabViewsIsDirty) {
            return;
        }
        tabViewsIsDirty = true;
        tabConfigContainer.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                tabViewsIsDirty = false;
                tabConfigContainer.getViewTreeObserver().removeOnPreDrawListener(this);
                refreshTabs();
                return true;
            }
        });
    }

    private void refreshTabs() {
        Log.d(TAG, "refreshTabs");
        traverseTabs();
        notifyTabsHaveChanged();
    }

    private void traverseTabs() {
        Stack<ViewGroup> stack = new Stack<>();
        stack.push(tabConfigContainer);

        prevTabBarConfig = renderedTabBarConfig;
        renderedTabBarConfig = ConversionUtil.EMPTY_MAP;
        tabViews = new ArrayMap<>();

        while (!stack.empty()) {
            ViewGroup view = stack.pop();
            int childCount = view.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                View child = view.getChildAt(i);

                if (child instanceof TabView) {
                    tabViews.put(child.getId(), (TabView) child);
                } else if (child instanceof TabBarView) {
                    TabBarView tabBarView = (TabBarView) child;
                    renderedTabBarConfig = ConversionUtil.combine(renderedTabBarConfig, tabBarView.getConfig());
                    stack.push(tabBarView);
                } else if (child instanceof ViewGroup) {
                    stack.push((ViewGroup) child);
                }
            }
        }
    }

    private void notifyTabsHaveChanged() {
        Log.d(TAG, "notifyTabsHaveChanged");
        Menu menu = bottomNavigationView.getMenu();

        getImplementation().reconcileTabBarProperties(
                bottomNavigationView,
                menu,
                prevTabBarConfig,
                renderedTabBarConfig
        );

        menu.clear();
        bottomNavigationView.clearIconHolders();

        int index = 0;
        for (TabView tab : tabViews.values()) {
            getImplementation().makeTabItem(
                    bottomNavigationView,
                    menu,
                    index,
                    tab.getId(),
                    tab.getRenderedConfig()
            );
            index++;
        }

        if (tabViews.size() > 0) {
            TabView view = tabViews.values().iterator().next();
            tabCoordinator.showTab(view.getFragment(), view.getId());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavigationItemSelected");
        TabView tab = tabViews.get(item.getItemId());
        if (tab != null) {
            Log.d(TAG, "found tab");
            Fragment fragment = tab.getFragment();
            tabCoordinator.showTab(fragment, item.getItemId());
        }
        return true;
    }
}
