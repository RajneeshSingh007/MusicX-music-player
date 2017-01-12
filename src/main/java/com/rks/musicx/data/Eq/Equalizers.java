package com.rks.musicx.data.Eq;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;

import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.BAND_LEVEL;
import static com.rks.musicx.misc.utils.Constants.EQ_ENABLED;
import static com.rks.musicx.misc.utils.Constants.SAVE_PRESET;

/**
 * Created by Coolalien on 12/23/2016.
 */

public class Equalizers {


    private static Equalizer equalizer;
    private static boolean enabled;
    private static short preset;
    private static short[] bandLevels;
    private static boolean customPreset;

    /**
     * Default Constructor
     */
    public Equalizers(){
    }

    /**
     * Init Equalizer
     * @param audioID
     */
    public static void initEq(int audioID){
        EndEq();
        equalizer = new Equalizer(0, audioID);
        bandLevels = new short[equalizer.getNumberOfBands()];
        for (short b = 0; b < equalizer.getNumberOfBands(); b++) {
            short level = (short) Extras.getInstance().saveEq().getInt(BAND_LEVEL + b, equalizer.getBandLevel(b));
            bandLevels[b] = level;
            if (customPreset) {
                setBandLevel(b,level);
            }
        }
        if (preset == -1){
            customPreset = true;
        }
    }

    /**
     * Close Eq
     */
    public static void EndEq(){
        if (equalizer != null){
            equalizer.release();
            equalizer = null;
        }
    }

    /**
     * Band Level Range
     * @return
     */
    public static short[] getBandLevelRange() {
        if (equalizer == null) {
            return null;
        }
        return equalizer.getBandLevelRange();
    }

    /**
     * Band Level
     * @param band
     * @return
     */
    public static short getBandLevel(short band) {
        if (equalizer == null) {
            if (bandLevels.length > band) {
                return bandLevels[band];
            }
        }
        return equalizer.getBandLevel(band);
    }

    /**
     * check is eq enabled/disabled
     * @return
     */
    public static boolean isEnabled() {
        return enabled;
    }

    /**
     * set Band Level
     * @param band
     * @param level
     */
    public static void setBandLevel(short band, short level) {
        customPreset = true;
        if (bandLevels.length > band) {
            preset = -1;
            bandLevels[band] = level;
        }
        if (equalizer != null) {
            equalizer.setBandLevel(band, level);
        }
    }

    /**
     * init default Values
     */
    public static void initEqualizerValues() {
        enabled = Extras.getInstance().saveEq().getBoolean(EQ_ENABLED, false);
        preset = (short) Extras.getInstance().saveEq().getInt(SAVE_PRESET, -1);
        if (preset == -1) {
            customPreset = true;
        }else {
            customPreset = false;
        }
    }

    /**
     * Preset
     * @param context
     * @return
     */
    public static String[] getEqualizerPresets(Context context) {
        if (equalizer == null) {
            return null;
        }
        String[] presets = new String[equalizer.getNumberOfPresets() + 1];
        presets[0] = context.getResources().getString(R.string.custom);
        for (short n = 0; n < equalizer.getNumberOfPresets(); n++) {
            presets[n + 1] = equalizer.getPresetName(n);
        }
        return presets;
    }

    /**
     * current preset
     * @return
     */
    public static int getCurrentPreset() {
        if (equalizer == null || customPreset) {
            return 0;
        }

        return equalizer.getCurrentPreset() + 1;
    }

    /**
     * working Preset
     * @param preset
     */
    public static void usePreset(short preset) {
        if (equalizer != null) {
            customPreset = false;
            equalizer.usePreset(preset);
        }
    }

    /**
     * no. of bands
     * @return
     */
    public static short getNumberOfBands() {
        if (equalizer != null) {
           return equalizer.getNumberOfBands();
        }
        return 0;
    }

    /**
     * Center Freq
     * @param band
     * @return
     */
    public static int getCenterFreq(short band) {
        if ( equalizer != null) {
            return equalizer.getCenterFreq(band);
        }
        return 0;
    }

    /**
     * save preset
     */
    public static void savePrefs() {
        if ( equalizer == null ) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        short preset = customPreset  ? -1 : equalizer.getCurrentPreset();
        editor.putInt(SAVE_PRESET, preset);
        short bands = equalizer.getNumberOfBands();
        for (short b = 0; b < bands; b++) {
            editor.putInt(BAND_LEVEL + b, getBandLevel(b));
        }
        editor.putBoolean(EQ_ENABLED, equalizer.getEnabled());
        editor.apply();
    }

    /**
     * Enabled and disabled eq
     * @param enabled1
     */
    public static void setEnabled(boolean enabled1) {
        enabled = enabled1;
        if (enabled){
            equalizer.setEnabled(true);
        }else {
            equalizer.setEnabled(false);
        }
    }

}
