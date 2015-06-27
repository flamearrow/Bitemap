package com.gb.ml.bitemap;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.gb.ml.bitemap.database.BitemapDBConnector;

/**
 * define common menu definitions
 */
public abstract class BitemapActionBarActivity extends ActionBarActivity {

    protected int getMenuId() {
        return R.menu.menu_main;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getMenuId(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_schedules:
                if (!(this instanceof ScheduleActivity)) {
                    startActivity(new Intent(this, ScheduleActivity.class));
                }
                break;
            case R.id.menu_all_trucks:
                if (!(this instanceof AllFoodTrucksActivity)) {
                    startActivity(new Intent(this, AllFoodTrucksActivity.class));
                }
                break;
            case R.id.menu_category:
                categorySelect();
                break;
            case R.id.menu_about:
                if (!(this instanceof AboutActivity)) {
                    startActivity(new Intent(this, AboutActivity.class));
                }
                break;
//            case R.id.menu_clear_debug_db:
//                BitemapDBConnector.getsInstance(this).clearDatabase();
//                Toast.makeText(this, "Debug DB cleared!", Toast.LENGTH_SHORT).show();
//                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    protected void categorySelect() {
        // Nothing
    }
}
