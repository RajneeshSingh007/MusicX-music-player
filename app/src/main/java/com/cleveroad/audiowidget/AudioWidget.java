package com.cleveroad.audiowidget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.afollestad.appthemeengine.Config;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Helper;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

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

public class AudioWidget {

    private static final long VIBRATION_DURATION = 100;
    private final PlayPauseButton playPauseButton;

    private final ExpandCollapseWidget expandCollapseWidget;

    private final RemoveWidgetView removeWidgetView;
    private final Controller controller;
    private final WindowManager windowManager;
    private final Vibrator vibrator;
    private final Handler handler;
    private final Point screenSize;
    private final Context context;
    private final TouchManager playPauseButtonManager;
    private final TouchManager expandedWidgetManager;
    private final TouchManager.BoundsChecker ppbToExpBoundsChecker;
    private final TouchManager.BoundsChecker expToPpbBoundsChecker;
    private final Map<Integer, WeakReference<Drawable>> albumCoverCache = new WeakHashMap<>();
    private final RectF removeBounds;
    private final Point hiddenRemWidPos;
    private final Point visibleRemWidPos;
    private final OnControlsClickListenerWrapper onControlsClickListener;
    private PlaybackState playbackState;
    private int animatedRemBtnYPos = -1;
    private float widgetWidth, widgetHeight, radius;
    private boolean shown;
    private boolean released;
    private boolean removeWidgetShown;
    private OnWidgetStateChangedListener onWidgetStateChangedListener;
    private int accentColor;
    private int primaryColor;
    private String ateKey;

    @SuppressWarnings("deprecation")
    private AudioWidget(@NonNull Builder builder) {
        this.context = builder.context.getApplicationContext();
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        this.handler = new Handler();
        this.screenSize = new Point();
        this.removeBounds = new RectF();
        this.hiddenRemWidPos = new Point();
        this.visibleRemWidPos = new Point();
        this.controller = newController();
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(screenSize);
        screenSize.y -= statusBarHeight() + navigationBarHeight();

        Configuration configuration = prepareConfiguration(builder);
        playPauseButton = new PlayPauseButton(configuration);
        expandCollapseWidget = new ExpandCollapseWidget(configuration);
        removeWidgetView = new RemoveWidgetView(configuration);
        int offsetCollapsed = context.getResources().getDimensionPixelOffset(R.dimen.aw_edge_offset_collapsed);
        int offsetExpanded = context.getResources().getDimensionPixelOffset(R.dimen.aw_edge_offset_expanded);
        playPauseButtonManager = new TouchManager(playPauseButton, playPauseButton.newBoundsChecker(
                builder.edgeOffsetXCollapsedSet ? builder.edgeOffsetXCollapsed : offsetCollapsed,
                builder.edgeOffsetYCollapsedSet ? builder.edgeOffsetYCollapsed : offsetCollapsed
        ))
                .screenWidth(screenSize.x)
                .screenHeight(screenSize.y);
        expandedWidgetManager = new TouchManager(expandCollapseWidget, expandCollapseWidget.newBoundsChecker(
                builder.edgeOffsetXExpandedSet ? builder.edgeOffsetXExpanded : offsetExpanded,
                builder.edgeOffsetYExpandedSet ? builder.edgeOffsetYExpanded : offsetExpanded
        ))
                .screenWidth(screenSize.x)
                .screenHeight(screenSize.y);

        playPauseButtonManager.callback(new PlayPauseButtonCallback());
        expandedWidgetManager.callback(new ExpandCollapseWidgetCallback());
        expandCollapseWidget.onWidgetStateChangedListener(new OnWidgetStateChangedListener() {
            @Override
            public void onWidgetStateChanged(@NonNull State state) {
                if (state == State.COLLAPSED) {
                    playPauseButton.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    try {
                        windowManager.removeView(expandCollapseWidget);
                    } catch (IllegalArgumentException e) {
                        // view not attached to window
                    }
                    playPauseButton.enableProgressChanges(true);
                }
                if (onWidgetStateChangedListener != null) {
                    onWidgetStateChangedListener.onWidgetStateChanged(state);
                }
            }

            @Override
            public void onWidgetPositionChanged(int cx, int cy) {

            }
        });
        onControlsClickListener = new OnControlsClickListenerWrapper();
        expandCollapseWidget.onControlsClickListener(onControlsClickListener);
        ppbToExpBoundsChecker = playPauseButton.newBoundsChecker(
                builder.edgeOffsetXExpandedSet ? builder.edgeOffsetXExpanded : offsetExpanded,
                builder.edgeOffsetYExpandedSet ? builder.edgeOffsetYExpanded : offsetExpanded
        );
        expToPpbBoundsChecker = expandCollapseWidget.newBoundsChecker(
                builder.edgeOffsetXCollapsedSet ? builder.edgeOffsetXCollapsed : offsetCollapsed,
                builder.edgeOffsetYCollapsedSet ? builder.edgeOffsetYCollapsed : offsetCollapsed
        );
    }

    private Configuration prepareConfiguration(@NonNull Builder builder) {
        /*int darkColor = builder.darkColorSet ? builder.darkColor : ContextCompat.getColor(context, R.color.colorPrimary);
        int lightColor = builder.lightColorSet ? builder.lightColor : ContextCompat.getColor(context, R.color.colorAccent);
        int progressColor = builder.progressColorSet ? builder.progressColor : ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme);
        int expandColor = builder.expandWidgetColorSet ? builder.expandWidgetColor : ContextCompat.getColor(context, R.color.colorPrimary);
        int crossColor = builder.crossColorSet ? builder.crossColor : ContextCompat.getColor(context, R.color.colorAccent);
        int crossOverlappedColor = builder.crossOverlappedColorSet ? builder.crossOverlappedColor : ContextCompat.getColor(context, R.color.colorPrimaryLight);*/
        int shadowColor = builder.shadowColorSet ? builder.shadowColor : ContextCompat.getColor(context, R.color.shadowbg);

        Drawable playDrawable = builder.playDrawable != null ? builder.playDrawable : ContextCompat.getDrawable(context, R.drawable.aw_ic_play);
        Drawable pauseDrawable = builder.pauseDrawable != null ? builder.pauseDrawable : ContextCompat.getDrawable(context, R.drawable.aw_ic_pause);
        Drawable prevDrawable = builder.prevDrawable != null ? builder.prevDrawable : ContextCompat.getDrawable(context, R.drawable.aw_ic_prev);
        Drawable nextDrawable = builder.nextDrawable != null ? builder.nextDrawable : ContextCompat.getDrawable(context, R.drawable.aw_ic_next);
        Drawable playlistDrawable = builder.playlistDrawable != null ? builder.playlistDrawable : ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
        Drawable albumDrawable = builder.defaultAlbumDrawable != null ? builder.defaultAlbumDrawable : ContextCompat.getDrawable(context, R.mipmap.ic_launcher);

        int buttonPadding = builder.buttonPaddingSet ? builder.buttonPadding : context.getResources().getDimensionPixelSize(R.dimen.aw_button_padding);
        float crossStrokeWidth = builder.crossStrokeWidthSet ? builder.crossStrokeWidth : context.getResources().getDimension(R.dimen.aw_cross_stroke_width);
        float progressStrokeWidth = builder.progressStrokeWidthSet ? builder.progressStrokeWidth : context.getResources().getDimension(R.dimen.aw_progress_stroke_width);
        float shadowRadius = builder.shadowRadiusSet ? builder.shadowRadius : context.getResources().getDimension(R.dimen.aw_shadow_radius);
        float shadowDx = builder.shadowDxSet ? builder.shadowDx : context.getResources().getDimension(R.dimen.aw_shadow_dx);
        float shadowDy = builder.shadowDySet ? builder.shadowDy : context.getResources().getDimension(R.dimen.aw_shadow_dy);
        float bubblesMinSize = builder.bubblesMinSizeSet ? builder.bubblesMinSize : context.getResources().getDimension(R.dimen.aw_bubbles_min_size);
        float bubblesMaxSize = builder.bubblesMaxSizeSet ? builder.bubblesMaxSize : context.getResources().getDimension(R.dimen.aw_bubbles_max_size);
        int prevNextExtraPadding = context.getResources().getDimensionPixelSize(R.dimen.aw_prev_next_button_extra_padding);

        widgetHeight = context.getResources().getDimensionPixelSize(R.dimen.aw_player_height);
        widgetWidth = context.getResources().getDimensionPixelSize(R.dimen.aw_player_width);
        radius = widgetHeight / 2f;
        playbackState = new PlaybackState();
        ateKey = Helper.getATEKey(context);
        accentColor = Config.accentColor(context, ateKey);
        primaryColor = Config.primaryColor(context, ateKey);
        return new Configuration.Builder()
                .context(context)
                .playbackState(playbackState)
                .random(new Random())
                .accDecInterpolator(new AccelerateDecelerateInterpolator())
                .darkColor(primaryColor)
                .playColor(primaryColor)
                .progressColor(accentColor)
                .expandedColor(primaryColor)
                .widgetWidth(widgetWidth)
                .radius(radius)
                .playlistDrawable(playlistDrawable)
                .playDrawable(playDrawable)
                .prevDrawable(prevDrawable)
                .nextDrawable(nextDrawable)
                .pauseDrawable(pauseDrawable)
                .albumDrawable(albumDrawable)
                .buttonPadding(buttonPadding)
                .prevNextExtraPadding(prevNextExtraPadding)
                .crossStrokeWidth(crossStrokeWidth)
                .progressStrokeWidth(progressStrokeWidth)
                .shadowRadius(shadowRadius)
                .shadowDx(shadowDx)
                .shadowDy(shadowDy)
                .shadowColor(shadowColor)
                .bubblesMinSize(bubblesMinSize)
                .bubblesMaxSize(bubblesMaxSize)
                .crossColor(accentColor)
                .crossOverlappedColor(accentColor)
                .build();
    }

    private int statusBarHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int navigationBarHeight() {
        if (hasNavigationBar()) {
            int result = 0;
            int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
            return result;
        }
        return 0;
    }

    private boolean hasNavigationBar() {
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
        int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        return !hasBackKey && !hasHomeKey || id > 0 && context.getResources().getBoolean(id);
    }

    @NonNull
    private Controller newController() {
        return new Controller() {

            @Override
            public void start() {
                playbackState.start(this);
            }

            @Override
            public void pause() {
                playbackState.pause(this);
            }

            @Override
            public void stop() {
                playbackState.stop(this);
            }

            @Override
            public int duration() {
                return playbackState.duration();
            }

            @Override
            public void duration(int duration) {
                playbackState.duration(duration);
            }

            @Override
            public int position() {
                return playbackState.position();
            }

            @Override
            public void position(int position) {
                playbackState.position(position);
            }

            @Override
            public void onControlsClickListener(@Nullable OnControlsClickListener onControlsClickListener) {
                AudioWidget.this.onControlsClickListener.onControlsClickListener(onControlsClickListener);
            }

            @Override
            public void onWidgetStateChangedListener(@Nullable OnWidgetStateChangedListener onWidgetStateChangedListener) {
                AudioWidget.this.onWidgetStateChangedListener = onWidgetStateChangedListener;
            }

            @Override
            public void albumCover(@Nullable Drawable albumCover) {
                expandCollapseWidget.albumCover(albumCover);
                playPauseButton.albumCover(albumCover);
            }

            @Override
            public void albumCoverBitmap(@Nullable Bitmap bitmap) {
                if (bitmap == null) {
                    return;
                }
                WeakReference<Drawable> wrDrawable = albumCoverCache.get(bitmap.hashCode());
                if (wrDrawable != null) {
                    Drawable drawable = wrDrawable.get();
                    if (drawable != null) {
                        expandCollapseWidget.albumCover(drawable);
                        playPauseButton.albumCover(drawable);
                        return;
                    }
                }
                Drawable albumCover = new BitmapDrawable(context.getResources(), bitmap);
                expandCollapseWidget.albumCover(albumCover);
                playPauseButton.albumCover(albumCover);
                albumCoverCache.put(bitmap.hashCode(), new WeakReference<>(albumCover));
            }
        };
    }

    public void show(int cx, int cy) {
        shown = true;
        widgetHeight = context.getResources().getDimensionPixelSize(R.dimen.aw_player_height);
        widgetWidth = context.getResources().getDimensionPixelSize(R.dimen.aw_player_width);
        float remWidX = screenSize.x / 2f - radius * RemoveWidgetView.SCALE_LARGE;
        hiddenRemWidPos.set((int) remWidX, (int) (screenSize.y + widgetHeight + navigationBarHeight()));
        visibleRemWidPos.set((int) remWidX, (int) (screenSize.y - radius - (hasNavigationBar() ? 0 : widgetHeight)));
        try {
            show(removeWidgetView, hiddenRemWidPos.x, hiddenRemWidPos.y);
        } catch (IllegalArgumentException e) {
            // widget not removed yet, animation in progress
        }
        show(playPauseButton, (int) (cx - widgetHeight), (int) (cy - widgetHeight));
        playPauseButtonManager.animateToBounds();
    }

    public void hide() {
        hideInternal(true);
    }

    private void hideInternal(boolean byPublic) {
        if (!shown) {
            return;
        }
        shown = false;
        released = true;
        if (byPublic) {
            try {
                windowManager.removeView(playPauseButton);
                windowManager.removeView(expandCollapseWidget);
                windowManager.removeView(removeWidgetView);
            } catch (IllegalArgumentException e) {
                // view not attached to window
            }
            if (onWidgetStateChangedListener != null) {
                onWidgetStateChangedListener.onWidgetStateChanged(State.REMOVED);
                // onWidgetStateChangedListener.onWidgetPositionChanged(100, 100);
            }
        }
    }

    public boolean isShown() {
        return shown;
    }

    public void expand() {
        removeWidgetShown = false;
        playPauseButton.enableProgressChanges(false);
        playPauseButton.postDelayed(this::checkSpaceAndShowExpanded, PlayPauseButton.PROGRESS_CHANGES_DURATION);
    }

    public void collapse() {
        expandCollapseWidget.setCollapseListener(playPauseButton::setAlpha);

        WindowManager.LayoutParams params = (WindowManager.LayoutParams) expandCollapseWidget.getLayoutParams();
        int cx = params.x + expandCollapseWidget.getWidth() / 2;
        if (cx > screenSize.x / 2) {
            expandCollapseWidget.expandDirection(ExpandCollapseWidget.DIRECTION_LEFT);
        } else {
            expandCollapseWidget.expandDirection(ExpandCollapseWidget.DIRECTION_RIGHT);
        }
        updatePlayPauseButtonPosition();
        if (expandCollapseWidget.collapse()) {
            playPauseButtonManager.animateToBounds();
            expandedWidgetManager.animateToBounds(expToPpbBoundsChecker, null);
        }
    }

    private void updatePlayPauseButtonPosition() {
        WindowManager.LayoutParams widgetParams = (WindowManager.LayoutParams) expandCollapseWidget.getLayoutParams();
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
        if (expandCollapseWidget.expandDirection() == ExpandCollapseWidget.DIRECTION_RIGHT) {
            params.x = (int) (widgetParams.x - radius);
        } else {
            params.x = (int) (widgetParams.x + widgetWidth - widgetHeight - radius);
        }
        params.y = widgetParams.y;
        try {
            windowManager.updateViewLayout(playPauseButton, params);
        } catch (IllegalArgumentException e) {
            // view not attached to window
        }
        if (onWidgetStateChangedListener != null) {
            onWidgetStateChangedListener.onWidgetPositionChanged((int) (params.x + widgetHeight), (int) (params.y + widgetHeight));
        }
    }

    @SuppressWarnings("deprecation")
    private void checkSpaceAndShowExpanded() {
        WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
        int x = params.x;
        int y = params.y;
        int expandDirection;
        if (x + widgetHeight > screenSize.x / 2) {
            expandDirection = ExpandCollapseWidget.DIRECTION_LEFT;
        } else {
            expandDirection = ExpandCollapseWidget.DIRECTION_RIGHT;
        }

        playPauseButtonManager.animateToBounds(ppbToExpBoundsChecker, () -> {
            WindowManager.LayoutParams params1 = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
            int x1 = params1.x;
            int y1 = params1.y;
            if (expandDirection == ExpandCollapseWidget.DIRECTION_LEFT) {
                x1 -= widgetWidth - widgetHeight * 1.5f;
            } else {
                x1 += widgetHeight / 2f;
            }
            show(expandCollapseWidget, x1, y1);
            playPauseButton.setLayerType(View.LAYER_TYPE_NONE, null);

            expandCollapseWidget.setExpandListener(percent -> playPauseButton.setAlpha(1f - percent));
            expandCollapseWidget.expand(expandDirection);
        });
    }

    @NonNull
    public Controller controller() {
        return controller;
    }

    public void setAlbumArt(@NonNull Bitmap bitmap) {
        if (controller != null) {
            controller.albumCoverBitmap(bitmap);
        }
    }

    public void Pause() {
        if (controller != null) {
            controller.pause();
        }
    }

    public void Start() {
        if (controller != null) {
            controller.start();
        }
    }

    public void Stop() {
        if (controller != null) {
            controller.stop();
        }
    }

    public void Pos(int pos) {
        if (controller != null) {
            controller.position(pos);
        }
    }

    public void cleanUp() {
        if (controller != null) {
            controller.onControlsClickListener(null);
            controller.onWidgetStateChangedListener(null);
        }
    }

    public void Dur(int dur) {
        if (controller != null) {
            controller.duration(dur);
        }
    }

    private void show(View view, int left, int top) {
        if (view.getParent() != null || view.getWindowToken() != null) {
            windowManager.removeView(view);
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = left;
        params.y = top;
        windowManager.addView(view, params);
    }

    public Map<Integer, WeakReference<Drawable>> getAlbumCoverCache() {
        return albumCoverCache;
    }

    public Controller getController() {
        return controller;
    }

    public enum State {
        COLLAPSED,
        EXPANDED,
        REMOVED
    }

    public interface Controller {

        void start();

        void pause();

        void stop();

        int duration();

        void duration(int duration);

        int position();

        void position(int position);

        void onControlsClickListener(@Nullable OnControlsClickListener onControlsClickListener);

        void onWidgetStateChangedListener(@Nullable OnWidgetStateChangedListener onWidgetStateChangedListener);

        void albumCover(@Nullable Drawable albumCover);

        void albumCoverBitmap(@Nullable Bitmap albumCover);
    }

    public interface OnControlsClickListener {

        boolean onPlaylistClicked();

        void onPlaylistLongClicked();

        void onPreviousClicked();

        void onPreviousLongClicked();

        boolean onPlayPauseClicked();

        void onPlayPauseLongClicked();

        void onNextClicked();

        void onNextLongClicked();

        void onAlbumClicked();

        void onAlbumLongClicked();
    }

    public interface OnWidgetStateChangedListener {

        void onWidgetStateChanged(@NonNull State state);

        void onWidgetPositionChanged(int cx, int cy);
    }

    abstract static class BoundsCheckerWithOffset implements TouchManager.BoundsChecker {

        private int offsetX, offsetY;

        public BoundsCheckerWithOffset(int offsetX, int offsetY) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        @Override
        public final float stickyLeftSide(float screenWidth) {
            return stickyLeftSideImpl(screenWidth) + offsetX;
        }

        @Override
        public final float stickyRightSide(float screenWidth) {
            return stickyRightSideImpl(screenWidth) - offsetX;
        }

        @Override
        public final float stickyTopSide(float screenHeight) {
            return stickyTopSideImpl(screenHeight) + offsetY;
        }

        @Override
        public final float stickyBottomSide(float screenHeight) {
            return stickyBottomSideImpl(screenHeight) - offsetY;
        }

        protected abstract float stickyLeftSideImpl(float screenWidth);

        protected abstract float stickyRightSideImpl(float screenWidth);

        protected abstract float stickyTopSideImpl(float screenHeight);

        protected abstract float stickyBottomSideImpl(float screenHeight);
    }

    public static class Builder {

        private final Context context;

        @ColorInt
        private int darkColor;
        @ColorInt
        private int lightColor;
        @ColorInt
        private int progressColor;
        @ColorInt
        private int crossColor;
        @ColorInt
        private int crossOverlappedColor;
        @ColorInt
        private int shadowColor;
        @ColorInt
        private int expandWidgetColor;
        private int buttonPadding;
        private float crossStrokeWidth;
        private float progressStrokeWidth;
        private float shadowRadius;
        private float shadowDx;
        private float shadowDy;
        private float bubblesMinSize;
        private float bubblesMaxSize;
        private Drawable playDrawable;
        private Drawable prevDrawable;
        private Drawable nextDrawable;
        private Drawable playlistDrawable;
        private Drawable defaultAlbumDrawable;
        private Drawable pauseDrawable;
        private boolean darkColorSet;
        private boolean lightColorSet;
        private boolean progressColorSet;
        private boolean crossColorSet;
        private boolean crossOverlappedColorSet;
        private boolean shadowColorSet;
        private boolean expandWidgetColorSet;
        private boolean buttonPaddingSet;
        private boolean crossStrokeWidthSet;
        private boolean progressStrokeWidthSet;
        private boolean shadowRadiusSet;
        private boolean shadowDxSet;
        private boolean shadowDySet;
        private boolean bubblesMinSizeSet;
        private boolean bubblesMaxSizeSet;
        private int edgeOffsetXCollapsed;
        private int edgeOffsetYCollapsed;
        private int edgeOffsetXExpanded;
        private int edgeOffsetYExpanded;
        private boolean edgeOffsetXCollapsedSet;
        private boolean edgeOffsetYCollapsedSet;
        private boolean edgeOffsetXExpandedSet;
        private boolean edgeOffsetYExpandedSet;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public Builder darkColor(@ColorInt int darkColor) {
            this.darkColor = darkColor;
            darkColorSet = true;
            return this;
        }

        public Builder lightColor(@ColorInt int lightColor) {
            this.lightColor = lightColor;
            lightColorSet = true;
            return this;
        }

        public Builder progressColor(@ColorInt int progressColor) {
            this.progressColor = progressColor;
            progressColorSet = true;
            return this;
        }

        public Builder crossColor(@ColorInt int crossColor) {
            this.crossColor = crossColor;
            crossColorSet = true;
            return this;
        }

        public Builder crossOverlappedColor(@ColorInt int crossOverlappedColor) {
            this.crossOverlappedColor = crossOverlappedColor;
            crossOverlappedColorSet = true;
            return this;
        }

        public Builder shadowColor(@ColorInt int shadowColor) {
            this.shadowColor = shadowColor;
            shadowColorSet = true;
            return this;
        }

        public Builder expandWidgetColor(@ColorInt int expandWidgetColor) {
            this.expandWidgetColor = expandWidgetColor;
            expandWidgetColorSet = true;
            return this;
        }

        public Builder buttonPadding(int buttonPadding) {
            this.buttonPadding = buttonPadding;
            buttonPaddingSet = true;
            return this;
        }

        public Builder crossStrokeWidth(float crossStrokeWidth) {
            this.crossStrokeWidth = crossStrokeWidth;
            crossStrokeWidthSet = true;
            return this;
        }

        public Builder progressStrokeWidth(float progressStrokeWidth) {
            this.progressStrokeWidth = progressStrokeWidth;
            progressStrokeWidthSet = true;
            return this;
        }

        public Builder shadowRadius(float shadowRadius) {
            this.shadowRadius = shadowRadius;
            shadowRadiusSet = true;
            return this;
        }

        public Builder shadowDx(float shadowDx) {
            this.shadowDx = shadowDx;
            shadowDxSet = true;
            return this;
        }

        public Builder shadowDy(float shadowDy) {
            this.shadowDy = shadowDy;
            shadowDySet = true;
            return this;
        }

        public Builder bubblesMinSize(float bubblesMinSize) {
            this.bubblesMinSize = bubblesMinSize;
            bubblesMinSizeSet = true;
            return this;
        }

        public Builder bubblesMaxSize(float bubblesMaxSize) {
            this.bubblesMaxSize = bubblesMaxSize;
            bubblesMaxSizeSet = true;
            return this;
        }

        public Builder playDrawable(@NonNull Drawable playDrawable) {
            this.playDrawable = playDrawable;
            return this;
        }

        public Builder prevTrackDrawale(@NonNull Drawable prevDrawable) {
            this.prevDrawable = prevDrawable;
            return this;
        }

        public Builder nextTrackDrawable(@NonNull Drawable nextDrawable) {
            this.nextDrawable = nextDrawable;
            return this;
        }

        public Builder playlistDrawable(@NonNull Drawable playlistDrawable) {
            this.playlistDrawable = playlistDrawable;
            return this;
        }

        public Builder defaultAlbumDrawable(@NonNull Drawable defaultAlbumCover) {
            this.defaultAlbumDrawable = defaultAlbumCover;
            return this;
        }

        public Builder pauseDrawable(@NonNull Drawable pauseDrawable) {
            this.pauseDrawable = pauseDrawable;
            return this;
        }

        public Builder edgeOffsetXCollapsed(int edgeOffsetX) {
            this.edgeOffsetXCollapsed = edgeOffsetX;
            edgeOffsetXCollapsedSet = true;
            return this;
        }

        public Builder edgeOffsetYCollapsed(int edgeOffsetY) {
            this.edgeOffsetYCollapsed = edgeOffsetY;
            edgeOffsetYCollapsedSet = true;
            return this;
        }

        public Builder edgeOffsetYExpanded(int edgeOffsetY) {
            this.edgeOffsetYExpanded = edgeOffsetY;
            edgeOffsetYExpandedSet = true;
            return this;
        }

        public Builder edgeOffsetXExpanded(int edgeOffsetX) {
            this.edgeOffsetXExpanded = edgeOffsetX;
            edgeOffsetXExpandedSet = true;
            return this;
        }

        public AudioWidget build() {
            if (buttonPaddingSet) {
                checkOrThrow(buttonPadding, "Button padding");
            }
            if (shadowRadiusSet) {
                checkOrThrow(shadowRadius, "Shadow radius");
            }
            if (shadowDxSet) {
                checkOrThrow(shadowDx, "Shadow dx");
            }
            if (shadowDySet) {
                checkOrThrow(shadowDy, "Shadow dy");
            }
            if (bubblesMinSizeSet) {
                checkOrThrow(bubblesMinSize, "Bubbles min size");
            }
            if (bubblesMaxSizeSet) {
                checkOrThrow(bubblesMaxSize, "Bubbles max size");
            }
            if (bubblesMinSizeSet && bubblesMaxSizeSet && bubblesMaxSize < bubblesMinSize) {
                throw new IllegalArgumentException("Bubbles max size must be greater than bubbles min size");
            }
            if (crossStrokeWidthSet) {
                checkOrThrow(crossStrokeWidth, "Cross stroke width");
            }
            if (progressStrokeWidthSet) {
                checkOrThrow(progressStrokeWidth, "Progress stroke width");
            }
            return new AudioWidget(this);
        }

        private void checkOrThrow(int number, String name) {
            if (number < 0)
                throw new IllegalArgumentException(name + " must be equals or greater zero.");
        }

        private void checkOrThrow(float number, String name) {
            if (number < 0)
                throw new IllegalArgumentException(name + " must be equals or greater zero.");
        }

    }

    private class PlayPauseButtonCallback extends TouchManager.SimpleCallback {

        private static final long REMOVE_BTN_ANIM_DURATION = 200;
        private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener;
        private boolean readyToRemove;

        PlayPauseButtonCallback() {
            animatorUpdateListener = animation -> {
                if (!removeWidgetShown) {
                    return;
                }
                animatedRemBtnYPos = (int) ((float) animation.getAnimatedValue());
                //updateRemoveBtnPosition();
            };
        }

        @Override
        public void onClick(float x, float y) {
            playPauseButton.onClick();
            if (onControlsClickListener != null) {
                onControlsClickListener.onPlayPauseClicked();
            }
        }

        @Override
        public void onLongClick(float x, float y) {
            released = true;
            expand();
        }

        @Override
        public void onTouched(float x, float y) {
            super.onTouched(x, y);
            released = false;
            handler.postDelayed(() -> {
                if (!released) {
                    removeWidgetShown = true;
                    ValueAnimator animator = ValueAnimator.ofFloat(hiddenRemWidPos.y, visibleRemWidPos.y);
                    animator.setDuration(REMOVE_BTN_ANIM_DURATION);
                    animator.addUpdateListener(animatorUpdateListener);
                    animator.start();
                }
            }, Configuration.LONG_CLICK_THRESHOLD);
            playPauseButton.onTouchDown();
        }

        @Override
        public void onMoved(float diffX, float diffY) {
            super.onMoved(diffX, diffY);
            boolean curReadyToRemove = isReadyToRemove();
            if (curReadyToRemove != readyToRemove) {
                readyToRemove = curReadyToRemove;
                removeWidgetView.setOverlapped(readyToRemove);
                if (readyToRemove && vibrator.hasVibrator()) {
                    vibrator.vibrate(VIBRATION_DURATION);
                }
            }
            updateRemoveBtnPosition();
        }

        private void updateRemoveBtnPosition() {
            if (removeWidgetShown) {
                WindowManager.LayoutParams playPauseBtnParams = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
                WindowManager.LayoutParams removeBtnParams = (WindowManager.LayoutParams) removeWidgetView.getLayoutParams();

                double tgAlpha = (screenSize.x / 2. - playPauseBtnParams.x) / (visibleRemWidPos.y - playPauseBtnParams.y);
                double rotationDegrees = 360 - Math.toDegrees(Math.atan(tgAlpha));

                float distance = (float) Math.sqrt(Math.pow(animatedRemBtnYPos - playPauseBtnParams.y, 2) +
                        Math.pow(visibleRemWidPos.x - hiddenRemWidPos.x, 2));
                float maxDistance = (float) Math.sqrt(Math.pow(screenSize.x, 2) + Math.pow(screenSize.y, 2));
                distance /= maxDistance;

                if (animatedRemBtnYPos == -1) {
                    animatedRemBtnYPos = visibleRemWidPos.y;
                }

                removeBtnParams.x = (int) DrawableUtils.rotateX(
                        visibleRemWidPos.x, animatedRemBtnYPos - radius * distance,
                        hiddenRemWidPos.x, animatedRemBtnYPos, (float) rotationDegrees);
                removeBtnParams.y = (int) DrawableUtils.rotateY(
                        visibleRemWidPos.x, animatedRemBtnYPos - radius * distance,
                        hiddenRemWidPos.x, animatedRemBtnYPos, (float) rotationDegrees);

                try {
                    windowManager.updateViewLayout(removeWidgetView, removeBtnParams);
                } catch (IllegalArgumentException e) {
                    // view not attached to window
                }
            }
        }

        @Override
        public void onReleased(float x, float y) {
            super.onReleased(x, y);
            playPauseButton.onTouchUp();
            released = true;
            if (removeWidgetShown) {
                ValueAnimator animator = ValueAnimator.ofFloat(visibleRemWidPos.y, hiddenRemWidPos.y);
                animator.setDuration(REMOVE_BTN_ANIM_DURATION);
                animator.addUpdateListener(animatorUpdateListener);
                animator.addListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        removeWidgetShown = false;
                        if (!shown) {
                            try {
                                windowManager.removeView(removeWidgetView);
                            } catch (IllegalArgumentException e) {
                                // view not attached to window
                            }
                        }
                    }
                });
                animator.start();
            }
            if (isReadyToRemove()) {
                hideInternal(true);
            } else {
                if (onWidgetStateChangedListener != null) {
                    WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
                    onWidgetStateChangedListener.onWidgetPositionChanged((int) (params.x + widgetHeight), (int) (params.y + widgetHeight));
                }
            }
        }

        @Override
        public void onAnimationCompleted() {
            super.onAnimationCompleted();
            if (onWidgetStateChangedListener != null) {
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
                onWidgetStateChangedListener.onWidgetPositionChanged((int) (params.x + widgetHeight), (int) (params.y + widgetHeight));
            }
        }

        private boolean isReadyToRemove() {
            WindowManager.LayoutParams removeParams = (WindowManager.LayoutParams) removeWidgetView.getLayoutParams();
            removeBounds.set(removeParams.x, removeParams.y, removeParams.x + widgetHeight, removeParams.y + widgetHeight);
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) playPauseButton.getLayoutParams();
            float cx = params.x + widgetHeight;
            float cy = params.y + widgetHeight;
            return removeBounds.contains(cx, cy);
        }
    }

    private class ExpandCollapseWidgetCallback extends TouchManager.SimpleCallback {

        @Override
        public void onTouched(float x, float y) {
            super.onTouched(x, y);
            expandCollapseWidget.onTouched(x, y);
        }

        @Override
        public void onReleased(float x, float y) {
            super.onReleased(x, y);
            expandCollapseWidget.onReleased(x, y);
        }

        @Override
        public void onClick(float x, float y) {
            super.onClick(x, y);
            expandCollapseWidget.onClick(x, y);
        }

        @Override
        public void onLongClick(float x, float y) {
            super.onLongClick(x, y);
            expandCollapseWidget.onLongClick(x, y);
        }

        @Override
        public void onTouchOutside() {
            if (!expandCollapseWidget.isAnimationInProgress()) {
                collapse();
            }
        }

        @Override
        public void onMoved(float diffX, float diffY) {
            super.onMoved(diffX, diffY);
            updatePlayPauseButtonPosition();
        }

        @Override
        public void onAnimationCompleted() {
            super.onAnimationCompleted();
            updatePlayPauseButtonPosition();
        }
    }

    private class OnControlsClickListenerWrapper implements OnControlsClickListener {

        private OnControlsClickListener onControlsClickListener;

        public OnControlsClickListenerWrapper onControlsClickListener(OnControlsClickListener inner) {
            this.onControlsClickListener = inner;
            return this;
        }

        @Override
        public boolean onPlaylistClicked() {
            if (onControlsClickListener == null || !onControlsClickListener.onPlaylistClicked()) {
                collapse();
                return true;
            }
            return false;
        }

        @Override
        public void onPlaylistLongClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onPlaylistLongClicked();
            }
        }

        @Override
        public void onPreviousClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onPreviousClicked();
            }
        }

        @Override
        public void onPreviousLongClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onPreviousLongClicked();
            }
        }

        @Override
        public boolean onPlayPauseClicked() {
            if (onControlsClickListener == null || !onControlsClickListener.onPlayPauseClicked()) {
                if (playbackState.state() != Configuration.STATE_PLAYING) {
                    playbackState.start(AudioWidget.this);
                } else {
                    playbackState.pause(AudioWidget.this);
                }
                return true;
            }
            return false;
        }

        @Override
        public void onPlayPauseLongClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onPlayPauseLongClicked();
            }
        }

        @Override
        public void onNextClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onNextClicked();
            }
        }

        @Override
        public void onNextLongClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onNextLongClicked();
            }
        }

        @Override
        public void onAlbumClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onAlbumClicked();
            }
        }

        @Override
        public void onAlbumLongClicked() {
            if (onControlsClickListener != null) {
                onControlsClickListener.onAlbumLongClicked();
            }
        }
    }
}
