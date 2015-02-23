package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends Activity {

    private static final int SPLASH_DELAY = 1000;

    private final BroadcastReceiver mOnInitCompeleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean hasNetwork = intent.getBooleanExtra(BitemapApplication.NETWORK, false);
            if (hasNetwork) {
                Toast.makeText(MainActivity.this,
                        "has network connection, issuing api requests and sync with local db",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this,
                        "No network connection, will load data from local database",
                        Toast.LENGTH_SHORT).show();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(MainActivity.this, AllFoodTrucksActivity.class));
                }
            }, SPLASH_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mOnInitCompeleteReceiver,
                new IntentFilter(BitemapApplication.INIT_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mOnInitCompeleteReceiver);
    }
}
