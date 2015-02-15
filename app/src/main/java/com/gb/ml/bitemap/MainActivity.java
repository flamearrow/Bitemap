package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends Activity {

    private static final int SPLASH_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, ScheduleActivity.class));
            }
        }, SPLASH_DELAY);
        initializeDB();
    }

    // debug data and insert into
    private void initializeDB() {
        DBConnector connector = DBConnector.getInstance(getApplicationContext());
        connector.open();
    }
}
