package com.gb.ml.bitemap.listFragments;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gb.ml.bitemap.BiteMapDebug;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.pojo.Schedule;

import java.io.IOException;
import java.text.SimpleDateFormat;
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

        private SimpleDateFormat mFormat;

        private List<Schedule> mScheduleList;

        private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";

        public List<Schedule> getScheduleList() {
            return mScheduleList;
        }

        public void setScheduleList(List<Schedule> scheduleList) {
            mScheduleList = scheduleList;
        }

        ScheduleAdaptor() {
            mScheduleList = BiteMapDebug.createDebugSchedules(getActivity());
            mFormat = new SimpleDateFormat(DATE_FORMAT);
        }


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
            final Schedule mS = mScheduleList.get(position);
            Drawable mDrawable = null;
            try {
                String logoPath = "debugData/trucks_info/" + mS.getTruck().getLogo().getPath();
                Log.d("mlgbLogo", logoPath);
                mDrawable = Drawable
                        .createFromStream(getActivity().getAssets()
                                .open(logoPath), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mVh.mLogoView.setImageDrawable(mDrawable);
            mVh.mFoodTruckNameView.setText(mS.getTruck().getName());
            mVh.mScheduleStart.setText(mFormat.format(mS.getStart().getTime()));
            mVh.mScheduleEnd.setText(mFormat.format(mS.getEnd().getTime()));

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
}
