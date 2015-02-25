package com.gb.ml.bitemap.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Schedule corresponds to on entry in ScheduleActivity, need to be sortable by Time and Name
 */
public class Schedule implements Comparable<Schedule>, Parcelable {

    private long mId;

    private Calendar mStart, mEnd;

    private long mFoodtruckId;

    private String mAddress;

    private LatLng mLocation;

    private LatLng mStreetLocation;

    private static SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Schedule(long id, Calendar start, Calendar end, long foodtruckId, String address,
            double lat, double lng, double streetLat, double streetLng) {
        mId = id;
        mStart = start;
        mEnd = end;
        mFoodtruckId = foodtruckId;
        mAddress = address;
        mLocation = new LatLng(lat, lng);
        mStreetLocation = new LatLng(streetLat, streetLng);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeLong(mFoodtruckId);
        dest.writeLong(mStart.getTimeInMillis());
        dest.writeLong(mEnd.getTimeInMillis());
        dest.writeString(mAddress);
        dest.writeDouble(mLocation.latitude);
        dest.writeDouble(mLocation.longitude);
        dest.writeDouble(mStreetLocation.latitude);
        dest.writeDouble(mStreetLocation.longitude);
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel source) {
            final long id = source.readLong();
            final long foodtruckId = source.readLong();
            final long start = source.readLong();
            final long end = source.readLong();
            final String address = source.readString();
            final double lat = source.readDouble();
            final double lng = source.readDouble();
            final double streetLat = source.readDouble();
            final double streetLng = source.readDouble();

            final Calendar cStart = Calendar.getInstance();
            final Calendar cEnd = Calendar.getInstance();
            cStart.setTimeInMillis(start);
            cEnd.setTimeInMillis(end);

            return new Builder().setId(id).setStart(cStart).setEnd(cEnd).setFoodtruckId(foodtruckId)
                    .setAddress(address).setLat(lat).setLng(lng).setStreetLat(streetLat)
                    .setStreetLng(streetLng).build();
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    public static class Builder {

        private long mId, mFoodtruckId;

        private double mLat, mLng, mStreetLat, mStreetLng;

        private Calendar mStart, mEnd;

        private String mAddress;

        public Builder setAddress(String address) {
            mAddress = address;
            return this;
        }

        public Builder setId(long id) {
            mId = id;
            return this;
        }

        public Builder setFoodtruckId(long foodtruckId) {
            mFoodtruckId = foodtruckId;
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

        public Schedule build() {
            return new Schedule(mId, mStart, mEnd, mFoodtruckId, mAddress, mLat, mLng, mStreetLat,
                    mStreetLng);
        }
    }

    @Override
    public int compareTo(Schedule another) {
        if (mStart.equals(another.mStart)) {
            return mEnd.before(another.mEnd) ? -1 : 1;
        } else {
            return mStart.before(another.mStart) ? -1 : 1;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Schedule: \n");
        sb.append(" Id: " + mId + "\n");
        sb.append(" FoodTruckId: " + mFoodtruckId + "\n");
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

    public long getFoodtruckId() {
        return mFoodtruckId;
    }

    public String getAddress() {
        return mAddress;
    }

    public long getId() {
        return mId;
    }

    public double getLat() {
        return mLocation.latitude;
    }

    public double getLng() {
        return mLocation.longitude;
    }

    public double getStreetLat() {
        return mStreetLocation.latitude;
    }

    public double getStreetLng() {
        return mStreetLocation.longitude;
    }

    public LatLng getLocation() {
        return mLocation;
    }

    public LatLng getStreetLocation() {
        return mStreetLocation;
    }

    public String getStartTimeString() {
        return mFormat.format(getStart().getTime());
    }

    public String getEndTimeString() {
        return mFormat.format(getEnd().getTime());
    }
}
