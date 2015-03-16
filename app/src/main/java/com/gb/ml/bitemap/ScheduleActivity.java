package com.gb.ml.bitemap;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.gb.ml.bitemap.listFragments.ScheduleList;
import com.gb.ml.bitemap.pojo.Schedule;

/**
 * Schedules of each food truck
 */
public class ScheduleActivity extends BitemapActionBarActivity {

    private ScheduleList mSchedulesList;

    private SchedulesMapFragment mSchedulesMap;

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_schedules_title);
        setContentView(R.layout.activity_schedule);

        getSchedulesList();
        getSchedulesMap();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(R.id.schedule, getSchedulesList()).commit();
        }
        mHandler = new Handler(getMainLooper());
    }

    private Fragment getSchedulesList() {
        if (mSchedulesList == null) {
            mSchedulesList = new ScheduleList();
        }
        return mSchedulesList;
    }

    private SchedulesMapFragment getSchedulesMap() {
        if (mSchedulesMap == null) {
            mSchedulesMap = new SchedulesMapFragment();
            final Bundle args = new Bundle();
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES,
                    BitemapListDataHolder.getSchedules());
            mSchedulesMap.setArguments(args);
        }
        return mSchedulesMap;
    }

    public void switchMapList(View view) {
        switchMapList(view, null);
    }

    public void switchMapList(View view, final Schedule scheduleToHighlight) {
        if (!getSchedulesMap().isAdded()) {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .add(R.id.schedule, getSchedulesMap()).hide(mSchedulesList).commit();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (scheduleToHighlight != null) {
                        getSchedulesMap().enableMarkerForSchedule(scheduleToHighlight);
                    }
                }
            });
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
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (scheduleToHighlight != null) {
                            getSchedulesMap().enableMarkerForSchedule(scheduleToHighlight);
                        }
                    }
                });
            }
        }
    }
}
