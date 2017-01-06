package com.cleveroad.audiowidget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Remove widget view.
 */
@SuppressLint("ViewConstructor")
class RemoveWidgetView extends View {

    static final float SCALE_DEFAULT = 1.0f;
    static final float SCALE_LARGE = 1.5f;

	private final float size;
	private final float radius;
	private final Paint paint;
    private final int defaultColor;
    private final int overlappedColor;
    private final ValueAnimator sizeAnimator;
    private float scale = 1.0f;

	public RemoveWidgetView(@NonNull Configuration configuration) {
		super(configuration.context());
		this.radius = configuration.radius();
		this.size = configuration.radius() * SCALE_LARGE * 2;
		this.paint = new Paint();
        this.defaultColor = configuration.crossColor();
        this.overlappedColor = configuration.crossOverlappedColor();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(configuration.crossStrokeWidth());
		paint.setColor(configuration.crossColor());
		paint.setStrokeCap(Paint.Cap.ROUND);
        sizeAnimator = new ValueAnimator();
        sizeAnimator.addUpdateListener(animation -> {
            scale = (float) animation.getAnimatedValue();
            invalidate();
        });
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int size = MeasureSpec.makeMeasureSpec((int) (this.size), MeasureSpec.EXACTLY);
		super.onMeasure(size, size);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int cx = canvas.getWidth() >> 1;
		int cy = canvas.getHeight() >> 1;
		float rad = radius * 0.75f;
        canvas.save();
        canvas.scale(scale, scale, cx, cy);
		canvas.drawCircle(cx, cy, rad, paint);
		drawCross(canvas, cx, cy, rad * 0.5f, 45);
        canvas.restore();
	}

	private void drawCross(@NonNull Canvas canvas, float cx, float cy, float radius, float startAngle) {
		drawLine(canvas, cx, cy, radius, startAngle);
		drawLine(canvas, cx, cy, radius, startAngle + 90);
	}

	private void drawLine(@NonNull Canvas canvas, float cx, float cy, float radius, float angle) {
		float x1 = DrawableUtils.rotateX(cx, cy + radius, cx, cy, angle);
		float y1 = DrawableUtils.rotateY(cx, cy + radius, cx, cy, angle);
		angle += 180;
		float x2 = DrawableUtils.rotateX(cx, cy + radius, cx, cy, angle);
		float y2 = DrawableUtils.rotateY(cx, cy + radius, cx, cy, angle);
		canvas.drawLine(x1, y1, x2, y2, paint);
	}

    /**
     * Set overlapped state.
     * @param overlapped true if widget overlapped, false otherwise
     */
    public void setOverlapped(boolean overlapped) {
        sizeAnimator.cancel();
        if (overlapped) {
            sizeAnimator.setFloatValues(scale, SCALE_LARGE);
            if (paint.getColor() != overlappedColor) {
                paint.setColor(overlappedColor);
                invalidate();
            }
        } else {
            sizeAnimator.setFloatValues(scale, SCALE_DEFAULT);
            if (paint.getColor() != defaultColor) {
                paint.setColor(defaultColor);
                invalidate();
            }
        }
        sizeAnimator.start();
    }
}
