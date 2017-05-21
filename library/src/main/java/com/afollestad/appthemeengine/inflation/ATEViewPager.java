package com.afollestad.appthemeengine.inflation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.tagprocessors.ATEDefaultTags;
import com.afollestad.appthemeengine.viewprocessors.DefaultProcessor;

/**
 * @author Aidan Follestad (afollestad)
 */
class ATEViewPager extends ViewPager implements ViewInterface {

    public ATEViewPager(Context context) {
        super(context);
        init(context, null);
    }

    public ATEViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public ATEViewPager(Context context, AttributeSet attrs, @Nullable ATEActivity keyContext) {
        super(context, attrs);
        init(context, keyContext);
    }

    private void init(Context context, @Nullable ATEActivity keyContext) {
        ATEDefaultTags.process(this);
        ATEViewUtil.init(keyContext, this, context);
    }

    @Override
    public boolean setsStatusBarColor() {
        return false;
    }

    @Override
    public boolean setsToolbarColor() {
        return false;
    }
}