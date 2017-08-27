package com.rks.musicx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.bumptech.glide.Glide;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.ui.fragments.PlayingViews.Playing1Fragment;
import com.rks.musicx.ui.fragments.PlayingViews.Playing2Fragment;
import com.rks.musicx.ui.fragments.PlayingViews.Playing3Fragment;
import com.rks.musicx.ui.fragments.PlayingViews.Playing4Fragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.One;
import static com.rks.musicx.misc.utils.Constants.PlayingView;
import static com.rks.musicx.misc.utils.Constants.Three;
import static com.rks.musicx.misc.utils.Constants.Two;
import static com.rks.musicx.misc.utils.Constants.Zero;


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

public class PlayingActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    @Override
    protected int setLayout() {
        return R.layout.activity_playing;
    }

    @Override
    protected void setUi() {
    }

    @Override
    protected void function() {

        String playing1 = Extras.getInstance().getmPreferences().getString(PlayingView, Zero);
        String playing2 = Extras.getInstance().getmPreferences().getString(PlayingView, One);
        String playing3 = Extras.getInstance().getmPreferences().getString(PlayingView, Two);
        String playing4 = Extras.getInstance().getmPreferences().getString(PlayingView, Three);

        Playing1Fragment playing1Fragment = new Playing1Fragment();
        Playing2Fragment playing2Fragment = new Playing2Fragment();
        Playing3Fragment playing3Fragment = new Playing3Fragment();
        Playing4Fragment playing4Fragment = new Playing4Fragment();

        if (playing1.equals(Zero)) {
            setFragment(playing1Fragment);
            Extras.getInstance().savePlayingViewTrack(false);
        } else if (playing2.equals(One)) {
            Extras.getInstance().savePlayingViewTrack(false);
            setFragment(playing2Fragment);
        } else if (playing3.equals(Two)) {
            Extras.getInstance().savePlayingViewTrack(true);
            setFragment(playing3Fragment);
        } else if (playing4.equals(Three)) {
            Extras.getInstance().savePlayingViewTrack(false);
            setFragment(playing4Fragment);
        } else {
            setFragment(playing1Fragment);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        returnHome();
    }


    /**
     * Launch mainAcivity
     */
    public void returnHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Launch EqAcivity
     */
    public void returnEq() {
        Intent intent = new Intent(this, EqualizerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Launch SettingsAcivity
     */
    public void returnSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Extras.getInstance().setNaviSettings(true);
        finish();
    }


    @StyleRes
    @Override
    public int getActivityTheme() {
        return getStyleTheme();
    }


    /*
    PlayingScreen Changing fragment Loader
    */
    public void setFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.playingcontainer, f).commit();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }
}


