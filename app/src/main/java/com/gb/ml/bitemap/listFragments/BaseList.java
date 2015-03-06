package com.gb.ml.bitemap.listFragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gb.ml.bitemap.BitemapApplication;
import com.gb.ml.bitemap.R;
import com.gb.ml.bitemap.pojo.FoodTruck;

/**
 * abstract fragment to display a list of customizable items from DB
 */
public abstract class BaseList extends ListFragment {

    protected BitemapApplication mAppContext;

    protected Bitmap mDefaultBm;

    private BroadcastReceiver mLogoCompletedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDefaultBm = BitmapFactory.decodeResource(getResources(), R.drawable.foreveralone);
        getActivity().registerReceiver(mLogoCompletedReceiver,
                new IntentFilter(FoodTruck.LOGO_DOWNLOADED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mLogoCompletedReceiver);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAppContext = (BitemapApplication) activity.getApplicationContext();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView listView = getListView();

        listView.setOnItemClickListener(createItemClickListener());
        setListAdapter(createListAdapter());
    }

    /**
     * Create DB adaptor for the list, decides how the list is displayed
     *
     * @return The list adaptor created from DB, should have it's own formatted defined
     */
    abstract ListAdapter createListAdapter();

    /**
     * Call back when list item is clicked
     *
     * @return ItemClickListener fro list items
     */
    AdapterView.OnItemClickListener createItemClickListener() {

        return new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "you're clicking item " + position,
                        Toast.LENGTH_LONG).show();
            }
        };
    }


}
