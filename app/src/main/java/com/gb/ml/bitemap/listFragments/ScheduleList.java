package com.gb.ml.bitemap.listFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.gb.ml.bitemap.BitemapListDataHolder;
import com.gb.ml.bitemap.DetailActivity;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.SchedulesMapFragment;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * A list to display schedules
 *
 * @author ccen
 */
public class ScheduleList extends BaseList {

    private ArrayList<Schedule> mSchedules;

    private Set<OnScheduleClickListener> mOnScheduleClickListenerList;

    public ScheduleList() {
        mOnScheduleClickListenerList = new HashSet<>();
    }

    public int getSize() {
        return mOnScheduleClickListenerList.size();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSchedules = getArguments().getParcelableArrayList(SchedulesMapFragment.SCHEDULES);
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
            final Schedule mS = mSchedules.get(position);
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

    public void addOnScheduleClickListener(OnScheduleClickListener listener) {
        mOnScheduleClickListenerList.add(listener);
    }

    @Override
    AdapterView.OnItemClickListener createItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (OnScheduleClickListener listener : mOnScheduleClickListenerList) {
                    listener.onScheduleClicked(mSchedules.get(position));
                }
            }
        };
    }

    public interface OnScheduleClickListener {

        public void onScheduleClicked(Schedule schedule);
    }
}
