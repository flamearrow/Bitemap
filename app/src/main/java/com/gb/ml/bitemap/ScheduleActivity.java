package com.gb.ml.bitemap;

import android.app.Fragment;
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

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.schedule, getSchedulesList()).commit();
        }
    }

    private Fragment getSchedulesList() {
        if (mSchedulesList == null) {
            mSchedulesList = new ScheduleList();
        }
        return mSchedulesList;
    }

    private Fragment getSchedulesMap() {
        if (mSchedulesMap == null) {
            mSchedulesMap = new SchedulesMapFragment();
        }
        return mSchedulesMap;
    }

    public void switchMapList(View view) {
        if (!getSchedulesMap().isAdded()) {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .add(R.id.schedule, getSchedulesMap()).hide(mSchedulesList).commit();
        } else {
            if (mSchedulesList.isHidden()) {
                getFragmentManager().beginTransaction().setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                        .show(mSchedulesList).hide(mSchedulesMap).commit();
            } else {
                getFragmentManager().beginTransaction().setCustomAnimations(
                        R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                        .show(mSchedulesMap).hide(mSchedulesList).commit();
            }
        }
    }
}
