package com.gb.ml.bitemap.pojo;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class Event {

    private Calendar mStart, mEnd;

    //    LatLng mLocation;
    private String mAddress;

    private List<FoodTruck> mAttendees;

    public Event(Calendar start, Calendar end, String address, List<FoodTruck> attendees) {
        mStart = start;
        mEnd = end;
        mAddress = address;
        mAttendees = attendees;
    }

    public Event(Calendar start, Calendar end, String address) {
        this(start, end, address, new LinkedList<FoodTruck>());
    }

    public Calendar getStart() {
        return mStart;
    }

    public void setStart(Calendar start) {
        mStart = start;
    }

    public Calendar getEnd() {
        return mEnd;
    }

    public void setEnd(Calendar end) {
        mEnd = end;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public List<FoodTruck> getAttendees() {
        return mAttendees;
    }

    public void setAttendees(List<FoodTruck> attendees) {
        mAttendees = attendees;
    }
}
