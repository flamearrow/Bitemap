package com.gb.ml.bitemap;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.pojo.Event;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hold global foodtruck state
 */
public class BitemapApplication extends Application {

    public static final String TAG = "bitemapApplication";

    public static final String INIT_COMPLETE = "INITIALIZATION_COMPLETE";

    public static final String NETWORK = "NETWORK";

    private List<Schedule> mSchedules;

    private List<FoodTruck> mFoodTrucks;

    private List<Event> mEvents;

    private Map<Long, FoodTruck> mFoodTruckMap;

    private BitemapDBConnector mDBConnector;

    private int mListsReadyMode;

    private boolean hasNetwork;

    private static final int SCHEDULE_LIST_READY = 1 << 0;

    private static final int FOODTRUCK_LIST_READY = 1 << 1;

    // TODO: need to include EVENT_LIST_READY when event api is ready
    // private static final int EVENT_LIST_READY = 1 << 2;

    private static final int ALL_LISTS_READY = SCHEDULE_LIST_READY | FOODTRUCK_LIST_READY;

    private static final int DAYS_OF_SCHEDULE_TO_GET = 7;

    @Override
    public void onCreate() {
        super.onCreate();
        mDBConnector = BitemapDBConnector.getInstance(getApplicationContext());
        syncDatabaseWithSever();
    }

    /**
     * Initialize mSchedules, mFoodTrucks and mEvents, for all three lists, do the following:
     * ) if there’s no network
     * --) load whatever we have in DB
     * --) schedule a reinitialize when network is up
     * ) if there is network
     * --) issue a ‘peer’ request, check if current database is in sync with server
     * ----) if not, issue a full pull request, clear DB, reinitialize db with current data
     * ----) if yes, load whatever we have in DB
     */
    private void syncDatabaseWithSever() {
        Log.d(TAG, "syncDatabaseWithServer");
        if (hasNetworkConnection()) {
            Log.d(TAG, "has network connection");
            if (dbIsUpToDate()) {
                Log.d(TAG, "db is up to date! no more api will be issued");
                loadListsFromDB();
            } else {
                Log.d(TAG, "local db not up to date, issuing api requests...");
                new DownloadFoodTrucks().execute();
                new DownloadSchedules().execute();
                // TODO: new DownloadEvents().execute();
            }
        } else {
            Log.d(TAG, "no network connection, load whatever we have in db");
            loadListsFromDB();
            // TODO: schedule a request when network connection is established
        }
    }

    // TODO: initialize a peer request check if local db is in sync with remote db
    private boolean dbIsUpToDate() {
        return false;
    }

    // load whatever we have in db
    private void loadListsFromDB() {
        new LoadFoodTrucksFromDB().execute();
        new LoadSchedulesFromDB().execute();
        // TODO: new LoadEventsFromDB().execute();

    }

    private boolean hasNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        hasNetwork = (networkInfo != null && networkInfo.isConnected());
        return hasNetwork;
    }

    private class DownloadFoodTrucks extends AsyncTask<Void, Void, List<FoodTruck>> {

        @Override
        protected List<FoodTruck> doInBackground(Void... params) {
            return BitemapNetworkAccessor.getTrucks();
        }

        @Override
        protected void onPostExecute(List<FoodTruck> foodTrucks) {
            Log.d(TAG, "DownloadFoodTrucks returns " + foodTrucks.size() + " trucks");
            mFoodTrucks = foodTrucks;
            // TODO: do this in a separate thread
            mDBConnector.addTruckBatch(mFoodTrucks);
            addModeAndCheckReady(FOODTRUCK_LIST_READY);
        }
    }

    private class LoadFoodTrucksFromDB extends AsyncTask<Void, Void, List<FoodTruck>> {

        @Override
        protected List<FoodTruck> doInBackground(Void... params) {
            return mDBConnector.getTrucks();
        }

        @Override
        protected void onPostExecute(List<FoodTruck> foodTrucks) {
            Log.d(TAG, "LoadFoodTrucksFromDB returns " + foodTrucks.size() + " trucks");
            mFoodTrucks = foodTrucks;
            addModeAndCheckReady(FOODTRUCK_LIST_READY);
        }
    }

    private class DownloadSchedules extends AsyncTask<Void, Void, List<Schedule>> {

        @Override
        protected List<Schedule> doInBackground(Void... params) {
            return BitemapNetworkAccessor.getSchedulesForDays(DAYS_OF_SCHEDULE_TO_GET);
        }

        @Override
        protected void onPostExecute(List<Schedule> schedules) {
            Log.d(TAG, "DownloadSchedules returns " + schedules.size() + " schedules");
            mSchedules = schedules;
            // TODO: do this in a separate thread
            mDBConnector.addScheduleBatch(schedules);
            addModeAndCheckReady(SCHEDULE_LIST_READY);
        }
    }

    private class LoadSchedulesFromDB extends AsyncTask<Void, Void, List<Schedule>> {

        @Override
        protected List<Schedule> doInBackground(Void... params) {
            return mDBConnector.getSchedules();
        }

        @Override
        protected void onPostExecute(List<Schedule> schedules) {
            Log.d(TAG, "LoadSchedulesFromDB returns " + schedules.size() + " schedules");
            mSchedules = schedules;
            addModeAndCheckReady(SCHEDULE_LIST_READY);
        }
    }

    // Only send init_complete signal when all three lists are ready
    private synchronized void addModeAndCheckReady(int mode) {
        mListsReadyMode |= mode;

        // All lists are ready, initialize the foodtruck map and send init_complete broadcast
        if (mListsReadyMode == ALL_LISTS_READY) {
            Log.d(TAG, "all lists ready, initializing foodTruckMap");
            mFoodTruckMap = new HashMap<>();
            for (FoodTruck ft : mFoodTrucks) {
                mFoodTruckMap.put(ft.getId(), ft);
            }
            Log.d(TAG, "all done! send init_complete broadcast");
            Intent completeIntent = new Intent(INIT_COMPLETE);
            completeIntent.putExtra(NETWORK, hasNetwork);
            sendBroadcast(completeIntent);
            // need to reset in case we re-request later
            mListsReadyMode = 0;
            // This is a heavy call, one asynctask would be fired for each foodtruck
            for (FoodTruck ft : getFoodTrucks()) {
                ft.loadImage(getApplicationContext());
            }
        } else {
            Log.d(TAG, "some lists are not ready, hold on sending init_complete signal");
        }
    }

    public List<Schedule> getSchedules() {
        return mSchedules;
    }

    public List<FoodTruck> getFoodTrucks() {
        return mFoodTrucks;
    }

    public FoodTruck findFoodtruckFromId(long foodtruckId) {
        return mFoodTruckMap.get(foodtruckId);
    }
}
