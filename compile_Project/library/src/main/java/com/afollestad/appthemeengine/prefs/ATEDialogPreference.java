package com.afollestad.appthemeengine.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.R;
import com.afollestad.materialdialogs.prefs.MaterialDialogPreference;

/**
 * @author Aidan Follestad (afollestad)
 */
public class ATEDialogPreference extends MaterialDialogPreference {

    public ATEDialogPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ATEDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ATEDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ATEDialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private String mKey;

    private void init(Context context, AttributeSet attrs) {
        setLayoutResource(R.layout.ate_preference_custom);

        if (attrs != null) {
            TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ATEDialogPreference, 0, 0);
            try {
                mKey = a.getString(R.styleable.ATEDialogPreference_ateKey_pref_dialog);
            } finally {
                a.recycle();
            }
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ATE.themeView(view, mKey);
    }
}