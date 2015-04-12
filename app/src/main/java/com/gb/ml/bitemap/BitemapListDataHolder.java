package com.gb.ml.bitemap;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class initializes and hold global foodtruck state
 */
public class BitemapListDataHolder {

    public static final String TAG = "BimtemapListDataHolder";

    public static final String INIT_COMPLETE = "INITIALIZATION_COMPLETE";

    public static final String NETWORK = "NETWORK";

    private static ArrayList<Schedule> mSchedules;

    private static List<FoodTruck> mFoodTrucks;

    private static List<Event> mEvents;

    private static Map<Long, FoodTruck> mFoodTruckMap;


    private static int mListsReadyMode;

    private static boolean hasNetwork;

    private static final int SCHEDULE_LIST_READY = 1 << 0;

    private static final int FOODTRUCK_LIST_READY = 1 << 1;

    // TODO: need to include EVENT_LIST_READY when event api is ready
    // private static final int EVENT_LIST_READY = 1 << 2;

    private static final int ALL_LISTS_READY = SCHEDULE_LIST_READY | FOODTRUCK_LIST_READY;

    private static final int DAYS_OF_SCHEDULE_TO_GET = 7;

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
    public static void syncDatabaseWithSever(Context context) {
        Log.d(TAG, "syncDatabaseWithServer");
        if (hasNetworkConnection(context)) {
            Log.d(TAG, "has network connection");
            if (dbIsUpToDate()) {
                Log.d(TAG, "db is up to date! no more api will be issued");
                loadListsFromDB(context);
            } else {
                Log.d(TAG, "local db not up to date, issuing api requests...");
                new DownloadFoodTrucks(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new DownloadSchedules(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                // TODO: new DownloadEvents().execute();
            }
        } else {
            Log.d(TAG, "no network connection, load whatever we have in db");
            loadListsFromDB(context);
            // TODO: schedule a request when network connection is established
        }

    }

    // TODO: initialize a peer request check if local db is in sync with remote db
    private static boolean dbIsUpToDate() {
        return false;
    }

    // load whatever we have in db
    private static void loadListsFromDB(Context context) {
        new LoadFoodTrucksFromDB(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadSchedulesFromDB(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // TODO: new LoadEventsFromDB().execute();

    }

    private static boolean hasNetworkConnection(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        hasNetwork = (networkInfo != null && networkInfo.isConnected());
        return hasNetwork;
    }

    private static class DownloadFoodTrucks extends AsyncTask<Void, Void, List<FoodTruck>> {

        private Context mContext;

        DownloadFoodTrucks(Context context) {
            mContext = context;
        }

        @Override
        protected List<FoodTruck> doInBackground(Void... params) {
            return BitemapNetworkAccessor.getTrucks();
        }

        @Override
        protected void onPostExecute(List<FoodTruck> foodTrucks) {
            Log.d(TAG, "DownloadFoodTrucks returns " + foodTrucks.size() + " trucks");
            mFoodTrucks = foodTrucks;
            // TODO: do this in a separate thread
            BitemapDBConnector.getInstance(mContext).addTruckBatch(mFoodTrucks);
            addModeAndCheckReady(FOODTRUCK_LIST_READY, mContext);
        }
    }

    private static class LoadFoodTrucksFromDB extends AsyncTask<Void, Void, List<FoodTruck>> {

        Context mContext;

        LoadFoodTrucksFromDB(Context context) {
            mContext = context;
        }

        @Override
        protected List<FoodTruck> doInBackground(Void... params) {
            return BitemapDBConnector.getInstance(mContext).getTrucks();
        }

        @Override
        protected void onPostExecute(List<FoodTruck> foodTrucks) {
            Log.d(TAG, "LoadFoodTrucksFromDB returns " + foodTrucks.size() + " trucks");
            mFoodTrucks = foodTrucks;
            addModeAndCheckReady(FOODTRUCK_LIST_READY, mContext);
        }
    }

    private static class DownloadSchedules extends AsyncTask<Void, Void, ArrayList<Schedule>> {

        Context mContext;

        DownloadSchedules(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Schedule> doInBackground(Void... params) {
            return BitemapNetworkAccessor.getSchedulesForDays(DAYS_OF_SCHEDULE_TO_GET);
        }

        @Override
        protected void onPostExecute(ArrayList<Schedule> schedules) {
            Log.d(TAG, "DownloadSchedules returns " + schedules.size() + " schedules");
//            mSchedules = schedules;
            mSchedules = BitemapDebug.createDebugSchedules(mContext);
            // TODO: do this in a separate thread
            BitemapDBConnector.getInstance(mContext).addScheduleBatch(schedules);
            addModeAndCheckReady(SCHEDULE_LIST_READY, mContext);
        }
    }

    private static class LoadSchedulesFromDB extends AsyncTask<Void, Void, ArrayList<Schedule>> {

        Context mContext;

        LoadSchedulesFromDB(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Schedule> doInBackground(Void... params) {
            return BitemapDBConnector.getInstance(mContext).getSchedules();
        }

        @Override
        protected void onPostExecute(ArrayList<Schedule> schedules) {
            Log.d(TAG, "LoadSchedulesFromDB returns " + schedules.size() + " schedules");
            mSchedules = schedules;
            addModeAndCheckReady(SCHEDULE_LIST_READY, mContext);
        }
    }

    // Only send init_complete signal when all three lists are ready
    private static synchronized void addModeAndCheckReady(int mode, Context context) {
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
            context.sendBroadcast(completeIntent);
            // need to reset in case we re-request later
            mListsReadyMode = 0;
        } else {
            Log.d(TAG, "some lists are not ready, hold on sending init_complete signal");
        }
    }

    public static ArrayList<Schedule> getSchedules() {
        return mSchedules;
    }

    public static List<FoodTruck> getFoodTrucks() {
        return mFoodTrucks;
    }

    public static FoodTruck findFoodtruckFromId(long foodtruckId) {
        return mFoodTruckMap.get(foodtruckId);
    }
}
