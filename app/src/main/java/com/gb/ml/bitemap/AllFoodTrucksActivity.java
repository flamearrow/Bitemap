package com.gb.ml.bitemap;

import android.os.Bundle;

import com.gb.ml.bitemap.listFragments.FoodTruckList;

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
