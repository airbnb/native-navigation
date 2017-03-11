package com.airbnb.android.react.navigation;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.util.Log;
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
  private static final String TAG = ScreenCoordinator.class.getSimpleName();
  static final String EXTRA_PAYLOAD = "payload";
  private static final String TRANSITION_GROUP = "transitionGroup";



  private final Stack<String> stackTagBackStack = new Stack<>();
  private final Map<String, List<Fragment>> fragmentStacks = new HashMap<>();
  private final Map<String, Promise> promisesMap = new HashMap<>();
  private final AppCompatActivity activity;
  private final ViewGroup container;

  private int stackId = 0;
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

  public void pushScreen(String moduleName, @Nullable Bundle props, @Nullable Bundle options) {
    Fragment fragment = ReactNativeFragment.newInstance(moduleName, props);
    pushScreen(fragment, options);
  }

  public void pushScreen(Fragment fragment) {
    pushScreen(fragment, null);
  }

  public void pushScreen(Fragment fragment, @Nullable Bundle options) {
    ensureContainerForCurrentStack();
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
            .setAllowOptimization(true);
    Fragment currentFragment = getCurrentFragment();
    if (currentFragment == null) {
      throw new IllegalStateException("There is no current fragment. You must present one first.");
    }

    if (ViewUtils.isAtLeastLollipop() && options != null && options.containsKey(TRANSITION_GROUP)) {
        setupFragmentForSharedElement(currentFragment,  fragment, ft, options);
    } else {
        ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    }
    ft
            .detach(currentFragment)
            .add(Math.abs(currentStackTag.hashCode()), fragment)
            .addToBackStack(null)
            .commit();
    fragmentStacks.get(currentStackTag).add(fragment);
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
    presentScreen(fragment, promise);
  }

  public void presentScreen(Fragment fragment) {
    presentScreen(fragment, null);
  }

  public void presentScreen(Fragment fragment, @Nullable Promise promise) {
    if (fragment == null) {
      throw new IllegalArgumentException("Fragment must not be null.");
    }
    Fragment currentFragment = getCurrentFragment();
    currentStackTag = getNextStackTag();
    stackTagBackStack.push(currentStackTag);
    promisesMap.put(currentStackTag, promise);
    // TODO: dry this up with pushScreen
    ensureContainerForCurrentStack();
    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction()
        .setAllowOptimization(true)
        .setCustomAnimations(R.anim.slide_up, R.anim.delay, R.anim.delay, R.anim.slide_down);
    if (currentFragment != null) {
      ft.detach(currentFragment);
    }
    ft
        .add(Math.abs(currentStackTag.hashCode()), fragment)
        .addToBackStack(currentStackTag)
        .commit();
    fragmentStacks.get(currentStackTag).add(fragment);
    Log.d(TAG, toString());
  }

  public void dismissAll() {
    while (!stackTagBackStack.isEmpty()) {
      dismiss(0, null, false);
      activity.getFragmentManager().executePendingTransactions();
    }
  }

  public void showTab(Fragment fragment, int id) {
    if (fragment == null) {
      throw new IllegalArgumentException("Fragment must not be null.");
    }
    if (!stackTagBackStack.isEmpty()) {
      dismissAll();
    }
    currentStackTag = getStackTag(id);
    stackTagBackStack.push(currentStackTag);
    ensureContainerForCurrentStack();
    activity.getSupportFragmentManager().beginTransaction()
            .setAllowOptimization(true)
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out, R.anim.fade_in, R.anim.fade_out)
            .add(Math.abs(currentStackTag.hashCode()), fragment)
            .addToBackStack(currentStackTag)
            .commit();
    fragmentStacks.get(currentStackTag).add(fragment);
    Log.d(TAG, toString());
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
    Log.d(TAG, toString());
  }

  public void dismiss() {
    dismiss(Activity.RESULT_OK, null);
  }

  public void dismiss(int resultCode, Map<String, Object> payload) {
    dismiss(resultCode, payload, true);
  }

  private void dismiss(int resultCode, Map<String, Object> payload, boolean finishIfEmpty) {
    String dismissingStackTag = stackTagBackStack.pop();
    Promise promise = promisesMap.remove(dismissingStackTag);
    deliverPromise(promise, resultCode, payload);
    List<Fragment> stack = fragmentStacks.remove(dismissingStackTag);
    // This is needed so we can override the pop exit animation to slide down.
    dismissingFragment = stack.get(stack.size() - 1);

    if (stackTagBackStack.isEmpty()) {
      if (finishIfEmpty) {
        activity.finish();
        activity.overridePendingTransition(R.anim.delay, R.anim.slide_down);
      }
    } else {
      currentStackTag = stackTagBackStack.peek();
    }

    activity.getSupportFragmentManager()
            .popBackStackImmediate(dismissingStackTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    Log.d(TAG, toString());
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

  private String getNextStackTag() {
    return getStackTag(stackId++);
  }

  private String getStackTag(int id) {
    return "STACK" + id;
  }

  @Nullable
  private Fragment getCurrentFragment() {
    List<Fragment> stack = fragmentStacks.get(currentStackTag);
    if (stack == null || stack.isEmpty()) {
      return null;
    }
    return stack.get(stack.size() - 1);
  }

  private View getContainerForId(int id) {
    View existingView = container.findViewById(id);
    if (existingView != null) {
      return existingView;
    }
    FrameLayout container = createContainerView();
    container.setId(id);
    this.container.addView(container);
    return container;
  }

  private void ensureContainerForCurrentStack() {
    // Ids must be > 0
    int id = Math.abs(currentStackTag.hashCode());
    View existingView = container.findViewById(id);
    if (fragmentStacks.get(currentStackTag) == null) {
      fragmentStacks.put(currentStackTag, new ArrayList<Fragment>());
    }
    if (existingView == null) {
      FrameLayout stackContainer = createContainerView();
      stackContainer.setId(id);
      container.addView(stackContainer);
    }
  }

  @NonNull
  private FrameLayout createContainerView() {
    return new FrameLayout(activity);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ScreenCoordinator{");
    sb.append("stackTagBackStack=").append(stackTagBackStack);
    boolean hasStack = currentStackTag != null && fragmentStacks.get(currentStackTag) != null;
    sb.append(", stackSize=").append(hasStack ? fragmentStacks.get(currentStackTag).size() : 0);
    sb.append(", currentStackTag='").append(currentStackTag).append('\'');
    sb.append('}');
    return sb.toString();
  }
}
