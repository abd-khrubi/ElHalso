package com.example.project;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

public class Initial_distance_slider extends AppCompatActivity {
    TextView tv;
    TextView radiusIndicator;
    SeekBar sb;
    String radius_string = "The radius of the search is: ";
    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial_distance_slider);
        tv = (TextView)findViewById(R.id.tv);
        radiusIndicator = (TextView)findViewById(R.id.tv2);
        String rad = radius_string + "1 Km";
        radiusIndicator.setText(rad);
        sb = (SeekBar)findViewById(R.id.seekBar);
        sb.setMin(1);
        sb.setMax(100);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String rad = radius_string + progress + " Km";
                radiusIndicator.setText(rad);
                ((AppLoader)getApplicationContext()).setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        btn = (Button)findViewById(R.id.rad_btn);
        btn.setOnClickListener((View.OnClickListener) v -> {
            Intent intent;
            intent = new Intent((AppLoader)getApplicationContext(), MainMapActivity.class);
            startActivity(intent);
            finish();
        });
    }


}