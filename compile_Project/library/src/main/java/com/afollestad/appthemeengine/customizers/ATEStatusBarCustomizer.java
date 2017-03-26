package com.afollestad.appthemeengine.customizers;

import android.support.annotation.ColorInt;

import com.afollestad.appthemeengine.Config;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface ATEStatusBarCustomizer {

    @ColorInt
    int getStatusBarColor();

    @Config.LightStatusBarMode
    int getLightStatusBarMode();
}
