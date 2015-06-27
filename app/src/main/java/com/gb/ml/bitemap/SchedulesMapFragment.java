package com.gb.ml.bitemap;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Display schedules on a map
 */
public class SchedulesMapFragment extends Fragment implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = "SchedulesMapFragment";

    private static final int MARKER_ZOOM_IN = 12;

    public static final String SCHEDULES = "SCHEDULES";

    private GoogleMap mGoogleMap;

    private List<Schedule> mScheduleList;

    private Bitmap mDefaultBm;

    private Map<Schedule, Marker> mScheduleMarkerMap;

    private Map<LatLng, Marker> mLatLngMarkerMap;

    // maps location and a set of schedule ids
    private Map<LatLng, Set<Long>> mLocationTruckMap;

    private boolean isUseIconPreview() {
        return mUseIconPreview;
    }

    public void flipUseIconPreview() {
        mUseIconPreview = !mUseIconPreview;
    }

    private boolean mUseIconPreview = true;

    private Marker currentSingleMarker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScheduleMarkerMap = new HashMap<>();
        mLatLngMarkerMap = new HashMap<>();
        mLocationTruckMap = new HashMap<>();
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
            mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    float container_height = getView().getHeight();
                    Projection projection = mGoogleMap.getProjection();
                    Point markerScreenPosition = projection.toScreenLocation(marker.getPosition());
                    Point pointHalfScreenAbove = new Point(markerScreenPosition.x,
                            (int) (markerScreenPosition.y - (container_height / 18)));

                    LatLng aboveMarkerLatLng = projection.fromScreenLocation(pointHalfScreenAbove);

                    marker.showInfoWindow();
                    CameraPosition center = new CameraPosition.Builder().target(aboveMarkerLatLng).zoom(
                            MARKER_ZOOM_IN).build();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(center));
                    return true;
                }
            });
            mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                // detect when a marker is deselected
                @Override
                public void onMapClick(LatLng latLng) {
                    if (currentSingleMarker != null) {
                        currentSingleMarker.setVisible(false);
                        currentSingleMarker = null;
                    }
                }
            });
            resetMarkers();
        }
    }

    private void resetMarkers() {
        for (Marker m : mScheduleMarkerMap.values()) {
            m.remove();
        }
        for (Marker m : mLatLngMarkerMap.values()) {
            m.remove();
        }
        mScheduleMarkerMap.clear();
        mLatLngMarkerMap.clear();
        mLocationTruckMap.clear();
        for (Schedule s : mScheduleList) {
            if (mLocationTruckMap.containsKey(s.getLocation())) {
                mLocationTruckMap.get(s.getLocation()).add(s.getId());
            } else {
                Set<Long> scheduleId = new HashSet<>();
                scheduleId.add(s.getId());
                mLocationTruckMap.put(s.getLocation(), scheduleId);
            }
            mScheduleMarkerMap.put(s, mGoogleMap.addMarker(createMarker(s)));
        }
        for (LatLng latLng : mLocationTruckMap.keySet()) {
            mLatLngMarkerMap.put(latLng,
                    mGoogleMap
                            .addMarker(createLatLngMarker(latLng, mLocationTruckMap.get(latLng))));
        }
        // latlng marker is always visible, envisible regular markers that's not overlapped with
        // latlng markers
        for (Schedule s : mScheduleMarkerMap.keySet()) {
            if (!mLatLngMarkerMap.containsKey(s.getLocation())) {
                mScheduleMarkerMap.get(s).setVisible(true);
            }
        }
    }

    /**
     * highlight a specific schedule by opening its marker, this needs to be called
     * on UI thread
     */
    public void enableMarkerForSchedule(Schedule schedule) {
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(schedule.getLocation(), MARKER_ZOOM_IN));
        if (!mScheduleMarkerMap.get(schedule).isVisible()) {
            currentSingleMarker = mScheduleMarkerMap.get(schedule);
            currentSingleMarker.setVisible(true);
        }
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
                                try {
                                    mGoogleMap.moveCamera(cu);
                                } catch (IllegalStateException e) {
                                    // give up here
                                    e.printStackTrace();
                                    Log.w(TAG, "failed to adjust camera");
                                }
                            }
                        });
            }
        }
    }

    public void updateList(List<Schedule> newSchedules) {
        mScheduleList = newSchedules;
        resetMarkers();
        zoomForAllMarkers();
    }

    private MarkerOptions createMarker(Schedule schedule) {
        return new MarkerOptions().title("" + schedule.getId()).position(schedule.getLocation())
                .visible(false);
    }

    private MarkerOptions createLatLngMarker(LatLng latLng, Set<Long> truckIds) {
        StringBuilder idsBuilder = new StringBuilder();
        for (Long id : truckIds) {
            idsBuilder.append(id + " ");
        }
        return new MarkerOptions().title(idsBuilder.toString()).position(latLng);
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        final LayoutInflater li = LayoutInflater.from(getActivity());
        String[] scheduleIds = marker.getTitle().split(" ");
        // single schedule
        if (scheduleIds.length == 1) {
            final GridLayout view = (GridLayout) li.inflate(R.layout.map_info_window, null);
            final Schedule schedule = BitemapListDataHolder
                    .getsInstance(getActivity()).findScheduleFromId(Long.valueOf(scheduleIds[0]));
            final FoodTruck truck = BitemapListDataHolder
                    .getsInstance(getActivity()).findFoodtruckFromId(schedule.getFoodtruckId());
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
        // multiple schedules
        else {
            final RelativeLayout layout = (RelativeLayout) li
                    .inflate(R.layout.map_info_preview_grid, null);
            final TextView timeTv = (TextView) layout.findViewById(R.id.schedules_time);
            final TextView addressTv = (TextView) layout.findViewById(R.id.schedules_address);

            Schedule s = BitemapListDataHolder.getsInstance(getActivity())
                    .findScheduleFromId(Long.valueOf(scheduleIds[0]));
            timeTv.setText(createTimeString(s.getStart(), s.getEnd()));
            addressTv.setText(s.getAddress());

            final GridView gridView = (GridView) layout.findViewById(R.id.map_info_grid_view);
            Set<String> truckUrls = new HashSet<>();
            for (String scheduelId : scheduleIds) {
                truckUrls.add(BitemapListDataHolder.getsInstance(getActivity())
                        .findFoodtruckFromId(
                                BitemapListDataHolder.getsInstance(getActivity()).findScheduleFromId(
                                        Long.valueOf(scheduelId)).getFoodtruckId())
                        .getFullUrlForLogo());
            }
            final ArrayList<String> mTruckUris = new ArrayList<>();
            int left = 9;
            for (String url : truckUrls) {
                mTruckUris.add(url);
                if (left-- <= 0) {
                    break;
                }
            }
            gridView.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return mTruckUris.size();
                }

                @Override
                public Object getItem(int position) {
                    return mTruckUris.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    final ImageView imageView;
                    if (convertView == null) {
                        imageView = (ImageView) LayoutInflater.from(getActivity())
                                .inflate(R.layout.map_info_preview_item, parent, false);
                    } else {
                        imageView = (ImageView) convertView;
                    }

                    VolleyNetworkAccessor.getInstance(getActivity()).getImageLoader()
                            .get(mTruckUris.get(position),
                                    new ImageLoader.ImageListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.w(TAG, "error loading image!");
                                            imageView.setImageResource(R.drawable.foreveralone);
                                        }

                                        @Override
                                        public void onResponse(
                                                ImageLoader.ImageContainer response,
                                                boolean isImmediate) {
                                            if (isImmediate && response.getBitmap() != null) {
                                                imageView.setImageBitmap(response.getBitmap());
                                            } else if (!isImmediate) {
                                                marker.showInfoWindow();
                                            }
                                        }
                                    });
                    return imageView;
                }
            });
            return layout;

        }
    }

    private String createTimeString(Calendar start, Calendar end) {
        StringBuilder sb = new StringBuilder();
        sb.append(Schedule.FORMAT_DATE.format(start.getTime()));
        sb.append(": from ");
        sb.append(Schedule.FORMAT_TIME.format(start.getTime()));
        sb.append(" to ");
        sb.append(Schedule.FORMAT_TIME.format(end.getTime()));
        return sb.toString();
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    public void setOnInfoWindowClickListenerOnMap(GoogleMap.OnInfoWindowClickListener listener) {
        mGoogleMap.setOnInfoWindowClickListener(listener);
    }
}
