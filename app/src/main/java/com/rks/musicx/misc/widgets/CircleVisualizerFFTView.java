package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.rks.musicx.R;

/*
 * Created by Coolalien on 6/28/2016.
 */

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class CircleVisualizerFFTView extends View {

    private static final int DEFAULT_CAKE_DEGREE = 5;
    private static final int DEFAULT_CAKE_COLOR = Color.WHITE;
    private static final int DEFAULT_PADDING_OFFSET = 60;
    private int[] mHeights;
    private int mCakeCount;
    private int mCakeDegree;
    private int mCakeColor = DEFAULT_CAKE_COLOR;
    private int mCircleRadius;
    private int mPaddingOffset;
    private Paint mPaint;
    private int mCenterX;
    private int mCenterY;
    private int mDrawStartY;
    private int[] mColors;
    private int mStrokeWidth = 10;
    private boolean mRotateColor = false;

    public CircleVisualizerFFTView(Context context) {
        this(context, null);
    }

    public CircleVisualizerFFTView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleVisualizerFFTView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public static int dp2px(int dp, Context context) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public static float dp2px(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleVisualizerFFTView);
            mCakeDegree = ta.getInteger(R.styleable.CircleVisualizerFFTView_cake_degree, DEFAULT_CAKE_DEGREE);
            mCakeColor = ta.getColor(R.styleable.CircleVisualizerFFTView_cake_color, DEFAULT_CAKE_COLOR);
            mPaddingOffset = (int) dp2px(ta.getDimension(R.styleable.CircleVisualizerFFTView_padding_offset, DEFAULT_PADDING_OFFSET), context);
            ta.recycle();
        } else {
            mCakeDegree = getmCakeDegree();
            mCakeColor = getmCakeColor();
            mPaddingOffset = DEFAULT_PADDING_OFFSET;
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mCakeColor);
        mPaint.setStrokeWidth(10);
        mCakeCount = 360 / mCakeDegree;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(MeasureSize(width, widthMode, 1), MeasureSize(height, heightMode, 2));
    }

    private int MeasureSize(int size, int sizeMode, int code) {
        if (sizeMode == MeasureSpec.EXACTLY) {
            return size;
        } else {
            int requireSize = 0;
            if (code == 1) {
                requireSize = getPaddingLeft() + getPaddingRight() + mPaddingOffset * 2;
            } else if (code == 2) {
                requireSize = getPaddingBottom() + getPaddingTop() + mPaddingOffset * 2;
            }
            if (sizeMode == MeasureSpec.AT_MOST) {
                requireSize = Math.min(size, requireSize);
            }
            return requireSize;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w < h) {
            mCircleRadius = (w - getPaddingLeft() - getPaddingRight() - mPaddingOffset * 2) / 2;
        } else {
            mCircleRadius = (h - getPaddingBottom() - getPaddingTop() - mPaddingOffset * 2) / 2;
        }
        mCenterX = w / 2;
        mCenterY = h / 2;
        mDrawStartY = mCenterY - mCircleRadius;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void updateVisualizer(byte[] fft) {
        int[] model = new int[fft.length / 2 + 1];
        model[0] = Math.abs(fft[0]);
        for (int i = 2, j = 1; j < mCakeCount; ) {
            model[j] = (int) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;
        }
        mHeights = model;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mHeights == null) {
            return;
        }
        drawLine(canvas, mHeights[0]);
        for (int i = 0; i < mCakeCount; i++) {
            canvas.rotate(mCakeDegree, mCenterX, mCenterY);
            drawLine(canvas, mHeights[i + 1]);
        }
    }

    public void drawLine(Canvas canvas, int height) {
        mPaint.setAlpha(255);
        if (height > mDrawStartY) {
            canvas.drawLine(mCenterX, mDrawStartY, mCenterX, 50, mPaint);
        } else {
            canvas.drawLine(mCenterX, mDrawStartY, mCenterX, mDrawStartY - height, mPaint);
        }
        mPaint.setAlpha(150);
        canvas.drawLine(mCenterX, mDrawStartY + 10, mCenterX, mDrawStartY + 10 + height * 0.4f, mPaint);
    }

    public int[] getmHeights() {
        return mHeights;
    }

    public void setmHeights(int[] mHeights) {
        this.mHeights = mHeights;
    }

    public int getmCakeCount() {
        return mCakeCount;
    }

    public void setmCakeCount(int mCakeCount) {
        this.mCakeCount = mCakeCount;
    }

    public int getmCakeDegree() {
        return mCakeDegree;
    }

    public void setmCakeDegree(int mCakeDegree) {
        this.mCakeDegree = mCakeDegree;
    }

    public int getmCakeColor() {
        return mCakeColor;
    }

    public void setmCakeColor(int mCakeColors) {
        if (mCakeColor == mCakeColors) {
            return;
        }
        mCakeColor = mCakeColors;
        mPaint.setColor(mCakeColors);
        invalidate();
    }

    public int getmCircleRadius() {
        return mCircleRadius;
    }

    public void setmCircleRadius(int mCircleRadius) {
        this.mCircleRadius = mCircleRadius;
    }


}