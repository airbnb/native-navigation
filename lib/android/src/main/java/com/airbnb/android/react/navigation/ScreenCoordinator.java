package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.airbnb.android.R;
import com.facebook.react.bridge.Promise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Owner of the navigation stack for a given Activity. There should be one per activity.
 */
public class ScreenCoordinator {
  private static int stackId = 0;

  private static String getNextStackTag() {
    return "STACK" + stackId++;
  }

  private final Stack<String> stackTagBackStack = new Stack<>();
  private final Map<String, List<Fragment>> fragmentStacks = new HashMap<>();
  private final AppCompatActivity activity;
  private final ViewGroup container;

  private String currentStackTag = getNextStackTag();

  public ScreenCoordinator(AppCompatActivity activity, ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    // TODO: restore state
    this.activity = activity;
    this.container = container;
  }

  void onSaveInstanceState(Bundle outState) {
    // TODO
  }

  public void pushScreen(
      String moduleName,
      @Nullable Bundle props,
      @Nullable Bundle options,
      @Nullable Promise promise) {
    // TODO: use options
    Fragment fragment = ReactNativeFragment.newInstance(moduleName, props);
    pushScreen(fragment, promise);
  }

  public void pushScreen(Fragment fragment) {
    pushScreen(fragment, null);
  }

  public void pushScreen(
      Fragment fragment,
      @Nullable Promise promise) {
    // TODO: use props, options, and promise.
    ensureContainerForCurrentStack();
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
        .setAllowOptimization(true)
        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
    Fragment currentFragment = getCurrentFragment();
    if (currentFragment != null) {
      ft.detach(currentFragment);
    }
    ft
        .add(Math.abs(currentStackTag.hashCode()), fragment)
        .addToBackStack(null)
        .commit();
    addFragmentToStack(fragment, currentStackTag);
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
    addFragmentToStack(fragment, currentStackTag);
  }

  public void pop() {
    List<Fragment> stack = fragmentStacks.get(currentStackTag);
    if (stack.size() == 1) {
      dismiss();
      return;
    }
    Fragment fragmentToRemove = stack.remove(stack.size() - 1);
    stack = fragmentStacks.get(currentStackTag);
    Fragment fragmentToAttach = stack.get(stack.size() - 1);

    activity.getSupportFragmentManager().beginTransaction()
        .setAllowOptimization(true)
        .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
        .remove(fragmentToRemove)
        .attach(fragmentToAttach)
        .commit();
  }

  public void dismiss() {
    String dismissingStackTag = stackTagBackStack.pop();;
    if (stackTagBackStack.isEmpty()) {
      activity.finish();
      activity.overridePendingTransition(R.anim.delay, R.anim.slide_down);
    } else {
      currentStackTag = stackTagBackStack.peek();
    }

    activity.getSupportFragmentManager()
        .popBackStack(dismissingStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
  }

  @Nullable
  private Fragment getCurrentFragment() {
    List<Fragment> stack = fragmentStacks.get(currentStackTag);
    if (stack == null || stack.isEmpty()) {
      return null;
    }
    return stack.get(stack.size() - 1);
  }

  private void addFragmentToStack(Fragment fragment, String stackTag) {
    List<Fragment> stack = fragmentStacks.get(stackTag);
    if (stack == null) {
      stack = new ArrayList<>();
      fragmentStacks.put(stackTag, stack);
    }
    stack.add(fragment);
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
