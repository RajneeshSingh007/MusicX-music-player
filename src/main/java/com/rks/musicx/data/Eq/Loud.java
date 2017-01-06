package com.rks.musicx.data.Eq;

import android.content.SharedPreferences;
import android.media.audiofx.LoudnessEnhancer;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.EQ_ENABLED;
import static com.rks.musicx.misc.utils.Constants.GAIN_MAX;
import static com.rks.musicx.misc.utils.Constants.LOUD_BOOST;

/**
 * Created by Coolalien on 12/23/2016.
 */

public class Loud {

    private static LoudnessEnhancer loudnessEnhancer;
    private static boolean enabled;
    private static int Gain;

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
        loudnessEnhancer = new LoudnessEnhancer(audioSessionId);
        if (Gain >=0 && Gain <= GAIN_MAX) {
            loudnessEnhancer.setTargetGain(Gain);
        }
    }

    /**
     * Set LoudnessEnhancer Gain
     * @param gain
     */
    public static void setLoudnessEnhancerGain(int gain) {
        if (loudnessEnhancer != null) {
            Gain = gain;
            loudnessEnhancer.setTargetGain(gain);
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
        editor.putInt(LOUD_BOOST, getGain());
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
        if (enabled){
            loudnessEnhancer.setEnabled(true);
        }else {
            loudnessEnhancer.setEnabled(false);
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
