package com.rks.musicx.data.Eq;

import android.content.SharedPreferences;
import android.media.audiofx.LoudnessEnhancer;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.EQ_ENABLED;
import static com.rks.musicx.misc.utils.Constants.GAIN_MAX;
import static com.rks.musicx.misc.utils.Constants.LOUD_BOOST;

/**
 * Created by Coolalien on 12/23/2016.
 */

public class Loud {

    private static LoudnessEnhancer loudnessEnhancer = null;
    private static boolean enabled;
    private static int Gain = -1;

    /**
     * Default Consturctor
     */
    public Loud(){
    }

    /*
     Init LoudnessEnhancer
    */
    public static void initLoudnessEnhancer(int audioSessionId){
        EndLoudnessEnhancer();
        try {
            loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Set LoudnessEnhancer Gain
     * @param gain
     */
    public static void setLoudnessEnhancerGain(int gain) {
        Gain = gain;
        if (loudnessEnhancer != null) {
            try {
                if (Gain != -1 && Gain <= GAIN_MAX) {
                    loudnessEnhancer.setTargetGain(Gain);
                }else {
                    loudnessEnhancer.setTargetGain(0);
                }
            }catch (IllegalArgumentException e) {
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

    /**
     * Close LoudnessEnhancer
     */
    public static void EndLoudnessEnhancer(){
        if (loudnessEnhancer !=null){
            loudnessEnhancer.release();
            loudnessEnhancer = null;
        }
    }

    /**
     * init default LoudnessEnhancer
     */
    public static void initLoudnessEnhancerValues() {
        enabled = Extras.getInstance().saveEq().getBoolean(EQ_ENABLED, false);
        Gain = Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0);
    }

    /**
     * Save LoudnessEnhancer
     */
    public static void saveLoudnessEnhancer() {
        if (loudnessEnhancer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putBoolean(EQ_ENABLED,loudnessEnhancer.getEnabled());
        int gain = getGain() == 0 ? 0 : getGain();
        editor.putInt(LOUD_BOOST, gain);
        editor.apply();
    }

    /**
     * LoudnessEnhancer Gain
     * @return
     */
    public static int getGain() {
        return Gain;
    }

    public static void setEnabled(boolean enabled1) {
        enabled = enabled1;
        if (loudnessEnhancer != null){
            loudnessEnhancer.setEnabled(enabled1);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
