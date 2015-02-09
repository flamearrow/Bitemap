package com.gb.ml.bitemap.pojo;

import java.util.Calendar;

/**
 * Schedule corresponds to on entry in ScheduleActivity, need to be sortable by Time and Name
 */
public class Schedule implements Comparable<Schedule> {

    private Calendar mStart, mEnd;

    FoodTruck mTruck;

    String mAddress;
//    LagLng mLocation

    @Override
    public int compareTo(Schedule another) {
        int ret = 0;

        if (mStart.equals(another.mStart)) {
            if (mEnd.equals(another.mEnd)) {
                ret = mTruck.getName().compareTo(another.mTruck.getName());
            } else {
                ret = mEnd.before(another.mEnd) ? -1 : 1;
            }
        } else {
            ret = mStart.before(another.mStart) ? -1 : 1;
        }
        return ret;
    }

}
