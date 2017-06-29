package com.spikes.umarells.features.splash;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.spikes.umarells.R;
import com.spikes.umarells.features.map.MapActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(MapActivity.getStartIntent(this));
    }
}
