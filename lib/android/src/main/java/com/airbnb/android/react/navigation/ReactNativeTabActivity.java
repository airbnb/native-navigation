package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.ActionBar;
import android.transition.Transition;
import android.util.Log;
import android.view.*;
import com.airbnb.android.R;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.PermissionListener;

import java.util.*;

import static com.airbnb.android.react.navigation.NavigatorModule.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.navigation.ReactNativeActivity.makeSlideLeftAnimation;

public class ReactNativeTabActivity extends ReactAwareActivity implements ReactInterface, BottomNavigationView.OnNavigationItemSelectedListener {


  public static Intent intent(Context context, String moduleName, Bundle props, boolean isModal) {
    return new Intent(context, ReactNativeTabActivity.class)
        .putExtra(REACT_MODULE_NAME, moduleName)
        .putExtra(REACT_PROPS, props);
  }

  public static Intent intent(Context context, String moduleName, Bundle props) {
    return intent(context, moduleName, props, false);
  }

  public static Intent intent(Context context, String moduleName, boolean isModal) {
    return intent(context, moduleName, null, isModal);
  }

  public static Intent intent(Context context, String moduleName) {
    return intent(context, moduleName, null, false);
  }


  private static final int SHARED_ELEMENT_TARGET_API = Build.VERSION_CODES.LOLLIPOP_MR1;
  /** We just need lollipop (not MR1) for the postponed slide in transition */
  private static final int WAITING_TRANSITION_TARGET_API = Build.VERSION_CODES.LOLLIPOP;

  private static final String ON_ENTER_TRANSITION_COMPLETE = "onEnterTransitionComplete";
  private static final String ON_DISAPPEAR = "onDisappear";
  private static final String ON_APPEAR = "onAppear";
  private static final int RENDER_TIMEOUT_IN_MS = 1700; // TODO(lmr): put this back down when done debugging
  private static final int FAKE_ENTER_TRANSITION_TIME_IN_MS = 500;
  private static final String TAG = "ReactNativeActivity";
  // An incrementing ID to identify each ReactNativeActivity instance (used in `instanceId`)
  private static int UUID = 1;

  private String instanceId;
  private float barHeight;
  private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap previousConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap renderedConfig = ConversionUtil.EMPTY_MAP;
  private ReactInterfaceManager activityManager;
  private final Handler transitionHandler = new Handler();
  private final Handler handler = new Handler();
  private boolean isWaitingForRenderToFinish = false;
  private boolean isSharedElementTransition = false;
  private PermissionListener permissionListener;

  private ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;
  private ReactInstanceManager reactInstanceManager = reactNavigationCoordinator.getReactInstanceManager();

//  ReactToolbar toolbar;
  ReactRootView reactRootView;
//  ViewPager viewPager;
//  TabLayout tabLayout;
//  ViewPagerAdapter adapter;
  ReactBottomNavigation bottomNavigation;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.tab_react_native);

//    toolbar = (ReactToolbar) findViewById(R.id.toolbar);

//    setSupportActionBar(toolbar);
//    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    bottomNavigation = (ReactBottomNavigation) findViewById(R.id.bottom_navigation);

    if (!isSuccessfullyInitialized()) {
      Log.d(TAG, "onCreate: !successfully initialized");
      // TODO(lmr): move to utils
      reactInstanceManager.addReactInstanceEventListener(
          new ReactInstanceManager.ReactInstanceEventListener() {
            @Override
            public void onReactContextInitialized(ReactContext context) {
              Log.d(TAG, "onReactContextInitialized");
              reactInstanceManager.removeReactInstanceEventListener(this);
              handler.post(new Runnable() {
                @Override
                public void run() {
                  ReactNativeTabActivity.this.onCreateWithReactContext();
                }
              });
            }
          });
    } else {
      Log.d(TAG, "onCreate: successfully initialized!");
      onCreateWithReactContext();
      setupTransition();
    }
  }

  private void onCreateWithReactContext() {
    Log.d(TAG, "onCreateWithReactContext");
    if (supportIsDestroyed()) {
      Log.d(TAG, "onCreateWithReactContext: is destroyed");
      return;
    }
    View loadingView = findViewById(R.id.loading_view);
    if (loadingView != null) {
      loadingView.setVisibility(View.GONE);
    }
//    if (!isSuccessfullyInitialized()) {
//      Log.d(TAG, "onCreateWithReactContext: is not fully initialized");
      // TODO(lmr): should we do something like this in OSS?
//            ReactNativeUtils.showAlertBecauseChecksFailed(this, dialog -> finish());
//      return;
//    }

    String moduleName = getIntent().getStringExtra(REACT_MODULE_NAME);
    activityManager = new ReactInterfaceManager(this);

    instanceId = String.format(Locale.ENGLISH, "%1s_%2$d", moduleName, UUID++);
    reactNavigationCoordinator.registerComponent(this, instanceId);

    if (reactRootView == null) {
      ViewStub reactViewStub = (ViewStub) findViewById(R.id.react_root_view_stub);
      reactViewStub.setLayoutResource(R.layout.view_holder_react_root_view);
      reactRootView = (ReactRootView) reactViewStub.inflate();
    }

    Bundle props = getIntent().getBundleExtra(REACT_PROPS);
    if (props == null) {
      props = new Bundle();
    }
    props.putString(INSTANCE_ID_PROP, instanceId);

    getImplementation().reconcileNavigationProperties(
        this,
        getToolbar(),
        getSupportActionBar(),
        ConversionUtil.EMPTY_MAP,
        renderedConfig,
        true
    );

//    getSupportActionBar().hide();

    barHeight = getImplementation().getBarHeight(
        this,
        getToolbar(),
        getSupportActionBar(),
        renderedConfig,
        true
    );

    props.putFloat(INITIAL_BAR_HEIGHT_PROP, barHeight);

    reactRootView.startReactApplication(reactInstanceManager, moduleName, props);


    // we aren't actually rendering anything in this root view, just using it to get the tab
    // bar configuration. super hacky and strange, but i think it works...
//    reactRootView.setVisibility(View.GONE);

    setupBottomNavigation();
  }

  private void setupTransition() {
    Log.d(TAG, "setupTransition");
    if (initialConfig.hasKey("waitForRender") && !initialConfig.getBoolean("waitForRender")) {
      return;
    }
    // Shared element transitions have been unreliable on Lollipop < MR1.
    if (Build.VERSION.SDK_INT >= SHARED_ELEMENT_TARGET_API && ReactNativeUtils.isSharedElementTransition(
        getIntent())) {
      Log.d(TAG, "setupTransition: sharedElementTransition");
      setupSharedElementTransition();
    } else if (isSuccessfullyInitialized() && Build.VERSION.SDK_INT >= WAITING_TRANSITION_TARGET_API) {
      Log.d(TAG, "setupTransition: waitingForRenderTransition");
      setupDefaultWaitingForRenderTransition();
    } else {
      Log.d(TAG, "setupTransition: postDelayed");
      // if we don't have the ability to use a `TransitionListener`, we do the poor man's approach of
      // just emitting the event after some amount of time has expired. :facepalm:
      handler.postDelayed(new Runnable() {
        @Override public void run() {
          if (!supportIsDestroyed()) {
            ReactNativeTabActivity.this.emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
          }
        }
      }, FAKE_ENTER_TRANSITION_TIME_IN_MS);
    }

    // in this case, we end up waiting for the first render to complete
    // doing the transition. If this never happens for some reason, we are going to push
    // anyway in 250ms. The handler should get canceled + called sooner though (it's za race).
    isWaitingForRenderToFinish = true;
    transitionHandler.postDelayed(new Runnable() {
      @Override public void run() {
        Log.d(TAG, "render timeout callback called");
        ReactNativeTabActivity.this.onFinishWaitingForRender();
      }
    }, RENDER_TIMEOUT_IN_MS);
  }

  @TargetApi(SHARED_ELEMENT_TARGET_API)
  private void setupSharedElementTransition() {
    isSharedElementTransition = true;
    Log.d(TAG, "supportPostponeEnterTransition");
    supportPostponeEnterTransition();

    // We are doing a shared element transition...
    setEnterSharedElementCallback(new AutoSharedElementCallback(this));

    attachEnterTransitionListener(getWindow().getEnterTransition());
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  private void attachEnterTransitionListener(Transition transition) {
    transition.addListener(new SimpleTransitionListener() {
      @Override public void onTransitionEnd(Transition transition) {
        emitEvent(ON_ENTER_TRANSITION_COMPLETE, null);
      }
    });
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  private void setEnterTransition(Transition transition) {
    attachEnterTransitionListener(transition);
    getWindow().setEnterTransition(transition);
  }

  @TargetApi(WAITING_TRANSITION_TARGET_API)
  private void setupDefaultWaitingForRenderTransition() {
    Log.d(TAG, "supportPostponeEnterTransition");
    // TODO(lmr): it seems like this isn't actually quite working on the first push.
    supportPostponeEnterTransition();
    setEnterTransition(makeSlideLeftAnimation());
  }

  private void setupBottomNavigation() {
    Log.d(TAG, "setupBottomNavigation");
    bottomNavigation.setOnNavigationItemSelectedListener(this);
    reactRootView.setOnHierarchyChangeListener(reactViewChangeListener);

    Menu menu = bottomNavigation.getMenu();

    getImplementation().reconcileTabBarProperties(
        bottomNavigation,
        menu,
        prevTabBarConfig,
        renderedTabBarConfig
    );
  }

  private void notifyTabsHaveChanged() {
    Log.d(TAG, "notifyTabsHaveChanged");
    Menu menu = bottomNavigation.getMenu();

    getImplementation().reconcileTabBarProperties(
        bottomNavigation,
        menu,
        prevTabBarConfig,
        renderedTabBarConfig
    );

    menu.clear();
    bottomNavigation.clearIconHolders();

    int index = 0;
    for (TabView tab : tabViews.values()) {
      getImplementation().makeTabItem(
          bottomNavigation,
          menu,
          index,
          tab.getId(),
          tab.getRenderedConfig()
      );
      index++;
    }

    if (tabViews.size() > 0) {
      TabView view = tabViews.values().iterator().next();
      Fragment fragment = view.getFragment();
      getSupportFragmentManager().beginTransaction()
          .setAllowOptimization(true)
          .replace(R.id.content, fragment)
//          .addToBackStack(null)
          .commit();
    }

  }

  private void refreshTabs() {
    Log.d(TAG, "refreshTabs");
    traverseTabs();
    notifyTabsHaveChanged();
  }

  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    Log.d(TAG, "onNavigationItemSelected");
    TabView tab = tabViews.get(item.getItemId());
    if (tab != null) {
      Log.d(TAG, "found tab");
      Fragment fragment = tab.getFragment();
      getSupportFragmentManager().beginTransaction()
          .setAllowOptimization(true)
          .replace(R.id.content, fragment)
//          .addToBackStack(null)
          .commit();
    }
    return true;
  }

  private boolean tabViewsIsDirty = false;
  private Map<Integer, TabView> tabViews = new ArrayMap<>();
  private ReadableMap prevTabBarConfig = ConversionUtil.EMPTY_MAP;
  private ReadableMap renderedTabBarConfig = ConversionUtil.EMPTY_MAP;

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
        ((ViewGroup)child).setOnHierarchyChangeListener(reactViewChangeListener);
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

  private void debouncedRefreshTabs() {
    if (tabViewsIsDirty) {
      return;
    }
    tabViewsIsDirty = true;
    reactRootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override
      public boolean onPreDraw() {
        tabViewsIsDirty = false;
        reactRootView.getViewTreeObserver().removeOnPreDrawListener(this);
        refreshTabs();
        return true;
      }
    });
  }

  private void traverseTabs() {
    Stack<ViewGroup> stack = new Stack<>();
    stack.push(reactRootView);

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
          TabBarView tabBarView = (TabBarView)child;
          renderedTabBarConfig = ConversionUtil.combine(renderedTabBarConfig, tabBarView.getConfig());
          stack.push(tabBarView);
        } else if (child instanceof ViewGroup) {
          stack.push((ViewGroup) child);
        }
      }
    }

  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public ReactRootView getReactRootView() {
    return reactRootView;
  }

  @Override
  public ReactToolbar getToolbar() {
    return new ReactToolbar(this);
  }

  @Override
  public boolean isDismissible() {
    return false;
  }

  @Override
  public void signalFirstRenderComplete() {
    Log.d(TAG, "signalFirstRenderComplete");
    // For some reason, this "signal" gets sent before the `transitionName` gets set on the shared
    // elements, so if we are doing a "Shared Element Transition", we want to keep waiting before
    // starting the enter transition.
    if (!isSharedElementTransition && isWaitingForRenderToFinish) {
      transitionHandler.removeCallbacksAndMessages(null);
      transitionHandler.post(new Runnable() {
        @Override public void run() {
          Log.d(TAG, "signalFirstRenderComplete: onRun");
          ReactNativeTabActivity.this.onFinishWaitingForRender();
        }
      });
    }
  }

  @Override
  public void notifySharedElementAddition() {
    Log.d(TAG, "notifySharedElementAddition");
    if (isWaitingForRenderToFinish) {
      // if we are receiving a sharedElement and we have postponed the enter transition, we want to cancel any existing
      // handler and create a new one. (this is effectively debouncing the call).
      transitionHandler.removeCallbacksAndMessages(null);
      transitionHandler.post(new Runnable() {
        @Override public void run() {
          ReactNativeTabActivity.this.onFinishWaitingForRender();
        }
      });
    }
  }

  private void onFinishWaitingForRender() {
    Log.d(TAG, "onFinishWaitingForRender");
    if (isWaitingForRenderToFinish && !supportIsDestroyed()) {
      isWaitingForRenderToFinish = false;
      Log.d(TAG, "onFinishWaitingForRender: supportStartPostponedEnterTransition");
      scheduleStartPostponedTransition();
    }
  }

  @Override
  public FragmentActivity getActivity() {
    return this;
  }

  @Override
  public void emitEvent(String eventName, Object object) {
    if (isSuccessfullyInitialized() && !supportIsDestroyed()) {
      String key =
          String.format(Locale.ENGLISH, "NativeNavigationScreen.%s.%s", eventName, instanceId);
      Log.d(TAG, key);
      ReactNativeUtils.maybeEmitEvent((ReactContext) reactInstanceManager.getCurrentReactContext(),
          key, object);
    }
  }

  private void reconcileNavigationProperties() {
    getImplementation().reconcileNavigationProperties(
        this,
        getToolbar(),
        getSupportActionBar(),
        this.previousConfig,
        this.renderedConfig,
        false
    );
  }

  @Override
  public void receiveNavigationProperties(ReadableMap properties) {
    Log.d(TAG, "receiveNavigationProperties");
    this.previousConfig = this.renderedConfig;
    this.renderedConfig = ConversionUtil.combine(this.initialConfig, properties);
    reconcileNavigationProperties();
//    Activity activity = getActivity();
//    if (activity instanceof ReactInterface) {
//      ((ReactInterface)activity).receiveNavigationProperties(properties);
//    }
  }

  @Override
  public void dismiss() {
    Intent intent = new Intent()
        .putExtra(EXTRA_IS_DISMISS, isDismissible());
    setResult(Activity.RESULT_OK, intent);
    finish();
  }
}
