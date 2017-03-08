package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.airbnb.android.R;
import com.facebook.react.bridge.Promise;
import com.facebook.react.common.MapBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static com.airbnb.android.react.navigation.ReactNativeIntents.EXTRA_CODE;

/**
 * Owner of the navigation stack for a given Activity. There should be one per activity.
 */
public class ScreenCoordinator {
  public static final String EXTRA_PAYLOAD = "payload";
  private static int stackId = 0;

  private static String getNextStackTag() {
    return "STACK" + stackId++;
  }

  private final Stack<String> stackTagBackStack = new Stack<>();
  private final Map<String, List<Fragment>> fragmentStacks = new HashMap<>();
  private final Map<String, Promise> promisesMap = new HashMap<>();
  private final AppCompatActivity activity;
  private final ViewGroup container;

  private String currentStackTag = getNextStackTag();
  private Fragment dismissingFragment;

  public ScreenCoordinator(AppCompatActivity activity, ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // TODO: restore state
    this.activity = activity;
    this.container = container;
  }

  void onSaveInstanceState(Bundle outState) {
    // TODO
  }

  public void pushScreen(String moduleName) {
    pushScreen(moduleName, null, null);
  }

  public void pushScreen(
      String moduleName,
      @Nullable Bundle props,
      @Nullable Bundle options) {
    // TODO: use options
    Fragment fragment = ReactNativeFragment.newInstance(moduleName, props);
    pushScreen(fragment);
  }

  public void pushScreen(Fragment fragment) {
    // TODO: use props, options, and promise.
    ensureContainerForCurrentStack();
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
        .setAllowOptimization(true)
        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    Fragment currentFragment = getCurrentFragment();
    if (currentFragment != null) {
      ft.detach(currentFragment);
    }
    ft
        .add(Math.abs(currentStackTag.hashCode()), fragment)
        .addToBackStack(null)
        .commit();
    fragmentStacks.get(currentStackTag).add(fragment);
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
    presentScreen(fragment, promise);
  }

  public void presentScreen(Fragment fragment) {
    presentScreen(fragment, null);
  }

  public void presentScreen(Fragment fragment, @Nullable Promise promise) {
    if (fragment == null) {
      throw new IllegalArgumentException("Fragment must not be null.");
    }
    currentStackTag = getNextStackTag();
    stackTagBackStack.push(currentStackTag);
    promisesMap.put(currentStackTag, promise);
    // TODO: dry this up with pushScreen
    // TODO: use promise
    ensureContainerForCurrentStack();
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
        .setAllowOptimization(true)
        .setCustomAnimations(R.anim.slide_up, R.anim.delay, R.anim.delay, R.anim.slide_down);
    Fragment currentFragment = getCurrentFragment();
    if (currentFragment != null) {
      ft.detach(currentFragment);
    }
    ft
        .add(Math.abs(currentStackTag.hashCode()), fragment)
        .addToBackStack(currentStackTag)
        .commit();
    fragmentStacks.get(currentStackTag).add(fragment);
  }

  public void onBackPressed() {
    pop();
  }

  public void pop() {
    List<Fragment> stack = fragmentStacks.get(currentStackTag);
    if (stack.size() == 1) {
      dismiss();
      return;
    }
    stack.remove(stack.size() - 1);
    activity.getSupportFragmentManager().popBackStack();
  }

  public void dismiss() {
    dismiss(Activity.RESULT_OK, null);
  }

  public void dismiss(int resultCode, Map<String, Object> payload) {
    String dismissingStackTag = stackTagBackStack.pop();
    Promise promise = promisesMap.remove(dismissingStackTag);
    deliverPromise(promise, resultCode, payload);
    List<Fragment> stack = fragmentStacks.remove(dismissingStackTag);
    // This is needed so we can override the pop exit animation to slide down.
    dismissingFragment = stack.get(stack.size() - 1);

    if (stackTagBackStack.isEmpty()) {
      activity.finish();
      activity.overridePendingTransition(R.anim.delay, R.anim.slide_down);
    } else {
      currentStackTag = stackTagBackStack.peek();
    }

    activity.getSupportFragmentManager()
        .popBackStack(dismissingStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
  }

  public Animation onCreateAnimation(Fragment fragment) {
    if (fragment == dismissingFragment) {
      // If this fragment was pushed on to the stack, it's pop exit animation will be
      // slide out right. However, we want it to be slide down in this case.
      dismissingFragment = null;
      return AnimationUtils.loadAnimation(activity, R.anim.slide_down);
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

  @Nullable
  private Fragment getCurrentFragment() {
    List<Fragment> stack = fragmentStacks.get(currentStackTag);
    if (stack == null || stack.isEmpty()) {
      return null;
    }
    return stack.get(stack.size() - 1);
  }

  private void ensureContainerForCurrentStack() {
    // Ids must be > 0
    int id = Math.abs(currentStackTag.hashCode());
    View existingView = container.findViewById(id);
    if (existingView != null) {
      return;
    }
    FrameLayout stackContainer = new FrameLayout(activity);
    stackContainer.setId(id);
    container.addView(stackContainer);
    if (fragmentStacks.get(currentStackTag) == null) {
      fragmentStacks.put(currentStackTag, new ArrayList<Fragment>());
    }
  }
}
