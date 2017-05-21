package com.afollestad.appthemeengine.tagprocessors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.afollestad.appthemeengine.util.ATEUtil;

import java.lang.reflect.Method;

/**
 * @author Aidan Follestad (afollestad)
 */
public class BackgroundTagProcessor extends TagProcessor {

    public static final String PREFIX = "background";

    @Override
    public boolean isTypeSupported(@NonNull View view) {
        return true;
    }

    @Override
    public void process(@NonNull Context context, @Nullable String key, @NonNull View view, @NonNull String suffix) {
        final ColorResult result = getColorFromSuffix(context, key, view, suffix);
        if (result == null) return;

        if (ATEUtil.isInClassPath("android.support.v7.widget.CardView") &&
                (view.getClass().getName().equalsIgnoreCase("android.support.v7.widget.CardView") ||
                        view.getClass().getSuperclass().getName().equals("android.support.v7.widget.CardView"))) {
            try {
                final Class<?> cardViewCls = Class.forName("android.support.v7.widget.CardView");
                final Method setCardBg = cardViewCls.getMethod("setCardBackgroundColor", Integer.class);
                setCardBg.invoke(view, result.getColor());
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            view.setBackgroundColor(result.getColor());
        }
    }


}