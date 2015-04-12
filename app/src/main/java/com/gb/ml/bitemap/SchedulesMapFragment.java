package com.gb.ml.bitemap;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;
import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
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
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getMyLocation(), 12));
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

    private MarkerOptions createMarker(Schedule schedule) {
        final FoodTruck truck = BitemapListDataHolder
                .findFoodtruckFromId(schedule.getFoodtruckId());
        return new MarkerOptions()
                .title("" + truck.getId())
                .snippet(schedule.getDateString() + FoodTruckConstants.AND
                        + schedule.getTimeString())
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

    @Override
    public View getInfoWindow(final Marker marker) {
        final LayoutInflater li = LayoutInflater.from(getActivity());
        final GridLayout ll = (GridLayout) li.inflate(R.layout.map_info_window, null);
        final FoodTruck truck = BitemapListDataHolder.findFoodtruckFromId(
                Long.valueOf(marker.getTitle()));
        ((TextView) ll.findViewById(R.id.map_info_name)).setText(truck.getName());
        final ImageView imageView = (ImageView) ll.findViewById(R.id.map_info_logo);
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

        final String date = marker.getSnippet().split(FoodTruckConstants.AND)[0];
        final String time = marker.getSnippet().split(FoodTruckConstants.AND)[1];
        ((TextView) ll.findViewById(R.id.map_info_date)).setText(date);
        ((TextView) ll.findViewById(R.id.map_info_time)).setText(time);
        return ll;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
