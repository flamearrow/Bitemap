package com.gb.ml.bitemap;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.listFragments.DetailScheduleList;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.ArrayList;

/**
 * Displays Category, names, events for a particular food truck
 * Should contain logo, name, category, a list of upcoming schedules and gallery
 */
public class DetailActivity extends ActionBarActivity {

    public static final String FOODTRUCK_ID = "FOODTRUCK_ID";

    private static final String TAG = "DETAILACTIVITY";

    private long mTruckId;

    private FoodTruck mTruck;

    private Bitmap mDefaultBm;

    private DetailScheduleList mDetailScheduleList;

    private SchedulesMapFragment mSchedulesMapFragment;

    private BroadcastReceiver mUpdateLogoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((ImageView) findViewById(R.id.detail_logo)).setImageBitmap(mTruck.getLogoBm());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mTruckId = getIntent().getLongExtra(FOODTRUCK_ID, -1);
        if (mTruckId == -1) {
            Log.w(TAG, "failed to get foodtruck id");
        }
        mTruck = BitemapListDataHolder.findFoodtruckFromId(mTruckId);
        setTitle(mTruck.getName());
        if (savedInstanceState == null) {
            ArrayList<Schedule> schedules = BitemapDBConnector.getInstance(this)
                    .getSchedulesForTruck(mTruckId);
            final Bundle args = new Bundle();
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES, schedules);
            mDetailScheduleList = new DetailScheduleList();
            mDetailScheduleList.setArguments(args);
            mSchedulesMapFragment = new SchedulesMapFragment();
            mSchedulesMapFragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.activity_detail, mDetailScheduleList)
                    .add(R.id.activity_detail, mSchedulesMapFragment).hide(mSchedulesMapFragment)
                    .commit();
        }
        if (mDefaultBm == null) {
            mDefaultBm = BitmapFactory.decodeResource(getResources(), R.drawable.foreveralone);
        }

        if (mTruck.getLogoBm() == null) {
            Log.d("mlgb", "no logo");
            ((ImageView) findViewById(R.id.detail_logo)).setImageBitmap(mDefaultBm);
        } else {
            Log.d("mlgb", "has logo");
            ((ImageView) findViewById(R.id.detail_logo)).setImageBitmap(mTruck.getLogoBm());
        }
        ((TextView) findViewById(R.id.detail_truck_name)).setText(mTruck.getName());
        ((TextView) findViewById(R.id.detail_category)).setText(mTruck.getCategory());
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mUpdateLogoReceiver, new IntentFilter(FoodTruck.LOGO_DOWNLOADED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mUpdateLogoReceiver);
    }

    public void switchMap(View v) {
        if (mSchedulesMapFragment.isHidden()) {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .hide(mDetailScheduleList)
                    .show(mSchedulesMapFragment).commit();
        } else {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .hide(mSchedulesMapFragment)
                    .show(mDetailScheduleList).commit();
        }
    }
}
