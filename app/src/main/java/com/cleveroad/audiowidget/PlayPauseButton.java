package com.cleveroad.audiowidget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@SuppressLint("ViewConstructor")
class PlayPauseButton extends ImageView implements PlaybackState.PlaybackStateListener {

    static final long PROGRESS_CHANGES_DURATION = (long) (6 * Configuration.FRAME_SPEED);
    private static final float BUBBLES_ANGLE_STEP = 18.0f;
    private static final float ANIMATION_TIME_F = 8 * Configuration.FRAME_SPEED;
    private static final long ANIMATION_TIME_L = (long) ANIMATION_TIME_F;
    private static final float COLOR_ANIMATION_TIME_F = ANIMATION_TIME_F / 4f;
    private static final float COLOR_ANIMATION_TIME_START_F = (ANIMATION_TIME_F - COLOR_ANIMATION_TIME_F) / 2;
    private static final float COLOR_ANIMATION_TIME_END_F = COLOR_ANIMATION_TIME_START_F + COLOR_ANIMATION_TIME_F;
    private static final int TOTAL_BUBBLES_COUNT = (int) (360 / BUBBLES_ANGLE_STEP);
    private static final long PROGRESS_STEP_DURATION = (long) (3 * Configuration.FRAME_SPEED);
    private static final int ALBUM_COVER_PLACEHOLDER_ALPHA = 100;

    private final Paint albumPlaceholderPaint;
    private final Paint buttonPaint;
    private final Paint bubblesPaint;
    private final Paint progressPaint;
    private final int pausedColor;
    private final int playingColor;
    private final float[] bubbleSizes;
    private final float[] bubbleSpeeds;
    private final float[] bubbleSpeedCoefficients;
    private final Random random;
    private final ColorChanger colorChanger;
    private final Drawable playDrawable;
    private final Drawable pauseDrawable;
    private final RectF bounds;
    private final float radius;
    private final PlaybackState playbackState;
    private final ValueAnimator touchDownAnimator;
    private final ValueAnimator touchUpAnimator;
    private final ValueAnimator bubblesAnimator;
    private final ValueAnimator progressAnimator;
    private final float buttonPadding;
    private final float bubblesMinSize;
    private final float bubblesMaxSize;
    private final Map<Integer, Boolean> isNeedToFillAlbumCoverMap = new HashMap<>();
    private final float hsvArray[] = new float[3];
    private boolean animatingBubbles;
    private float randomStartAngle;
    private float buttonSize = 1.0f;
    private float progress = 0.0f;
    private float animatedProgress = 0;
    private boolean progressChangesEnabled;
    @Nullable
    private Drawable albumCover;
    @Nullable
    private AsyncTask lastPaletteAsyncTask;

    public PlayPauseButton(@NonNull Configuration configuration) {
        super(configuration.context());
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        this.playbackState = configuration.playbackState();
        this.random = configuration.random();
        this.buttonPaint = new Paint();
        this.buttonPaint.setColor(configuration.lightColor());
        this.buttonPaint.setStyle(Paint.Style.FILL);
        this.buttonPaint.setAntiAlias(true);
        this.buttonPaint.setShadowLayer(
                configuration.shadowRadius(),
                configuration.shadowDx(),
                configuration.shadowDy(),
                configuration.shadowColor()
        );
        this.bubblesMinSize = configuration.bubblesMinSize();
        this.bubblesMaxSize = configuration.bubblesMaxSize();
        this.bubblesPaint = new Paint();
        this.bubblesPaint.setStyle(Paint.Style.FILL);
        this.progressPaint = new Paint();
        this.progressPaint.setAntiAlias(true);
        this.progressPaint.setStyle(Paint.Style.STROKE);
        this.progressPaint.setStrokeWidth(configuration.progressStrokeWidth());
        this.progressPaint.setColor(configuration.progressColor());
        this.albumPlaceholderPaint = new Paint();
        this.albumPlaceholderPaint.setStyle(Paint.Style.FILL);
        this.albumPlaceholderPaint.setColor(configuration.lightColor());
        this.albumPlaceholderPaint.setAntiAlias(true);
        this.albumPlaceholderPaint.setAlpha(ALBUM_COVER_PLACEHOLDER_ALPHA);
        this.pausedColor = configuration.lightColor();
        this.playingColor = configuration.darkColor();
        this.radius = configuration.radius();
        this.buttonPadding = configuration.buttonPadding();
        this.bounds = new RectF();
        this.bubbleSizes = new float[TOTAL_BUBBLES_COUNT];
        this.bubbleSpeeds = new float[TOTAL_BUBBLES_COUNT];
        this.bubbleSpeedCoefficients = new float[TOTAL_BUBBLES_COUNT];
        this.colorChanger = new ColorChanger();
        this.playDrawable = configuration.playDrawable().getConstantState().newDrawable().mutate();
        this.pauseDrawable = configuration.pauseDrawable().getConstantState().newDrawable().mutate();
        this.pauseDrawable.setAlpha(0);
        this.playbackState.addPlaybackStateListener(this);
        final ValueAnimator.AnimatorUpdateListener listener = animation -> {
            buttonSize = (float) animation.getAnimatedValue();
            invalidate();
        };
        this.touchDownAnimator = ValueAnimator.ofFloat(1, 0.9f).setDuration(Configuration.TOUCH_ANIMATION_DURATION);
        this.touchDownAnimator.addUpdateListener(listener);
        this.touchUpAnimator = ValueAnimator.ofFloat(0.9f, 1).setDuration(Configuration.TOUCH_ANIMATION_DURATION);
        this.touchUpAnimator.addUpdateListener(listener);
        this.bubblesAnimator = ValueAnimator.ofInt(0, (int) ANIMATION_TIME_L).setDuration(ANIMATION_TIME_L);
        this.bubblesAnimator.setInterpolator(new LinearInterpolator());
        this.bubblesAnimator.addUpdateListener(animation -> {
            long position = animation.getCurrentPlayTime();
            float fraction = animation.getAnimatedFraction();
            updateBubblesPosition(position, fraction);
            invalidate();
        });
        this.bubblesAnimator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                animatingBubbles = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                animatingBubbles = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                animatingBubbles = false;
            }
        });
        this.progressAnimator = new ValueAnimator();
        this.progressAnimator.addUpdateListener(animation -> {
            animatedProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.makeMeasureSpec((int) (radius * 4), MeasureSpec.EXACTLY);
        super.onMeasure(size, size);
    }

    private void updateBubblesPosition(long position, float fraction) {
        int alpha = (int) DrawableUtils.customFunction(fraction, 0, 0, 0, 0.3f, 255, 0.5f, 225, 0.7f, 0, 1f);
        bubblesPaint.setAlpha(alpha);
        if (DrawableUtils.isBetween(position, COLOR_ANIMATION_TIME_START_F, COLOR_ANIMATION_TIME_END_F)) {
            float colorDt = DrawableUtils.normalize(position, COLOR_ANIMATION_TIME_START_F, COLOR_ANIMATION_TIME_END_F);
            buttonPaint.setColor(colorChanger.nextColor(colorDt));
            if (playbackState.state() == Configuration.STATE_PLAYING) {
                pauseDrawable.setAlpha((int) DrawableUtils.between(255 * colorDt, 0, 255));
                playDrawable.setAlpha((int) DrawableUtils.between(255 * (1 - colorDt), 0, 255));
            } else {
                playDrawable.setAlpha((int) DrawableUtils.between(255 * colorDt, 0, 255));
                pauseDrawable.setAlpha((int) DrawableUtils.between(255 * (1 - colorDt), 0, 255));
            }
        }
        for (int i = 0; i < TOTAL_BUBBLES_COUNT; i++) {
            bubbleSpeeds[i] = fraction * bubbleSpeedCoefficients[i];
        }
    }

    public void onClick() {
        if (isAnimationInProgress()) {
            return;
        }
        if (playbackState.state() == Configuration.STATE_PLAYING) {
            colorChanger
                    .fromColor(playingColor)
                    .toColor(pausedColor);
            bubblesPaint.setColor(pausedColor);
        } else {
            colorChanger
                    .fromColor(pausedColor)
                    .toColor(playingColor);
            bubblesPaint.setColor(playingColor);
        }
        startBubblesAnimation();
    }

    private void startBubblesAnimation() {
        randomStartAngle = 360 * random.nextFloat();
        for (int i = 0; i < TOTAL_BUBBLES_COUNT; i++) {
            float speed = 0.5f + 0.5f * random.nextFloat();
            float size = bubblesMinSize + (bubblesMaxSize - bubblesMinSize) * random.nextFloat();
            float radius = size / 2f;
            bubbleSizes[i] = radius;
            bubbleSpeedCoefficients[i] = speed;
        }
        bubblesAnimator.start();
    }

    public boolean isAnimationInProgress() {
        return animatingBubbles;
    }

    public void onTouchDown() {
        touchDownAnimator.start();
    }

    public void onTouchUp() {
        touchUpAnimator.start();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        float cx = getWidth() >> 1;
        float cy = getHeight() >> 1;
        canvas.scale(buttonSize, buttonSize, cx, cy);
        if (animatingBubbles) {
            for (int i = 0; i < TOTAL_BUBBLES_COUNT; i++) {
                float angle = randomStartAngle + BUBBLES_ANGLE_STEP * i;
                float speed = bubbleSpeeds[i];
                float x = DrawableUtils.rotateX(cx, cy * (1 - speed), cx, cy, angle);
                float y = DrawableUtils.rotateY(cx, cy * (1 - speed), cx, cy, angle);
                canvas.drawCircle(x, y, bubbleSizes[i], bubblesPaint);
            }
        } else if (playbackState.state() != Configuration.STATE_PLAYING) {
            playDrawable.setAlpha(255);
            pauseDrawable.setAlpha(0);
            // in case widget was drawn without animation in different state
            if (buttonPaint.getColor() != pausedColor) {
                buttonPaint.setColor(pausedColor);
            }
        } else {
            playDrawable.setAlpha(0);
            pauseDrawable.setAlpha(255);
            // in case widget was drawn without animation in different state
            if (buttonPaint.getColor() != playingColor) {
                buttonPaint.setColor(playingColor);
            }
        }

        canvas.drawCircle(cx, cy, radius, buttonPaint);
        if (albumCover != null && isNeedToFillAlbumCoverMap != null) {
            canvas.drawCircle(cx, cy, radius, buttonPaint);
            albumCover.setBounds((int) (cx - radius), (int) (cy - radius), (int) (cx + radius), (int) (cy + radius));
            Bitmap bitmap = DrawableUtils.drawableToBitmap(albumCover);
            if (bitmap != null && !bitmap.isRecycled()) {
                albumCover.draw(canvas);
            }
            if (albumCover.hashCode() > 0) {
                Boolean isNeedToFillAlbumCover = isNeedToFillAlbumCoverMap.get(albumCover.hashCode());
                if (isNeedToFillAlbumCover != null && isNeedToFillAlbumCover) {
                    canvas.drawCircle(cx, cy, radius, albumPlaceholderPaint);
                }
            }
        }

        float padding = progressPaint.getStrokeWidth() / 2f;
        bounds.set(cx - radius + padding, cy - radius + padding, cx + radius - padding, cy + radius - padding);
        canvas.drawArc(bounds, -90, animatedProgress, false, progressPaint);

        int l = (int) (cx - radius + buttonPadding);
        int t = (int) (cy - radius + buttonPadding);
        int r = (int) (cx + radius - buttonPadding);
        int b = (int) (cy + radius - buttonPadding);
        if (animatingBubbles || playbackState.state() != Configuration.STATE_PLAYING) {
            playDrawable.setBounds(l, t, r, b);
            playDrawable.draw(canvas);
        }
        if (animatingBubbles || playbackState.state() == Configuration.STATE_PLAYING) {
            pauseDrawable.setBounds(l, t, r, b);
            pauseDrawable.draw(canvas);
        }
    }

    @Override
    public void onStateChanged(int oldState, int newState, Object initiator) {
        if (initiator instanceof AudioWidget)
            return;
        if (newState == Configuration.STATE_PLAYING) {
            buttonPaint.setColor(playingColor);
            pauseDrawable.setAlpha(255);
            playDrawable.setAlpha(0);
        } else {
            buttonPaint.setColor(pausedColor);
            pauseDrawable.setAlpha(0);
            playDrawable.setAlpha(255);
        }
        postInvalidate();
    }

    @Override
    public void onProgressChanged(int position, int duration, float percentage) {
        if (percentage > progress) {
            float old = progress;
            post(() -> {
                if (animateProgressChanges(old * 360, percentage * 360, PROGRESS_STEP_DURATION)) {
                    progress = percentage;
                }
            });
        } else {
            this.progress = percentage;
            this.animatedProgress = percentage * 360;
            postInvalidate();
        }

    }

    public void enableProgressChanges(boolean enable) {
        if (progressChangesEnabled == enable)
            return;
        progressChangesEnabled = enable;
        if (progressChangesEnabled) {
            animateProgressChangesForce(0, progress * 360, PROGRESS_CHANGES_DURATION);
        } else {
            animateProgressChangesForce(progress * 360, 0, PROGRESS_CHANGES_DURATION);
        }
    }

    private void animateProgressChangesForce(float oldValue, float newValue, long duration) {
        if (progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
        animateProgressChanges(oldValue, newValue, duration);
    }

    private boolean animateProgressChanges(float oldValue, float newValue, long duration) {
        if (progressAnimator.isRunning()) {
            return false;
        }
        progressAnimator.setFloatValues(oldValue, newValue);
        progressAnimator.setDuration(duration);
        progressAnimator.start();
        return true;
    }

    public TouchManager.BoundsChecker newBoundsChecker(int offsetX, int offsetY) {
        return new BoundsCheckerImpl(radius, offsetX, offsetY);
    }

    public void albumCover(Drawable newAlbumCover) {
        if (this.albumCover == newAlbumCover) return;
        this.albumCover = newAlbumCover;
        if (albumCover.hashCode() == 0) {
            return;
        }
        if (!isNeedToFillAlbumCoverMap.containsKey(albumCover.hashCode())) {
            //Bitmap bitmap = ((BitmapDrawable) albumCover).getBitmap();
            Bitmap bitmap = DrawableUtils.drawableToBitmap(albumCover);
            if (bitmap != null && !bitmap.isRecycled()) {
                if (lastPaletteAsyncTask != null && !lastPaletteAsyncTask.isCancelled()) {
                    lastPaletteAsyncTask.cancel(true);
                }
                lastPaletteAsyncTask = Palette.from(bitmap).generate(palette -> {
                    int dominantColor = palette.getLightVibrantColor(Integer.MAX_VALUE);
                    if (dominantColor != Integer.MAX_VALUE) {
                        Color.colorToHSV(dominantColor, hsvArray);
                        isNeedToFillAlbumCoverMap.put(albumCover.hashCode(), hsvArray[2] > 0.65f);
                        postInvalidate();
                    }
                });
                // postInvalidate();
            }
        }
    }

    private static final class BoundsCheckerImpl extends AudioWidget.BoundsCheckerWithOffset {

        private float radius;

        BoundsCheckerImpl(float radius, int offsetX, int offsetY) {
            super(offsetX, offsetY);
            this.radius = radius;
        }

        @Override
        public float stickyLeftSideImpl(float screenWidth) {
            return -radius;
        }

        @Override
        public float stickyRightSideImpl(float screenWidth) {
            return screenWidth - radius * 3;
        }

        @Override
        public float stickyBottomSideImpl(float screenHeight) {
            return screenHeight - radius * 3;
        }

        @Override
        public float stickyTopSideImpl(float screenHeight) {
            return -radius;
        }
    }

}