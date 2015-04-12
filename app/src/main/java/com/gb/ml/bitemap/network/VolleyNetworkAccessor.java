package com.gb.ml.bitemap.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Accessing network using volley, this is a singleton used on ApplicationContext
 */
public class VolleyNetworkAccessor {

    private Context mContext;

    private ImageLoader mImageLoader;

    private RequestQueue mRequestQueue;

    private static VolleyNetworkAccessor mInstance;

    private VolleyNetworkAccessor(Context context) {
        mContext = context;
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(mContext));
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // make sure the queue is alive during the app's life time
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static synchronized VolleyNetworkAccessor getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyNetworkAccessor(context);
        }
        return mInstance;
    }


}

/**
 * In-memory cache for ImageLoader, if there's no cache hit here, volley will tries to find
 * cache from file, if there's still no cache hit, will request network
 */
class LruBitmapCache extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {

    public LruBitmapCache(int maxSize) {
        super(maxSize);
    }

    public LruBitmapCache(Context context) {
        this(getCacheSize(context));
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getWidth();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    // Size is worth 3 screens
    public static int getCacheSize(Context ctx) {
        final DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        final int screenSize = displayMetrics.widthPixels * displayMetrics.heightPixels;
        // 4 bytes per pixel
        return screenSize * 4 * 3;
    }
}