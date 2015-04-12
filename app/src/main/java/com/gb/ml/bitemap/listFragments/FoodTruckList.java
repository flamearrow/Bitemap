package com.gb.ml.bitemap.listFragments;

import android.content.Intent;
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
import com.gb.ml.bitemap.network.NetworkConstants;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
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
            return BitemapListDataHolder.getFoodTrucks().size();
        }

        @Override
        public Object getItem(int position) {
            return BitemapListDataHolder.getFoodTrucks().get(position);
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
                mVh = new ViewHolder((NetworkImageView) convertView
                        .findViewById(R.id.food_truck_logo),
                        (TextView) convertView.findViewById(R.id.food_truck_name),
                        (TextView) convertView.findViewById(R.id.food_truck_category));
                convertView.setTag(mVh);
            } else {
                mVh = (ViewHolder) convertView.getTag();
            }

            final FoodTruck mFt = BitemapListDataHolder.getFoodTrucks().get(position);
            mVh.mLogoView.setImageUrl(mFt.getFullUrlForLogo(),
                    VolleyNetworkAccessor.getInstance(getActivity()).getImageLoader());
            mVh.mFoodTruckNameView.setText(mFt.getName());
            mVh.mCategoryView.setText(mFt.getCategory());
            return convertView;
        }

        class ViewHolder {

            ViewHolder(NetworkImageView logoView, TextView foodTruckNameView,
                    TextView categoryView) {
                mLogoView = logoView;
                mFoodTruckNameView = foodTruckNameView;
                mCategoryView = categoryView;
            }

            NetworkImageView mLogoView;

            TextView mFoodTruckNameView, mCategoryView;
        }

    }

    /**
     * should open detail of a foodtruck
     */
    @Override
    AdapterView.OnItemClickListener createItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final long truckId = BitemapListDataHolder.getFoodTrucks().get(position).getId();
                final Intent i = new Intent(getActivity(), DetailActivity.class);
                i.putExtra(DetailActivity.FOODTRUCK_ID, truckId);
                startActivity(i);
            }
        };
    }
}
