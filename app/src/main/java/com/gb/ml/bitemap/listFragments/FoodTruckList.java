package com.gb.ml.bitemap.listFragments;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.gb.ml.bitemap.BitemapApplication;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.pojo.FoodTruck;

import java.io.IOException;

/**
 * List of food truck stores, sorted by food truck name
 */
public class FoodTruckList extends BaseList {

    @Override
    ListAdapter createListAdapter() {
        return new FoodTruckAdaptor();
    }

    private class FoodTruckAdaptor extends BaseAdapter {

        private BitemapApplication mAppContext;

        public FoodTruckAdaptor() {
            mAppContext = (BitemapApplication) getActivity().getApplicationContext();
        }


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
            Drawable mDrawable = null;
            try {
                String logoPath = "debugData/trucks_info/" + mFt.getLogo().getPath();
                mDrawable = Drawable
                        .createFromStream(getActivity().getAssets()
                                .open(logoPath), null);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mVh.mLogoView.setImageDrawable(mDrawable);
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
