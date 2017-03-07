package com.airbnb.android.react.navigation.example;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;

import com.airbnb.android.react.navigation.BundleBuilder;
import com.airbnb.android.react.navigation.ScreenCoordinatorComponent;

public class NativeFragment extends Fragment {
  private static final String ARG_COUNT = "count";

  static NativeFragment newInstance(int count) {
    NativeFragment frag = new NativeFragment();
    frag.setArguments(new BundleBuilder().putInt(ARG_COUNT, count).toBundle());
    return frag;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_native, container, false);
    final int count = getArguments().getInt(ARG_COUNT);

    ((TextView) view.findViewById(R.id.text)).setText("Fragment " + count);

    view.findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().pushScreen(newInstance(count + 1));
      }
    });

    view.findViewById(R.id.present).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().presentScreen(newInstance(0));
      }
    });

    view.findViewById(R.id.push_rn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().pushScreen("ScreenOne");
      }
    });

    view.findViewById(R.id.present_rn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().presentScreen("ScreenOne");
      }
    });

    view.findViewById(R.id.pop).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().pop();
      }
    });

    view.findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().dismiss();
      }
    });

    return view;
  }

  @Override
  public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
    return ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator().onCreateAnimation(this);
  }
}
