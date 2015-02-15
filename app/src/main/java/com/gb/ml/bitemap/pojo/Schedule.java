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

    public Schedule(Calendar start, Calendar end, FoodTruck truck, String address) {
        mStart = start;
        mEnd = end;
        mTruck = truck;
        mAddress = address;
    }

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule: \n");
        sb.append(" FoodTruck: " + mTruck.getName() + "\n");
        sb.append(" startTime: " + mStart + "\n");
        sb.append(" endTime: " + mEnd + "\n");
        sb.append(" Address: " + mAddress + "\n");
        return sb.toString();
    }

    public Calendar getStart() {
        return mStart;
    }

    public Calendar getEnd() {
        return mEnd;
    }

    public FoodTruck getTruck() {
        return mTruck;
    }

    public String getAddress() {
        return mAddress;
    }
}
