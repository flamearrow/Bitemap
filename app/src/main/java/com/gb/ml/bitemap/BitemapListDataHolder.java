package com.gb.ml.bitemap;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.network.NetworkConstants;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
import com.gb.ml.bitemap.pojo.Event;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class initializes and hold global foodtruck state
 */
public class BitemapListDataHolder {

    public static final String TAG = "BimtemapListDataHolder";

    public static final String INIT_COMPLETE = "INITIALIZATION_COMPLETE";

    public static final String NETWORK = "NETWORK";

    public static final int DAYS_OF_SCHEDULE_TO_GET = 7;

    private static final int SCHEDULE_LIST_READY = 1 << 0;

    private static final int FOODTRUCK_LIST_READY = 1 << 1;

    // TODO: need to include EVENT_LIST_READY when event api is ready
    // private static final int EVENT_LIST_READY = 1 << 2;

    private static final int CATEGORY_LIST_READY = 1 << 3;

    private static final int ALL_LISTS_READY = SCHEDULE_LIST_READY | FOODTRUCK_LIST_READY
            | CATEGORY_LIST_READY;

    private static BitemapListDataHolder mInstance;

    private ArrayList<String> mCategory;

    private ArrayList<Schedule> mSchedules;

    private List<FoodTruck> mFoodTrucks;

    private List<Event> mEvents;

    private Map<Long, FoodTruck> mFoodTruckMap;

    private Map<Long, Schedule> mScheduleMap;

    private int mListsReadyMode;

    private boolean hasNetwork;

    private Context mContext;

    private static class InstanceNotYetReadyException extends RuntimeException {

        public InstanceNotYetReadyException() {
            super("BitemapListDataHolder instance is not initialized");
        }
    }

    public synchronized static BitemapListDataHolder getInstance() {
        if (mInstance == null) {
            throw new InstanceNotYetReadyException();
        }
        return mInstance;
    }

    public synchronized static BitemapListDataHolder getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BitemapListDataHolder(context);
        }
        return mInstance;
    }

    private BitemapListDataHolder(Context context) {
        mContext = context;
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
    public void syncDatabaseWithSever() {
        Log.d(TAG, "syncDatabaseWithServer");
        if (hasNetworkConnection(mContext)) {
            Log.d(TAG, "has network connection");
            if (dbIsUpToDate()) {
                Log.d(TAG, "db is up to date! no more api will be issued");
                loadListsFromDB(mContext);
            } else {
                Log.d(TAG, "local db not up to date, issuing api requests...");
                new DownloadFoodTrucks(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new DownloadSchedules(mContext).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                // TODO: new DownloadEvents().execute();
                // request category
                requestCategory();
            }
        } else {
            Log.d(TAG, "no network connection, load whatever we have in db");
            loadListsFromDB(mContext);
            // TODO: schedule a request when network connection is established
        }

    }

    private void requestCategory() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                NetworkConstants.CATEGORIES, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    mCategory = new ArrayList<>();
                    mCategory.add("All");
                    for (int i = 0; i < response.length(); i++) {
                        mCategory.add(response.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addModeAndCheckReady(CATEGORY_LIST_READY, mContext);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.w(TAG, error.toString());
            }
        });
        VolleyNetworkAccessor.getInstance(mContext).addToRequestQueue(jsonArrayRequest);
    }

    public ArrayList<String> getCategory() {
        return mCategory;
    }

    // TODO: initialize a peer request check if local db is in sync with remote db
    private boolean dbIsUpToDate() {
        return false;
    }

    // load whatever we have in db
    private void loadListsFromDB(Context context) {
        new LoadFoodTrucksFromDB(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new LoadSchedulesFromDB(context).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        // TODO: new LoadEventsFromDB().execute();

    }

    private boolean hasNetworkConnection(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        hasNetwork = (networkInfo != null && networkInfo.isConnected());
        return hasNetwork;
    }

    private class DownloadFoodTrucks extends AsyncTask<Void, Void, List<FoodTruck>> {

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

    private class LoadFoodTrucksFromDB extends AsyncTask<Void, Void, List<FoodTruck>> {

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

    private class DownloadSchedules extends AsyncTask<Void, Void, ArrayList<Schedule>> {

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
            mSchedules = schedules;
            // TODO: do this in a separate thread
            BitemapDBConnector.getInstance(mContext).addScheduleBatch(schedules);
            addModeAndCheckReady(SCHEDULE_LIST_READY, mContext);
        }
    }

    private class LoadSchedulesFromDB extends AsyncTask<Void, Void, ArrayList<Schedule>> {

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
    private synchronized void addModeAndCheckReady(int mode, Context context) {
        mListsReadyMode |= mode;

        // All lists are ready, initialize the foodtruck map and send init_complete broadcast
        if (mListsReadyMode == ALL_LISTS_READY) {
            Log.d(TAG, "all lists ready, initializing foodTruckMap");
            mFoodTruckMap = new HashMap<>();
            for (FoodTruck ft : mFoodTrucks) {
                mFoodTruckMap.put(ft.getId(), ft);
            }
            mScheduleMap = new HashMap<>();
            for (Schedule s : mSchedules) {
                mScheduleMap.put(s.getId(), s);
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

    public ArrayList<Schedule> getSchedules() {
        return mSchedules;
    }

    // return schedules in {@link i} days from today
    public ArrayList<Schedule> getSchedulesOnDay(int i) {
        if (i == 0) {
            return mSchedules;
        }
        Calendar targetDay = Calendar.getInstance();
        targetDay.set(Calendar.DAY_OF_YEAR, targetDay.get(Calendar.DAY_OF_YEAR) + i - 1);
        ArrayList<Schedule> ret = new ArrayList<>();
        for (Schedule s : mSchedules) {
            if (sameDay(s.getStart(), targetDay)) {
                ret.add(s);
            }
        }
        return ret;
    }

    public ArrayList<Schedule> getSchedulesWithCategory(int i, ArrayList<Schedule> schedules) {
        // 0 is All
        if (i == 0) {
            return schedules;
        }
        String category = mCategory.get(i);
        ArrayList<Schedule> ret = new ArrayList<>();
        for (Schedule s : schedules) {
            if (findFoodtruckFromId(s.getFoodtruckId()).getCategory().contains(category)) {
                ret.add(s);
            }
        }
        return ret;
    }

    public ArrayList<Schedule> getSchedulesOnDayAndCategory(int dayIndex, int categoryIndex) {
        ArrayList<Schedule> ret = getSchedulesOnDay(dayIndex);
        return getSchedulesWithCategory(categoryIndex, ret);
    }

    private boolean sameDay(Calendar day1, Calendar day2) {
        return day1.get(Calendar.YEAR) == day2.get(Calendar.YEAR)
                && day1.get(Calendar.DAY_OF_YEAR) == day2.get(Calendar.DAY_OF_YEAR);
    }

    public List<FoodTruck> getFoodTrucks() {
        return mFoodTrucks;
    }

    public FoodTruck findFoodtruckFromId(long foodtruckId) {
        return mFoodTruckMap.get(foodtruckId);
    }

    public Schedule findScheduleFromId(long scheduleId) {
        return mScheduleMap.get(scheduleId);
    }

    public List<FoodTruck> getTruckCategory(String category) {
        if (category.equals(FoodTruckConstants.ALL)) {
            return mFoodTrucks;
        }
        List<FoodTruck> ret = new LinkedList<>();
        for (FoodTruck ft : mFoodTrucks) {
            if (ft.getCategory().contains(category)) {
                ret.add(ft);
            }
        }
        return ret;
    }
}
