package com.gb.ml.bitemap;

import android.os.Bundle;
import android.view.View;

import com.gb.ml.bitemap.listFragments.ScheduleList;

/**
 * Schedules of each food truck
 */
public class ScheduleActivity extends BitemapActionBarActivity {

    private ScheduleList mSchedulesList;

    private SchedulesMapFragment mSchedulesMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_schedules_title);
        setContentView(R.layout.activity_schedule);
        if (mSchedulesList == null) {
            mSchedulesList = new ScheduleList();
        }
        if (mSchedulesMap == null) {
            mSchedulesMap = new SchedulesMapFragment();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.schedule, mSchedulesMap).add(R.id.schedule, mSchedulesList)
                    .show(mSchedulesList).hide(mSchedulesMap).commit();
        }
    }

    public void switchMapList(View view) {
        if (mSchedulesList.isHidden()) {
            getSupportFragmentManager().beginTransaction().show(mSchedulesList).hide(mSchedulesMap)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction().show(mSchedulesMap).hide(mSchedulesList)
                    .commit();
        }
    }
}
