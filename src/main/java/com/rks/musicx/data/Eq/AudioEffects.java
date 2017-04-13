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
        if (scan.equals(Constants.OPEN_EFFECTS)) {
            Equalizers.initEq();
            BassBoosts.initBass();
            Virtualizers.initVirtualizer();
            Loud.initLoudnessEnhancer();
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
