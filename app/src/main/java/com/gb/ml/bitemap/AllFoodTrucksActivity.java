package com.gb.ml.bitemap;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gb.ml.bitemap.listFragments.FoodTruckList;
import com.gb.ml.bitemap.pojo.Schedule;

/**
 * A list of all available food trucks
 */
public class AllFoodTrucksActivity extends BitemapActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_all_trucks);
        setContentView(R.layout.activity_all_foodtrucks);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.all_food_trucks, new FoodTruckList())
                    .commit();
        }
    }
}
