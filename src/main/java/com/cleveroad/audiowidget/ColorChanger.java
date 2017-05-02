package com.cleveroad.audiowidget;

import android.graphics.Color;

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

class ColorChanger {

    private final float[] fromColorHsv;
    private final float[] toColorHsv;
    private final float[] resultColorHsv;

    ColorChanger() {
        fromColorHsv = new float[3];
        toColorHsv = new float[3];
        resultColorHsv = new float[3];
    }

    ColorChanger fromColor(int fromColor) {
        Color.colorToHSV(fromColor, fromColorHsv);
        return this;
    }

    ColorChanger toColor(int toColor) {
        Color.colorToHSV(toColor, toColorHsv);
        return this;
    }

    int nextColor(float dt) {
        for (int k = 0; k < 3; k++) {
            resultColorHsv[k] = fromColorHsv[k] + (toColorHsv[k] - fromColorHsv[k]) * dt;
        }
        return Color.HSVToColor(resultColorHsv);
    }
}
