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


    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";

    private static final String LIST_FRAGMENT = "LIST_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_schedules_title);
        setContentView(R.layout.activity_schedule);

        initializeFragments();
        mHandler = new Handler(getMainLooper());
    }

    private void initializeFragments() {
        if (getFragmentManager().findFragmentByTag(LIST_FRAGMENT) == null) {
            mSchedulesList = new ScheduleList();
            mSchedulesMap = new SchedulesMapFragment();
            final Bundle args = new Bundle();
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES,
                    BitemapListDataHolder.getSchedules());
            mSchedulesMap.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.schedule, mSchedulesList, LIST_FRAGMENT)
                    .add(R.id.schedule, mSchedulesMap, MAP_FRAGMENT).commit();
        } else {
            mSchedulesList = (ScheduleList) getFragmentManager().findFragmentByTag(LIST_FRAGMENT);
            mSchedulesMap = (SchedulesMapFragment) getFragmentManager()
                    .findFragmentByTag(MAP_FRAGMENT);
        }
        getFragmentManager().beginTransaction().hide(mSchedulesMap).commit();
    }

    public void switchMapList(View view) {
        switchMapList(view, null);
    }

    public void switchMapList(View view, final Schedule scheduleToHighlight) {
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
            if (scheduleToHighlight != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSchedulesMap.enableMarkerForSchedule(scheduleToHighlight);
                    }
                });
            }
        }
    }
}
