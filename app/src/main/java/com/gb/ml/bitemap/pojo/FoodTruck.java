    package com.gb.ml.bitemap.pojo;

    import java.net.URI;

    public class FoodTruck {

        private long mId;

        private String mName;

        // TODO: use enum?
        private String mCategory;

    private String mCategoryDetail;

    private URI mLogo;

    private String mUrl;

    private FoodTruck() {
    }

    private FoodTruck(String name, String category, String cDetail, URI logo, String url, long id) {
        mName = name;
        mCategory = category;
        mCategoryDetail = cDetail;
        mLogo = logo;
        mUrl = url;
        mId = id;
    }

    public static class Builder {

        private String mName, mCategory, mCategoryDetail, mUrl;

        private long mId;

        private URI mLogo;

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

        public Builder setLogo(URI logo) {
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

    public URI getLogo() {
        return mLogo;
    }

    public void setLogo(URI logo) {
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
