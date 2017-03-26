package com.afollestad.appthemeengine.inflation;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.afollestad.appthemeengine.tagprocessors.ATEDefaultTags;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATECheckBox extends AppCompatCheckBox implements ViewInterface {

    public ATECheckBox(Context context) {
        super(context);
        init(context, null);
    }

    public ATECheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, null);
    }

    public ATECheckBox(Context context, AttributeSet attrs, @Nullable ATEActivity keyContext) {
        super(context, attrs);
        init(context, keyContext);
    }

    private void init(Context context, @Nullable ATEActivity keyContext) {
        ATEDefaultTags.process(this);
        ATEViewUtil.init(keyContext, this, context);
    }

    public void setKey(String key) {
        ATE.themeView(getContext(), this, key);
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
