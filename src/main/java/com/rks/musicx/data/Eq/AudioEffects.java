package com.rks.musicx.data.eq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rks.musicx.misc.utils.Constants;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class AudioEffects extends BroadcastReceiver {

    public AudioEffects() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String scan = intent.getAction();
        int AudioID = intent.getIntExtra(Constants.AUDIO_ID, 0);
        if (Constants.OPEN_EFFECTS.equals(scan)) {
            Equalizers.initEq(AudioID);
            BassBoosts.initBass(AudioID);
            Virtualizers.initVirtualizer(AudioID);
            Loud.initLoudnessEnhancer(AudioID);
            Reverb.initReverb(AudioID);
        } else if (Constants.CLOSE_EFFECTS.equals(scan)) {
            Equalizers.EndEq();
            BassBoosts.EndBass();
            Virtualizers.EndVirtual();
            Loud.EndLoudnessEnhancer();
            Reverb.EndReverb();
        }
    }

}
