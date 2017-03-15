package com.airbnb.android.react.navigation.example;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.Toast;

import com.airbnb.android.react.navigation.BundleBuilder;
import com.airbnb.android.react.navigation.ScreenCoordinator;
import com.airbnb.android.react.navigation.ScreenCoordinatorComponent;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.PromiseImpl;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

public class NativeFragment extends Fragment {
  private static final String ARG_COUNT = "count";
  private static final String RESULT_TEXT = "text";

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

    Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    toolbar.setTitle("Fragment " + count);
    toolbar.setNavigationIcon(R.drawable.n2_ic_arrow_back_white);
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });

    view.findViewById(R.id.push).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getScreenCoordinator().pushScreen(newInstance(count + 1));
      }
    });

    view.findViewById(R.id.present).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Promise promise = new PromiseImpl(new Callback() {
          @Override
          public void invoke(Object... args) {
            WritableMap map = (WritableMap) args[0];
            ReadableMap payload = map.getMap("payload");
            if (payload != null) {
              String text = "Result: " + payload.getString(RESULT_TEXT);
              Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
            }
          }
        }, new Callback() {
          @Override
          public void invoke(Object... args) {
            Toast.makeText(getContext(), "Promise was rejected.", Toast.LENGTH_LONG).show();
          }
        });
        getScreenCoordinator().presentScreen(newInstance(0), promise);
      }
    });

    view.findViewById(R.id.push_rn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getScreenCoordinator().pushScreen("ScreenOne");
      }
    });

    view.findViewById(R.id.present_rn).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getScreenCoordinator().presentScreen("ScreenOne");
      }
    });

    view.findViewById(R.id.pop).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getScreenCoordinator().pop();
      }
    });

    final EditText editText = (EditText) view.findViewById(R.id.payload);
    view.findViewById(R.id.dismiss).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(RESULT_TEXT, editText.getText().toString());
        getScreenCoordinator().dismiss(Activity.RESULT_OK, payload);
      }
    });

    return view;
  }

  @Override
  public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
    return getScreenCoordinator().onCreateAnimation(transit, enter, nextAnim);
  }

  private ScreenCoordinator getScreenCoordinator() {
    return ((ScreenCoordinatorComponent) getActivity()).getScreenCoordinator();
  }
}
