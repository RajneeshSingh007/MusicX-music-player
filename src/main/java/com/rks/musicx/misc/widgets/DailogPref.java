package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.util.AttributeSet;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class DailogPref extends android.preference.DialogPreference {

    public DailogPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        persistBoolean(positiveResult);
    }


}
