package com.afollestad.appthemeengine.customizers;

import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.afollestad.appthemeengine.Config;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface ATEToolbarCustomizer {

    @Config.LightToolbarMode
    int getLightToolbarMode(@Nullable Toolbar toolbar);

    @ColorInt
    int getToolbarColor(@Nullable Toolbar toolbar);
}
