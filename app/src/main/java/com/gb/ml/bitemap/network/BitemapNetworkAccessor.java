package com.gb.ml.bitemap.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Access Network
 */
public class BitemapNetworkAccessor {

    public static long DEBUG_ID_BASE = 1;

    public static final String TAG = "NetwrokAccessor";

    public static void main(String... args) {
        for (FoodTruck ft : getTrucks()) {
            System.out.println(ft);
        }
    }

    /**
     * Issue a network request to download all schedules
     */
    public static List<Schedule> getSchedules() {
        return null;
    }

    /**
     * Issue a network request to download all foodtrucks
     */
    public static List<FoodTruck> getTrucks() {
        final List<FoodTruck> list = new LinkedList<>();
        InputStream is = null;
        try {
            URL url = new URL(NetworkConstants.ALL_TRUCKS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            conn.connect();
            int response = conn.getResponseCode();
            // TODO: we might need to handle according to different response
            Log.d(TAG, "The response is: " + response);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            // only return one line
            String nextLine = br.readLine();
            while (nextLine != null) {
                appendToList(list, nextLine);
                nextLine = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public static Drawable getDrawableFromURI(URI uri) {
        try {
            final Bitmap bm;
            URL url = new URL(NetworkConstants.SERVER_IP + uri.getPath());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            return new BitmapDrawable(null, BitmapFactory.decodeStream(conn.getInputStream()));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
     * "url": "http://www.meltsmyhearttruck.com/",
     * "category_detail": "American",
     * "category": "American",
     * "name": "Melts My Heart",
     * "img": "trucks/Melts_My_Heart/logo.jpg"
     * }
     *
     * DB would need to add a self increment id to it later
     */
    private static void appendToList(List<FoodTruck> list, String newLine) {
        try {
            JSONArray arr = new JSONArray(newLine);
            int size = arr.length();
            for (int i = 0; i < size; i++) {
                JSONObject jOb = arr.getJSONObject(i);
                FoodTruck newFT = new FoodTruck.Builder().setCategory(jOb.getString("category"))
                        .setCategoryDetail(jOb.getString("category_detail"))
                        .setName(jOb.getString("name")).setUrl(jOb.getString("url"))
                        .setLogo(URI.create(jOb.getString("img")))
                        .setId(DEBUG_ID_BASE++).build();
                list.add(newFT);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error in appendToList");
            e.printStackTrace();
        }
    }


}