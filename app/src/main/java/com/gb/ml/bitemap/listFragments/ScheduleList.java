package com.gb.ml.bitemap.listFragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.gb.ml.bitemap.BitemapListDataHolder;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.ScheduleActivity;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

public class ScheduleList extends BaseList {

    @Override
    ListAdapter createListAdapter() {
        return new ScheduleAdaptor();
    }

    private class ScheduleAdaptor extends BaseAdapter {

        @Override
        public int getCount() {
            return BitemapListDataHolder.getSchedules().size();
        }

        @Override
        public Object getItem(int position) {
            return BitemapListDataHolder.getSchedules().get(position);
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
                mVh = new ViewHolder(
                        (NetworkImageView) (convertView
                                .findViewById(R.id.food_truck_logo)),
                        (TextView) (convertView.findViewById(R.id.food_truck_name)),
                        (TextView) (convertView.findViewById(R.id.schedule_start)),
                        (TextView) (convertView.findViewById(R.id.schedule_end)));
                convertView.setTag(mVh);


            } else {
                mVh = (ViewHolder) convertView.getTag();
            }
            final Schedule mS = BitemapListDataHolder.getSchedules().get(position);
            final FoodTruck mFt = BitemapListDataHolder.findFoodtruckFromId(mS.getFoodtruckId());
            mVh.mLogoView.setImageUrl(mFt.getFullUrlForLogo(),
                    VolleyNetworkAccessor.getInstance(getActivity()).getImageLoader());
            mVh.mFoodTruckNameView.setText(
                    BitemapListDataHolder.findFoodtruckFromId(mS.getFoodtruckId()).getName());
            mVh.mScheduleStart.setText(mS.getStartTimeString());
            mVh.mScheduleEnd.setText(mS.getEndTimeString());

            return convertView;
        }

        class ViewHolder {

            NetworkImageView mLogoView;

            TextView mFoodTruckNameView, mScheduleStart, mScheduleEnd;

            ViewHolder(NetworkImageView logoView, TextView foodTruckNameView,
                    TextView scheduleStart,
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
            ((ScheduleActivity) getActivity())
                    .switchMapList(null, BitemapListDataHolder.getSchedules().get(position));
        }
    }
}
