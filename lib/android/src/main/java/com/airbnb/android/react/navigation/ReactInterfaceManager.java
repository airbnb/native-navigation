package com.airbnb.android.react.navigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.SparseArray;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;

import java.util.Collections;
import java.util.Map;

import static com.airbnb.android.react.navigation.ReactNativeIntents.EXTRA_CODE;
import static com.airbnb.android.react.navigation.ScreenCoordinator.EXTRA_PAYLOAD;

public final class ReactInterfaceManager {

    private final ReactInterface component;

    private final ReactAwareActivityFacade activity;

    private static int puuid = 1;

    private static final SparseArray<Promise> resultPromises = new SparseArray<>();

    public ReactInterfaceManager(ReactInterface component) {
        this.component = component;
        activity = (ReactAwareActivityFacade) component.getActivity();
    }

    /**
     * This method is semantically equivalent to `startActivityForResult`, except it instead resolves
     * a promise when the activity finishes, rather than calling `onActivityResult`. <p> We are
     * supporting the startActivityForResult behavior with RN by using promises. We register each one
     * in a dictionary with an id, then on onActivityResult we pull out the promise object by id and
     * resolve/reject it.
     */
    static void startActivityWithPromise(final Activity activity, final Intent intent,
                                         Promise promise, final ReadableMap options) {
        final int requestCode = puuid++;
        resultPromises.put(requestCode, promise);
        if (AndroidVersion.isAtLeastLollipop() && ReactNativeUtils.isReactNativeIntent(intent)) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Bundle optionsBundle =
                            ReactNativeIntents.getSharedElementOptionsBundle(activity, intent, options);
                    activity.startActivityForResult(intent, requestCode, optionsBundle);
                }
            });
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Needed for tests
     */
    public static int getPuuid() {
        return puuid;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        deliverPromise(requestCode, resultCode, data);
        if (isDismiss(data)) {
            activity.setResult(resultCode, getResultIntent(data));
            activity.finish();
        }
    }

    /**
     * Returns a result Intent to be forwarded to the next Activity via onActivityResult(). Only set
     * EXTRA_IS_DISMISS to true if this Activity is not a modal, since they work as a navigation
     * boundary. That means a Navigation.dismiss call will finish all activities up to a modal,
     * including the modal.
     */
    private Intent getResultIntent(Intent data) {
        return new Intent()
                .putExtras(data.getExtras())
                .putExtra(ReactNativeIntents.EXTRA_IS_DISMISS, component.isDismissible());
    }

    private void deliverPromise(int requestCode, int resultCode, Intent data) {
        Promise promise = getAndRemovePromise(requestCode);
        if (promise != null) {
            Map<String, Object> payload = getPayloadFromIntent(data);
            Map<String, Object> newPayload =
                    MapBuilder.of(EXTRA_CODE, resultCode, EXTRA_PAYLOAD, payload);
            promise.resolve(ConversionUtil.toWritableMap(newPayload));
        }
    }

    private static boolean isDismiss(Intent data) {
        return data != null && data.getBooleanExtra(ReactNativeIntents.EXTRA_IS_DISMISS, false);
    }

    private Promise getAndRemovePromise(int requestCode) {
        if (resultPromises.indexOfKey(requestCode) < 0) {
            return null;
        }
        Promise promise = resultPromises.get(requestCode);
        resultPromises.remove(requestCode);
        return promise;
    }

    private static Map<String, Object> getPayloadFromIntent(Intent data) {
        if (data != null && data.hasExtra(EXTRA_PAYLOAD)) {
            //noinspection unchecked
            return (Map<String, Object>) data.getSerializableExtra(EXTRA_PAYLOAD);
        } else {
            return Collections.emptyMap();
        }
    }
}
