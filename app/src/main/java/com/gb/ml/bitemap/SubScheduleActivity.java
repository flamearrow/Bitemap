package com.gb.ml.bitemap;

import android.content.Intent;
import android.os.Bundle;

import com.gb.ml.bitemap.listFragments.ScheduleList;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.ArrayList;

public class SubScheduleActivity extends BitemapActionBarActivity {

    public static final String SCHEDULE_ID = "SCHEDULE_ID";

    private ScheduleList mScheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_schedules_title);
        setContentView(R.layout.activity_sub_schedule);

        final Bundle args = new Bundle();
        args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES,
                createSchedules(getIntent().getStringArrayExtra(SCHEDULE_ID)));
        mScheduleList = new ScheduleList();
        mScheduleList.setArguments(args);
        mScheduleList.addOnScheduleClickListener(new SubScheduleOnScheduleClickListener());
        getFragmentManager().beginTransaction()
                .add(R.id.schedules_container, mScheduleList, SCHEDULE_ID).commit();
    }

    private ArrayList<Schedule> createSchedules(String[] ids) {
        ArrayList<Schedule> schedules = new ArrayList<>();
        for (String s : ids) {
            schedules.add(BitemapListDataHolder.getsInstance(getApplicationContext()).findScheduleFromId(Long.valueOf(s)));
        }
        return schedules;
    }

    private class SubScheduleOnScheduleClickListener
            implements ScheduleList.OnScheduleClickListener {

        @Override
        public void onScheduleClicked(Schedule schedule) {
            // open the detail activity of this foodtruck
            final Intent i = new Intent(SubScheduleActivity.this, DetailActivity.class);
            i.putExtra(DetailActivity.FOODTRUCK_ID, schedule.getFoodtruckId());
            startActivity(i);
        }
    }
}
