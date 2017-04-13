package com.rks.musicx.data.eq;

import android.content.SharedPreferences;
import android.media.audiofx.Virtualizer;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.services.MediaPlayerSingleton;

import static com.rks.musicx.misc.utils.Constants.VIRTUAL_BOOST;
import static com.rks.musicx.misc.utils.Constants.Virtualizer_STRENGTH;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class Virtualizers {

    private static Virtualizer virtualizer = null;
    private static short virtualstr;

    public Virtualizers() {
    }

    /*
     Init Virtualizer
    */
    public static void initVirtualizer() {
        EndVirtual();
        try {
            virtualizer = new Virtualizer(0, MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
            short str = (short) Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0);
            if (str != 0) {
                virtualizer.setStrength(str);
            }
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
        }
    }

    public static void initVirtualBoostValues() {
        virtualstr = (short) Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0);
    }

    public static void saveVirtual() {
        if (virtualizer == null) {
            return;
        }
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putInt(VIRTUAL_BOOST, virtualstr);
        editor.apply();
    }

    public static short getVirtualStrength() {
        return virtualstr;
    }


    public static void setEnabled(boolean enabled) {
        if (virtualizer != null) {
            virtualizer.setEnabled(enabled);
        }
    }
}
