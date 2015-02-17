package com.gb.ml.bitemap.pojo;

import java.util.Calendar;

/**
 * Schedule corresponds to on entry in ScheduleActivity, need to be sortable by Time and Name
 */
public class Schedule implements Comparable<Schedule> {

    private long mId;

    private double mLat;

    private double mLng;

    private double mStreetLat;

    private double mStreetLng;

    private Calendar mStart, mEnd;

    private FoodTruck mTruck;

    private String mAddress;
//    LagLng mLocation

    private Schedule(long id, Calendar start, Calendar end, FoodTruck truck, String address,
            double lat, double lng, double streetLat, double streetLng) {
        mId = id;
        mStart = start;
        mEnd = end;
        mTruck = truck;
        mAddress = address;
        mLat = lat;
        mLng = lng;
        mStreetLat = streetLat;
        mStreetLng = streetLng;
    }

    public static class Builder {

        private long mId;

        private double mLat, mLng, mStreetLat, mStreetLng;

        private Calendar mStart, mEnd;

        private FoodTruck mTruck;

        private String mAddress;

        public Builder setAddress(String address) {
            mAddress = address;
            return this;
        }

        public Builder setId(long id) {
            mId = id;
            return this;
        }

        public Builder setLat(double lat) {
            mLat = lat;
            return this;
        }

        public Builder setLng(double lng) {
            mLng = lng;
            return this;
        }

        public Builder setStreetLat(double streetLat) {
            mStreetLat = streetLat;
            return this;
        }

        public Builder setStreetLng(double streetLng) {
            mStreetLng = streetLng;
            return this;
        }

        public Builder setStart(Calendar start) {
            mStart = start;
            return this;
        }

        public Builder setEnd(Calendar end) {
            mEnd = end;
            return this;
        }

        public Builder setTruck(FoodTruck truck) {
            mTruck = truck;
            return this;
        }

        public Schedule build() {
            return new Schedule(mId, mStart, mEnd, mTruck, mAddress, mLat, mLng, mStreetLat,
                    mStreetLng);
        }
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

    public long getId() {
        return mId;
    }

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }

    public double getStreetLat() {
        return mStreetLat;
    }

    public double getStreetLng() {
        return mStreetLng;
    }
}
