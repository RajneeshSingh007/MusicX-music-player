package com.rks.musicx;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.rks.musicx.data.Eq.BassBoosts;
import com.rks.musicx.data.Eq.Equalizers;
import com.rks.musicx.data.Eq.Loud;
import com.rks.musicx.data.Eq.Virtualizers;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

public class MusicXApplication extends Application {

    private MusicXApplication instance;
    private Context mContext;

    public Context getContext() {
        return mContext;
    }

    public MusicXApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();
        Extras.init(this);
        instance = this;
        createDirectory();
        totalEqDefaulValue();
    }

    /**
     * create Directory
     */
    private void createDirectory(){
        if (PermissionChecker.checkSelfPermission(mContext, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED){
            new Helper(mContext).getAppDir();
        }else {
            Log.d("oops error", "Failed to create directory");
        }
    }

    private void totalEqDefaulValue(){
        Equalizers.initEqualizerValues();
        BassBoosts.initBassBoostValues();
        Virtualizers.initVirtualBoostValues();
        Loud.initLoudnessEnhancerValues();
    }

}
