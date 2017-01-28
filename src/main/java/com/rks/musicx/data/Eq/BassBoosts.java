package com.rks.musicx.data.Eq;

import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.BASSBOOST_STRENGTH;
import static com.rks.musicx.misc.utils.Constants.BASS_BOOST;
import static com.rks.musicx.misc.utils.Constants.BASS_ENABLED;

/**
 * Created by Coolalien on 12/23/2016.
 */

public class BassBoosts {

    private static BassBoost bassBoost = null;
    private static boolean enabled;
    private static short str = -1;

    /**
     * Default Constructor
     */
    public BassBoosts(){}

    /**
     * Init Bass
     * @param audioID
     */
    public static void initBass(int audioID) {
        EndBass();
        try {
            bassBoost = new BassBoost(0, audioID);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Close Bass
     */
    public static void EndBass(){
        if (bassBoost != null){
            bassBoost.release();
            bassBoost = null;
        }
    }

    /**
     * Set Bass Strength
     * @param strength
     */
    public static void setBassBoostStrength(short strength) {
        str = strength;
        if (bassBoost != null && bassBoost.getStrengthSupported()){
            try {
                if (str != -1 && str <= BASSBOOST_STRENGTH){
                    bassBoost.setStrength(strength);
                }else {
                    bassBoost.setStrength((short) 0);
                }
            } catch (IllegalArgumentException e) {
                Log.e("BassBoosts", "Bassboost effect not supported");
            } catch (IllegalStateException e) {
                Log.e("BassBoosts", "Bassboost cannot get strength supported");
            } catch (UnsupportedOperationException e) {
                Log.e("BassBoosts", "Bassboost library not loaded");
            } catch (RuntimeException e) {
                Log.e("BassBoosts", "Bassboost effect not found");
            }
            saveBass();
        }
    }

    /**
     * init default bass
     */
    public static void initBassBoostValues() {
        enabled = Extras.getInstance().saveEq().getBoolean(BASS_ENABLED, false);
        str = (short) Extras.getInstance().saveEq().getInt(BASS_BOOST, 0);
    }
    /**
     * Save Bass
     */
    public static void saveBass() {
        if (bassBoost == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putBoolean(BASS_ENABLED, bassBoost.getEnabled());
        short str = getStr() == 0 ? 0 : getStr();
        editor.putInt(BASS_BOOST, str);
        editor.apply();
    }

    /**
     * bass boost strength
     * @return
     */

    public static short getStr() {
        return str;
    }

    public static void setEnabled(boolean enabled1) {
        enabled = enabled1;
        if (bassBoost != null){
            bassBoost.setEnabled(enabled1);
        }
    }


    /**
     * isEnbaled
     * @return
     */
    public static boolean isEnabled() {
        return enabled;
    }
}
