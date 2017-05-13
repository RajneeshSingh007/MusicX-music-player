package com.rks.musicx.data.eq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.services.MediaPlayerSingleton;

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

public class AudioEffects extends BroadcastReceiver {

    public AudioEffects() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String scan = intent.getAction();
        int audioID = MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId();
        if (scan.equals(Constants.OPEN_EFFECTS)) {
            Equalizers.initEq(audioID);
            BassBoosts.initBass(audioID);
            Virtualizers.initVirtualizer(audioID);
            Loud.initLoudnessEnhancer(audioID);
            Reverb.initReverb();
        } else if (scan.equals(Constants.CLOSE_EFFECTS)) {
            Equalizers.EndEq();
            BassBoosts.EndBass();
            Virtualizers.EndVirtual();
            Loud.EndLoudnessEnhancer();
            Reverb.EndReverb();
        }
    }

}
