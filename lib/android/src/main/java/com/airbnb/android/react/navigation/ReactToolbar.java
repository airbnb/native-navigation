package com.airbnb.android.react.navigation;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;

// TODO(lmr): we might want to make this an abstract class and have a default implementation
public class ReactToolbar extends Toolbar {

  public ReactToolbar(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
  }

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
