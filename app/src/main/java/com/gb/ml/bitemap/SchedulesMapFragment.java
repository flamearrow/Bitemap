package com.gb.ml.bitemap;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Display schedules on a map
 */
public class SchedulesMapFragment extends Fragment implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "SchedulesMapFragment";

    public static final String SCHEDULES = "SCHEDULES";

    private GoogleMap mGoogleMap;

    private List<Schedule> mScheduleList;

    private Bitmap mDefaultBm;

    private Map<Schedule, Marker> mScheduleMarkerMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleMarkerMap = new HashMap<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.schedules_map_fragment, container, false);
        Bundle schedules = getArguments();
        // TODO: do we need to perform a deep copy of schedule list?
        mScheduleList = schedules.getParcelableArrayList(SCHEDULES);
        initializeMap();
        if (mDefaultBm == null) {
            mDefaultBm = BitmapFactory.decodeResource(getResources(), R.drawable.foreveralone);
        }
        return v;
    }

    private void initializeMap() {
        if (mGoogleMap == null) {
            mGoogleMap = ((MapFragment) getChildFragmentManager().findFragmentById(
                    R.id.schedules_map))
                    .getMap();
            mGoogleMap.setInfoWindowAdapter(this);
            mGoogleMap.setMyLocationEnabled(true);
            for (Schedule s : mScheduleList) {
                mScheduleMarkerMap.put(s, mGoogleMap.addMarker(createMarker(s)));
            }
            mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    final Schedule schedule = BitemapListDataHolder
                            .getInstance().findScheduleFromId(Long.valueOf(marker.getTitle()));
                    final Intent i = new Intent(getActivity(), DetailActivity.class);
                    i.putExtra(DetailActivity.FOODTRUCK_ID, schedule.getFoodtruckId());
                    startActivity(i);
                }
            });
        }
    }

    /**
     * highlight a specific schedule by opening its marker, this needs to be called
     * on UI thread
     */
    public void enableMarkerForSchedule(Schedule schedule) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(schedule.getLocation(), 12));
        mScheduleMarkerMap.get(schedule).showInfoWindow();
    }

    /**
     * Enable zooming the map view to contain all markers, zoom is done after map is layed out.
     */
    public void zoomForAllMarkers() {
        if (mScheduleMarkerMap.size() == 0) {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker m : mScheduleMarkerMap.values()) {
            if (m.isInfoWindowShown()) {
                m.hideInfoWindow();
            }
            builder.include(m.getPosition());
        }
        LatLngBounds bounds = builder.build();
        final CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        try {
            mGoogleMap.moveCamera(cu);
        } catch (IllegalStateException e) {
            final View mapView = getView();
            if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @SuppressWarnings("deprecation")
                            @SuppressLint("NewApi")
                            // We check which build version we are using.
                            @Override
                            public void onGlobalLayout() {
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                                    mapView.getViewTreeObserver()
                                            .removeGlobalOnLayoutListener(this);
                                } else {
                                    mapView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                }
                                mGoogleMap.moveCamera(cu);
                            }
                        });
            }
        }
    }

    private MarkerOptions createMarker(Schedule schedule) {
        return new MarkerOptions().title("" + schedule.getId()).position(schedule.getLocation());
    }

    private LatLng getMyLocation() {
        LocationManager service = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Location location = service.getLastKnownLocation(provider);
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        final LayoutInflater li = LayoutInflater.from(getActivity());
        final GridLayout view = (GridLayout) li.inflate(R.layout.map_info_window, null);
        final Schedule schedule = BitemapListDataHolder
                .getInstance().findScheduleFromId(Long.valueOf(marker.getTitle()));
        final FoodTruck truck = BitemapListDataHolder
                .getInstance().findFoodtruckFromId(schedule.getFoodtruckId());
        ((TextView) view.findViewById(R.id.map_info_name)).setText(truck.getName());
        final ImageView imageView = (ImageView) view.findViewById(R.id.map_info_logo);
        VolleyNetworkAccessor.getInstance(getActivity()).getImageLoader()
                .get(truck.getFullUrlForLogo(),
                        new ImageLoader.ImageListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.w(TAG, "error loading image!");
                                imageView.setImageResource(R.drawable.foreveralone);
                            }

                            @Override
                            public void onResponse(ImageLoader.ImageContainer response,
                                    boolean isImmediate) {
                                if (isImmediate && response.getBitmap() != null) {
                                    imageView.setImageBitmap(response.getBitmap());
                                } else if (!isImmediate) {
                                    // this is an ugly hack, for some reason if the image
                                    // is from network setImageBitmap() doesn't work, force
                                    //  the marker to redraw itself
                                    marker.showInfoWindow();
                                }
                            }
                        });
        ((TextView) view.findViewById(R.id.map_info_time)).setText(schedule.getTimeString());
        ((TextView) view.findViewById(R.id.map_info_address)).setText(schedule.getAddress());
        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
