package com.gb.ml.bitemap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Display schedules on a map
 */
public class SchedulesMapFragment extends Fragment {

    private GoogleMap mMap;

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
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mMap != null) {
            getChildFragmentManager().beginTransaction().remove(this).commit();
            mMap = null;
        }
    }
}
