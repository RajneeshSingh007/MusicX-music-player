package com.rks.musicx.data.Eq;

import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.BASSBOOST_STRENGTH;
import static com.rks.musicx.misc.utils.Constants.BASS_BOOST;
import static com.rks.musicx.misc.utils.Constants.BASS_ENABLED;

/**
 * Created by Coolalien on 12/23/2016.
 */

public class BassBoosts {

    private static BassBoost bassBoost;
    private static boolean enabled;
    private static short str;

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
        bassBoost = new BassBoost(0, audioID);
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
        if (bassBoost != null) {
            str = strength;
            if (str >0 && str <= BASSBOOST_STRENGTH){
                bassBoost.setStrength(str);
            }
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
        if (enabled){
            bassBoost.setEnabled(true);
        }else {
            bassBoost.setEnabled(false);
        }
    }

    public static boolean isEnabled() {
       return enabled;
    }
}
