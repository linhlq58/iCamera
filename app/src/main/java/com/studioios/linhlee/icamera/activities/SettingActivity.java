package com.studioios.linhlee.icamera.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kyleduo.switchbutton.SwitchButton;
import com.studioios.linhlee.icamera.MyApplication;
import com.studioios.linhlee.icamera.R;

/**
 * Created by lequy on 12/23/2016.
 */

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView backButton;
    private SwitchButton swTouchToTake;
    private SwitchButton swSound;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = MyApplication.getSharedPreferences();

        backButton = (TextView) findViewById(R.id.back_button);
        swTouchToTake = (SwitchButton) findViewById(R.id.sw_touch_to_take);
        swSound = (SwitchButton) findViewById(R.id.sw_sound);

        if (sharedPreferences.getBoolean("touch", false)) {
            swTouchToTake.setChecked(true);
        } else {
            swTouchToTake.setChecked(false);
        }

        if (sharedPreferences.getBoolean("sound", false)) {
            swSound.setChecked(true);
        } else {
            swSound.setChecked(false);
        }

        swTouchToTake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("touch", true).apply();
                } else {
                    sharedPreferences.edit().putBoolean("touch", false).apply();
                }
            }
        });

        swSound.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    sharedPreferences.edit().putBoolean("sound", true).apply();
                } else {
                    sharedPreferences.edit().putBoolean("sound", false).apply();
                }
            }
        });

        backButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                finish();
                break;
        }
    }
}
