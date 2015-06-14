package com.gb.ml.bitemap;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.gb.ml.bitemap.listFragments.ScheduleList;
import com.gb.ml.bitemap.pojo.Schedule;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Schedules of each food truck
 */
public class ScheduleActivity extends BitemapActionBarActivity {

    private ScheduleList mSchedulesList;

    private SchedulesMapFragment mSchedulesMap;

    private Button mSwitchButton;

    private Handler mHandler;

    private ScheduleList.OnScheduleClickListener mOnScheduleClickListener;

    private GoogleMap.OnInfoWindowClickListener mOnInfoWindowClickListener;

    private int mButtonMoveDelta = -1;

    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";

    private static final String LIST_FRAGMENT = "LIST_FRAGMENT";

    private int currentDateSelection = 0;

    private int currentCategorySelection = 0;

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
        mOnInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String[] titles = marker.getTitle().split(" ");
                // single schedule
                if (titles.length == 1) {
                    final Schedule schedule = BitemapListDataHolder
                            .getInstance().findScheduleFromId(Long.valueOf(titles[0]));
                    final Intent i = new Intent(ScheduleActivity.this, DetailActivity.class);
                    i.putExtra(DetailActivity.FOODTRUCK_ID, schedule.getFoodtruckId());
                    startActivity(i);
                }
                // multiple schedules
                else {
                    Intent i = new Intent(ScheduleActivity.this, SubScheduleActivity.class);
                    i.putExtra(SubScheduleActivity.SCHEDULE_ID, titles);
                    startActivity(i);
                }
            }
        };
        initializeFragments();
        mHandler = new Handler(getMainLooper());
        mSwitchButton = (Button) findViewById(R.id.switch_button);
    }

    @Override
    protected void categorySelect() {
        final View layout = LayoutInflater.from(this)
                .inflate(R.layout.date_category_filter, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle("Pick a date and category..");

        final Spinner categorySpinner = (Spinner) layout.findViewById(R.id.category_spinner);
        final Spinner dateSpinner = (Spinner) layout.findViewById(R.id.date_spinner);

        categorySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                BitemapListDataHolder.getInstance().getCategory()));
        categorySpinner.setSelection(currentCategorySelection);
        dateSpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                createDateArrayList(BitemapListDataHolder.DAYS_OF_SCHEDULE_TO_GET)));
        dateSpinner.setSelection(currentDateSelection);

        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currentDateSelection = dateSpinner.getSelectedItemPosition();
                currentCategorySelection = categorySpinner.getSelectedItemPosition();

                String[] date = ((String) dateSpinner.getSelectedItem()).split(FoodTruckConstants.DASH);
                Calendar targetDay = Calendar.getInstance();
                targetDay.set(Calendar.YEAR, Integer.parseInt(date[0]));
                targetDay.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                targetDay.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
                ArrayList<Schedule> updatedSchedules = BitemapListDataHolder.getInstance()
                        .getSchedulesOnDayAndCategory(targetDay, currentCategorySelection);
                mSchedulesList.updateList(updatedSchedules);
                mSchedulesMap.updateList(updatedSchedules);
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private ArrayList<String> createDateArrayList(int days) {
        ArrayList<String> ret = new ArrayList<>(days);
        for (int i = 0; i <= days; i++) {
            Calendar day = getDay(i);
            if (BitemapListDataHolder.getInstance().hasSchedule(day)) {
                ret.add(getDate(day));
            }
        }
        return ret;
    }

    Calendar getDay(int position) {
        Calendar targetDay = Calendar.getInstance();
        targetDay.add(Calendar.DAY_OF_YEAR, position);
        return targetDay;
    }

    // return a date string in position days from today
    private String getDate(Calendar targetDay) {
        return targetDay.get(Calendar.YEAR) + FoodTruckConstants.DASH + (targetDay.get(Calendar.MONTH) + 1)
                + FoodTruckConstants.DASH + targetDay.get(
                Calendar.DAY_OF_MONTH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSchedulesMap.setOnInfoWindowClickListenerOnMap(mOnInfoWindowClickListener);
    }

    private void initializeFragments() {
        if (getFragmentManager().findFragmentByTag(LIST_FRAGMENT) == null) {
            final Bundle args = new Bundle();
            Calendar targetDay = Calendar.getInstance();
            targetDay.add(Calendar.DAY_OF_YEAR, 1);
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES,
                    BitemapListDataHolder.getInstance().getSchedulesOnDay(targetDay));
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

    private void moveSwitchLeft() {
        ObjectAnimator mover = ObjectAnimator
                .ofFloat(mSwitchButton, "translationX", 0, 0 - getButtonMoveDelta());
        mover.start();
    }

    private void moveSwitchRight() {
        ObjectAnimator mover = ObjectAnimator
                .ofFloat(mSwitchButton, "translationX", 0 - getButtonMoveDelta(), 0);
        mover.start();
    }

    private int getButtonMoveDelta() {
        if (mButtonMoveDelta < 0) {
            int[] location = new int[2];
            mSwitchButton.getLocationOnScreen(location);
            int locationX = location[0];
            int viewW = mSwitchButton.getWidth();
            int rootW = mSwitchButton.getRootView().getWidth();
            int margin = rootW - locationX - viewW;
            mButtonMoveDelta = locationX - margin;
        }
        return mButtonMoveDelta;
    }

    public void switchMapList(View view, final Schedule scheduleToHighlight) {
        if (mSchedulesList.isHidden()) {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .show(mSchedulesList).hide(mSchedulesMap).commit();
            moveSwitchRight();
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
            moveSwitchLeft();
        }
    }
}
