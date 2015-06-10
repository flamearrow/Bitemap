package com.gb.ml.bitemap.layouts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.GridView;

/**
 * @author ccen
 */
public class GridViewEx extends GridView {

    private int mRequestedColumns;

    public GridViewEx(Context context) {
        super(context);
    }

    public GridViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GridViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setNumColumns(int numColumns) {
        super.setNumColumns(numColumns);
        mRequestedColumns = numColumns;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (mRequestedColumns > 0) {
//            int width = (mRequestedColumns * getColumnWidth())
//                    + ((mRequestedColumns - 1) * getHorizontalSpacing())
//                    + getListPaddingLeft() + getListPaddingRight();
//            int expandMeasure = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.EXACTLY);
//            super.onMeasure(expandMeasure, heightMeasureSpec);
//        }

//        if (mRequestedColumns > 0) {
//            int width = (mRequestedColumns * getColumnWidth())
//                    + ((mRequestedColumns-1) * getHorizontalSpacing())
//                    + getListPaddingLeft() + getListPaddingRight();
//
//            setMeasuredDimension(width, getMeasuredHeight());
//        }
    }
}
