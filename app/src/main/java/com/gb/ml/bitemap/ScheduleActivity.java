package com.gb.ml.bitemap;

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

    private ScheduleList.OnScheduleClickListener mOnScheduleClickListener;

    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";

    private static final String LIST_FRAGMENT = "LIST_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_schedules_title);
        setContentView(R.layout.activity_schedule);

        mOnScheduleClickListener = new ScheduleList.OnScheduleClickListener() {
            @Override
            public void onScheduleClicked(Schedule schedule) {
                switchMapList(null, schedule);
            }
        };
        initializeFragments();
        mHandler = new Handler(getMainLooper());
    }

    private void initializeFragments() {
        if (getFragmentManager().findFragmentByTag(LIST_FRAGMENT) == null) {
            final Bundle args = new Bundle();
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES,
                    BitemapListDataHolder.getInstance().getSchedules());
            mSchedulesList = new ScheduleList();
            mSchedulesMap = new SchedulesMapFragment();
            mSchedulesList.setArguments(args);
            mSchedulesMap.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.schedules_container, mSchedulesList, LIST_FRAGMENT)
                    .add(R.id.schedules_container, mSchedulesMap, MAP_FRAGMENT).commit();
        } else {
            mSchedulesList = (ScheduleList) getFragmentManager()
                    .findFragmentByTag(LIST_FRAGMENT);
            mSchedulesMap = (SchedulesMapFragment) getFragmentManager()
                    .findFragmentByTag(MAP_FRAGMENT);
        }
        mSchedulesList.addOnScheduleClickListener(mOnScheduleClickListener);
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
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSchedulesMap.zoomForAllMarkers();
                    }
                });
            }
        }
    }
}
