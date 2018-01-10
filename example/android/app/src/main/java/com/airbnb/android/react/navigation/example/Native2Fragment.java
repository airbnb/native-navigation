package com.airbnb.android.react.navigation.example;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.android.react.navigation.NativeScreenFactory;

public class Native2Fragment extends Fragment {
  static final NativeScreenFactory FACTORY = new NativeScreenFactory() {
    @NonNull
    @Override
    public Fragment newScreen(@Nullable Bundle props) {
      return new Native2Fragment();
    }
  };

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_native_2, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    toolbar.setTitle("Native Fragment");
    toolbar.setNavigationIcon(R.drawable.n2_ic_arrow_back_white);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });
  }
}
