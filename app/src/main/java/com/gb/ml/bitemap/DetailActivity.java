package com.gb.ml.bitemap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.listFragments.DetailScheduleList;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
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

    private Handler mHandler;

    private LayoutInflater mLayoutInflater;

    private BroadcastReceiver mUpdateLogoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((ImageView) findViewById(R.id.detail_logo)).setImageBitmap(mTruck.getLogoBm());
        }
    };

    private LinearLayout mGallery;

    private ArrayList<Uri> mGalleryURIs;

    private Button mSeeAllButton;

    private static final String MAP_FRAGMENT = "MAP_FRAGMENT";

    private static final String LIST_FRAGMENT = "LIST_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mTruckId = getIntent().getLongExtra(FOODTRUCK_ID, -1);
        if (mTruckId == -1) {
            Log.w(TAG, "failed to get foodtruck id");
        }
        if (mDefaultBm == null) {
            mDefaultBm = BitmapFactory.decodeResource(getResources(), R.drawable.foreveralone);
        }
        mGallery = (LinearLayout) findViewById(R.id.gallery_id);
        initializeFragments();
        initializeGallery();
        initializeSeeAllButton();
        mTruck = BitemapListDataHolder.findFoodtruckFromId(mTruckId);
        setTitle(mTruck.getName());

        if (mTruck.getLogoBm() == null) {
            ((ImageView) findViewById(R.id.detail_logo)).setImageBitmap(mDefaultBm);
        } else {
            ((ImageView) findViewById(R.id.detail_logo)).setImageBitmap(mTruck.getLogoBm());
        }
        ((TextView) findViewById(R.id.detail_truck_name)).setText(mTruck.getName());
        ((TextView) findViewById(R.id.detail_category)).setText(mTruck.getCategory());

        mHandler = new Handler(getMainLooper());
        mLayoutInflater = LayoutInflater.from(this);
    }

    private void initializeFragments() {
        ArrayList<Schedule> schedules = BitemapDBConnector.getInstance(this)
                .getSchedulesForTruck(mTruckId);
        if (getFragmentManager().findFragmentByTag(MAP_FRAGMENT) == null) {
            final Bundle args = new Bundle();
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES, schedules);
            mDetailScheduleList = new DetailScheduleList();
            mDetailScheduleList.setArguments(args);
            mSchedulesMapFragment = new SchedulesMapFragment();
            mSchedulesMapFragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.schedules_container, mDetailScheduleList, LIST_FRAGMENT)
                    .add(R.id.schedules_container, mSchedulesMapFragment, MAP_FRAGMENT)
                    .commit();
        } else {
            mSchedulesMapFragment = (SchedulesMapFragment) getFragmentManager().findFragmentByTag(
                    MAP_FRAGMENT);
            mDetailScheduleList = (DetailScheduleList) getFragmentManager().findFragmentByTag(
                    LIST_FRAGMENT);
        }
        getFragmentManager().beginTransaction().hide(mSchedulesMapFragment).commit();
    }

    private void addImageToGallery(Bitmap newImage) {
        final ImageView iv = (ImageView) mLayoutInflater
                .inflate(R.layout.gallery_preview_item, mGallery, false);
        iv.setImageBitmap(newImage);
        iv.setId(mGallery.getChildCount());
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mlgb", "you're clicking an view: " + v.getId());
                Log.d("mlgb", "you're clicking an image view: " + iv.getId());
            }
        });
        mGallery.addView(iv);
    }

    /**
     * *) Initialize an api request to pull all URIs, get the size of URI list
     * *) Populate the gallery with size number of mDefaultBm
     * *) Issue an image pull request for all URIs
     */
    private void initializeGallery() {
        new AsyncTask<Void, Void, ArrayList<Uri>>() {
            @Override
            protected ArrayList<Uri> doInBackground(Void... params) {
                return BitemapNetworkAccessor.getGalleryForTruck(mTruckId);
            }

            @Override
            protected void onPostExecute(ArrayList<Uri> uris) {
                mGalleryURIs = uris;
                for (final Uri uri : uris) {
                    new AsyncTask<Void, Void, Bitmap>() {
                        @Override
                        protected Bitmap doInBackground(Void... params) {
                            return BitemapNetworkAccessor.getThumbnailBitmapFromURI(uri);
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            addImageToGallery(bitmap);
                            if (!mSeeAllButton.isEnabled()) {
                                mSeeAllButton.setEnabled(true);
                            }
                        }
                    }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void initializeSeeAllButton() {
        mSeeAllButton = (Button) findViewById(R.id.btn_see_all_pics);
        mSeeAllButton.setEnabled(false);
        mSeeAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GridViewGalleryActivity.class);
                i.putParcelableArrayListExtra(GridViewGalleryActivity.IMAGE_URIS, mGalleryURIs);
                startActivity(i);
            }
        });
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

    public void switchMapList(View v, final Schedule scheduleToHighlight) {
        if (mSchedulesMapFragment.isHidden()) {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .hide(mDetailScheduleList)
                    .show(mSchedulesMapFragment).commit();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (scheduleToHighlight != null) {
                        mSchedulesMapFragment.enableMarkerForSchedule(scheduleToHighlight);
                    }
                }
            });
        } else {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .hide(mSchedulesMapFragment)
                    .show(mDetailScheduleList).commit();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (scheduleToHighlight != null) {
                        mSchedulesMapFragment.enableMarkerForSchedule(scheduleToHighlight);
                    }
                }
            });
        }
    }

    public void switchMapList(View v) {
        switchMapList(v, null);
    }
}
