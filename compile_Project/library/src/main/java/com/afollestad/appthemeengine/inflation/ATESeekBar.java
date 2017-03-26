package com.afollestad.appthemeengine.inflation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.tagprocessors.ATEDefaultTags;

/**
 * @author Aidan Follestad (afollestad)
 */
class ATESeekBar extends AppCompatSeekBar implements ViewInterface {

    public ATESeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public ATESeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public ATESeekBar(Context context, AttributeSet attrs, @Nullable ATEActivity keyContext) {
        super(context, attrs);
        init(context, keyContext);
    }

    private void init(Context context, @Nullable ATEActivity keyContext) {
        ATEDefaultTags.process(this);
        try {
            ATEViewUtil.init(keyContext, this, context);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
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