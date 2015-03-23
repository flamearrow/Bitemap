package com.gb.ml.bitemap.listFragments;

import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gb.ml.bitemap.DetailActivity;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.List;

/**
 * A list to display upcoming schedules for a food truck
 *
 * @author ccen
 */
public class DetailScheduleList extends BaseList {


    private List<Schedule> mSchedules;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final long foodTruckId = getArguments().getLong(DetailActivity.FOODTRUCK_ID);
        mSchedules = BitemapDBConnector.getInstance(getActivity())
                .getSchedulesForTruck(foodTruckId);
        Log.d("mlgb", "found " + mSchedules.size() + "schedules");
    }

    @Override
    ListAdapter createListAdapter() {
        return new DetailScheduleAdaptor();
    }

    private class DetailScheduleAdaptor extends BaseAdapter {

        @Override
        public int getCount() {
            return mSchedules.size();
        }

        @Override
        public Object getItem(int position) {
            return mSchedules.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder mVh;
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity())
                        .inflate(R.layout.detail_list_item, parent, false);
                mVh = new ViewHolder((TextView) convertView.findViewById(R.id.schedule_date),
                        (TextView) convertView.findViewById(R.id.schedule_time));
                convertView.setTag(mVh);
            } else {
                mVh = (ViewHolder) convertView.getTag();
            }

            final Schedule s = mSchedules.get(position);
            mVh.mDateText.setText(s.getDateString());
            mVh.mTimeText.setText(s.getTimeString());
            return convertView;
        }

        class ViewHolder {

            TextView mDateText, mTimeText;

            ViewHolder(TextView dateText, TextView timeText) {
                mDateText = dateText;
                mTimeText = timeText;
            }
        }
    }
}