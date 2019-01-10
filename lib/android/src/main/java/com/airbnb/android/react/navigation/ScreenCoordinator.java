package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.airbnb.android.R;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;

import java.util.Map;
import java.util.Stack;

import static com.airbnb.android.react.navigation.ReactNativeIntents.EXTRA_CODE;

/**
 * Owner of the navigation stack for a given Activity. There should be one per activity.
 */
public class ScreenCoordinator {
  private static final String TAG = ScreenCoordinator.class.getSimpleName();
  static final String EXTRA_PAYLOAD = "payload";
  private static final String TRANSITION_GROUP = "transitionGroup";
  private static final String ADD_TO_BACKSTACK_OPTION = "addToAndroidBackStack";

  enum PresentAnimation {
    Modal(R.anim.slide_up, R.anim.delay, R.anim.delay, R.anim.slide_down),
    Push(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right),
    Fade(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out);

    @AnimRes int enter;
    @AnimRes int exit;
    @AnimRes int popEnter;
    @AnimRes int popExit;

    PresentAnimation(int enter, int exit, int popEnter, int popExit) {
      this.enter = enter;
      this.exit = exit;
      this.popEnter = popEnter;
      this.popExit = popExit;
    }
  }

  private final Stack<BackStack> backStacks = new Stack<>();
  private final AppCompatActivity activity;
  private final ScreenCoordinatorLayout container;
  private ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;

  private int stackId = 0;
  /**
   * When we dismiss a back stack, the fragment manager would normally execute the latest fragment's
   * pop exit animation. However, if we present A as a modal, push, B, then dismiss(), the latest
   * pop exit animation would be from when B was pushed, not from when A was presented.
   * We want the dismiss animation to be the popExit of the original present transaction.
   */
  @AnimRes private int nextPopExitAnim;

  public ScreenCoordinator(AppCompatActivity activity, ScreenCoordinatorLayout container,
                           @Nullable Bundle savedInstanceState) {
    this.activity = activity;
    this.container = container;
    container.setFragmentManager(activity.getSupportFragmentManager());
    // TODO: restore state
  }

  void onSaveInstanceState(Bundle outState) {
    // TODO
  }

  public void pushScreen(String moduleName) {
    pushScreen(moduleName, null, null);
  }

  public void pushScreen(String moduleName, @Nullable Bundle props, @Nullable Bundle options) {
    Fragment fragment = ReactNativeFragment.newInstance(moduleName, props);
    pushScreen(fragment, options);
  }

  public void resetTo(String moduleName, @Nullable Bundle props, @Nullable Bundle options) {
    Fragment fragment = ReactNativeFragment.newInstance(moduleName, props);
    resetTo(fragment, options);
  }

  public void pushScreen(Fragment fragment) {
    pushScreen(fragment, null);
  }

  public void pushScreen(Fragment fragment, @Nullable Bundle options) {
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
            .setReorderingAllowed(true);
    Fragment currentFragment = getCurrentFragment();
    if (currentFragment == null) {
      throw new IllegalStateException("There is no current fragment. You must present one first.");
    }

    if (ViewUtils.isAtLeastLollipop() && options != null && options.containsKey(TRANSITION_GROUP)) {
      setupFragmentForSharedElement(currentFragment,  fragment, ft, options);
    } else {
      PresentAnimation anim = PresentAnimation.Fade;
      ft.setCustomAnimations(anim.enter, anim.exit, anim.popEnter, anim.popExit);
    }
    BackStack bsi = getCurrentBackStack();

    if (bsi == null) {
      bsi = new BackStack(getNextStackTag(), null, null);
      backStacks.push(bsi);
    }

    ft
            .add(container.getId(), fragment)
            .addToBackStack(bsi.getTag())
            .commit();
    bsi.pushFragment();
    Log.d(TAG, toString());

    if (currentFragment instanceof ReactNativeFragment) {
      ((ReactNativeFragment) currentFragment).emitOnDisappear();
    }
  }

  public void resetTo(Fragment fragment, @Nullable Bundle options) {
    final boolean addToAndroidBackStack = options != null &&
            options.containsKey(ADD_TO_BACKSTACK_OPTION) &&
            options.getBoolean(ADD_TO_BACKSTACK_OPTION);

    final FragmentManager fragmentManager = activity.getSupportFragmentManager();
    final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction()
            .setReorderingAllowed(true);

    PresentAnimation anim = PresentAnimation.Fade;
    fragmentTransaction.setCustomAnimations(anim.enter, anim.exit, anim.popEnter, anim.popExit);

    BackStack bsi = getCurrentBackStack();

    if (bsi == null) {
      bsi = new BackStack(getNextStackTag(), null, null);
      backStacks.push(bsi);
    }

    if (!addToAndroidBackStack) {
      while (!bsi.isEmpty()) {
        bsi.popFragment();
      }

      fragmentManager.popBackStackImmediate();
    }

    fragmentTransaction.replace(container.getId(), fragment);

    if (addToAndroidBackStack) {
      fragmentTransaction.addToBackStack(bsi.getTag());
    }
    // When resetting we don't care about state loss
    fragmentTransaction.commitAllowingStateLoss();

    bsi.pushFragment();
    Log.d(TAG, toString());
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP)
  private void setupFragmentForSharedElement(
          Fragment outFragment, Fragment inFragment, FragmentTransaction transaction, Bundle options) {
    FragmentSharedElementTransition transition = new FragmentSharedElementTransition();
    inFragment.setSharedElementEnterTransition(transition);
    inFragment.setSharedElementReturnTransition(transition);
    Fade fade = new Fade();
    inFragment.setEnterTransition(fade);
    inFragment.setReturnTransition(fade);
    ViewGroup rootView = (ViewGroup) outFragment.getView();
    ViewGroup transitionGroup = ViewUtils.findViewGroupWithTag(
            rootView,
            R.id.react_shared_element_group_id,
            options.getString(TRANSITION_GROUP));
    AutoSharedElementCallback.addSharedElementsToFragmentTransaction(transaction, transitionGroup);
  }

  public void presentScreen(String moduleName) {
    presentScreen(moduleName, null, null, null);
  }

  public void presentScreen(
          String moduleName,
          @Nullable Bundle props,
          @Nullable Bundle options,
          @Nullable Promise promise) {
    // TODO: use options
    Fragment fragment = ReactNativeFragment.newInstance(moduleName, props);
    presentScreen(fragment, PresentAnimation.Modal, promise);
  }

  public void presentScreen(Fragment fragment) {
    presentScreen(fragment, null);
  }

  private Boolean isFragmentTranslucent(Fragment fragment) {
    Bundle bundle = fragment.getArguments();
    if (bundle != null) {
      String moduleName = bundle.getString(ReactNativeIntents.EXTRA_MODULE_NAME);
      if (moduleName != null) {
        ReadableMap config = reactNavigationCoordinator.getInitialConfigForModuleName(moduleName);
        if (config != null && config.hasKey("screenColor")) {
          return Color.alpha(config.getInt("screenColor")) < 255;
        }
      }
    }
    return false;
  }

  public void presentScreen(Fragment fragment, @Nullable Promise promise) {
    presentScreen(fragment, PresentAnimation.Modal, promise);
  }

  public void presentScreen(Fragment fragment, PresentAnimation anim, @Nullable Promise promise) {
    if (fragment == null) {
      throw new IllegalArgumentException("Fragment must not be null.");
    }

    BackStack bsi = new BackStack(getNextStackTag(), anim, promise);
    backStacks.push(bsi);

    // TODO: dry this up with pushScreen
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
            .setAllowOptimization(true)
            .setCustomAnimations(anim.enter, anim.exit, anim.popEnter, anim.popExit);

    ft
            .add(container.getId(), fragment)
            .addToBackStack(bsi.getTag())
            .commit();
    activity.getSupportFragmentManager().executePendingTransactions();
    bsi.pushFragment();
    Log.d(TAG, toString());
  }

  public void dismissAll() {
    while (!backStacks.isEmpty()) {
      dismiss(0, null, false);
      activity.getFragmentManager().executePendingTransactions();
    }
  }

  public void onBackPressed() {
    if (getCurrentFragment() != null && getCurrentFragment() instanceof ReactNativeFragment) {
      final ReactNativeFragment fragment = (ReactNativeFragment) getCurrentFragment();

      if (fragment.isOnBackPressImplemented()) {
        fragment.onBackPressed();
        return;
      }

    }
    pop();
  }

  public void pop() {
    final BackStack bsi = getCurrentBackStack();

//    View decorView = activity.getWindow().getDecorView();
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//      decorView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
//        @Override
//        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
//          WindowInsets defaultInsets = null;
//          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
//            defaultInsets = v.onApplyWindowInsets(insets);
//            return defaultInsets.replaceSystemWindowInsets(
//                    defaultInsets.getSystemWindowInsetLeft(),
//                    defaultInsets.getSystemWindowInsetTop(),
//                    defaultInsets.getSystemWindowInsetRight(),
//                    defaultInsets.getSystemWindowInsetBottom());
//          }
//
//          return null;
//        }
//      });
//    }

//    ViewCompat.requestApplyInsets(decorView);

    // When using resetTo and double tapping the back button, we crash on the second tap because
    // there is no back stack.
    // Not the best way of fixing this - ideally the backstack state should mirror the state of the
    // FragmentManager
    if (bsi == null) {
      Log.w(TAG, "Attempting to call ScreenCoordinator.pop() when BackStack is null");
      return;
    }

    if (bsi.getSize() == 1) {
      dismiss();
      return;
    }
    bsi.popFragment();
    // We use popBackStackImmediate() here to force the pop to happen synchronously. This is so the
    // user doesn't crash the app by double tapping the back button when navigating forward.
    activity.getSupportFragmentManager().popBackStackImmediate();
    Log.d(TAG, toString());
  }

  public void dismiss() {
    dismiss(Activity.RESULT_OK, null);
  }

  public void dismiss(int resultCode, Map<String, Object> payload) {
    // BREAKING UCL exit after dismissing last activity
    dismiss(resultCode, payload, activity instanceof ReactActivity);
  }

  public void dismiss(int resultCode, Map<String, Object> payload, boolean finishIfEmpty) {
    BackStack bsi = backStacks.pop();
    Promise promise = bsi.getPromise();
    deliverPromise(promise, resultCode, payload);
    // This is needed so we can override the pop exit animation to slide down.
    PresentAnimation anim = bsi.getAnimation();

    if (backStacks.isEmpty()) {
      if (finishIfEmpty) {
        activity.supportFinishAfterTransition();
        return;
      }
    } else {
      // This will be used when the fragment delegates its onCreateAnimation to this.
      nextPopExitAnim = anim.popExit;
    }

    activity.getSupportFragmentManager()
            .popBackStackImmediate(bsi.getTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
    Log.d(TAG, toString());
  }

  public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
    if (!enter && nextPopExitAnim != 0) {
      // If this fragment was pushed on to the stack, it's pop exit animation will be
      // slide out right. However, we want it to be slide down in this case.
      int anim = nextPopExitAnim;
      nextPopExitAnim = 0;
      return AnimationUtils.loadAnimation(activity, anim);
    }
    return null;
  }

  private void deliverPromise(Promise promise, int resultCode, Map<String, Object> payload) {
    if (promise != null) {
      Map<String, Object> newPayload =
              MapBuilder.of(EXTRA_CODE, resultCode, EXTRA_PAYLOAD, payload);
      promise.resolve(ConversionUtil.toWritableMap(newPayload));
    }
  }

  private String getNextStackTag() {
    return getStackTag(stackId++);
  }

  private String getStackTag(int id) {
    return "STACK" + id;
  }

  @Nullable
  public Fragment getCurrentFragment() {
    return activity.getSupportFragmentManager().findFragmentById(container.getId());
  }

  @Nullable
  private BackStack getCurrentBackStack() {
    if (backStacks.isEmpty()) {
      return null;
    }

    return backStacks.peek();
  }

  @NonNull
  private FrameLayout createContainerView() {
    return new FrameLayout(activity);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ScreenCoordinator{");
    for (int i = 0; i < backStacks.size(); i++) {
      sb.append("Back stack ").append(i).append(":\t").append(backStacks.get(i));
    }
    sb.append('}');
    return sb.toString();
  }
}
