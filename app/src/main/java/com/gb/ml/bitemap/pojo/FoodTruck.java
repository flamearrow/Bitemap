package com.gb.ml.bitemap.pojo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.gb.ml.bitemap.network.BitemapNetworkAccessor;
import com.gb.ml.bitemap.network.NetworkConstants;

public class FoodTruck implements Comparable<FoodTruck> {

    private long mId;

    private String mName;

    // TODO: use enum?
    private String mCategory;

    private String mCategoryDetail;

    private Uri mLogo;

    private String mUrl;

    public static final String LOGO_DOWNLOADED = "logo_downloaded";

    private FoodTruck() {
    }

    private FoodTruck(String name, String category, String cDetail, Uri logo, String url, long id) {
        mName = name;
        mCategory = category;
        mCategoryDetail = cDetail;
        mLogo = logo;
        mUrl = url;
        mId = id;
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param another the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(FoodTruck another) {
        return mName.compareTo(another.mName);
    }

    public static class Builder {

        private String mName, mCategory, mCategoryDetail, mUrl;

        private long mId;

        private Uri mLogo;

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setCategory(String category) {
            mCategory = category;
            return this;
        }

        public Builder setCategoryDetail(String categoryDetail) {
            mCategoryDetail = categoryDetail;
            return this;
        }

        public Builder setUrl(String url) {
            mUrl = url;
            return this;
        }

        public Builder setLogo(Uri logo) {
            mLogo = logo;
            return this;
        }

        public Builder setId(long id) {
            mId = id;
            return this;
        }

        public FoodTruck build() {
            return new FoodTruck(mName, mCategory, mCategoryDetail, mLogo, mUrl, mId);
        }
    }

    public String getFullUrlForLogo() {
        return NetworkConstants.SERVER_IP + mLogo.getPath();
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getCategory() {
        return mCategory;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public String getCategoryDetail() {
        return mCategoryDetail;
    }

    public void setCategoryDetail(String categoryDetail) {
        mCategoryDetail = categoryDetail;
    }

    public Uri getLogo() {
        return mLogo;
    }

    public void setLogo(Uri logo) {
        mLogo = logo;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FoodTruck " + getName() + "\n");
        sb.append(" id: " + getId() + "\n");
        sb.append(" name: " + getName() + "\n");
        sb.append(" category: " + getCategory() + "\n");
        sb.append(" category detail: " + getCategoryDetail() + "\n");
        sb.append(" logo: " + getLogo() + "\n");
        sb.append(" url: " + getUrl() + "\n");
        return sb.toString();
    }
}
