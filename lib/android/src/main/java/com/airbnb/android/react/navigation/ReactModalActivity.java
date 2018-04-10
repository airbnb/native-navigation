package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.airbnb.android.R;
import com.facebook.react.bridge.ReadableMap;

public class ReactModalActivity extends ReactAwareActivity {
    private static final String TAG = ReactNativeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        String moduleName = getIntent().getStringExtra(ReactNativeIntents.EXTRA_MODULE_NAME);

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
    public void finish() {
        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
