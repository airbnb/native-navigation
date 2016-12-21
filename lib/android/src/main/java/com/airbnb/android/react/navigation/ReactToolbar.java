package com.airbnb.android.react.navigation;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

public abstract class ReactToolbar extends Toolbar {
  public ReactToolbar(Context context) {
    super(context);
  }

  public void onCreateOptionsMenu(int i, Menu menu, MenuInflater menuInflater) {
    // TODO
  }

  public void setForegroundColor(int color) {
    // TODO
  }
}
