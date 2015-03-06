package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gb.ml.bitemap.pojo.Schedule;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Display schedules on a map
 */
public class SchedulesMapFragment extends Fragment {

    private GoogleMap mMap;

    private BitemapApplication mBitemapApplication;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mBitemapApplication = (BitemapApplication) activity.getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedules_map_fragment, container, false);
        initializeMap();
        return v;
    }

    private void initializeMap() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.schedules_map)).getMap();
            mMap.setMyLocationEnabled(true);
            for (Schedule s : mBitemapApplication.getSchedules()) {
                mMap.addMarker(createMarker(s));
            }
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), 12));
        }
    }

    private MarkerOptions createMarker(Schedule schedule) {
        return new MarkerOptions()
                .title(mBitemapApplication.findFoodtruckFromId(schedule.getFoodtruckId()).getName())
                .snippet(schedule.getStartTimeString() + " to " + schedule.getEndTimeString())
                .position(schedule.getLocation());
    }

    private LatLng getMyLocation() {
        LocationManager service = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Location location = service.getLastKnownLocation(provider);
        return new LatLng(location.getLatitude(), location.getLongitude());
    }
}
