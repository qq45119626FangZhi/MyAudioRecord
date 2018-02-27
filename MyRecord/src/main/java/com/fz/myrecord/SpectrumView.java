package com.fz.myrecord;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * ================================================
 * 作    者：方志
 * 版    本：1.0
 * 创建日期：2017/8/28
 * 描    述：频谱图布局
 * 修订历史：
 * ================================================
 */

public class SpectrumView extends View {
    private static final int DEFAULT_ITEM_UNIT_HEIGHT = 6;
    private static final int DEFAULT_SPACING = 2;
    private static final int DEFAULT_MAX_LEVEL = 10;
    private static final int DEFAULT_ITEM_WIDTH =  2;
    private static final int DEFAULT_ITEM_COUNT =  30;
    private static final int DEFAULT_MIN_LEVEL = 2;
    private int mMaxLevel = DEFAULT_MAX_LEVEL;
    private int[] mItemLevels;
    private int mItemWidth = DEFAULT_ITEM_WIDTH;
    private int mItemUnitHeight = DEFAULT_ITEM_UNIT_HEIGHT;
    private int mSpacing = DEFAULT_SPACING;
    private Paint mPaint;
    private int mItemCount = 0;
    private int mCenterHeight;
    private int mMinLevel = DEFAULT_MIN_LEVEL;
    private int mItemColor = Color.BLUE;

    public SpectrumView(Context context) {
        this(context, null);
    }

    public SpectrumView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("Recycle")
    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SpectrumView);
            if (a != null) {
                mItemCount = a.getInteger(R.styleable.SpectrumView_itemCount, DEFAULT_ITEM_COUNT);
                mMaxLevel = a.getInteger(R.styleable.SpectrumView_maxLevel, DEFAULT_MAX_LEVEL);
                mMinLevel = a.getInteger(R.styleable.SpectrumView_minLevel, DEFAULT_MIN_LEVEL);
                mSpacing = a.getInteger(R.styleable.SpectrumView_spacing, DEFAULT_SPACING);
                mItemUnitHeight = a.getInteger(R.styleable.SpectrumView_itemUnitHeight, DEFAULT_ITEM_UNIT_HEIGHT);
                mItemColor = a.getColor(R.styleable.SpectrumView_itemColor, Color.BLUE);
            }
        }
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mItemColor);
        updateLevels();
    }

    private void updateLevels() {
        mItemLevels = new int[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mItemLevels[i] = mMinLevel;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureHeight = mMaxLevel * mItemUnitHeight;
        int measureWidth = 0;
        for (int i = 0; i < mItemCount; i++) {
            measureWidth += mItemWidth;
            if (i != mItemCount - 1) {
                measureWidth += mSpacing;
            }
        }

        setMeasuredDimension(measureWidth,measureHeight);
        Log.e("TAG", "measureWidth : " + measureWidth + " , measureHeight : " + measureHeight );
//        setMeasuredDimension(resolveSize(measureWidth, heightMeasureSpec), resolveSize(measureHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mCenterHeight = (bottom - top) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = 0;
        for (int i = 0; i < mItemCount; i++) {
            int half = mItemUnitHeight * mItemLevels[i] / 2;
            canvas.drawRect(x, mCenterHeight - half, x + mItemWidth, mCenterHeight + half, mPaint);
            x += mSpacing + mItemWidth;
        }
    }


    public void updateForward(int newLevel) {
        newLevel = Math.max(mMinLevel, Math.min(mMaxLevel, newLevel));
        int offset = 1;
        int[] levels = new int[mItemCount];
        System.arraycopy(mItemLevels, offset, levels, 0, mItemLevels.length - 1);
        levels[mItemCount - 1] = newLevel;
        this.mItemLevels = levels;
        refresh();
    }

    public void updateBackward(int newLevel) {
        newLevel = Math.max(mMinLevel, Math.min(mMaxLevel, newLevel));
        int offset = 1;
        int[] levels = new int[mItemCount];
        System.arraycopy(mItemLevels, 0, levels, offset, mItemLevels.length - 1);
        levels[0] = newLevel;
        this.mItemLevels = levels;
        refresh();
    }

    private void refresh() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            invalidate();
        } else {
            postInvalidate();//invalidate();
        }
    }

    public void reset() {
        for (int i = 0; i < mItemCount; i++) {
            mItemLevels[i] = mMinLevel;
        }
        refresh();
    }

    public void setSpacing(int spacing) {
        this.mSpacing = spacing;
        requestLayout();
    }


    public void setItemCount(int itemCount) {
        this.mItemCount = itemCount;
        updateLevels();
        requestLayout();
    }


    public void setMinLevel(int minLevel) {
        if (minLevel < 0) {
            throw new IllegalArgumentException("minLevel must be greater than 0");
        }
        this.mMinLevel = minLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.mMaxLevel = maxLevel;
        requestLayout();
    }

    public int getMinLevel() {
        return mMinLevel;
    }

    public int getMaxLevel() {
        return mMaxLevel;
    }


    public int getItemCount() {
        return mItemCount;
    }
}
