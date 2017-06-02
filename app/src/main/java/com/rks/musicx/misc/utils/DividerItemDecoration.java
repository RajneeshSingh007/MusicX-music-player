package com.rks.musicx.misc.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
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

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    int size = 75;
    private Drawable mDivider;

    public DividerItemDecoration(Context context, int paddingLeft, boolean darks) {
        this.size = paddingLeft;
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            mDivider = ContextCompat.getDrawable(context, R.drawable.divider_white);
        } else {
            if (darks) {
                mDivider = ContextCompat.getDrawable(context, R.drawable.divider_white);
            } else {
                mDivider = ContextCompat.getDrawable(context, R.drawable.divider_black);
            }
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