package com.gb.ml.bitemap;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.gb.ml.bitemap.database.BitemapDBConnector;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hold global foodtruck state
 */
public class BitemapApplication extends Application {

    public static final String INIT_COMPLETE = "INITIALIZATION_COMPLETE";

    private List<Schedule> mSchedules;

    private List<FoodTruck> mFoodTrucks;

    private Map<Long, FoodTruck> mFoodTruckMap;

    private BitemapDBConnector mDBConnector;

    @Override
    public void onCreate() {
        super.onCreate();
        initializeDB();
        syncDatabaseWithSever();
    }

    // poll from server and check with DB
    private void syncDatabaseWithSever() {
        new RequestFoodTrucks().execute();
    }

    private class RequestFoodTrucks extends AsyncTask<Void, Void, List<FoodTruck>> {

        @Override
        protected List<FoodTruck> doInBackground(Void... params) {
            return BitemapNetworkAccessor.getTrucks();
        }

        @Override
        protected void onPostExecute(List<FoodTruck> foodTrucks) {
            Log.d("mlgb", "food truck api returns with " + foodTrucks.size() + " trucks");
            mFoodTrucks = foodTrucks;
            populateLists();
        }
    }

    // Pull data from DB to populate lists
    private void populateLists() {
        mFoodTruckMap = new HashMap<>();
        for (FoodTruck ft : mFoodTrucks) {
            mFoodTruckMap.put(ft.getId(), ft);
        }
        mSchedules = mDBConnector.getSchedules(mFoodTruckMap);
        // initialization complete
        sendBroadcast(new Intent(INIT_COMPLETE));
    }

    private void initializeDB() {
        mDBConnector = BitemapDBConnector.getInstance(getApplicationContext());
        mDBConnector.updateDBIfNecessary();
    }

    public List<Schedule> getSchedules() {
        return mSchedules;
    }

    // TODO: this will cause a race condition when network doesn't return immediately
    public List<FoodTruck> getFoodTrucks() {
        return mFoodTrucks;
    }

    public FoodTruck findFoodtruckFromId(long foodtruckId) {
        return mFoodTruckMap.get(foodtruckId);
    }
}
