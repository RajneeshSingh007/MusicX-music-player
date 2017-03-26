package com.afollestad.appthemeengine.util;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class RecyclerViewUtil {

    // External class is used after checking if RecyclerView is on the class path. Avoids compile errors.
    public static boolean isRecyclerView(View view) {
        return view instanceof RecyclerView;
    }

    private RecyclerViewUtil() {
    }
}
