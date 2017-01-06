package com.cleveroad.audiowidget;

/**
 * Helpful utils class.
 */
class DrawableUtils {

	private DrawableUtils() {}

	static float customFunction(float t, float ... pairs) {
		if (pairs.length == 0 || pairs.length % 2 != 0) {
			throw new IllegalArgumentException("Length of pairs must be multiple by 2 and greater than zero.");
		}
		if (t < pairs[1]) {
			return pairs[0];
		}
		int size = pairs.length / 2;
		for (int i=0; i<size - 1; i++) {
			float a = pairs[2 * i];
			float b = pairs[2 * (i + 1)];
			float aT = pairs[2 * i + 1];
			float bT = pairs[2 * (i + 1) + 1];
			if (t >= aT && t <= bT) {
				float norm = normalize(t, aT, bT);
				return a + norm * (b - a);
			}
		}
		return pairs[pairs.length - 2];
	}

	/**
	 * Normalize value between minimum and maximum.
	 * @param val value
	 * @param minVal minimum value
	 * @param maxVal maximum value
	 * @return normalized value in range <code>0..1</code>
	 * @throws IllegalArgumentException if value is out of range <code>[minVal, maxVal]</code>
	 */
	static float normalize(float val, float minVal, float maxVal) {
		if (val < minVal)
			return 0;
		if (val > maxVal)
			return 1;
        return (val - minVal) / (maxVal - minVal);
	}

	/**
	 * Rotate point P around center point C.
	 * @param pX x coordinate of point P
	 * @param pY y coordinate of point P
	 * @param cX x coordinate of point C
	 * @param cY y coordinate of point C
	 * @param angleInDegrees rotation angle in degrees
	 * @return new x coordinate
	 */
	static float rotateX(float pX, float pY, float cX, float cY, float angleInDegrees) {
		double angle = Math.toRadians(angleInDegrees);
		return (float) (Math.cos(angle) * (pX - cX) - Math.sin(angle) * (pY - cY) + cX);
	}

	/**
	 * Rotate point P around center point C.
	 * @param pX x coordinate of point P
	 * @param pY y coordinate of point P
	 * @param cX x coordinate of point C
	 * @param cY y coordinate of point C
	 * @param angleInDegrees rotation angle in degrees
	 * @return new y coordinate
	 */
	static float rotateY(float pX, float pY, float cX, float cY, float angleInDegrees) {
		double angle = Math.toRadians(angleInDegrees);
		return (float) (Math.sin(angle) * (pX - cX) + Math.cos(angle) * (pY - cY) + cY);
	}

	/**
	 * Checks if value belongs to range <code>[start, end]</code>
	 * @param value value
	 * @param start start of range
	 * @param end end of range
	 * @return true if value belongs to range, false otherwise
	 */
	static boolean isBetween(float value, float start, float end) {
		if (start > end) {
			float tmp = start;
			start = end;
			end = tmp;
		}
		return value >= start && value <= end;
	}

	static float between(float val, float min, float max) {
		return Math.min(Math.max(val, min), max);
	}

	static int between(int val, int min, int max) {
		return Math.min(Math.max(val, min), max);
	}

	/**
	 * Enlarge value from startValue to endValue
	 * @param startValue start size
	 * @param endValue end size
	 * @param time time of animation
	 * @return new size value
	 */
	static float enlarge(float startValue, float endValue, float time) {
		if (startValue > endValue)
			throw new IllegalArgumentException("Start size can't be larger than end size.");
		return startValue + (endValue - startValue) * time;
	}

	/**
	 * Reduce value from startValue to endValue
	 * @param startValue start size
	 * @param endValue end size
	 * @param time time of animation
	 * @return new size value
	 */
	static float reduce(float startValue, float endValue, float time) {
		if (startValue < endValue)
			throw new IllegalArgumentException("End size can't be larger than start size.");
		return endValue + (startValue - endValue) * (1 - time);
	}

    /**
     * Exponential smoothing (Holt - Winters).
     * @param prevValue previous values in series <code>X[i-1]</code>
     * @param newValue new value in series <code>X[i]</code>
     * @param a smooth coefficient
     * @return smoothed value
     */
    static float smooth(float prevValue, float newValue, float a) {
        return a * newValue + (1 - a) * prevValue;
    }

}
