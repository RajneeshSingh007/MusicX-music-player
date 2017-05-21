package com.afollestad.appthemeengine.customizers;

import android.support.annotation.ColorInt;

/**
 * CollapsingToolbarLayout customizer
 *
 * @author Aidan Follestad (afollestad)
 */
public interface ATECollapsingTbCustomizer {

    @ColorInt
    int getCollapsedTintColor();

    @ColorInt
    int getExpandedTintColor();
}
