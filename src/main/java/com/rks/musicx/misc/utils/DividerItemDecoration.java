package com.rks.musicx.misc.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.rks.musicx.R;


/**
 * Created by Coolalien on 2/2/2016.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    int size = 75;
    private Drawable mDivider;

    public DividerItemDecoration(Context context, int paddingLeft) {
        this.size = paddingLeft;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false)) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.divider_white);
        } else {
            mDivider = ContextCompat.getDrawable(context, R.drawable.divider_black);
        }
        a.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft() + (size * 2);
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

}