package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.Intent;
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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.listFragments.ScheduleList;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.network.NetworkConstants;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
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

    private ScheduleList mScheduleList;

    private SchedulesMapFragment mSchedulesMapFragment;

    private Handler mHandler;

    private LayoutInflater mLayoutInflater;

    private LinearLayout mGallery;

    private ArrayList<Uri> mGalleryURIs;

    private Button mSeeAllButton;

    private ScheduleList.OnScheduleClickListener mOnScheduleClickListener
            = new ScheduleList.OnScheduleClickListener() {
        @Override
        public void onScheduleClicked(Schedule schedule) {
            switchMapList(null, schedule);
        }
    };

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
        mTruck = BitemapListDataHolder.getInstance().findFoodtruckFromId(mTruckId);
        setTitle(mTruck.getName());

        ((NetworkImageView) findViewById(R.id.detail_logo))
                .setImageUrl(mTruck.getFullUrlForLogo(), VolleyNetworkAccessor
                        .getInstance(this).getImageLoader());

        ((TextView) findViewById(R.id.detail_truck_name)).setText(mTruck.getName());
        ((TextView) findViewById(R.id.detail_category)).setText(mTruck.getCategory());
        ((TextView) findViewById(R.id.detail_website)).setText(mTruck.getUrl());
        mHandler = new Handler(getMainLooper());
        mLayoutInflater = LayoutInflater.from(this);
    }

    private void initializeFragments() {
        ArrayList<Schedule> schedules = BitemapDBConnector.getInstance(this)
                .getSchedulesForTruck(mTruckId);
        if (getFragmentManager().findFragmentByTag(MAP_FRAGMENT) == null) {
            final Bundle args = new Bundle();
            args.putParcelableArrayList(SchedulesMapFragment.SCHEDULES, schedules);
            mScheduleList = new ScheduleList();
            mScheduleList.setArguments(args);
            mSchedulesMapFragment = new SchedulesMapFragment();
            mSchedulesMapFragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .add(R.id.schedules_container, mScheduleList, LIST_FRAGMENT)
                    .add(R.id.schedules_container, mSchedulesMapFragment, MAP_FRAGMENT)
                    .commit();
        } else {
            mSchedulesMapFragment = (SchedulesMapFragment) getFragmentManager().findFragmentByTag(
                    MAP_FRAGMENT);
            mScheduleList = (ScheduleList) getFragmentManager().findFragmentByTag(
                    LIST_FRAGMENT);
        }
        mScheduleList.addOnScheduleClickListener(mOnScheduleClickListener);
        getFragmentManager().beginTransaction().hide(mSchedulesMapFragment).commit();
    }

    private void addImageToGallery(Bitmap newImage, final int index) {
        final ImageView iv = (ImageView) mLayoutInflater
                .inflate(R.layout.gallery_preview_item, mGallery, false);
        iv.setImageBitmap(newImage);
        iv.setId(mGallery.getChildCount());
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                i.putParcelableArrayListExtra(FoodTruckConstants.IMAGE_URIS, mGalleryURIs);
                i.putExtra(FoodTruckConstants.POSITION, index);
                startActivity(i);
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
        final Activity activity = this;
        new AsyncTask<Void, Void, ArrayList<Uri>>() {
            @Override
            protected ArrayList<Uri> doInBackground(Void... params) {
                return BitemapNetworkAccessor.getGalleryForTruck(mTruckId);
            }

            @Override
            protected void onPostExecute(ArrayList<Uri> uris) {
                mGalleryURIs = uris;
                for (int i = 0; i < uris.size(); i++) {
                    final int finalI = i;
                    VolleyNetworkAccessor.getInstance(activity).getImageLoader()
                            .get(NetworkConstants.SERVER_IP + uris.get(i).getPath(),
                                    new ImageLoader.ImageListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.w(TAG, "error loading image!");
                                        }

                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response,
                                                boolean isImmediate) {
                                            if (response.getBitmap() != null) {
                                                addImageToGallery(response.getBitmap(), finalI);
                                            }
                                            if (!mSeeAllButton.isEnabled()) {
                                                mSeeAllButton.setEnabled(true);
                                            }
                                        }
                                    });

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
                i.putParcelableArrayListExtra(FoodTruckConstants.IMAGE_URIS, mGalleryURIs);
                startActivity(i);
            }
        });
    }

    public void switchMapList(View v, final Schedule scheduleToHighlight) {
        if (mSchedulesMapFragment.isHidden()) {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .hide(mScheduleList)
                    .show(mSchedulesMapFragment).commit();
            if (scheduleToHighlight != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSchedulesMapFragment.enableMarkerForSchedule(scheduleToHighlight);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSchedulesMapFragment.zoomForAllMarkers();
                    }
                });
            }
        } else {
            getFragmentManager().beginTransaction().setCustomAnimations(
                    R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                    R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .hide(mSchedulesMapFragment)
                    .show(mScheduleList).commit();
            if (scheduleToHighlight != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSchedulesMapFragment.enableMarkerForSchedule(scheduleToHighlight);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mSchedulesMapFragment.zoomForAllMarkers();
                    }
                });
            }
        }
    }

    public void switchMapList(View v) {
        switchMapList(v, null);
    }
}
