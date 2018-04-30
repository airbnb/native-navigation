package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.airbnb.android.react.navigation.ReactNativeIntents.EXTRA_IS_DISMISS;
import static com.airbnb.android.react.navigation.ReactNativeUtils.VERSION_CONSTANT_KEY;
import static com.airbnb.android.react.navigation.ScreenCoordinator.EXTRA_PAYLOAD;

class NavigatorModule extends ReactContextBaseJavaModule {

    private static final int VERSION = 2;

    private static final String CLOSE_BEHAVIOR_DISMISS = "dismiss";

    private static final String RESULT_CODE = "resultCode";

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final ReactNavigationCoordinator coordinator;

    NavigatorModule(ReactApplicationContext reactContext, ReactNavigationCoordinator coordinator) {
        super(reactContext);
        this.coordinator = coordinator;
    }

    @Override
    public String getName() {
        return "NativeNavigationModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        return Collections.<String, Object> singletonMap(VERSION_CONSTANT_KEY, VERSION);
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void registerScreen(
            String sceneName,
            @Nullable ReadableMap properties,
            boolean waitForRender,
            String mode
    ) {
        if (properties == null) {
            properties = ConversionUtil.EMPTY_MAP;
        }
        coordinator.registerScreen(
                sceneName,
                properties,
                waitForRender,
                mode
        );
    }

    @SuppressWarnings("unused")
    @ReactMethod
    public void setScreenProperties(final ReadableMap properties, final String instanceId) {
//    final Map<String, Object> props = payloadToMap(properties);
        withToolbar(instanceId, new NavigationModifier() {
            @Override
            public void call(ReactInterface component, ReactToolbar toolbar) {
                component.receiveNavigationProperties(properties);
            }
        });
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @ReactMethod
    public void signalFirstRenderComplete(String id) {
        final ReactInterface component = coordinator.componentFromId(id);
        if (component != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    component.signalFirstRenderComplete();
                }
            });
        }
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void push(final String screenName, final ReadableMap props,
                     final ReadableMap options) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    return;
                }
                ensureCoordinatorComponent(activity);
                ((ScreenCoordinatorComponent) activity).getScreenCoordinator().pushScreen(
                        screenName,
                        ConversionUtil.toBundle(props),
                        ConversionUtil.toBundle(options));
            }
        });
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void resetTo(final String screenName, final ReadableMap props,
                        final ReadableMap options) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    return;
                }
                ensureCoordinatorComponent(activity);
                ((ScreenCoordinatorComponent) activity).getScreenCoordinator().resetTo(
                        screenName,
                        ConversionUtil.toBundle(props),
                        ConversionUtil.toBundle(options));
            }
        });
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @ReactMethod
    public void pushNative(String name, ReadableMap props, ReadableMap options, Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        Intent intent = coordinator.intentForKey(activity.getBaseContext(), name, props);
        startActivityWithPromise(activity, intent, promise, options);
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void present(final String screenName, final ReadableMap props, final ReadableMap options, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    return;
                }
                ensureCoordinatorComponent(activity);
                ((ScreenCoordinatorComponent) activity).getScreenCoordinator().presentScreen(
                        screenName,
                        ConversionUtil.toBundle(props),
                        ConversionUtil.toBundle(options),
                        promise);
            }
        });
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void showModal(final String screenName, final ReadableMap props, final ReadableMap options, final Promise promise) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    return;
                }

                ReactNativeUtils.presentScreen(getCurrentActivity(), screenName, ConversionUtil.toBundle(props));
            }
        });
    }

    @SuppressWarnings({"UnusedParameters", "unused"})
    @ReactMethod
    public void presentNative(String name, ReadableMap props, ReadableMap options, Promise promise) {
        Activity activity = getCurrentActivity();
        if (activity == null) {
            return;
        }
        Intent intent = coordinator.intentForKey(activity.getBaseContext(), name, props);
        startActivityWithPromise(activity, intent, promise, options);
    }

    @ReactMethod
    public void dismiss(final ReadableMap payload, @SuppressWarnings("UnusedParameters") boolean animated) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: handle payload
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    return;
                }
                ensureCoordinatorComponent(activity);
                ((ScreenCoordinatorComponent) activity).getScreenCoordinator().dismiss();
            }
        });
    }

    @SuppressWarnings("UnusedParameters")
    @ReactMethod
    public void pop(ReadableMap payload, boolean animated) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: handle payload
                Activity activity = getCurrentActivity();
                if (activity == null) {
                    return;
                }

                if (activity instanceof ReactModalActivity) {
                    final ReactModalActivity modalActivity = (ReactModalActivity) activity;
                    modalActivity.finish();
                    return;
                }

                ensureCoordinatorComponent(activity);
                ((ScreenCoordinatorComponent) activity).getScreenCoordinator().pop();
            }
        });
    }

    private interface NavigationModifier {

        void call(ReactInterface component, ReactToolbar toolbar);
    }

    private void withToolbar(String id, final NavigationModifier modifier) {
        final ReactInterface component = coordinator.componentFromId(id);
        if (component != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ReactToolbar toolbar = component.getToolbar();
                    if (toolbar != null) {
                        modifier.call(component, toolbar);
                    }
                }
            });
        }
    }

    private void startActivityWithPromise(final Activity activity, final Intent intent,
                                          final Promise promise, final ReadableMap options) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (ActivityUtils.hasActivityStopped(activity)) {
                    return;
                }
                ReactInterfaceManager.startActivityWithPromise(activity, intent, promise, options);
            }
        });
    }

    private void dismiss(Activity activity, ReadableMap payload) {
        Intent intent = new Intent()
                .putExtra(EXTRA_PAYLOAD, payloadToMap(payload));
        if (activity instanceof ReactInterface) {
            // TODO: 10/6/16 emily this doesn't work for ReactNativeFragment
            intent.putExtra(EXTRA_IS_DISMISS, ((ReactInterface) activity).isDismissible());
        }
        activity.setResult(getResultCodeFromPayload(payload), intent);
        activity.finish();
    }

    private void ensureCoordinatorComponent(Activity activity) {
        if (!(activity instanceof ScreenCoordinatorComponent)) {
            throw new IllegalStateException("Your activity must implement ScreenCoordinatorComponent.");
        }
    }

    /**
     * Returns the result_code from the ReadableMap payload or RESULT_OK if none found. <p> Throws
     * IllegalArgumentException if the resultCode is not a number.
     */
    private static int getResultCodeFromPayload(ReadableMap payload) {
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

    private static HashMap<String, Object> payloadToMap(ReadableMap payload) {
        return payload == null
                ? new HashMap<String, Object>()
                : new HashMap<>(ConversionUtil.toMap(payload));
    }
}
