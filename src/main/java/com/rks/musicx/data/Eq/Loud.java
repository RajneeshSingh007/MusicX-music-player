package com.rks.musicx.data.eq;

import android.content.SharedPreferences;
import android.media.audiofx.LoudnessEnhancer;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.GAIN_MAX;
import static com.rks.musicx.misc.utils.Constants.LOUD_BOOST;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class Loud {

    private static LoudnessEnhancer loudnessEnhancer = null;
    private static int Gain = -1;

    public Loud() {
    }

    /*
     Init LoudnessEnhancer
    */
    public static void initLoudnessEnhancer(int audioSessionId) {
        EndLoudnessEnhancer();
        try {
            loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
            int loud = Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0);
            if (loud != 0){
                loudnessEnhancer.setTargetGain(loud);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLoudnessEnhancerGain(int gain) {
        Gain = gain;
        if (loudnessEnhancer != null) {
            try {
                if (Gain != -1 && Gain <= GAIN_MAX) {
                    loudnessEnhancer.setTargetGain(Gain);
                } else {
                    loudnessEnhancer.setTargetGain(0);
                }
            } catch (IllegalArgumentException e) {
                Log.e("Loud", "Loud effect not supported");
            } catch (IllegalStateException e) {
                Log.e("Loud", "Loud cannot get gain supported");
            } catch (UnsupportedOperationException e) {
                Log.e("Loud", "Loud library not loaded");
            } catch (RuntimeException e) {
                Log.e("Loud", "Loud effect not found");
            }
            saveLoudnessEnhancer();
        }

    }

    public static void EndLoudnessEnhancer() {
        if (loudnessEnhancer != null) {
            loudnessEnhancer.release();
            loudnessEnhancer = null;
        }
    }

    public static void initLoudnessEnhancerValues() {
        Gain = Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0);
    }

    public static void saveLoudnessEnhancer() {
        if (loudnessEnhancer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putInt(LOUD_BOOST, Gain);
        editor.apply();
    }

    public static int getGain() {
        return Gain;
    }

    public static void setEnabled(boolean enabled) {
        if (loudnessEnhancer != null) {
            loudnessEnhancer.setEnabled(enabled);
        }
    }
}
