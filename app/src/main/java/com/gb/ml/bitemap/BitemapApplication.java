package com.gb.ml.bitemap;

import android.app.Application;
import android.os.AsyncTask;

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
            mFoodTrucks = foodTrucks;
            populateLists();
        }
    }

    // Pull data from DB to populate lists
    private void populateLists() {
//        mFoodTrucks = mDBConnector.getTrucks();
        mFoodTruckMap = new HashMap<>();
        for (FoodTruck ft : mFoodTrucks) {
            mFoodTruckMap.put(ft.getId(), ft);
        }
        mSchedules = mDBConnector.getSchedules(mFoodTruckMap);
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
