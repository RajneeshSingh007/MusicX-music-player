package com.cleveroad.audiowidget;

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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

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

    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.RGB_565);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

}
