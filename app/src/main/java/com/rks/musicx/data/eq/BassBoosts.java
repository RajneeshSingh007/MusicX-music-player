package com.rks.musicx.data.eq;

import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.util.Log;

import com.rks.musicx.misc.utils.Extras;

import static com.rks.musicx.misc.utils.Constants.BASSBOOST_STRENGTH;
import static com.rks.musicx.misc.utils.Constants.BASS_BOOST;

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

public class BassBoosts {

    private static BassBoost bassBoost = null;

    public BassBoosts() {
    }

    public static void initBass(int audioID) {
        EndBass();
        try {
            bassBoost = new BassBoost(0, audioID);
            short savestr = (short) Extras.getInstance().saveEq().getInt(BASS_BOOST, 0);
            if (savestr > 0) {
                setBassBoostStrength(savestr);
            }else {
                setBassBoostStrength((short) 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void EndBass() {
        if (bassBoost != null) {
            bassBoost.release();
            bassBoost = null;
        }
    }

    public static void setBassBoostStrength(short strength) {
        if (bassBoost != null && bassBoost.getStrengthSupported() &&  strength >= 0) {
            try {
                if (strength <= BASSBOOST_STRENGTH) {
                    bassBoost.setStrength(strength);
                    saveBass(strength);
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
        }
    }


    public static void saveBass(short strength) {
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        int str = (int) strength;
        editor.putInt(BASS_BOOST, str);
        editor.commit();
    }

    public static void setEnabled(boolean enabled) {
        if (bassBoost != null) {
            bassBoost.setEnabled(enabled);
        }
    }
}
