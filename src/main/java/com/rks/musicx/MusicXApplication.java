

package com.rks.musicx;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.rks.musicx.data.eq.BassBoosts;
import com.rks.musicx.data.eq.Equalizers;
import com.rks.musicx.data.eq.Loud;
import com.rks.musicx.data.eq.Reverb;
import com.rks.musicx.data.eq.Virtualizers;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.permissionManager;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;


/*
 * Created by Coolalien on 6/28/2016.
 */

@ReportsCrashes(formUri = "https://coolalienx.cloudant.com/acra-musicx/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "theiraingthaddrifilightf",
        formUriBasicAuthPassword = "a144efe5c5929defb9523c02a7a6bbc24df3e066", customReportContent = {
        ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME,
        ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
        ReportField.BRAND, ReportField.USER_APP_START_DATE, ReportField.USER_CRASH_DATE,
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
        ArtworkUtils.init(this);
        instance = this;
        createDirectory();
        ACRA.init(this);
        AndroidNetworking.initialize(getApplicationContext());
        Extras.getInstance().setwidgetPosition(100);
        totalEqDefaulValue();
        Extras.getInstance().eqSwitch(false);
    }

  /**
   * Create directory
   */
  private void createDirectory() {
        if (permissionManager.writeExternalStorageGranted(mContext)) {
            Helper.createAppDir("Lyrics");
        } else {
            Log.d("oops error", "Failed to create directory");
        }
  }

    /**
     * Default init
     */
    private void totalEqDefaulValue() {
        Equalizers.initEqualizerValues();
        BassBoosts.initBassBoostValues();
        Virtualizers.initVirtualBoostValues();
        Loud.initLoudnessEnhancerValues();
        Reverb.initpresetReverbValues();
    }

}
