package com.gb.ml.bitemap.database;

public class DBConstants {

    static final String SCHEDULES = "SCHEDULES";

    static final String ALL_FOODTRUCKS = "ALL_FOODTRUCKS";

    static final String DB_NAME = "foodtruck_db";

    static final String ID = "id";

    static final String NAME = "name";

    static final String CATEGORY = "category";

    static final String CATEGORY_DETAIL = "category_detail";

    static final String LOGO = "logo";

    static final String URL = "url";

    static final String FOOD_TRUCK_ID = "foodtruck_id";

    static final String START_TIME = "start_time";

    static final String END_TIME = "end_time";

    static final String ADDRESS = "address";

    static final String LAT = "lat";

    static final String LNG = "lng";

    static final String STREET_LAT = "street_lat";

    static final String STREET_LNG = "street_lng";

    static final String CREATE_SCHEDULES = "CREATE TABLE IF NOT EXISTS " + SCHEDULES +
            " (" + ID + " INTEGER primary key, "
            + FOOD_TRUCK_ID + " INTEGER, "
            + START_TIME + " INTEGER, "
            + END_TIME + " INTEGER, "
            + ADDRESS + " TEXT, "
            + LAT + " REAL, "
            + LNG + " REAL, "
            + STREET_LAT + " REAL, "
            + STREET_LNG + " REAL)";

    static final String CREATE_ALL_FOOD_TRUCKS = "CREATE TABLE IF NOT EXISTS " + ALL_FOODTRUCKS +
            " (" + ID + " INTEGER primary key, "
            + NAME + " TEXT, "
            + CATEGORY + " TEXT, "
            + CATEGORY_DETAIL + " TEXT, "
            + LOGO + " TEXT, "
            + URL + " TEXT)";

    static final String CLEAR_SCHEDULES = "DELETE FROM " + SCHEDULES;

    static final String CLEAR_ALL_FOOD_TRUCKS = "DELETE FROM " + ALL_FOODTRUCKS;
}
