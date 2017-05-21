package com.afollestad.appthemeengine.util;

import android.support.v4.widget.NestedScrollView;
import android.view.View;

/**
 * @author Aidan Follestad (afollestad)
 */
public final class NestedScrollViewUtil {

    // External class is used after checking if NestedScrollView is on the class path. Avoids compile errors.
    public static boolean isNestedScrollView(View view) {
        return view instanceof NestedScrollView;
    }

    private NestedScrollViewUtil() {
    }
}
