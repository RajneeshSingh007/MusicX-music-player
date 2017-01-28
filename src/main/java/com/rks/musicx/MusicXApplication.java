package com.rks.musicx;

import android.app.Application;
import android.content.Context;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.rks.musicx.data.Eq.BassBoosts;
import com.rks.musicx.data.Eq.Equalizers;
import com.rks.musicx.data.Eq.Loud;
import com.rks.musicx.data.Eq.Reverb;
import com.rks.musicx.data.Eq.Virtualizers;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

@ReportsCrashes(formUri = "https://coolalienx.cloudant.com/acra-musicx/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "theiraingthaddrifilightf",
        formUriBasicAuthPassword = "a144efe5c5929defb9523c02a7a6bbc24df3e066", customReportContent = {
        ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
        ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
        ReportField.BRAND,ReportField.USER_APP_START_DATE,ReportField.USER_CRASH_DATE,
        ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT},
        mode = ReportingInteractionMode.TOAST, resToastText = R.string.crash_toast_text)

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
        ACRA.init(this);
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

    /**
     * Initial value for equalizer
     */
    private void totalEqDefaulValue(){
        Equalizers.initEqualizerValues();
        BassBoosts.initBassBoostValues();
        Virtualizers.initVirtualBoostValues();
        Loud.initLoudnessEnhancerValues();
        Reverb.initpresetReverbValues();
    }

}
