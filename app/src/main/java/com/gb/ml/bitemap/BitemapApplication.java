package com.gb.ml.bitemap;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Hold global foodtruck state
 */
public class BitemapApplication extends Application {

    public static final String TAG = "bitemapApplication";

    @Override
    public void onCreate() {
        Fabric.with(this, new Crashlytics());
        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());

    }
}
