package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;

import com.rks.musicx.R;

/**
 * Created by Coolalien on 6/25/2016.
 */
public class TextView extends android.widget.TextView {

    private static TextView instance;
    private static Typeface headerTypeface;
    private static Typeface lightTypeFace;

    public TextView(Context context) {
        super(context);
    }

    public TextView(Context context, int defStyle) {
        super(context);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        postConstruct(context, attrs);
    }

    public TextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        postConstruct(context, attrs);
    }

    public static TextView getInstance(Context context) {
        synchronized (TextView.class) {
            if (instance == null) {
                instance = new TextView(context);
                headerTypeface = Typeface.createFromAsset(context.getApplicationContext().getResources().getAssets(), "Header.ttf");
            }
            return instance;
        }
    }

    private void postConstruct(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.TextView);
        setTypeface(TextView.getInstance(context.getApplicationContext())
                .getTypeFace(typedArray.getString(R.styleable.TextView_textType)));
        typedArray.recycle();
    }

    public Typeface getTypeFace(String fontType) {
        if (fontType != null && fontType.equals("header")) {
            return headerTypeface;
        }
        return headerTypeface;
    }
}
