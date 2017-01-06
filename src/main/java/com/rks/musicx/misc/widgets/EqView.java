package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.misc.utils.Helper;

/**
 * Created by Coolalien on 12/19/2016.
 */

public class EqView extends View{

    float midx, midy;
    Paint textPaint;
    Paint circlePaint;
    public Paint circlePaint2;
    public Paint linePaint;
    String angle,ateKey;
    float currdeg, deg = 3, downdeg;
    float dimension,dimension2,caldimension;
    int progressColor, lineColor;
    int accentColor;
    int max;

    onProgressChangedListener mListener;

    String label;

    public interface onProgressChangedListener {
        void onProgressChanged(int progress);
    }

    public void setOnProgressChangedListener(onProgressChangedListener listener) {
        mListener = listener;
    }

    public EqView(Context context) {
        super(context);
        init();
    }

    public EqView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EqView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        dimension = (float)display.getHeight() / (float) 1920;
        dimension2 = (float)display.getHeight() / (float) 1920;
        caldimension = Math.min(dimension,dimension2);
        textPaint.setTextSize(33 * caldimension);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        circlePaint = new Paint();
        circlePaint.setColor(Color.parseColor("#222222"));
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint.setTextAlign(Paint.Align.CENTER);
        circlePaint2 = new Paint();
        ateKey = Helper.getATEKey(getContext());
        accentColor = Config.accentColor(getContext(),ateKey);
        circlePaint2.setColor(accentColor);
        circlePaint2.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaint2.setTextAlign(Paint.Align.CENTER);
        linePaint = new Paint();
        linePaint.setColor(accentColor);
        linePaint.setStrokeWidth(7 * caldimension);
        angle = "0.0";
        label = "Label";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        midx = canvas.getWidth() / 2;
        midy = canvas.getHeight() / 2;

        float x, y;
        int radius = (int) (Math.min(midx, midy) * ((float) 14.5 / 16));
        float deg2 = Math.max(3, deg);
        float deg3 = Math.min(deg, 21);
        for (int i = (int) (deg2); i < 22; i++) {
            float tmp = (float) i / 24;
            x = midx + (float) (radius * Math.sin(2 * Math.PI * (1.0 - tmp)));
            y = midy + (float) (radius * Math.cos(2 * Math.PI * (1.0 - tmp)));
            circlePaint.setColor(Color.parseColor("#111111"));
            canvas.drawCircle(x, y, ((float) radius / 15), circlePaint);
        }
        for (int i = 3; i <= deg3; i++) {
            float tmp = (float) i / 24;
            x = midx + (float) (radius * Math.sin(2 * Math.PI * (1.0 - tmp)));
            y = midy + (float) (radius * Math.cos(2 * Math.PI * (1.0 - tmp)));
            canvas.drawCircle(x, y, ((float) radius / 15), circlePaint2);
        }

        float tmp2 = (float) deg / 24;
        float x1 = midx + (float) (radius * ((float) 2 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
        float y1 = midy + (float) (radius * ((float) 2 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));
        float x2 = midx + (float) (radius * ((float) 3 / 5) * Math.sin(2 * Math.PI * (1.0 - tmp2)));
        float y2 = midy + (float) (radius * ((float) 3 / 5) * Math.cos(2 * Math.PI * (1.0 - tmp2)));

        circlePaint.setColor(Color.parseColor("#222222"));
        canvas.drawCircle(midx, midy, radius * ((float) 13 / 15), circlePaint);
        circlePaint.setColor(Color.parseColor("#000000"));
        circlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawCircle(midx, midy, radius * ((float) 11 / 15), circlePaint);
        canvas.drawText(label, midx, midy + (float) (radius * 1.1), textPaint);
        canvas.drawLine(x1, y1, x2, y2, linePaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {

        mListener.onProgressChanged((int) (deg - 2));

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            float dx = e.getX() - midx;
            float dy = e.getY() - midy;
            downdeg = (float) ((Math.atan2(dy, dx) * 180) / Math.PI);
            downdeg -= 90;
            if (downdeg < 0) {
                downdeg += 360;
            }
            downdeg = (float) Math.floor(downdeg / 15);
            return true;
        }
        if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - midx;
            float dy = e.getY() - midy;
            currdeg = (float) ((Math.atan2(dy, dx) * 180) / Math.PI);
            currdeg -= 90;
            if (currdeg < 0) {
                currdeg += 360;
            }
            currdeg = (float) Math.floor(currdeg / 15);

            if (currdeg == 0 && downdeg == 23) {
                deg++;
                if (deg > 21) {
                    deg = 21;
                }
                downdeg = currdeg;
            } else if (currdeg == 23 && downdeg == 0) {
                deg--;
                if (deg < 3) {
                    deg = 3;
                }
                downdeg = currdeg;
            } else {
                deg += (currdeg - downdeg);
                if (deg > 21) {
                    deg = 21;
                }
                if (deg < 3) {
                    deg = 3;
                }
                downdeg = currdeg;
            }

            angle = String.valueOf(String.valueOf(deg));
            invalidate();
            return true;
        }
        if (e.getAction() == MotionEvent.ACTION_UP) {
            return true;
        }
        return super.onTouchEvent(e);
    }

    public int getProgress() {
        return (int) (deg - 2);
    }

    public void setProgress(int x) {
        deg = x + 2;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String txt) {
        label = txt;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public int getProgressColor() {
        return progressColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public void setTextPaint(Paint textPaint) {
        this.textPaint = textPaint;
    }

    public Paint getCirclePaint() {
        return circlePaint;
    }

    public Paint getCirclePaint2() {
        return circlePaint2;
    }

    public void setCirclePaint(Paint circlePaint) {
        this.circlePaint = circlePaint;
    }

    public void setCirclePaint2(Paint circlePaint2) {
        this.circlePaint2 = circlePaint2;
    }
}
