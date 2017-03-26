package com.rks.musicx.data.eq;

import static com.rks.musicx.misc.utils.Constants.VIRTUAL_BOOST;
import static com.rks.musicx.misc.utils.Constants.VIRTUAL_ENABLED;
import static com.rks.musicx.misc.utils.Constants.Virtualizer_STRENGTH;

import android.content.SharedPreferences;
import android.media.audiofx.Virtualizer;
import android.util.Log;
import com.rks.musicx.misc.utils.Extras;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class Virtualizers {

    private static Virtualizer virtualizer = null;
    private static boolean venabled;
    private static short virtualstr;

    public Virtualizers() {
    }

    /*
     Init Virtualizer
    */
    public static void initVirtualizer(int audioID) {
        EndVirtual();
        try {
            virtualizer = new Virtualizer(0, audioID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVirtualizerStrength(short strength) {
        virtualstr = strength;
        if (virtualizer != null && virtualizer.getStrengthSupported()) {
            try {
                if (virtualstr != -1 && virtualstr <= Virtualizer_STRENGTH) {
                    virtualizer.setStrength(strength);
                } else {
                    virtualizer.setStrength((short) 0);
                }
            } catch (IllegalArgumentException e) {
                Log.e("Virtualizers", "Virtualizers effect not supported");
            } catch (IllegalStateException e) {
                Log.e("Virtualizers", "Virtualizers cannot get strength supported");
            } catch (UnsupportedOperationException e) {
                Log.e("Virtualizers", "Virtualizers library not loaded");
            } catch (RuntimeException e) {
                Log.e("Virtualizers", "Virtualizers effect not found");
            }
            saveVirtual();
        }

    }

    public static void EndVirtual() {
        if (virtualizer != null) {
            virtualizer.release();
            virtualizer = null;
            venabled = false;
        }
    }

    public static void initVirtualBoostValues() {
        venabled = Extras.getInstance().saveEq().getBoolean(VIRTUAL_ENABLED, false);
        virtualstr = (short) Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0);
    }

    public static void saveVirtual() {
        if (virtualizer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putBoolean(VIRTUAL_ENABLED, isEnabled());
        short str = getVirtualStrength() == 0 ? 0 : getVirtualStrength();
        editor.putInt(VIRTUAL_BOOST, str);
        editor.apply();
    }

    public static short getVirtualStrength() {
        return virtualstr;
    }

    public static boolean isEnabled() {
        return venabled;
    }

    public static void setEnabled(boolean enabled1) {
        venabled = enabled1;
        if (virtualizer != null) {
            virtualizer.setEnabled(venabled);
        }
    }
}
