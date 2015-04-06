package com.gb.ml.bitemap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.gb.ml.bitemap.network.BitemapNetworkAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GridViewGalleryActivity extends Activity {

    public static final String IMAGE_URIS = "IMAGE_URIS";

    // TODO: remove this after we have global lru cache
    private Map<Uri, Bitmap> mBitmapCache;

    private GridView mGridView;

    private List<Uri> mUris;

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
        mUris = getIntent().getParcelableArrayListExtra(IMAGE_URIS);

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
        Log.d("mlgb", "imageWidth: " + mImageWidth);
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
            new AsyncTask<Void, Void, Bitmap>() {

                @Override
                protected Bitmap doInBackground(Void... params) {
                    return BitemapNetworkAccessor.getThumbnailBitmapFromURI(uri);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    mBitmapCache.put(uri, bitmap);
                    ((BaseAdapter) mGridView.getAdapter()).notifyDataSetChanged();
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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
            } else {
                imageView = (ImageView) convertView;
            }
            Bitmap bm = mBitmapCache.get(mUris.get(position));
            imageView.setLayoutParams(new GridView.LayoutParams(mImageWidth, mImageWidth));
            if (bm != null) {
                imageView.setImageBitmap(bm);
            }
            return imageView;
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