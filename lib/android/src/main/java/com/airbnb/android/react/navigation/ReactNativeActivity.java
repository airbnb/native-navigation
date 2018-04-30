package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.airbnb.android.R;
import com.facebook.react.bridge.ReadableMap;

public class ReactNativeActivity extends ReactAwareActivity {

    private static final String TAG = ReactNativeActivity.class.getSimpleName();

    private final Handler handler = new Handler();

    private ReactNavigationCoordinator reactNavigationCoordinator = ReactNavigationCoordinator.sharedInstance;

    private ReadableMap initialConfig = ConversionUtil.EMPTY_MAP;

    private ReactNativeFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        String moduleName = getIntent().getStringExtra(ReactNativeIntents.EXTRA_MODULE_NAME);
        initialConfig = reactNavigationCoordinator.getInitialConfigForModuleName(moduleName);

        setContentView(R.layout.activity_react_native);
        fragment = ReactNativeFragment.newInstance(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .setAllowOptimization(true)
                .add(R.id.content, fragment)
                .commitNow();
        getSupportFragmentManager().executePendingTransactions();
        supportPostponeEnterTransition();
    }
}
