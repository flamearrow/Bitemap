package com.gb.ml.bitemap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Access database
 */
public class DBConnector {

    private static final String SCHEDULES = "SCHEDULES";

    private static final String ALL_FOODTRUCKS = "ALL_FOODTRUCKS";

    private static final String DB_NAME = "foodtruck_db";

    private static DBConnector mInstance;

    private SQLiteDatabase mDb;

    private DBOpenHelper mHelper;

    private static final String TAG = "mlDB";

    private DBConnector(Context context) {
        mHelper = new DBOpenHelper(context, DB_NAME, null, 1);
    }

    private class DBOpenHelper extends SQLiteOpenHelper {

        public DBOpenHelper(Context context, String name,
                SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // no foreign key support yet
            final String createFoodtrucks = "CREATE TABLE IF NOT EXISTS " + ALL_FOODTRUCKS +
                    " (id INTEGER primary key, name TEXT, category TEXT, category_detail TEXT, "
                    + "logo TEXT, url TEXT)";
            final String createSchedules = "CREATE TABLE IF NOT EXISTS " + SCHEDULES +
                    " (id INTEGER primary key, foodtruck_id INTEGER, start_time TEXT, end_time TEXT, "
                    + "address TEXT, lat REAL, lng REAL, street_lat REAL, street_lng REAL)";
            db.execSQL(createFoodtrucks);
            db.execSQL(createSchedules);
            Log.d(TAG, "db created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // nothing
        }
    }

    public static synchronized DBConnector getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new DBConnector(context);
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
}
