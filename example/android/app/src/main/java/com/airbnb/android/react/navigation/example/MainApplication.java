package com.airbnb.android.react.navigation.example;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.airbnb.android.react.navigation.NativeNavigationPackage;
import com.airbnb.android.react.navigation.ReactNavigationCoordinator;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.common.LifecycleState;
import com.facebook.react.shell.MainReactPackage;

public class MainApplication extends Application {

  private static final int APP_INITIALIZE_TOAST_DELAY = 3000;

  ReactInstanceManager manager;

  @Override
  public void onCreate() {
    super.onCreate();

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
      handleOverlayPermissionsMissing(this);
      return;
    }

    manager = ReactInstanceManager.builder()
            .setApplication(this)
            .setBundleAssetName("example/index.js") // file name to be used locally
            .setJSMainModuleName("example/index") // file name to be used for packager
            .addPackage(new MainReactPackage())
            .addPackage(new NativeNavigationPackage())
            .setUseDeveloperSupport(true)
            .setInitialLifecycleState(LifecycleState.BEFORE_RESUME)
            .build();

    Log.d("ReactNativeActivity", "application onCreate");

    ReactNavigationCoordinator.sharedInstance.injectReactInstanceManager(manager);

    manager.createReactContextInBackground();
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
