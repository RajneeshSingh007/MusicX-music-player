package com.rks.musicx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.ui.fragments.Playing1Fragment;
import com.rks.musicx.ui.fragments.Playing2Fragment;
import com.rks.musicx.ui.fragments.Playing3Fragment;
import com.rks.musicx.ui.fragments.Playing4Fragment;

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
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
    }

    @Override
    protected void function() {

        String playing1 = Extras.getInstance().mPreferences.getString(PlayingView, Zero);
        String playing2 = Extras.getInstance().mPreferences.getString(PlayingView, One);
        String playing3 = Extras.getInstance().mPreferences.getString(PlayingView, Two);
        String playing4 = Extras.getInstance().mPreferences.getString(PlayingView, Three);
      //  String playing5 = Extras.getInstance().mPreferences.getString(PlayingView, Four);

        Playing1Fragment playing1Fragment = new Playing1Fragment();
        Playing2Fragment playing2Fragment = new Playing2Fragment();
        Playing3Fragment playing3Fragment = new Playing3Fragment();
        Playing4Fragment playing4Fragment = new Playing4Fragment();
       // Playing5Fragment playing5Fragment = new Playing5Fragment();

        if (playing1.equals(Zero)) {
            setFragment(playing1Fragment);
        } else if (playing2.equals(One)) {
            setFragment(playing2Fragment);
        } else if (playing3.equals(Two)) {
            setFragment(playing3Fragment);
        } else if (playing4.equals(Three)) {
            setFragment(playing4Fragment);
        }else {
            setFragment(playing1Fragment);
        }/*else if (playing5.equals(Four)){
            setFragment(playing5Fragment);
        }else {
            setFragment(playing1Fragment);
        }*/
    }


    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_down);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_forward);
        startActivity(intent);
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
}


