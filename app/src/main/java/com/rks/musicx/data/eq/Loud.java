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

public class Loud {

    private static LoudnessEnhancer loudnessEnhancer = null;

    public Loud() {
    }

    /*
     Init LoudnessEnhancer
    */
    public static void initLoudnessEnhancer(int audioID) {
        EndLoudnessEnhancer();
        try {
            loudnessEnhancer = new LoudnessEnhancer(audioID);
            int loud = Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0);
            if (loud > 0) {
                setLoudnessEnhancerGain(loud);
            }else {
                setLoudnessEnhancerGain(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLoudnessEnhancerGain(int gain) {
        if (loudnessEnhancer != null && gain >= 0) {
            try {
                if (gain <= GAIN_MAX) {
                    loudnessEnhancer.setTargetGain(gain);
                    saveLoudnessEnhancer(gain);
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
        }

    }

    public static void EndLoudnessEnhancer() {
        if (loudnessEnhancer != null) {
            loudnessEnhancer.release();
            loudnessEnhancer = null;
        }
    }


    public static void saveLoudnessEnhancer(int Gain) {
        SharedPreferences.Editor editor = Extras.getInstance().saveEq().edit();
        editor.putInt(LOUD_BOOST, Gain);
        editor.apply();
    }

    public static void setEnabled(boolean enabled) {
        if (loudnessEnhancer != null) {
            loudnessEnhancer.setEnabled(enabled);
        }
    }
}
