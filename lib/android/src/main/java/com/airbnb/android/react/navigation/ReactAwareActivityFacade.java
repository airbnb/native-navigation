package com.airbnb.android.react.navigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

interface ReactAwareActivityFacade {
    void runOnUiThread(Runnable action);
    Context getBaseContext();
    ReactToolbarFacade getSupportActionBar();
    void startActivityForResult(Intent intent, int requestCode, Bundle bundle);
    void startActivityForResult(Intent intent, int requestCode);
    void setResult(int resultCode, Intent resultIntent);
    void finish();
}
