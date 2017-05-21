package com.cleveroad.audiowidget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.IntEvaluator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

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

class TouchManager implements View.OnTouchListener {

    private final View view;
    private final BoundsChecker boundsChecker;
    private final WindowManager windowManager;
    private final StickyEdgeAnimator stickyEdgeAnimator;
    private final FlingGestureAnimator velocityAnimator;

    private GestureListener gestureListener;
    private GestureDetector gestureDetector;
    private Callback callback;
    private int screenWidth;
    private int screenHeight;
    private Float lastRawX, lastRawY;
    private boolean touchCanceled;

    TouchManager(@NonNull View view, @NonNull BoundsChecker boundsChecker) {
        this.gestureDetector = new GestureDetector(view.getContext(), gestureListener = new GestureListener());
        gestureDetector.setIsLongpressEnabled(true);
        this.view = view;
        this.boundsChecker = boundsChecker;
        this.view.setOnTouchListener(this);
        Context context = view.getContext().getApplicationContext();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.screenHeight = context.getResources().getDisplayMetrics().heightPixels - getStatusBarHeight(context);
        stickyEdgeAnimator = new StickyEdgeAnimator();
        velocityAnimator = new FlingGestureAnimator();
    }

    public int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    TouchManager screenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
        return this;
    }

    TouchManager screenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
        return this;
    }

    TouchManager callback(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public boolean onTouch(@NonNull View v, @NonNull MotionEvent event) {
        boolean res = (!touchCanceled || event.getAction() == MotionEvent.ACTION_UP) && gestureDetector.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchCanceled = false;
            gestureListener.onDown(event);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!touchCanceled) {
                gestureListener.onUpEvent(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (!touchCanceled) {
                gestureListener.onMove(event);
            }
        } else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            gestureListener.onTouchOutsideEvent(event);
            touchCanceled = false;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            touchCanceled = true;
        }
        return res;
    }

    void animateToBounds(BoundsChecker boundsChecker, @Nullable Runnable afterAnimation) {
        stickyEdgeAnimator.animate(boundsChecker, afterAnimation);
    }

    void animateToBounds() {
        stickyEdgeAnimator.animate(boundsChecker, null);
    }

    interface Callback {

        void onClick(float x, float y);

        void onLongClick(float x, float y);

        void onTouchOutside();

        void onTouched(float x, float y);

        void onMoved(float diffX, float diffY);

        void onReleased(float x, float y);

        void onAnimationCompleted();
    }

    interface BoundsChecker {

        float stickyLeftSide(float screenWidth);

        float stickyRightSide(float screenWidth);

        float stickyTopSide(float screenHeight);

        float stickyBottomSide(float screenHeight);
    }

    static class SimpleCallback implements Callback {

        @Override
        public void onClick(float x, float y) {

        }

        @Override
        public void onLongClick(float x, float y) {

        }

        @Override
        public void onTouchOutside() {

        }

        @Override
        public void onTouched(float x, float y) {

        }

        @Override
        public void onMoved(float diffX, float diffY) {

        }

        @Override
        public void onReleased(float x, float y) {

        }

        @Override
        public void onAnimationCompleted() {

        }



    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private int prevX, prevY;
        private float velX, velY;
        private long lastEventTime;

        @Override
        public boolean onDown(MotionEvent e) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            prevX = params.x;
            prevY = params.y;
            boolean result = !stickyEdgeAnimator.isAnimating();
            if (result) {
                if (callback != null) {
                    callback.onTouched(e.getX(), e.getY());
                }
            }
            return result;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (callback != null) {
                callback.onClick(e.getX(), e.getY());
            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float diffX = e2.getRawX() - e1.getRawX();
            float diffY = e2.getRawY() - e1.getRawY();
            float l = prevX + diffX;
            float t = prevY + diffY;
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) view.getLayoutParams();
            params.x = (int) l;
            params.y = (int) t;
            try {
                windowManager.updateViewLayout(view, params);
            } catch (IllegalArgumentException e) {
                // view not attached to window
            }
            if (callback != null) {
                callback.onMoved(distanceX, distanceY);
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (callback != null) {
                callback.onLongClick(e.getX(), e.getY());
            }
            long downTime = SystemClock.uptimeMillis();
            long eventTime = SystemClock.uptimeMillis() + 100;
            float x = 0.0f;
            float y = 0.0f;
            int metaState = 0;
            MotionEvent event = MotionEvent.obtain(
                    downTime,
                    eventTime,
                    MotionEvent.ACTION_CANCEL,
                    x,
                    y,
                    metaState
            );
            view.dispatchTouchEvent(event);
//            onUpEvent(e);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            velocityAnimator.animate(velX, velY);
            return true;
        }

        private void onMove(MotionEvent e2) {
            if (lastRawX != null && lastRawY != null) {
                long diff = e2.getEventTime() - lastEventTime;
                float dt = diff == 0 ? 0 : 1000f / diff;
                float newVelX = (e2.getRawX() - lastRawX) * dt;
                float newVelY = (e2.getRawY() - lastRawY) * dt;
                velX = DrawableUtils.smooth(velX, newVelX, 0.2f);
                velY = DrawableUtils.smooth(velY, newVelY, 0.2f);
            }
            lastRawX = e2.getRawX();
            lastRawY = e2.getRawY();
            lastEventTime = e2.getEventTime();
        }

        private void onUpEvent(MotionEvent e) {
            if (callback != null) {
                callback.onReleased(e.getX(), e.getY());
            }
            lastRawX = null;
            lastRawY = null;
            lastEventTime = 0;
            velX = velY = 0;
            if (!velocityAnimator.isAnimating()) {
                stickyEdgeAnimator.animate(boundsChecker);
            }
        }

        private void onTouchOutsideEvent(MotionEvent e) {
            if (callback != null) {
                callback.onTouchOutside();
            }
        }
    }

    private class FlingGestureAnimator {
        private static final long DEFAULT_ANIM_DURATION = 200;
        private final ValueAnimator flingGestureAnimator;
        private final PropertyValuesHolder dxHolder;
        private final PropertyValuesHolder dyHolder;
        private final Interpolator interpolator;
        private WindowManager.LayoutParams params;

        FlingGestureAnimator() {
            interpolator = new DecelerateInterpolator();
            dxHolder = PropertyValuesHolder.ofFloat("x", 0, 0);
            dyHolder = PropertyValuesHolder.ofFloat("y", 0, 0);
            dxHolder.setEvaluator(new FloatEvaluator());
            dyHolder.setEvaluator(new FloatEvaluator());
            flingGestureAnimator = ValueAnimator.ofPropertyValuesHolder(dxHolder, dyHolder);
            flingGestureAnimator.setInterpolator(interpolator);
            flingGestureAnimator.setDuration(DEFAULT_ANIM_DURATION);
            flingGestureAnimator.addUpdateListener(animation -> {
                float newX = (float) animation.getAnimatedValue("x");
                float newY = (float) animation.getAnimatedValue("y");
                if (callback != null) {
                    callback.onMoved(newX - params.x, newY - params.y);
                }
                params.x = (int) newX;
                params.y = (int) newY;

                try {
                    windowManager.updateViewLayout(view, params);
                } catch (IllegalArgumentException e) {
                    animation.cancel();
                }
            });
            flingGestureAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    stickyEdgeAnimator.animate(boundsChecker);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    stickyEdgeAnimator.animate(boundsChecker);
                }
            });
        }

        void animate(float velocityX, float velocityY) {
            if (isAnimating()) {
                return;
            }

            params = (WindowManager.LayoutParams) view.getLayoutParams();

            float dx = velocityX / 1000f * DEFAULT_ANIM_DURATION;
            float dy = velocityY / 1000f * DEFAULT_ANIM_DURATION;

            final float newX, newY;

            if (dx + params.x > screenWidth / 2f) {
                newX = boundsChecker.stickyRightSide(screenWidth) + Math.min(view.getWidth(), view.getHeight()) / 2f;
            } else {
                newX = boundsChecker.stickyLeftSide(screenWidth) - Math.min(view.getWidth(), view.getHeight()) / 2f;
            }

            newY = params.y + dy;

            dxHolder.setFloatValues(params.x, newX);
            dyHolder.setFloatValues(params.y, newY);

            flingGestureAnimator.start();
        }

        boolean isAnimating() {
            return flingGestureAnimator.isRunning();
        }
    }

    private class StickyEdgeAnimator {
        private static final long DEFAULT_ANIM_DURATION = 300;
        private final PropertyValuesHolder dxHolder;
        private final PropertyValuesHolder dyHolder;
        private final ValueAnimator edgeAnimator;
        private final Interpolator interpolator;
        private WindowManager.LayoutParams params;

        public StickyEdgeAnimator() {
            interpolator = new OvershootInterpolator();
            dxHolder = PropertyValuesHolder.ofInt("x", 0, 0);
            dyHolder = PropertyValuesHolder.ofInt("y", 0, 0);
            dxHolder.setEvaluator(new IntEvaluator());
            dyHolder.setEvaluator(new IntEvaluator());
            edgeAnimator = ValueAnimator.ofPropertyValuesHolder(dxHolder, dyHolder);
            edgeAnimator.setInterpolator(interpolator);
            edgeAnimator.setDuration(DEFAULT_ANIM_DURATION);
            edgeAnimator.addUpdateListener(animation -> {
                int x = (int) animation.getAnimatedValue("x");
                int y = (int) animation.getAnimatedValue("y");
                if (callback != null) {
                    callback.onMoved(x - params.x, y - params.y);
                }
                params.x = x;
                params.y = y;
                try {
                    windowManager.updateViewLayout(view, params);
                } catch (IllegalArgumentException e) {
                    // view not attached to window
                    animation.cancel();
                }
            });
            edgeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (callback != null) {
                        callback.onAnimationCompleted();
                    }
                }
            });
        }

        private void animate(BoundsChecker boundsChecker) {
            animate(boundsChecker, null);
        }

        public void animate(BoundsChecker boundsChecker, @Nullable Runnable afterAnimation) {
            if (edgeAnimator.isRunning()) {
                return;
            }
            params = (WindowManager.LayoutParams) view.getLayoutParams();
            float cx = params.x + view.getWidth() / 2f;
            float cy = params.y + view.getWidth() / 2f;
            int x;
            if (cx < screenWidth / 2f) {
                x = (int) boundsChecker.stickyLeftSide(screenWidth);
            } else {
                x = (int) boundsChecker.stickyRightSide(screenWidth);
            }
            int y = params.y;
            int top = (int) boundsChecker.stickyTopSide(screenHeight);
            int bottom = (int) boundsChecker.stickyBottomSide(screenHeight);
            if (params.y > bottom || params.y < top) {
                if (cy < screenHeight / 2f) {
                    y = top;
                } else {
                    y = bottom;
                }
            }
            dxHolder.setIntValues(params.x, x);
            dyHolder.setIntValues(params.y, y);
            edgeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    edgeAnimator.removeListener(this);
                    if (afterAnimation != null) {
                        afterAnimation.run();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    edgeAnimator.removeListener(this);
                    if (afterAnimation != null) {
                        afterAnimation.run();
                    }
                }
            });
            edgeAnimator.start();
        }

        public boolean isAnimating() {
            return edgeAnimator.isRunning();
        }
    }
}
