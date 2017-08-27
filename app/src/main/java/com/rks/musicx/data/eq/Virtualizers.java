package com.rks.musicx.data.eq;

import android.content.SharedPreferences;
import android.media.audiofx.Virtualizer;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.VIRTUAL_BOOST;
import static com.rks.musicx.misc.utils.Constants.Virtualizer_STRENGTH;

/*
 * Created by Coolalien on 06/01/2017.
 */

/*
 * ©2017 Rajneesh Singh
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Virtualizers {

    private static Virtualizer virtualizer = null;

    public Virtualizers() {
    }

    /*
     Init Virtualizer
    */
    public static void initVirtualizer(int audioID) {
        EndVirtual();
        try {
            virtualizer = new Virtualizer(0, audioID);
            short str = (short) Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0);
            if (str > 0) {
                setVirtualizerStrength(str);
            }else {
                setVirtualizerStrength((short) 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setVirtualizerStrength(short strength) {
        if (virtualizer != null && virtualizer.getStrengthSupported() && strength >= 0) {
            try {
                if (strength <= Virtualizer_STRENGTH) {
                    virtualizer.setStrength(strength);
                    saveVirtual(strength);
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
        }

    }

    public static void EndVirtual() {
        if (virtualizer != null) {
            virtualizer.release();
            virtualizer = null;
        }
    }

    public static void saveVirtual(Short virtualstr) {
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        int str = (int) virtualstr;
        editor.putInt(VIRTUAL_BOOST, str);
        editor.apply();
    }

    public static void setEnabled(boolean enabled) {
        if (virtualizer != null) {
            virtualizer.setEnabled(enabled);
        }
    }
}
