package com.rks.musicx.data.eq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.services.MediaPlayerSingleton;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class AudioEffects extends BroadcastReceiver {

    public AudioEffects() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String scan = intent.getAction();
        int AudioID = MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId();
        if (scan.equals(Constants.OPEN_EFFECTS)) {
            Equalizers.initEq(AudioID);
            BassBoosts.initBass(AudioID);
            Virtualizers.initVirtualizer(AudioID);
            Loud.initLoudnessEnhancer(AudioID);
            Reverb.initReverb(AudioID);
        } else if (scan.equals(Constants.CLOSE_EFFECTS)) {
            Equalizers.EndEq();
            BassBoosts.EndBass();
            Virtualizers.EndVirtual();
            Loud.EndLoudnessEnhancer();
            Reverb.EndReverb();
        }
    }

}
