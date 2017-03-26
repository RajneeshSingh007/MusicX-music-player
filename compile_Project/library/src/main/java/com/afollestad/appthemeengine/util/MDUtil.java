package com.afollestad.appthemeengine.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.internal.ThemeSingleton;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class MDUtil {

    public static final String MAIN_CLASS = "com.afollestad.materialdialogs.MaterialDialog";

    public static void initMdSupport(@NonNull Context context, @Nullable String key) {
        final ThemeSingleton md = ThemeSingleton.get();
        md.titleColor = Config.textColorPrimary(context, key);
        md.contentColor = Config.textColorSecondary(context, key);
        md.itemColor = md.titleColor;
        md.widgetColor = Config.accentColor(context, key);
        md.linkColor = ColorStateList.valueOf(md.widgetColor);
        md.positiveColor = ColorStateList.valueOf(md.widgetColor);
        md.neutralColor = ColorStateList.valueOf(md.widgetColor);
        md.negativeColor = ColorStateList.valueOf(md.widgetColor);
    }

    private MDUtil() {
    }
}