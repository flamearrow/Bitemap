package com.gb.ml.bitemap;

import android.app.Application;
import android.util.Log;

import com.facebook.stetho.Stetho;

/**
 * Hold global foodtruck state
 */
public class BitemapApplication extends Application {

    public static final String TAG = "bitemapApplication";

    @Override
    public void onCreate() {
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

    }
}
