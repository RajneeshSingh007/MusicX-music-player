package com.rks.musicx.data.eq;

import android.content.SharedPreferences;
import android.media.audiofx.PresetReverb;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.PRESET_BOOST;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class Reverb {

    private static PresetReverb presetReverb = null;
    private static short str = -1;

    public Reverb() {
    }

    public static void initReverb(int audioID) {
        EndReverb();
        try {
            presetReverb = new PresetReverb(0, audioID);
            short str = (short) Extras.getInstance().saveEq().getInt(PRESET_BOOST, 0);
            if (str != 0){
                presetReverb.setPreset(str);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void EndReverb() {
        if (presetReverb != null) {
            presetReverb.release();
            presetReverb = null;
        }
    }

    public static void setPresetReverbStrength(short strength) {
        str = strength;
        if (presetReverb != null) {
            try {
                if (str != -1) {
                    presetReverb.setPreset(strength);
                } else {
                    presetReverb.setPreset((short) 0);
                }
            } catch (IllegalArgumentException e) {
                Log.e("Reverb", "Reverb effect not supported");
            } catch (IllegalStateException e) {
                Log.e("Reverb", "Reverb cannot get strength supported");
            } catch (UnsupportedOperationException e) {
                Log.e("Reverb", "Reverb library not loaded");
            } catch (RuntimeException e) {
                Log.e("Reverb", "Reverb effect not found");
            }
            saveReverb();
        }
    }

    public static void initpresetReverbValues() {
        str = (short) Extras.getInstance().saveEq().getInt(PRESET_BOOST, 0);
    }

    public static void saveReverb() {
        if (presetReverb == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putInt(PRESET_BOOST, str);
        editor.apply();
    }

    public static short getStr() {
        return str;
    }


    public static void setEnabled(boolean enabled) {
        if (presetReverb != null) {
            presetReverb.setEnabled(enabled);
        }
    }
}
