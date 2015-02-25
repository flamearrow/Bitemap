package com.gb.ml.bitemap.listFragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gb.ml.bitemap.MapActivity;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.io.IOException;

/**
 * List of events of all food trucks, sorted by event dates
 */
public class ScheduleList extends BaseList {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    ListAdapter createListAdapter() {
        return new ScheduleAdaptor();
    }


    private class ScheduleAdaptor extends BaseAdapter {

        @Override
        public int getCount() {
            return mAppContext.getSchedules().size();
        }

        @Override
        public Object getItem(int position) {
            return mAppContext.getSchedules().get(position);
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
                        .inflate(R.layout.schedule_item, parent, false);

                mVh = new ViewHolder((ImageView) (convertView.findViewById(R.id.food_truck_logo)),
                        (TextView) (convertView.findViewById(R.id.food_truck_name)),
                        (TextView) (convertView.findViewById(R.id.schedule_start)),
                        (TextView) (convertView.findViewById(R.id.schedule_end)));
                convertView.setTag(mVh);


            } else {
                mVh = (ViewHolder) convertView.getTag();
            }
            final Schedule mS = mAppContext.getSchedules().get(position);
            final FoodTruck mFt = mAppContext.findFoodtruckFromId(mS.getFoodtruckId());
            if (mFt.getLogoBm() == null) {
                mVh.mLogoView.setImageBitmap(mDefaultBm);
            } else {
                mVh.mLogoView.setImageBitmap(mFt.getLogoBm());
            }
            mVh.mFoodTruckNameView.setText(
                    mAppContext.findFoodtruckFromId(mS.getFoodtruckId()).getName());
            mVh.mScheduleStart.setText(mS.getStartTimeString());
            mVh.mScheduleEnd.setText(mS.getEndTimeString());

            return convertView;
        }

        class ViewHolder {

            ImageView mLogoView;

            TextView mFoodTruckNameView, mScheduleStart, mScheduleEnd;

            ViewHolder(ImageView logoView, TextView foodTruckNameView, TextView scheduleStart,
                    TextView scheduleEnd) {
                mLogoView = logoView;
                mFoodTruckNameView = foodTruckNameView;
                mScheduleStart = scheduleStart;
                mScheduleEnd = scheduleEnd;
            }
        }
    }


    @Override
    AdapterView.OnItemClickListener createItemClickListener() {
        return new ScheduleItemClickListener();
    }

    private class ScheduleItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Intent i = new Intent(getActivity(), MapActivity.class);
            i.putExtra(MapActivity.SCHEDULE, mAppContext.getSchedules().get(position));
            startActivity(i);
        }
    }
}
