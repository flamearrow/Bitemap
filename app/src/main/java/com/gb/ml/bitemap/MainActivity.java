package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;


public class MainActivity extends Activity {

    private static final int SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, AllFoodTrucksActivity.class));
            }
        }, SPLASH_DELAY);
    }
}
