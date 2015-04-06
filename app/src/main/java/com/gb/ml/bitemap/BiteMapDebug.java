package com.gb.ml.bitemap;

import android.content.Context;
import android.net.Uri;

import com.gb.ml.bitemap.pojo.FoodTruck;
import com.gb.ml.bitemap.pojo.Schedule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Debug helper to insert raw user data
 */
public class BitemapDebug {

    private static final String FOOD_TRUCKS_DATA = "debugData/debug_foodtrucks";

    private static final String SCHEDULES_DATA = "debugData/debug_schedules";

    private static final String SPLITER = "\\$";

    private static final String DASH = "-";

    private static final String COLON = ":";

    private static FoodTruck parseFoodTruck(String inputLine) {
        String[] fields = inputLine.split(SPLITER);
        FoodTruck ret = new FoodTruck.Builder().setId(Long.parseLong(fields[0])).setName(fields[1])
                .setCategory(fields[2])
                .setCategoryDetail(fields[3]).setLogo(
                        Uri.parse(fields[4])).setUrl(fields[5]).build();
        return ret;
    }

    public static List<FoodTruck> createDebugFoodTrucks(Context context) {
        List<FoodTruck> ret = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(
                    FOOD_TRUCKS_DATA)));
            String nextLine = br.readLine();
            while (nextLine != null) {
                if (nextLine.startsWith("!")) {
                    nextLine = br.readLine();
                    continue;
                }
                ret.add(parseFoodTruck(nextLine));
                nextLine = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Map<Long, FoodTruck> convertIntoMap(List<FoodTruck> foodTrucks) {
        Map<Long, FoodTruck> ret = new HashMap<>();
        for (FoodTruck fr : foodTrucks) {
            ret.put(fr.getId(), fr);
        }
        return ret;
    }


    private static Schedule parseSchedule(String inputLine, Map<Long, FoodTruck> foodTruckMap) {
        String[] fields = inputLine.split(SPLITER);
        FoodTruck fr = foodTruckMap.get(Long.parseLong(fields[1]));

        String[] date = fields[0].split(DASH);
        int year = Integer.parseInt(date[0]);
        int month = Integer.parseInt(date[1]);
        int day = Integer.parseInt(date[2]);
        String[] startTime = fields[2].split(COLON);
        String[] endTime = fields[3].split(COLON);
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        start.set(Calendar.YEAR, year);
        start.set(Calendar.MONTH, month);
        start.set(Calendar.DAY_OF_MONTH, day);
        start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(startTime[0]));
        start.set(Calendar.MINUTE, Integer.parseInt(startTime[1]));
        end.set(Calendar.YEAR, year);
        end.set(Calendar.MONTH, month);
        end.set(Calendar.DAY_OF_MONTH, day);
        end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(endTime[0]));
        end.set(Calendar.MINUTE, Integer.parseInt(endTime[1]));

        String address = fields[4];
        // no id for schedule at the moment
        // not supporting street view lat and lng at the moment

        Schedule s = new Schedule.Builder().setId(0).setStart(start).setEnd(end).setFoodtruckId(
                fr.getId()).setAddress(address).setLat(Double.parseDouble(fields[5]))
                .setLng(Double.parseDouble(fields[6])).setStreetLat(0).setStreetLng(0).build();
        return s;
    }

    public static List<Schedule> createDebugSchedules(Context context,
            Map<Long, FoodTruck> foodTrackMap) {
        List<Schedule> ret = new LinkedList<>();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(SCHEDULES_DATA)));
            String nextLine = br.readLine();
            while (nextLine != null) {
                if (nextLine.startsWith("!")) {
                    nextLine = br.readLine();
                    continue;
                }
                ret.add(parseSchedule(nextLine, foodTrackMap));
                nextLine = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static List<Schedule> createDebugSchedules(Context context) {
        return createDebugSchedules(context, convertIntoMap(createDebugFoodTrucks(context)));
    }
}
