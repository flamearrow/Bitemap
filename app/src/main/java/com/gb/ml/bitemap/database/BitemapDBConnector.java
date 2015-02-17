package com.gb.ml.bitemap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gb.ml.bitemap.BitemapDebug;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.net.URI;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.gb.ml.bitemap.database.DBConstants.*;

/**
 * Access database
 */
public class BitemapDBConnector {


    private static BitemapDBConnector mInstance;

    private SQLiteDatabase mDb;

    private DBOpenHelper mHelper;

    private static final String TAG = "mlDB";

    private Context mContext;

    private BitemapDBConnector(Context context) {
        mHelper = new DBOpenHelper(context, DB_NAME, null, 1);
        mContext = context;
    }

    private class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name,
                SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // no foreign key support yet
            db.execSQL(CREATE_ALL_FOOD_TRUCKS);
            db.execSQL(CREATE_SCHEDULES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // nothing
        }
    }

    public static synchronized BitemapDBConnector getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BitemapDBConnector(context);
        }
        return mInstance;
    }

    public void open() {
        mDb = mHelper.getWritableDatabase();
    }

    public void close() {
        if (mDb != null) {
            mDb.close();
        }
    }

    private void addTruck(long id, String name, String category, String category_detail, URI logo,
            String url) {
        ContentValues newTruck = new ContentValues();
        newTruck.put(ID, id);
        newTruck.put(NAME, name);
        newTruck.put(CATEGORY, category);
        newTruck.put(CATEGORY_DETAIL, category_detail);
        newTruck.put(LOGO, logo.getPath());
        newTruck.put(URL, url);
        open();
        mDb.insert(ALL_FOODTRUCKS, null, newTruck);
        close();
    }

    private void addSchedule(long id, long foodTruckId, Calendar startTime, Calendar endTime,
            String address, double lat, double lng, double streetLat, double streetLng) {
        ContentValues newSchedule = new ContentValues();
        // all ids are 0 now, we should ignore adding id at the moment, sqlite will generate an PK
        // newSchedule.put(ID, id);
        newSchedule.put(FOOD_TRUCK_ID, foodTruckId);

        newSchedule.put(START_TIME, startTime.getTimeInMillis());
        newSchedule.put(END_TIME, endTime.getTimeInMillis());
        newSchedule.put(ADDRESS, address);
        newSchedule.put(LAT, lat);
        newSchedule.put(LNG, lng);
        newSchedule.put(STREET_LAT, streetLat);
        newSchedule.put(STREET_LNG, streetLng);
        open();
        mDb.insertWithOnConflict(SCHEDULES, null, newSchedule, SQLiteDatabase.CONFLICT_IGNORE);
        close();
    }

    public void addTruck(FoodTruck foodTruck) {
        addTruck(foodTruck.getId(), foodTruck.getName(), foodTruck.getCategory(),
                foodTruck.getCategoryDetail(), foodTruck.getLogo(), foodTruck.getUrl());
    }

    public void addSchedule(Schedule schedule) {
        addSchedule(schedule.getId(), schedule.getFoodtruckId(), schedule.getStart(),
                schedule.getEnd(), schedule.getAddress(), schedule.getLat(), schedule.getLng(),
                schedule.getStreetLat(), schedule.getStreetLng());
    }

    public List<Schedule> getSchedules(Map<Long, FoodTruck> foodTruckMap) {
        //(id INTEGER primary key, foodtruck_id INTEGER, start_time INTEGER, end_time INTEGER,
        // address TEXT, lat REAL, lng REAL, street_lat REAL, street_lng REAL)
        open();
        Cursor result = mDb.query(SCHEDULES, null, null, null, null, null, null);
        List<Schedule> ret = new LinkedList<>();
        while (result.moveToNext()) {
            final long mId = result.getLong(0);
            final long mFoodTruckId = result.getLong(1);
            final long mStartTime = result.getLong(2);
            final long mEndTime = result.getLong(3);
            final String mAddress = result.getString(4);
            final double mLat = result.getDouble(5);
            final double mLng = result.getDouble(6);
            final double mStreetLat = result.getDouble(7);
            final double mStreetLng = result.getDouble(8);
            final Calendar mStart = Calendar.getInstance();
            mStart.setTimeInMillis(mStartTime);
            final Calendar mEnd = Calendar.getInstance();
            mEnd.setTimeInMillis(mEndTime);
            final Schedule newSchedule = new Schedule.Builder().setId(mId)
                    .setFoodtruckId(mFoodTruckId).setStart(mStart).setEnd(mEnd)
                    .setAddress(mAddress).setLat(mLat).setLng(mLng).setStreetLat(mStreetLat)
                    .setStreetLng(mStreetLng).build();
            ret.add(newSchedule);
        }
        close();
        return ret;
    }

    public List<FoodTruck> getTrucks() {
        // (id INTEGER primary key, name TEXT, category TEXT, category_detail TEXT, logo TEXT,
        // url TEXT)
        open();
        Cursor result = mDb.query(ALL_FOODTRUCKS, null, null, null, null, null, null);
        List<FoodTruck> ret = new LinkedList<>();
        while (result.moveToNext()) {
            final long mId = result.getInt(0);
            final String mName = result.getString(1);
            final String mCategory = result.getString(2);
            final String mCategoryDetail = result.getString(3);
            final String mLogo = result.getString(4);
            final String mUrl = result.getString(5);
            final FoodTruck newFt = new FoodTruck.Builder().setId(mId).setName(mName).setCategory(
                    mCategory).setCategoryDetail(mCategoryDetail).setLogo(URI.create(mLogo)).setUrl(
                    mUrl).build();
            ret.add(newFt);
        }
        close();
        return ret;
    }

    /**
     * fire a network request to update DB if necessary
     */
    public void updateDBIfNecessary() {
        // TODO: add an initial network request here to populate DB first, currently use debug data
        initializeDebugData();
    }

    /**
     * load test data into database
     */
    public void initializeDebugData() {

        for (Schedule schedule : BitemapDebug.createDebugSchedules(mContext)) {
            addSchedule(schedule);
        }

        for (FoodTruck truck : BitemapDebug.createDebugFoodTrucks(mContext)) {
            addTruck(truck);
        }
    }

    /**
     * Clear all data rows from database, but keep the tables
     */
    public void clearDatabase() {
        open();
        mDb.execSQL(CLEAR_SCHEDULES);
        mDb.execSQL(CLEAR_ALL_FOOD_TRUCKS);
        close();
    }
}
