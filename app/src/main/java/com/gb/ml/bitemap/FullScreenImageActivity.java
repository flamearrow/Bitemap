package com.gb.ml.bitemap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.network.NetworkConstants;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;

import java.util.List;

/**
 * @author ccen
 */
public class FullScreenImageActivity extends Activity {

    private List<Uri> mUriList;

    private ViewPager mViewPager;

    private static final String TAG = "FullScreenImageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_view);
        Intent i = getIntent();
        mUriList = i.getParcelableArrayListExtra(FoodTruckConstants.IMAGE_URIS);
        int currentPosition = i.getIntExtra(FoodTruckConstants.POSITION, 0);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new FullScreenImagePagerAdapter());
        mViewPager.setCurrentItem(currentPosition);
    }

    class FullScreenImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mUriList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LinearLayout ll = (LinearLayout) LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.full_screen_image, container, false);
            final ImageView iv = (ImageView) ll.findViewById(R.id.img);
            VolleyNetworkAccessor.getInstance(getApplicationContext()).getImageLoader().get(
                    BitemapNetworkAccessor.createFullImageUri(
                            NetworkConstants.SERVER_IP + mUriList.get(position)),
                    new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer response,
                                boolean isImmediate) {
                            if (response.getBitmap() != null) {
                                iv.setImageBitmap(response.getBitmap());
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.w(TAG, "error loading image!");
                        }
                    });
            container.addView(ll);
            return ll;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

}
