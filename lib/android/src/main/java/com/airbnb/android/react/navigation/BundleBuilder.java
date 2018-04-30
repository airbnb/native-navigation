package com.airbnb.android.react.navigation;

import android.os.Bundle;

/**
 * A Bundle that doesn't suck. Allows you to chain method calls as you'd expect.
 */
public class BundleBuilder extends ExtendableBundleBuilder<BundleBuilder> {

    public BundleBuilder() {
    }

    public BundleBuilder(Bundle bundle) {
        putAll(bundle);
    }
}
