package com.gb.ml.bitemap.listFragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.pojo.FoodTruck;


/**
 * List of food truck stores, sorted by food truck name
 */
public class FoodTruckList extends BaseList {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    ListAdapter createListAdapter() {
        return new FoodTruckAdaptor();
    }

    private class FoodTruckAdaptor extends BaseAdapter {


        @Override
        public int getCount() {
            return mAppContext.getFoodTrucks().size();
        }

        @Override
        public Object getItem(int position) {
            return mAppContext.getFoodTrucks().get(position);
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
                        .inflate(R.layout.food_truck_item, parent, false);

                mVh = new ViewHolder((ImageView) convertView.findViewById(R.id.food_truck_logo),
                        (TextView) convertView.findViewById(R.id.food_truck_name),
                        (TextView) convertView.findViewById(R.id.food_truck_category));
                convertView.setTag(mVh);
            } else {
                mVh = (ViewHolder) convertView.getTag();
            }

            final FoodTruck mFt = mAppContext.getFoodTrucks().get(position);

            if (mFt.getLogoBm() == null) {
                mVh.mLogoView.setImageBitmap(mDefaultBm);
            } else {
                mVh.mLogoView.setImageBitmap(mFt.getLogoBm());
            }
            mVh.mFoodTruckNameView.setText(mFt.getName());
            mVh.mCategoryView.setText(mFt.getCategory());
            return convertView;
        }

        class ViewHolder {

            ViewHolder(ImageView logoView, TextView foodTruckNameView, TextView categoryView) {
                mLogoView = logoView;
                mFoodTruckNameView = foodTruckNameView;
                mCategoryView = categoryView;
            }

            ImageView mLogoView;

            TextView mFoodTruckNameView, mCategoryView;
        }

    }
}
