package com.airbnb.android.react.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public interface NativeScreenFactory {
  /**
   * Creates a new {@linkplain Fragment native screen} with {@code props}.
   */
  @NonNull
  Fragment newScreen(@Nullable Bundle props);
}
