package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import com.facebook.react.BuildConfig;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableMap;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ReactNavigationCoordinator {

    public static ReactNavigationCoordinator sharedInstance = new ReactNavigationCoordinator();

    private ReactInstanceManager reactInstanceManager;

    private NavigationImplementation navigationImplementation = new DefaultNavigationImplementation();

    private final Collection<ReactInstanceManager.ReactInstanceEventListener> initializationListeners =
            Collections.synchronizedSet(new HashSet<ReactInstanceManager.ReactInstanceEventListener>());

    private boolean isSuccessfullyInitialized = false;

    private static final int APP_INITIALIZE_TOAST_DELAY = 3000;


    private ReactNavigationCoordinator() {
    }

    public ReactInstanceManager getReactInstanceManager() {
        return reactInstanceManager;
    }

    public void injectReactInstanceManager(final ReactInstanceManager reactInstanceManager) {
        if (this.reactInstanceManager != null) {
            // TODO: throw error. can only initialize once.
        }
        this.reactInstanceManager = reactInstanceManager;
        this.reactInstanceManager.addReactInstanceEventListener(
                new ReactInstanceManager.ReactInstanceEventListener() {
                    @Override
                    public void onReactContextInitialized(final ReactContext context) {
                        reactInstanceManager.removeReactInstanceEventListener(this);
                        isSuccessfullyInitialized = true;
                        for (ReactInstanceManager.ReactInstanceEventListener listener : initializationListeners) {
                            listener.onReactContextInitialized(context);
                        }
                        initializationListeners.clear();
                    }
                });
    }

    public void injectImplementation(NavigationImplementation implementation) {
        if (this.navigationImplementation != null) {
            // TODO: throw error. can only initialize once.
        }
        this.navigationImplementation = implementation;
    }

    public NavigationImplementation getImplementation() {
        return this.navigationImplementation;
    }

    public boolean isSuccessfullyInitialized() {
        return isSuccessfullyInitialized;
    }

    /**
     * Instead of using {@link ReactInstanceManager#addReactInstanceEventListener} directly, we should
     * use this method.  This prevents a race condition when checking against
     * {@link #isSuccessfullyInitialized}, because the listener callbacks are called asynchronously
     * from {@link ReactInstanceManager#setupReactContext(ReactApplicationContext)} via
     * {@link Handler#post(Runnable)}
     * <p>
     * Should not be called if {@link #isSuccessfullyInitialized()} returns true
     *
     * @param listener The listener you want to add. It will be automatically removed once called
     *
     * @throws IllegalStateException if already initialized (i.e. if {@link #isSuccessfullyInitialized()}
     *                               returns true
     */
    public void addInitializationListener(ReactInstanceManager.ReactInstanceEventListener listener) {
        if (isSuccessfullyInitialized()) {
            throw new IllegalStateException("Cannot add initialization listener, because React " +
                    "instance is already initialized");
        }
        initializationListeners.add(listener);
    }

    public void injectExposedActivities(List<ReactExposedActivityParams> exposedActivities) {
        // TODO(lmr): would it make sense to warn or throw here if it's already set?
        this.exposedActivities = exposedActivities;
    }

    /**
     * NOTE(lmr): In the future, we would like to replace this with an annotation parser that
     * generates this map based off of the `ReactExposedActivity` annotations. For now, this should
     * work well enough in the interim.
     */
    private List<ReactExposedActivityParams> exposedActivities;

    private final Map<String /* instance id */, WeakReference<ReactInterface>> componentsMap = new HashMap<>();

    private final Map<String /* instance id */, Boolean> dismissCloseBehaviorMap = new HashMap<>();

    private final Map<String /* name */, ReactScreenConfig> screenMap = new HashMap<>();

    ReactScreenConfig getOrDefault(String screenName) {
        ReactScreenConfig screen = screenMap.get(screenName);
        if (screen == null) {
            screen = ReactScreenConfig.EMPTY;
        }
        return screen;
    }

    public void registerComponent(ReactInterface component, String name) {
        componentsMap.put(name, new WeakReference<>(component));
    }

    public void unregisterComponent(String name) {
        componentsMap.remove(name);
    }

    /**
     * Returns an {@link Intent} used for launching an {@link Activity} exposed to React Native flows
     * based on the provided {@code key}. Will pass the provided {@code arguments} as {@link Intent}
     * extras. Activities should have been previously registered via {@code exposedActivities} in the
     * {@link ReactNavigationCoordinator} constructor.
     *
     * @see ReactExposedActivityParams#toIntent(Context, ReadableMap)
     */
    Intent intentForKey(Context context, String key, ReadableMap arguments) {
        for (ReactExposedActivityParams exposedActivity : exposedActivities) {
            if (exposedActivity.key().equals(key)) {
                return exposedActivity.toIntent(context, arguments);
            }
        }
        throw new IllegalArgumentException(
                String.format("Tried to push Activity with key '%s', but it could not be found", key));
    }

    ReactAwareActivityFacade activityFromId(String id) {
        WeakReference<ReactInterface> ref = componentsMap.get(id);
        return ref == null ? null : (ReactAwareActivityFacade) ref.get().getActivity();
    }

    ReactInterface componentFromId(String id) {
        WeakReference<ReactInterface> ref = componentsMap.get(id);
        return ref == null ? null : ref.get();
    }

    // If set to true, the Activity will be dismissed when its Toolbar NavigationIcon (home button) is clicked,
    // instead of performing the default behavior (finish)
    public void setDismissCloseBehavior(String id, boolean dismissClose) {
        dismissCloseBehaviorMap.put(id, dismissClose);
    }

    public boolean getDismissCloseBehavior(ReactInterface reactInterface) {
        String id = reactInterface.getInstanceId();
        Boolean dismissClose = dismissCloseBehaviorMap.get(id);
        return dismissClose != null && dismissClose;
    }

    public void registerScreen(
            String screenName,
            ReadableMap initialConfig,
            boolean waitForRender,
            String mode
    ) {
        screenMap.put(screenName, new ReactScreenConfig(
                initialConfig,
                waitForRender,
                ReactScreenMode.fromString(mode)
        ));
    }

//  public void setInitialConfigForModuleName(String screenName, ReadableMap config) {
//    screenMap.put(screenName, config);
//  }

    public ReadableMap getInitialConfigForModuleName(String screenName) {
        return getOrDefault(screenName).initialConfig;
    }

    public void start(final Application application) {
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(application)) {
            handleOverlayPermissionsMissing(application);
            return;
        }
        reactInstanceManager.createReactContextInBackground();
    }

    private static void handleOverlayPermissionsMissing(final Application application) {
        // RN needs "OVERLAY_PERMISSION" in dev mode in order to render the menu and redbox and stuff.
        // In dev we check if we have that permission (if we've made it here, we don't) and send the user
        // to the settings page with a toast indicating why.
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // Delaying an arbitrary 3 seconds so that the app can bootstrap, or else this intent doesn't
                // seem to really work.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                application.startActivity(intent);
                Toast.makeText(application, "This app must have permissions to draw over other apps in order to run React Native in dev mode", Toast.LENGTH_LONG).show();
            }
        }, APP_INITIALIZE_TOAST_DELAY);
    }
}
