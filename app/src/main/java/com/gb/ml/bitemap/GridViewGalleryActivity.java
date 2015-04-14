package com.gb.ml.bitemap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.HashMap;
import java.util.Map;

public class GridViewGalleryActivity extends Activity {

    private static final String TAG = "GridViewGalleryActivity";

    private Map<Uri, Bitmap> mBitmapCache;

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
        initializeMap();
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

    private void initializeMap() {
        RetainFragment retainFragment = RetainFragment
                .findOrCreateRetainFragment(getFragmentManager());
        if (retainFragment.mBitmapCache == null) {
            mBitmapCache = new HashMap<>();
            retainFragment.mBitmapCache = mBitmapCache;
            for (Uri uri : mUris) {
                mBitmapCache.put(uri, null);
            }
            pullImages();
        }
        mBitmapCache = retainFragment.mBitmapCache;
    }

    private void pullImages() {
        for (final Uri uri : mBitmapCache.keySet()) {

            VolleyNetworkAccessor.getInstance(this).getImageLoader()
                    .get(NetworkConstants.SERVER_IP + uri.getPath(),
                            new ImageLoader.ImageListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.w(TAG, "error loading image!");
                                }

                                @Override
                                public void onResponse(ImageLoader.ImageContainer response,
                                        boolean isImmediate) {
                                    if (response.getBitmap() != null) {
                                        mBitmapCache.put(uri, response.getBitmap());
                                        ((BaseAdapter) mGridView.getAdapter())
                                                .notifyDataSetChanged();
                                    }
                                }
                            });
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
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = (ImageView) LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.gallery_item, parent, false);
                OnImageClickListener ocl = new OnImageClickListener();
                imageView.setTag(ocl);
                imageView.setOnClickListener(ocl);
            } else {
                imageView = (ImageView) convertView;
            }
            Bitmap bm = mBitmapCache.get(mUris.get(position));
            imageView.setLayoutParams(new GridView.LayoutParams(mImageWidth, mImageWidth));
            if (bm != null) {
                imageView.setImageBitmap(bm);
            }
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

// Use this to buffer the bitmap map buffer
class RetainFragment extends Fragment {

    private static final String TAG = "RETAIN_FRAGMENT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
        RetainFragment ret = (RetainFragment) fm.findFragmentByTag(TAG);
        if (ret == null) {
            ret = new RetainFragment();
            fm.beginTransaction().add(ret, TAG).commit();
        }
        return ret;
    }

    Map<Uri, Bitmap> mBitmapCache;
}