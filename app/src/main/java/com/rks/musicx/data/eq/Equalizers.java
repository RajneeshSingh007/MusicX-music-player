package com.rks.musicx.data.eq;

import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.BAND_LEVEL;
import static com.rks.musicx.misc.utils.Constants.SAVE_PRESET;

/*
 * Created by Coolalien on 06/01/2017.
 */

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

public class Equalizers {


    private static Equalizer equalizer = null;
    private static short preset;

    public Equalizers() {
    }

    public static void initEq(int audioID) {
        EndEq();
        try {
            equalizer = new Equalizer(0, audioID);
            preset = (short) Extras.getInstance().saveEq().getInt(SAVE_PRESET, 0);
            if (preset < equalizer.getNumberOfPresets()){
                usePreset(preset);
            }else {
                for (short b = 0; b < equalizer.getNumberOfBands(); b++) {
                    short level = (short) Extras.getInstance().saveEq().getInt(BAND_LEVEL + b, 0);
                    setBandLevel(b, level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void EndEq() {
        if (equalizer != null) {
            equalizer.release();
            equalizer = null;
        }
    }

    public static short[] getBandLevelRange() {
        if (equalizer != null) {
            return equalizer.getBandLevelRange();
        } else {
            return null;
        }
    }

    public static short getPresetNo(){
        if (equalizer != null){
            return equalizer.getNumberOfPresets();
        }else {
            return 0;
        }
    }

    public static String getPresetNames(short name){
        if (equalizer != null){
            return equalizer.getPresetName(name);
        }else {
            return "";
        }
    }

    public static short getBandLevel(short band) {
        if (equalizer == null) {
            return 0;
        }
        return equalizer.getBandLevel(band);
    }

    public static void setEnabled(boolean enabled) {
        if (equalizer != null) {
            equalizer.setEnabled(enabled);
        }
    }

    public static void setBandLevel(short band, short level) {
        if (equalizer != null) {
            equalizer.setBandLevel(band, level);
        }
    }

    public static void usePreset(short presets) {
        if (equalizer != null) {
            try {
                if (presets >= 0) {
                    preset = presets;
                    equalizer.usePreset(presets);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static short getNumberOfBands() {
        if (equalizer != null) {
            return equalizer.getNumberOfBands();
        }
        return 0;
    }

    public static int getCenterFreq(short band) {
        if (equalizer != null) {
            return equalizer.getCenterFreq(band);
        }
        return 0;
    }

    public static void savePrefs(int band, int bandLevel) {
        if (equalizer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putInt(BAND_LEVEL + band, bandLevel);
        editor.commit();
    }

}
