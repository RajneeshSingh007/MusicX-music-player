package com.cleveroad.audiowidget;

class DrawableUtils {

    private DrawableUtils() {
    }

    static float customFunction(float t, float... pairs) {
        if (pairs.length == 0 || pairs.length % 2 != 0) {
            throw new IllegalArgumentException("Length of pairs must be multiple by 2 and greater than zero.");
        }
        if (t < pairs[1]) {
            return pairs[0];
        }
        int size = pairs.length / 2;
        for (int i = 0; i < size - 1; i++) {
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

    static float normalize(float val, float minVal, float maxVal) {
        if (val < minVal)
            return 0;
        if (val > maxVal)
            return 1;
        return (val - minVal) / (maxVal - minVal);
    }

    static float rotateX(float pX, float pY, float cX, float cY, float angleInDegrees) {
        double angle = Math.toRadians(angleInDegrees);
        return (float) (Math.cos(angle) * (pX - cX) - Math.sin(angle) * (pY - cY) + cX);
    }

    static float rotateY(float pX, float pY, float cX, float cY, float angleInDegrees) {
        double angle = Math.toRadians(angleInDegrees);
        return (float) (Math.sin(angle) * (pX - cX) + Math.cos(angle) * (pY - cY) + cY);
    }

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

    static float enlarge(float startValue, float endValue, float time) {
        if (startValue > endValue)
            throw new IllegalArgumentException("Start size can't be larger than end size.");
        return startValue + (endValue - startValue) * time;
    }

    static float reduce(float startValue, float endValue, float time) {
        if (startValue < endValue)
            throw new IllegalArgumentException("End size can't be larger than start size.");
        return endValue + (startValue - endValue) * (1 - time);
    }

    static float smooth(float prevValue, float newValue, float a) {
        return a * newValue + (1 - a) * prevValue;
    }

}
