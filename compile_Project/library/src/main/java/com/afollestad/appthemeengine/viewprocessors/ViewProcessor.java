package com.afollestad.appthemeengine.viewprocessors;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * @author Aidan Follestad (afollestad)
 */
public interface ViewProcessor<T extends View, E> {

    void process(@NonNull Context context, @Nullable String key, @Nullable T target, @Nullable E extra);
}