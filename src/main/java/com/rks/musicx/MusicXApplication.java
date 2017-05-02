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

    private void createDirectory() {
        if (permissionManager.writeExternalStorageGranted(mContext)) {
            Helper.createAppDir("Lyrics");
        } else {
            Log.d("oops error", "Failed to create directory");
        }
    }

    private void totalEqDefaulValue() {
        Equalizers.initEqualizerValues();
        BassBoosts.initBassBoostValues();
        Virtualizers.initVirtualBoostValues();
        Loud.initLoudnessEnhancerValues();
        Reverb.initpresetReverbValues();
    }

}
