package com.airbnb.android.react.navigation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.airbnb.android.R;

public class ReactModalActivity extends ReactAwareActivity {

    private static final String TAG = ReactNativeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // TODO: this restarts the app when the modal is recreated, we should recreate the React app associated with the modal and show it instead
        if (!ReactNavigationCoordinator.sharedInstance.isSuccessfullyInitialized()) {
            final Intent intent = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_react_native);

        final ReactNativeFragment fragment = ReactNativeFragment.newInstance(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .setAllowOptimization(true)
                .add(R.id.content, fragment)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
        supportPostponeEnterTransition();
    }

    @Override
    protected void onDestroy() {
        ((ReactNativeFragment) getSupportFragmentManager().findFragmentById(R.id.content))
                .getReactRootView()
                .unmountReactApplication();
        super.onDestroy();
    }
}
