package com.rks.musicx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.ui.fragments.EqFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.BlackTheme;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;

/*
 * Created by Coolalien on 06/01/2017.
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

public class EqualizerActivity extends BaseActivity implements ATEActivityThemeCustomizer {


    @Override
    protected int setLayout() {
        return R.layout.activity_equalizer;
    }

    @Override
    protected void setUi() {
    }

    @Override
    protected void function() {
        EqFragment eqFragment = new EqFragment();
        setFragment(eqFragment);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /*
    EqualizerMode Changing fragment Loader
     */
    public void setFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.eqContainer, f).commit();
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean darktheme = sharedPreferences.getBoolean(DarkTheme, false);
        Boolean blacktheme = sharedPreferences.getBoolean(BlackTheme, false);
        if (darktheme) {
            return R.style.AppThemeNormalDark;
        } else if (blacktheme) {
            return R.style.AppThemeNormalBlack;
        } else {
            return R.style.AppThemeNormalDark;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.onBackPressed();
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
