package com.afollestad.appthemeengine.prefs;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.R;
import com.afollestad.appthemeengine.tagprocessors.TextColorTagProcessor;
import com.afollestad.appthemeengine.tagprocessors.TextSizeTagProcessor;

public class ATEPreferenceCategory extends PreferenceCategory {

    private String mAteKey;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ATEPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mAteKey = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ATEPreferenceCategory, 0, 0).getString(R.styleable.ATEPreferenceCategory_ateKey_prefCategory_textColor);
    }

    public ATEPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAteKey = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ATEPreferenceCategory, 0, 0).getString(R.styleable.ATEPreferenceCategory_ateKey_prefCategory_textColor);
    }

    public ATEPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        mAteKey = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ATEPreferenceCategory, 0, 0).getString(R.styleable.ATEPreferenceCategory_ateKey_prefCategory_textColor);
    }

    public ATEPreferenceCategory(Context context, String ateKey) {
        super(context);
        this.mAteKey = ateKey;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView mTitle = (TextView) view.findViewById(android.R.id.title);
        mTitle.setTag(String.format("%s|body,%s|accent_color",
                TextSizeTagProcessor.PREFIX, TextColorTagProcessor.PREFIX));
        ATE.themeView(mTitle, mAteKey);
    }
}
