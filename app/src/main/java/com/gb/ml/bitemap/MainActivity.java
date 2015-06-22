package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int SPLASH_DELAY = 1000;

    private final BroadcastReceiver mOnInitCompeleteReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final boolean hasNetwork = intent.getBooleanExtra(BitemapListDataHolder.NETWORK, false);
            if (hasNetwork) {
                Log.d(TAG, "has network connection, issuing api requests and sync with local db");
            } else {
                Log.d(TAG, "No network connection, will load data from local database");
            }
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    startActivity(new Intent(MainActivity.this, ScheduleActivity.class));
//                }
//            }, SPLASH_DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BitemapListDataHolder.getInstance(getApplicationContext()).syncDatabaseWithSever();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mOnInitCompeleteReceiver,
                new IntentFilter(BitemapListDataHolder.INIT_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mOnInitCompeleteReceiver);
    }
}
