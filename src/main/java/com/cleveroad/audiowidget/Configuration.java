package com.cleveroad.audiowidget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import java.util.Random;

/**
 * Audio widget configuration class.
 */
class Configuration {

	static final float FRAME_SPEED = 70.0f;

	static final long LONG_CLICK_THRESHOLD = ViewConfiguration.getLongPressTimeout() + 128;
    static final int STATE_STOPPED = 0;
    static final int STATE_PLAYING = 1;
    static final int STATE_PAUSED = 2;
    static final long TOUCH_ANIMATION_DURATION = 100;

    private final int lightColor;
	private final int darkColor;
	private final int progressColor;
	private final int expandedColor;
	private final Random random;
	private final float width;
	private final float height;
	private final Drawable playDrawable;
	private final Drawable pauseDrawable;
	private final Drawable prevDrawable;
	private final Drawable nextDrawable;
	private final Drawable playlistDrawable;
	private final Drawable albumDrawable;
	private final Context context;
	private final PlaybackState playbackState;
    private final int buttonPadding;
    private final float crossStrokeWidth;
    private final float progressStrokeWidth;
    private final float shadowRadius;
    private final float shadowDx;
    private final float shadowDy;
    private final int shadowColor;
    private final float bubblesMinSize;
    private final float bubblesMaxSize;
    private final int crossColor;
    private final int crossOverlappedColor;
    private final Interpolator accDecInterpolator;
    private final int prevNextExtraPadding;

	private Configuration(Builder builder) {
		this.context = builder.context;
		this.random = builder.random;
		this.width = builder.width;
		this.height = builder.radius;
		this.lightColor = builder.lightColor;
		this.darkColor = builder.darkColor;
		this.progressColor = builder.progressColor;
		this.expandedColor = builder.expandedColor;
		this.playlistDrawable = builder.playlistDrawable;
		this.playDrawable = builder.playDrawable;
		this.pauseDrawable = builder.pauseDrawable;
		this.prevDrawable = builder.prevDrawable;
		this.nextDrawable = builder.nextDrawable;
		this.albumDrawable = builder.albumDrawable;
		this.playbackState = builder.playbackState;
        this.buttonPadding = builder.buttonPadding;
        this.crossStrokeWidth = builder.crossStrokeWidth;
        this.progressStrokeWidth = builder.progressStrokeWidth;
        this.shadowRadius = builder.shadowRadius;
        this.shadowDx = builder.shadowDx;
        this.shadowDy = builder.shadowDy;
        this.shadowColor = builder.shadowColor;
        this.bubblesMinSize = builder.bubblesMinSize;
        this.bubblesMaxSize = builder.bubblesMaxSize;
        this.crossColor = builder.crossColor;
        this.crossOverlappedColor = builder.crossOverlappedColor;
        this.accDecInterpolator = builder.accDecInterpolator;
        this.prevNextExtraPadding = builder.prevNextExtraPadding;
	}

	Context context() {
		return context;
	}

	Random random() {
		return random;
	}

	@ColorInt
	int lightColor() {
		return lightColor;
	}

	@ColorInt
	int darkColor() {
		return darkColor;
	}

	@ColorInt
	int progressColor() {
		return progressColor;
	}

	@ColorInt
	int expandedColor() {
		return expandedColor;
	}

	float widgetWidth() {
		return width;
	}

	float radius() {
		return height;
	}

	Drawable playDrawable() {
		return playDrawable;
	}

	Drawable pauseDrawable() {
		return pauseDrawable;
	}

	Drawable prevDrawable() {
		return prevDrawable;
	}

	Drawable nextDrawable() {
		return nextDrawable;
	}

	Drawable playlistDrawable() {
		return playlistDrawable;
	}

	Drawable albumDrawable() {
		return albumDrawable;
	}

	PlaybackState playbackState() {
		return playbackState;
	}

    float crossStrokeWidth() {
        return crossStrokeWidth;
    }

    float progressStrokeWidth() {
        return progressStrokeWidth;
    }

    int buttonPadding() {
        return buttonPadding;
    }

    float shadowRadius() {
        return shadowRadius;
    }

    float shadowDx() {
        return shadowDx;
    }

    float shadowDy() {
        return shadowDy;
    }

    int shadowColor() {
        return shadowColor;
    }

    float bubblesMinSize() {
        return bubblesMinSize;
    }

    float bubblesMaxSize() {
        return bubblesMaxSize;
    }

    int crossColor() {
        return crossColor;
    }

    int crossOverlappedColor() {
        return crossOverlappedColor;
    }

    Interpolator accDecInterpolator() {
        return accDecInterpolator;
    }

    int prevNextExtraPadding() {
        return prevNextExtraPadding;
    }

    static final class Builder {

		private int lightColor;
		private int darkColor;
		private int progressColor;
		private int expandedColor;
		private float width;
		private float radius;
		private Context context;
		private Random random;
		private Drawable playDrawable;
		private Drawable pauseDrawable;
		private Drawable prevDrawable;
		private Drawable nextDrawable;
		private Drawable playlistDrawable;
		private Drawable albumDrawable;
		private PlaybackState playbackState;
        private int buttonPadding;
        private float crossStrokeWidth;
        private float progressStrokeWidth;
        private float shadowRadius;
        private float shadowDx;
        private float shadowDy;
        private int shadowColor;
        private float bubblesMinSize;
        private float bubblesMaxSize;
        private int crossColor;
        private int crossOverlappedColor;
        private Interpolator accDecInterpolator;
        private int prevNextExtraPadding;

        Builder context(Context context) {
			this.context = context;
			return this;
		}

		Builder playColor(@ColorInt int pauseColor) {
			this.lightColor = pauseColor;
			return this;
		}

		Builder darkColor(@ColorInt int playColor) {
			this.darkColor = playColor;
			return this;
		}

		Builder progressColor(@ColorInt int progressColor) {
			this.progressColor = progressColor;
			return this;
		}

		Builder expandedColor(@ColorInt int expandedColor) {
			this.expandedColor = expandedColor;
			return this;
		}

		Builder random(Random random) {
			this.random = random;
			return this;
		}

		Builder widgetWidth(float width) {
			this.width = width;
			return this;
		}

		Builder radius(float radius) {
			this.radius = radius;
			return this;
		}

		Builder playDrawable(@Nullable Drawable playDrawable) {
			this.playDrawable = playDrawable;
			return this;
		}

		Builder pauseDrawable(@Nullable Drawable pauseDrawable) {
			this.pauseDrawable = pauseDrawable;
			return this;
		}

		Builder prevDrawable(@Nullable Drawable prevDrawable) {
			this.prevDrawable = prevDrawable;
			return this;
		}

		Builder nextDrawable(@Nullable Drawable nextDrawable) {
			this.nextDrawable = nextDrawable;
			return this;
		}

		Builder playlistDrawable(@Nullable Drawable plateDrawable) {
			this.playlistDrawable = plateDrawable;
			return this;
		}

		Builder albumDrawable(@Nullable Drawable albumDrawable) {
			this.albumDrawable = albumDrawable;
			return this;
		}

		Builder playbackState(PlaybackState playbackState) {
			this.playbackState = playbackState;
			return this;
		}

        Builder buttonPadding(int buttonPadding) {
            this.buttonPadding = buttonPadding;
            return this;
        }

        Builder crossStrokeWidth(float crossStrokeWidth) {
            this.crossStrokeWidth = crossStrokeWidth;
            return this;
        }

        Builder progressStrokeWidth(float progressStrokeWidth) {
            this.progressStrokeWidth = progressStrokeWidth;
            return this;
        }

        Builder shadowRadius(float shadowRadius) {
            this.shadowRadius = shadowRadius;
            return this;
        }

        Builder shadowDx(float shadowDx) {
            this.shadowDx = shadowDx;
            return this;
        }

        Builder shadowDy(float shadowDy) {
            this.shadowDy = shadowDy;
            return this;
        }

        Builder shadowColor(@ColorInt int shadowColor) {
            this.shadowColor = shadowColor;
            return this;
        }

        Builder bubblesMinSize(float bubblesMinSize) {
            this.bubblesMinSize = bubblesMinSize;
            return this;
        }

        Builder bubblesMaxSize(float bubblesMaxSize) {
            this.bubblesMaxSize = bubblesMaxSize;
            return this;
        }

        Builder crossColor(@ColorInt int crossColor) {
            this.crossColor = crossColor;
            return this;
        }

        Builder crossOverlappedColor(@ColorInt int crossOverlappedColor) {
            this.crossOverlappedColor = crossOverlappedColor;
            return this;
        }

        Builder accDecInterpolator(Interpolator accDecInterpolator) {
            this.accDecInterpolator = accDecInterpolator;
            return this;
        }

        Builder prevNextExtraPadding(int prevNextExtraPadding) {
            this.prevNextExtraPadding = prevNextExtraPadding;
            return this;
        }

        Configuration build() {
			return new Configuration(this);
		}
    }
}
