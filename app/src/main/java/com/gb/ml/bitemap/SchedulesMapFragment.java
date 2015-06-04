package com.gb.ml.bitemap;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
                Set<Long> truckIds = new HashSet<>();
                truckIds.add(s.getFoodtruckId());
                mLocationTruckMap.put(s.getLocation(), truckIds);
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
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(schedule.getLocation(), 12));
        mScheduleMarkerMap.get(schedule).setVisible(true);
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
        String[] scheduleIds = marker.getTitle().split(" ");
        // single schedule
        if (scheduleIds.length == 1) {
            final GridLayout view = (GridLayout) li.inflate(R.layout.map_info_window, null);
            final Schedule schedule = BitemapListDataHolder
                    .getInstance().findScheduleFromId(Long.valueOf(scheduleIds[0]));
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
        // multiple schedules
        else {
            // if use gridView
            if (isUseIconPreview()) {
                final GridView gridView = (GridView) li
                        .inflate(R.layout.map_info_preview_grid, null);
                Set<String> truckUrls = new HashSet<>();
                for (String scheduelId : scheduleIds) {
                    truckUrls.add(BitemapListDataHolder.getInstance()
                            .findFoodtruckFromId(
                                    BitemapListDataHolder.getInstance().findScheduleFromId(
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
                return gridView;
            }
            // else if text view
            else {
                final LinearLayout linearLayout = (LinearLayout) li
                        .inflate(R.layout.map_info_preview_text, null);
                String address = BitemapListDataHolder.getInstance()
                        .findScheduleFromId(Long.valueOf(scheduleIds[0])).getAddress();
                ((TextView) linearLayout.findViewById(R.id.preview_address)).setText(address);

                Map<String, Integer> times = getTimeBlocks(scheduleIds);
                for (String time : times.keySet()) {
                    TextView tv = new TextView(this.getActivity());
                    tv.setText(time.toString() + ": " + times.get(time));
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
                    tv.setTextColor(getResources().getColor(R.color.white));
                    tv.setLayoutParams(
                            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT));
                    linearLayout.addView(tv);

                }
                return linearLayout;
            }
        }
    }

    private Map<String, Integer> getTimeBlocks(String[] scheduleIds) {
        Map<String, Integer> ret = new HashMap<>();
        for (String scheduleId : scheduleIds) {
            Schedule s = BitemapListDataHolder.getInstance()
                    .findScheduleFromId(Long.valueOf(scheduleId));
            String tb = createTimeString(s.getStart(), s.getEnd());
            if (ret.containsKey(tb)) {
                ret.put(tb, ret.get(tb) + 1);
            } else {
                ret.put(tb, 1);
            }
        }
        return ret;
    }

    private String createTimeString(Calendar start, Calendar end) {
        StringBuilder sb = new StringBuilder();
        sb.append(Schedule.FORMAT_DATE.format(start.getTime()));
        sb.append(": from ");
        sb.append(Schedule.FORMAT_TIME.format(start.getTime()));
        sb.append(" to ");
        sb.append(Schedule.FORMAT_TIME.format(end.getTime()));
        sb.append(" trucks");
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
