package com.gb.ml.bitemap;

import android.app.Fragment;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gb.ml.bitemap.pojo.Schedule;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Display schedules on a map
 */
public class SchedulesMapFragment extends Fragment {

    private GoogleMap mGoogleMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedules_map_fragment, container, false);
        initializeMap();
        return v;
    }

    private void initializeMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getChildFragmentManager().findFragmentById(
                    R.id.schedules_map))
                    .getMap();
            mGoogleMap.setMyLocationEnabled(true);
            for (Schedule s : BitemapListDataHolder.getSchedules()) {
                mGoogleMap.addMarker(createMarker(s));
            }
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), 12));
        }
    }

    private MarkerOptions createMarker(Schedule schedule) {
        return new MarkerOptions()
                .title(BitemapListDataHolder.findFoodtruckFromId(schedule.getFoodtruckId())
                        .getName())
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
