package com.studioios.linhlee.icamera;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mz.Mz;

/**
 * Created by lequy on 12/23/2016.
 */

public class MyApplication extends Application {
    private static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Mz.initApplication(this, this.getApplicationContext().getPackageName());
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
