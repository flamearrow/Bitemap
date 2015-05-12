package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.gb.ml.bitemap.network.NetworkConstants;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;

import java.util.ArrayList;

public class GridViewGalleryActivity extends Activity {

    private static final String TAG = "GridViewGalleryActivity";

    private GridView mGridView;

    private ArrayList<Uri> mUris;

    private int mColNum;

    private int mImageWidth;

    private static int PORTRAIT_COLUMN = 3;

    private static int LANDSCAPE_COLUMN = 5;

    private static int PADDING = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid_view);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        mUris = getIntent().getParcelableArrayListExtra(FoodTruckConstants.IMAGE_URIS);

        mGridView = (GridView) findViewById(R.id.grid_view);
        mGridView.setAdapter(new GridViewGalleryAdapter());
        initilializeWidth();
        mGridView.setNumColumns(mColNum);
    }

    // set screen width in portrait mode and screen height in landscape mode
    private void initilializeWidth() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        // Portrait
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            mImageWidth = mGridView.getWidth() / PORTRAIT_COLUMN - (PORTRAIT_COLUMN + 1) * PADDING;
            mColNum = PORTRAIT_COLUMN;
        }
        // Landscape
        else {
            mImageWidth = mGridView.getWidth() / LANDSCAPE_COLUMN
                    - (LANDSCAPE_COLUMN + 1) * PADDING;
            mColNum = LANDSCAPE_COLUMN;
        }
    }

    private class GridViewGalleryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUris.size();
        }

        @Override
        public Object getItem(int position) {
            return mUris.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.gallery_item, parent, false);
                imageView.setLayoutParams(new GridView.LayoutParams(mImageWidth, mImageWidth));
                OnImageClickListener ocl = new OnImageClickListener();
                imageView.setTag(ocl);
                imageView.setOnClickListener(ocl);
            } else {
                imageView = (ImageView) convertView;
            }

            VolleyNetworkAccessor.getInstance(getApplicationContext()).getImageLoader()
                    .get(NetworkConstants.SERVER_IP + mUris.get(position).getPath(),
                            new ImageLoader.ImageListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.w(TAG, "error loading image!");
                                }

                                @Override
                                public void onResponse(ImageLoader.ImageContainer response,
                                        boolean isImmediate) {
                                    if (response.getBitmap() != null) {
                                        imageView.setImageBitmap(response.getBitmap());
                                    }
                                }
                            });
            ((OnImageClickListener) imageView.getTag()).setPosition(position);
            return imageView;
        }

        class OnImageClickListener implements View.OnClickListener {

            private int mPosition;

            void setPosition(int position) {
                mPosition = position;
            }

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FullScreenImageActivity.class);
                i.putParcelableArrayListExtra(FoodTruckConstants.IMAGE_URIS, mUris);
                i.putExtra(FoodTruckConstants.POSITION, mPosition);
                startActivity(i);

            }
        }
    }
}
