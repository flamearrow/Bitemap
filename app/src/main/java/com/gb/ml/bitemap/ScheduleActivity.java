package com.gb.ml.bitemap;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gb.ml.bitemap.listFragments.ScheduleList;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.List;

/**
 * Schedules of each food truck
 */
public class ScheduleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.schedule, new ScheduleList())
                    .commit();
        }
    }
}

