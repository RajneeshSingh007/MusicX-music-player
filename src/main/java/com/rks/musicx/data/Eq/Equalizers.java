package com.rks.musicx.data.eq;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;

import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.BAND_LEVEL;
import static com.rks.musicx.misc.utils.Constants.SAVE_PRESET;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class Equalizers {


    private static Equalizer equalizer = null;
    private static short preset;
    private static short[] bandLevels;

    public Equalizers() {
    }

    public static void initEq(int audioID) {
        try {
            EndEq();
            equalizer = new Equalizer(0, audioID);
            bandLevels = new short[equalizer.getNumberOfBands()];
            for (short b = 0; b < equalizer.getNumberOfBands(); b++) {
                bandLevels[b] = equalizer.getBandLevel(b);
                short level = (short) Extras.getInstance().saveEq().getInt(BAND_LEVEL+b, 0);
               if (level != 0){
                   setBandLevel(b, level);
               }
               short preset = (short) Extras.getInstance().saveEq().getInt(SAVE_PRESET, 0);
                if (preset != -1){
                    usePreset(preset);
                }
            }
            try {
                preset = equalizer.getCurrentPreset();
            }catch (Exception e){
                preset = -1;
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
        }
        return null;
    }

    public static String[] getEqualizerPresets(Context context) {
        if (equalizer == null) {
            return new String[]{};
        }
        String[] presets = new String[equalizer.getNumberOfPresets() + 1];
        presets[0] = context.getResources().getString(R.string.custom);
        for (short n = 0; n < equalizer.getNumberOfPresets(); n++) {
            presets[n + 1] = equalizer.getPresetName(n);
        }
        return presets;
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

    public static void initEqualizerValues() {
        preset = (short) Extras.getInstance().saveEq().getInt(SAVE_PRESET, 0);
    }

    public static int getCurrentPreset() {
        if (equalizer == null) {
            return 0;
        }
        return equalizer.getCurrentPreset() + 1;
    }

    public static void usePreset(short presets) {
        if (equalizer == null){
            return;
        }
        preset = presets;
        if (preset >= 0 && preset < equalizer.getNumberOfPresets()){
            equalizer.usePreset(preset);
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

    public static void savePrefs(int preset, int bandlevel) {
        if (equalizer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putInt(SAVE_PRESET, preset);
        short bands = equalizer.getNumberOfBands();
        for (short b = 0; b < bands; b++) {
            editor.putInt(BAND_LEVEL + b, bandlevel);
        }
        editor.commit();
    }
}
