package com.rks.musicx;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Encryption;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.permissionManager;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.PlayStoreListener;

import javax.annotation.Nonnull;


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
        formUriBasicAuthLogin = "hishorybrentraolutterece",
        formUriBasicAuthPassword = "7a6a382cba5e7a20e8aec27a2311d9aa4d83c5cb", customReportContent = {
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

    @Nonnull
    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {
        @Nonnull
        @Override
        public String getPublicKey() {
            final String s = "use your key";
            return Encryption.xor(s, "decryption string");
        }
    });

    @Override
    public void onCreate() {
        mContext = getApplicationContext();
        super.onCreate();
        Extras.init(this);
        ArtworkUtils.init(this);
        instance = this;
        createDirectory();
        ACRA.init(this);
        Extras.getInstance().setwidgetPosition(100);
        Extras.getInstance().eqSwitch(false);
        mBilling.addPlayStoreListener(new PlayStoreListener() {
            @Override
            public void onPurchasesChanged() {
                Toast.makeText(MusicXApplication.this, "Play Store: purchases have changed!", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void createDirectory() {
        if (permissionManager.writeExternalStorageGranted(mContext)) {
            Helper.createAppDir("Lyrics");
            Helper.createAppDir(".AlbumArtwork");
            Helper.createAppDir(".ArtistArtwork");
        } else {
            Log.d("oops error", "Failed to create directory");
        }
    }

    @Nonnull
    public Billing getmBilling() {
        return mBilling;
    }

    public static MusicXApplication get(Activity activity) {
        return (MusicXApplication) activity.getApplication();
    }
}
