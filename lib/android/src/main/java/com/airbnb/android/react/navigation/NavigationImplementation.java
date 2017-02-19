package com.airbnb.android.react.navigation;

import android.view.Menu;
import android.view.MenuItem;
import com.facebook.react.bridge.ReadableMap;

import java.util.Map;

interface NavigationImplementation {
  void reconcileNavigationProperties(
      ReactInterface component,
      ReactToolbar toolbar,
//      Map<String, Object> properties
      ReadableMap properties
  );

  void createOptionsMenu(
      ReactInterface component,
      ReactToolbar toolbar,
//      Map<String, Object> properties,
      ReadableMap properties,
      Menu menu
  );

  boolean onOptionsItemSelected(
      ReactInterface component,
      ReactToolbar toolbar,
//      Map<String, Object> properties,
      ReadableMap properties,
      MenuItem item
  );
}

