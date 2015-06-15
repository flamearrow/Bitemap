package com.gb.ml.bitemap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends BitemapActionBarActivity {

    @Override
    protected int getMenuId() {
        return R.menu.menu_no_category;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void sendfeedback(View view) {
        startActivity(new Intent(this, FeedbackActivity.class));
    }
}
