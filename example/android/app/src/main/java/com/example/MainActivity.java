package com.example;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;
import com.airbnb.android.react.navigation.ReactNativeActivity;
import com.airbnb.android.react.navigation.ReactAwareActivity;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

  Toolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initToolBar();
  }

  private void initToolBar() {
    toolbar = (Toolbar) findViewById(R.id.toolbar);
    toolbar.setTitle("App Toolbar");
    toolbar.setBackgroundColor(Color.BLUE);
    setActionBar(toolbar);
    toolbar.setNavigationOnClickListener(
      new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Toast.makeText(MainActivity.this, "clicking the toolbar!", Toast.LENGTH_SHORT).show();
        }
      }
    );

    Button btn = (Button)findViewById(R.id.button);

    btn.setText("Screen One");

    btn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = ReactNativeActivity.intent(MainActivity.this, "ScreenOne");
        startActivity(intent);
      }
    });

  }
}
