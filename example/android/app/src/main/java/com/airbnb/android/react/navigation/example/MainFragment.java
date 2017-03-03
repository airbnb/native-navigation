package com.airbnb.android.react.navigation.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.airbnb.android.react.navigation.ReactNativeIntents;

public class MainFragment extends Fragment {

  static MainFragment newInstance() {
    return new MainFragment();
  }

  Toolbar toolbar;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);
    toolbar = (Toolbar) view.findViewById(R.id.toolbar);

    Button btnScreen = (Button) view.findViewById(R.id.button_screen);

    btnScreen.setText("Screen One");

    btnScreen.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ReactNativeIntents.presentScreen(getActivity(), "ScreenOne");
      }
    });

    Button btnTabs = (Button) view.findViewById(R.id.button_tab);

    btnTabs.setText("Tabs");

    btnTabs.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ReactNativeIntents.presentScreen(getActivity(), "TabScreen");
      }
    });

    initToolBar();
    return view;
  }

  private void initToolBar() {
    toolbar.setTitle("App Toolbar");
    toolbar.setBackgroundColor(Color.BLUE);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                Toast.makeText(getContext(), "clicking the toolbar!", Toast.LENGTH_SHORT).show();
              }
            }
    );
  }

}
