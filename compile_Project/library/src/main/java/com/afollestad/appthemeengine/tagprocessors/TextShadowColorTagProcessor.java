package com.afollestad.appthemeengine.tagprocessors;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

/**
 * @author Aidan Follestad (afollestad)
 */
public class TextShadowColorTagProcessor extends TagProcessor {

    public static final String PREFIX = "text_color_shadow";

    @Override
    public boolean isTypeSupported(@NonNull View view) {
        return view instanceof TextView;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void process(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String suffix) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
            return;
        final TextView tv = (TextView) view;
        final ColorResult result = getColorFromSuffix(context, key, view, suffix);
        if (result == null) return;
        tv.setShadowLayer(tv.getShadowRadius(), tv.getShadowDx(), tv.getShadowDy(), result.getColor());
    }
}