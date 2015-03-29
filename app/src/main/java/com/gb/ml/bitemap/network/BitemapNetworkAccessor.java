package com.gb.ml.bitemap.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.gb.ml.bitemap.network.NetworkConstants.*;

/**
 * Access Network
 */
public class BitemapNetworkAccessor {

    public static final String TAG = "NetwrokAccessor";

    public static long DEBUG_ID_BASE = 1;

    public static int THUMBNAIL_IMAGE_SIZE = 100;

    public static int GALLERY_IMAGE_SIZE = 200;


    public static void main(String... args) {
        for (FoodTruck ft : getTrucks()) {
            System.out.println(ft);
        }
    }

    /**
     * Issue a network request to download all schedules
     */
    public static List<Schedule> getSchedulesToday() {
        final Calendar today = Calendar.getInstance();
        return getSchedules(today, today);
    }

    public static List<Schedule> getSchedulesFeb() {
        final Calendar start = Calendar.getInstance();
        start.set(Calendar.MONTH, Calendar.FEBRUARY);
        start.set(Calendar.DAY_OF_MONTH, 1);
        final Calendar end = Calendar.getInstance();
        return getSchedules(start, end);
    }

    public static ArrayList<Schedule> getSchedulesForDays(int days) {
        final Calendar start = Calendar.getInstance();
        final Calendar end = Calendar.getInstance();
        end.add(Calendar.DAY_OF_MONTH, days);
        return getSchedules(start, end);
    }

    private static BufferedReader getBufferedReaderFromURLString(String apiString)
            throws IOException {
        URL url = new URL(apiString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);

        conn.connect();
        int response = conn.getResponseCode();
        // TODO: we might need to handle according to different response
        return new BufferedReader(new InputStreamReader(conn.getInputStream()));
    }

    public static ArrayList<Schedule> getSchedules(Calendar start, Calendar end) {
        final StringBuilder scheduleApiBuilder = new StringBuilder();
        scheduleApiBuilder.append(SCHEDULES);
        scheduleApiBuilder.append(QUESTION_MARK);
        scheduleApiBuilder.append(START_DATE);
        scheduleApiBuilder.append(EQUALS);
        final int startMonth = start.get(Calendar.MONTH) + 1;
        // TODO: this is an ugly hack
        if (startMonth < 10) {
            scheduleApiBuilder.append(ZERO);
        }
        scheduleApiBuilder.append(startMonth);
        scheduleApiBuilder.append(SLASH);
        scheduleApiBuilder.append(start.get(Calendar.DAY_OF_MONTH));
        scheduleApiBuilder.append(AND);
        scheduleApiBuilder.append(END_DATE);
        scheduleApiBuilder.append(EQUALS);
        final int endMonth = end.get(Calendar.MONTH) + 1;
        // TODO: this is an ugly hack
        if (endMonth < 10) {
            scheduleApiBuilder.append(ZERO);
        }
        scheduleApiBuilder.append(endMonth);
        scheduleApiBuilder.append(SLASH);
        scheduleApiBuilder.append(end.get(Calendar.DAY_OF_MONTH));

        final ArrayList<Schedule> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = getBufferedReaderFromURLString(scheduleApiBuilder.toString());
            // only return one line
            String nextLine = br.readLine();
            while (nextLine != null) {
                appendScheduleToList(list, nextLine);
                nextLine = br.readLine();
            }
        } catch (IOException e) {
            // TODO: something is wrong with network request, connection fails, need to handle it here
            Log.d(TAG, "Fails to get food truck info at the moment!");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    /**
     * Issue a network request to download all foodtrucks
     */
    public static List<FoodTruck> getTrucks() {
        final List<FoodTruck> list = new LinkedList<>();
        BufferedReader br = null;
        try {
            br = getBufferedReaderFromURLString(NetworkConstants.ALL_TRUCKS);
            // only return one line
            String nextLine = br.readLine();
            while (nextLine != null) {
                appendTruckToList(list, nextLine);
                nextLine = br.readLine();
            }
        } catch (IOException e) {
            // TODO: something is wrong with network request, connection fails, need to handle it here
            Log.d(TAG, "Fails to get food truck info at the moment!");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    public static List<URI> getGalleryForTruck(long truckId) {
        List<URI> ret = new LinkedList<>();
        BufferedReader br = null;
        try {
            br = getBufferedReaderFromURLString(NetworkConstants.TRUCK_GALLERY + truckId);
            String nextLine = br.readLine();
            while (nextLine != null) {
                appendImageUrlToList(nextLine, ret);
                nextLine = br.readLine();
            }
            return ret;
        } catch (IOException e) {
            Log.d(TAG, "Fails to get foodtruck gallery at the moment!");
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static Bitmap getGalleryBitmapFromURI(URI uri) {
        try {
            return decodeSampledBitmapFromStream(NetworkConstants.SERVER_IP + uri.getPath(),
                    GALLERY_IMAGE_SIZE, GALLERY_IMAGE_SIZE);
        } catch (IOException e) {
            Log.d(TAG, "Fails to get image at the moment!");
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getThumbnailBitmapFromURI(URI uri) {
        try {
            return decodeSampledBitmapFromStream(NetworkConstants.SERVER_IP + uri.getPath(),
                    THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE);
        } catch (IOException e) {
            Log.d(TAG, "Fails to get image at the moment!");
            e.printStackTrace();
        }
        return null;
    }

    private static int calculateSamplingSize(BitmapFactory.Options options, int reqWidth,
            int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // Read an bitmap from URL and scale it to the required width to save memory
    private static Bitmap decodeSampledBitmapFromStream(String urlPath, int reqWidth,
            int reqHeight) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        InputStream is = conn.getInputStream();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // don't load the actual image
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        is.close();
        options.inSampleSize = calculateSamplingSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        try {
            is = url.openStream();
            return BitmapFactory.decodeStream(is, null, options);
        } finally {
            is.close();
        }
    }

    /**
     * Issue a network request to download all events
     */
    public static List<FoodTruck> getEvents() {
        return null;
    }

    /**
     * Parse new line as a list of food truck items and add them into list
     * Currently the json object looks like the following
     * {
     * "category_detail": "Asian Fusion",
     * "id": "136",
     * "img": "trucks/3_Brothers_Kitchen/logo.jpg",
     * "url": "http://www.3brotherskitchen.com",
     * "category": "Asian",
     * "name": "3 Brothers Kitchen"
     * }
     */
    private static void appendTruckToList(List<FoodTruck> list, String newLine) {
        try {
            JSONArray arr = new JSONArray(newLine);
            int size = arr.length();
            for (int i = 0; i < size; i++) {
                JSONObject jOb = arr.getJSONObject(i);
                FoodTruck newFT = new FoodTruck.Builder().setCategory(jOb.getString("category"))
                        .setCategoryDetail(jOb.getString("category_detail"))
                        .setName(jOb.getString("name")).setUrl(jOb.getString("url"))
                        .setLogo(URI.create(jOb.getString("img")))
                        .setId(Long.parseLong(jOb.getString("id"))).build();
                list.add(newFT);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in appendTruckToList");
            e.printStackTrace();
        }
    }


    /**
     * Parse the following json and store them as URI
     * [
     * "/trucks/Taqueria_Angelicas/logo.jpg",
     * "/trucks/Taqueria_Angelicas/tagueria-angelicas-menu.jpg",
     * "/trucks/Taqueria_Angelicas/taqueria-angelicas-churros.jpg",
     * "/trucks/Taqueria_Angelicas/taqueria-angelicas-tacos.jpg",
     * "/trucks/Taqueria_Angelicas/taqueria-angelicas.jpg"
     * ]
     */
    private static void appendImageUrlToList(String imagesJson, List<URI> list) {
        try {
            JSONArray arr = new JSONArray(imagesJson);
            int size = arr.length();
            for (int i = 0; i < size; i++) {
                String s = arr.getString(i);
                list.add(URI.create(s));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse new line as a list of food truck items and add them into list
     * Currently the json object looks like the following
     * {
     * "img": "trucks/Casey_s_Pizza/logo.jpg",
     * "short_address": "Spear and Mission",
     * "address": "Mission Street & Spear Street, San Francisco, CA 94105, USA",
     * "type": "truck",
     * "lat": "37.7925466",
     * "street_lng": null,
     * "start_time": "11:00:00",
     * "street_lat": null,
     * "lng": "-122.3940893",
     * "meal": "lunch",
     * "end_time": "14:00:00",
     * "date": "2015-02-05",
     * "name": "Casey's Pizza",
     * "truck_id": "124"
     * }
     *
     *
     * Currently missing "id" field, use debug increment
     * Not supporting street_lat and street_lng now as they are nullable
     * setting schedule id from debug base
     */
    private static void appendScheduleToList(List<Schedule> list, String newLine) {
        try {
            JSONArray arr = new JSONArray(newLine);
            int size = arr.length();
            for (int i = 0; i < size; i++) {
                JSONObject jOb = arr.getJSONObject(i);
                final String[] date = jOb.getString("date").split(DASH);
                int year = Integer.parseInt(date[0]);
                int month = Integer.parseInt(date[1]);
                int day = Integer.parseInt(date[2]);

                final String[] startTime = jOb.getString("start_time").split(COLON);
                final String[] endTime = jOb.getString("end_time").split(COLON);

                final Calendar start = Calendar.getInstance();
                final Calendar end = Calendar.getInstance();

                start.set(Calendar.YEAR, year);
                start.set(Calendar.MONTH, month - 1);
                start.set(Calendar.DAY_OF_MONTH, day);
                start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
                start.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
                end.set(Calendar.YEAR, year);
                end.set(Calendar.MONTH, month - 1);
                end.set(Calendar.DAY_OF_MONTH, day);
                end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
                end.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));
                // TODO: remove this after serer bug is fixed
                if (jOb.has("truck_id")) {
                    Schedule newSc = new Schedule.Builder().setAddress(jOb.getString("address"))
                            .setFoodtruckId(Long.parseLong(jOb.getString("truck_id")))
                            .setId(DEBUG_ID_BASE++)
                            .setLat(Double.parseDouble(jOb.getString("lat")))
                            .setLng(Double.parseDouble(jOb.getString("lng"))).setStart(start)
                            .setEnd(end).build();
                    list.add(newSc);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in appendScheduleToList");
            e.printStackTrace();
        }
    }
}