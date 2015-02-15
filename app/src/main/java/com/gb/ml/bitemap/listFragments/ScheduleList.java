package com.gb.ml.bitemap.listFragments;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;

import com.gb.ml.bitemap.pojo.Schedule;

import java.util.List;

/**
 * List of events of all food trucks, sorted by event dates
 */
public class ScheduleList extends BaseList {

    @Override
    ListAdapter createListAdapter() {
        return new ScheduleAdaptor();
    }


    private class ScheduleAdaptor extends BaseAdapter {

        public List<Schedule> getScheduleList() {
            return mScheduleList;
        }

        public void setScheduleList(List<Schedule> scheduleList) {
            mScheduleList = scheduleList;
        }

        private List<Schedule> mScheduleList;


        @Override
        public int getCount() {
            return mScheduleList.size();
        }

        @Override
        public Object getItem(int position) {
            return mScheduleList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // how to draw a schedule in a list?
            return null;
        }
    }
}
