package com.airbnb.android.react.navigation;

import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;

import com.facebook.react.ReactInstanceManager;
import com.facebook.react.devsupport.DoubleTapReloadRecognizer;
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler;

public abstract class ReactAwareActivity extends AppCompatActivity
        implements ReactAwareActivityFacade, DefaultHardwareBackBtnHandler {

    private DoubleTapReloadRecognizer mDoubleTapReloadRecognizer = new DoubleTapReloadRecognizer();

    ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;

    ReactInstanceManager reactInstanceManager = reactNavigationCoordinator.getReactInstanceManager();

    @Override
    protected void onPause() {
        reactInstanceManager.onHostPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reactInstanceManager.onHostResume(this, this);
    }

    @Override
    public void invokeDefaultOnBackPressed() {
        onBackPressed();
    }

    /**
     * Schedules the shared element transition to be started immediately after the shared element has been measured and laid out within the activity's
     * view hierarchy. Some common places where it might make sense to call this method are:
     * <p>
     * (1) Inside a Fragment's onCreateView() method (if the shared element lives inside a Fragment hosted by the called Activity).
     * <p>
     * (2) Inside a Glide Callback object (if you need to wait for Glide to asynchronously load/scale a bitmap before the transition can begin).
     */
    public void scheduleStartPostponedTransition() {
        getWindow().getDecorView().getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        getWindow().getDecorView().getViewTreeObserver().removeOnPreDrawListener(this);
                        supportStartPostponedEnterTransition();
                        return true;
                    }
                });
    }

    boolean supportIsDestroyed() {
        return AndroidVersion.isAtLeastJellyBeanMR1() && isDestroyed();
    }

    boolean isSuccessfullyInitialized() {
        return reactNavigationCoordinator.isSuccessfullyInitialized();
    }

    NavigationImplementation getImplementation() {
        return reactNavigationCoordinator.getImplementation();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (/* BuildConfig.DEBUG && */keyCode == KeyEvent.KEYCODE_MENU) {
            // TODO(lmr): disable this in prod
            reactInstanceManager.getDevSupportManager().showDevOptionsDialog();
            return true;
        }
        if (keyCode == 0) { // this is the "backtick"
            // TODO(lmr): disable this in prod
            reactInstanceManager.getDevSupportManager().showDevOptionsDialog();
            return true;
        }
        if (mDoubleTapReloadRecognizer.didDoubleTapR(keyCode, getCurrentFocus())) {
            reactInstanceManager.getDevSupportManager().handleReloadJS();
        }

        return super.onKeyUp(keyCode, event);
    }
}
