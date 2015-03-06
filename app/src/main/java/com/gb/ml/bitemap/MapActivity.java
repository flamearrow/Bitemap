package com.gb.ml.bitemap;

import android.app.Activity;
import android.os.Bundle;

import com.gb.ml.bitemap.pojo.Schedule;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity implements OnMapReadyCallback {

    public static final String SCHEDULE = "SCHEDULE";

    private BitemapApplication mBitemapApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        MapFragment mf = MapFragment.newInstance();
        getFragmentManager().beginTransaction().add(R.id.map_container, mf)
                .commit();
        mf.getMapAsync(this);
        mBitemapApplication = (BitemapApplication) getApplication();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        final Schedule schedule = getIntent().getParcelableExtra(SCHEDULE);
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(schedule.getLocation(), 13));
        googleMap.addMarker(
                new MarkerOptions()
                        .title(mBitemapApplication.findFoodtruckFromId(schedule.getFoodtruckId()).getName())
                        .snippet(schedule.getStartTimeString() + " to " + schedule
                                .getEndTimeString()).position(schedule.getLocation()));

    }
}
