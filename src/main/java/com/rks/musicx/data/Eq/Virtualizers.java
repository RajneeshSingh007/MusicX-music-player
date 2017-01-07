package com.rks.musicx.data.Eq;

import android.content.SharedPreferences;
import android.media.audiofx.Virtualizer;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.VIRTUAL_BOOST;
import static com.rks.musicx.misc.utils.Constants.VIRTUAL_ENABLED;
import static com.rks.musicx.misc.utils.Constants.Virtualizer_STRENGTH;

/**
 * Created by Coolalien on 12/23/2016.
 */

public class Virtualizers {

    private static Virtualizer virtualizer;
    private static boolean venabled;
    private static short virtualstr;

    /**
     * Default Consturctor
     */
    public Virtualizers(){
    }

    /*
     Init Virtualizer
    */
    public static void initVirtualizer(int audioID){
        EndVirtual();
        virtualizer = new Virtualizer(0,audioID);
    }

    /**
     * Set Virtualizer Strength
     * @param strength
     */
    public static void setVirtualizerStrength(short strength) {
        if (virtualizer != null) {
            virtualstr = strength;
            if (virtualstr >0 && virtualstr <= Virtualizer_STRENGTH){
                virtualizer.setStrength(virtualstr);
            }
        }

    }

    /**
     * Close Virtual
     */
    public static void EndVirtual(){
        if (virtualizer !=null){
            virtualizer.release();
            virtualizer = null;
        }
    }

    /**
     * init default virtual
     */
    public static void initVirtualBoostValues() {
        venabled = Extras.getInstance().saveEq().getBoolean(VIRTUAL_ENABLED, false);
        virtualstr = (short) Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0);
    }

    /**
     * Save Virtualizer
     */
    public static void saveVirtual() {
        if (virtualizer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putBoolean(VIRTUAL_ENABLED, virtualizer.getEnabled());
        short str = getVirtualStrength() == 0 ? 0 : getVirtualStrength();
        editor.putInt(VIRTUAL_BOOST, str);
        editor.apply();
    }

    /**
     * bass boost strength
     * @return
     */
    public static short getVirtualStrength() {
        return virtualstr;
    }

    public static void setEnabled(boolean enabled1) {
        venabled = enabled1;
        if (venabled){
            virtualizer.setEnabled(true);
        }else {
            virtualizer.setEnabled(false);
        }
    }

    public static boolean isEnabled() {
        return venabled;
    }
}
