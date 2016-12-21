package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.airbnb.android.R;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.airbnb.android.react.navigation.ReactNativeUtils.VERSION_CONSTANT_KEY;

class NavigatorModule extends ReactContextBaseJavaModule {
  private static final int VERSION = 2;
  static final String EXTRA_PAYLOAD = "payload";
  static final String EXTRA_CODE = "code";
  static final String EXTRA_IS_DISMISS = "isDismiss";

  private static final String CLOSE_BEHAVIOR_DISMISS = "dismiss";
  private static final String SHARED_ELEMENT_TRANSITION_GROUP_OPTION = "transitionGroup";
  private static final String RESULT_CODE = "resultCode";
  private final Handler handler = new Handler(Looper.getMainLooper());
  private final ReactNavigationCoordinator coordinator;

  NavigatorModule(ReactApplicationContext reactContext, ReactNavigationCoordinator coordinator) {
    super(reactContext);
    this.coordinator = coordinator;
  }

  @Override public String getName() {
    return "NativeNavigationModule";
  }

  @Override public Map<String, Object> getConstants() {
    return Collections.<String, Object>singletonMap(VERSION_CONSTANT_KEY, VERSION);
  }

  @SuppressWarnings("unused")
  @ReactMethod
  public void registerScreenProperties(String sceneName, ReadableMap properties) {
    // TODO(lmr):
    // backgroundColor
  }

  @SuppressWarnings("unused")
  @ReactMethod
  public void registerScreenNavigatorProperties(String sceneName, ReadableMap properties) {
    // TODO(lmr):
    // theme
    // bar color
    // bar type
    // title
    // link
    // leftIcon
  }

  @SuppressWarnings("unused")
  @ReactMethod
  public void setNavigatorProperties(ReadableMap properties, String instanceId) {
    // TODO(lmr):
    // theme
    // bar color
    // bar type
    // title
    // link
    // leftIcon
  }

  @SuppressWarnings({"UnusedParameters", "unused"})
  @ReactMethod
  public void signalFirstRenderComplete(String id) {
    final ReactInterface component = coordinator.componentFromId(id);
    if (component != null) {
      handler.post(new Runnable() {
        @Override public void run() {
          component.signalFirstRenderComplete();
        }
      });
    }
  }

  @SuppressWarnings("UnusedParameters")
  @ReactMethod
  public void push(String screenName, ReadableMap props, ReadableMap options, Promise promise) {
    Activity activity = getCurrentActivity();
    if (activity != null) {
      Bundle propsBundle = ConversionUtil.toBundle(props);
      Intent intent =
          ReactNativeIntents.intent(getReactApplicationContext(), screenName, propsBundle);
      startActivityWithPromise(activity, intent, promise, options);
    }
  }

  @SuppressWarnings({"UnusedParameters", "unused"})
  @ReactMethod
  public void pushNative(String name, ReadableMap props, @Nullable ReadableMap options,
      Promise promise) {
    Activity activity = getCurrentActivity();
    if (activity != null) {
      Intent intent = coordinator.intentForKey(activity.getBaseContext(), name, props);
      startActivityWithPromise(activity, intent, promise, options);
    }
  }

  @SuppressWarnings("UnusedParameters")
  @ReactMethod
  public void present(String screenName, ReadableMap props, ReadableMap options, Promise promise) {
    Activity activity = getCurrentActivity();
    if (activity != null) {
      Bundle propsBundle = ConversionUtil.toBundle(props);
      Intent intent =
          ReactNativeIntents.modalIntent(getReactApplicationContext(), screenName, propsBundle);
      startActivityWithPromise(activity, intent, promise, options);
    }
  }

  @SuppressWarnings({"UnusedParameters", "unused"})
  @ReactMethod
  public void presentNative(String name, ReadableMap props, @Nullable ReadableMap options,
      Promise promise) {
    Activity activity = getCurrentActivity();
    if (activity != null) {
      Intent intent = coordinator.intentForKey(activity.getBaseContext(), name, props);
      startActivityWithPromise(activity, intent, promise, options);
    }
  }

  @ReactMethod
  public void dismiss(ReadableMap payload, @SuppressWarnings("UnusedParameters") boolean animated) {
    Activity activity = getCurrentActivity();
    if (activity != null) {
      dismiss(activity, payload);
    }
  }

  @SuppressWarnings("UnusedParameters")
  @ReactMethod
  public void pop(ReadableMap payload, boolean animated) {
    Activity activity = getCurrentActivity();
    if (activity != null) {
      pop(activity, payload);
    }
  }

  private interface ToolbarModifier {
    void call(ReactToolbar toolbar);
  }

  private void withToolbar(@Nullable final ReactInterface component,
      final ToolbarModifier modifier) {
    if (component != null) {
      handler.post(new Runnable() {
        @Override public void run() {
          ReactToolbar toolbar = component.getToolbar();
          if (toolbar != null) {
            modifier.call(toolbar);
          }
        }
      });
    }
  }

  private void startActivityWithPromise(final Activity activity, final Intent intent,
      final Promise promise, @Nullable final ReadableMap options) {
    handler.post(new Runnable() {
      @Override public void run() {
        if (ActivityUtils.hasActivityStopped(activity)) {
          return;
        }
        Bundle optionsBundle = null;
        if (options != null && options.hasKey(
            SHARED_ELEMENT_TRANSITION_GROUP_OPTION) && activity instanceof ReactInterface) {
          // TODO: 10/6/16 emily this doesn't work for ReactNativeFragment
          ReactRootView reactRootView = ((ReactInterface) activity).getReactRootView();
          ViewGroup transitionGroup = ViewUtils.findViewGroupWithTag(
              reactRootView,
              R.id.react_shared_element_group_id,
              options.getString(SHARED_ELEMENT_TRANSITION_GROUP_OPTION));
          if (transitionGroup != null) {
            ReactNativeUtils.setIsSharedElementTransition(intent, true);
            optionsBundle =
                AutoSharedElementCallback.getActivityOptionsBundle(activity, transitionGroup);
          }
        }
        ReactInterfaceManager.startActivityWithPromise(activity, intent, promise, optionsBundle);
      }
    });
  }

  private void dismiss(Activity activity, @Nullable ReadableMap payload) {
    Intent intent = new Intent()
        .putExtra(EXTRA_PAYLOAD, payloadToMap(payload));
    if (activity instanceof ReactInterface) {
      // TODO: 10/6/16 emily this doesn't work for ReactNativeFragment
      intent.putExtra(EXTRA_IS_DISMISS, ((ReactInterface) activity).isDismissible());
    }
    activity.setResult(getResultCodeFromPayload(payload), intent);
    activity.finish();
  }

  private void pop(Activity activity, @Nullable ReadableMap payload) {
    Intent intent = new Intent().putExtra(EXTRA_PAYLOAD, payloadToMap(payload));
    activity.setResult(getResultCodeFromPayload(payload), intent);
    activity.finish();
  }

  /**
   * Returns the result_code from the ReadableMap payload or RESULT_OK if none found. <p> Throws
   * IllegalArgumentException if the resultCode is not a number.
   */
  private static int getResultCodeFromPayload(@Nullable ReadableMap payload) {
    if (payload == null) {
      return Activity.RESULT_OK;
    }
    if (!payload.hasKey(RESULT_CODE)) {
      return Activity.RESULT_OK;
    }
    if (payload.getType(RESULT_CODE) != ReadableType.Number) {
      throw new IllegalArgumentException("Found non-integer resultCode.");
    }
    return payload.getInt(RESULT_CODE);
  }

  private static HashMap<String, Object> payloadToMap(@Nullable ReadableMap payload) {
    return payload == null
        ? new HashMap<String, Object>()
        : new HashMap<>(ConversionUtil.toMap(payload));
  }

//    interface OnMenuButtonClickListener {
//        /**
//         * @param button The selected button.
//         * @param index  The position of the button in the toolbar.
//         */
//        void onClick(MenuButton button, int index);
//    }

  /** Adds all the buttons to the given menu. Uses the given click listener as a callback for when any button is selected. */
//    static void addButtonsToMenu(Context context, Menu menu, List<MenuButton> buttons, OnMenuButtonClickListener onClickListener) {
//        for (int i = 0; i < buttons.size(); i++) {
//            MenuButton button = buttons.get(i);
//            MenuItem item = menu.add(button.title);
//            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//
//            final int buttonIndex = i;
//            if (button.useForegroundColor) {
//                item.setIcon(button.icon);
//                item.setOnMenuItemClickListener(menuItem -> {
//                    onClickListener.onClick(button, buttonIndex);
//                    return true;
//                });
//            } else {
//                // Uses a linear layout to provide layout bounds. This is copied from what MenuButton does internally if a layout resource is set.
//                ReactMenuItemView itemView =
//                        (ReactMenuItemView) LayoutInflater.from(context).inflate(R.layout.menu_item_view, new LinearLayout(context), false);
//                itemView.setImageResource(button.icon);
//                itemView.setOnClickListener(v -> onClickListener.onClick(button, buttonIndex));
//                itemView.setContentDescription(context.getString(button.title));
//                item.setActionView(itemView);
//            }
//        }
//    }
}
