package com.afollestad.appthemeengine.customizers;

import android.support.annotation.StyleRes;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface ATEActivityThemeCustomizer {

    @StyleRes
    int getActivityTheme();
}