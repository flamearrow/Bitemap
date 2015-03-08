package com.gb.ml.bitemap;

import android.app.Application;
import android.util.Log;

/**
 * Hold global foodtruck state
 */
public class BitemapApplication extends Application {

    public static final String TAG = "bitemapApplication";

    @Override
    public void onCreate() {
        Log.d(TAG, "mlgb");
    }
}
