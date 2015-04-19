package com.gb.ml.bitemap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.gb.ml.bitemap.listFragments.FoodTruckList;

/**
 * A list of all available food trucks
 */
public class AllFoodTrucksActivity extends BitemapActionBarActivity {

    private FoodTruckList mFoodTruckList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.menu_all_trucks);
        setContentView(R.layout.activity_all_foodtrucks);
        if (savedInstanceState == null) {
            mFoodTruckList = new FoodTruckList();
            getFragmentManager().beginTransaction().add(R.id.all_food_trucks, mFoodTruckList)
                    .commit();
        }
    }

    @Override
    protected void categorySelect() {
        final View layout = LayoutInflater.from(this).inflate(R.layout.category_filter, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(layout);
        alertDialogBuilder.setTitle(R.string.menu_category);
        final Spinner categorySpinner = (Spinner) layout.findViewById(R.id.category_spinner);
        categorySpinner.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                BitemapListDataHolder.getInstance().getCategory()));
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mFoodTruckList.updateCategory((String) categorySpinner.getSelectedItem());
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
