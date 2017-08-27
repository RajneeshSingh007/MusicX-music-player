package com.rks.musicx.base;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import static com.rks.musicx.misc.utils.Constants.BlackTheme;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.Four;
import static com.rks.musicx.misc.utils.Constants.LightTheme;
import static com.rks.musicx.misc.utils.Constants.One;
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

public abstract class BaseActivity extends ATEActivity {

    protected AppBarLayout appBarLayout;
    protected Toolbar toolbar;
    private int setLayout;
    private int ContainerId;
    private Fragment fragment;
    private long updateTime = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(setLayout());
        setUi();
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null && appBarLayout != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        updateTime = System.currentTimeMillis();
        fontConfig();
        function();
        themeConfig();
    }

    /*
    Set Layout
    */
    protected int setLayout() {
        return setLayout;
    }

    /*
    *@ui
    */
    protected abstract void setUi();

    /*
    *@function
    */
    protected abstract void function();


    /*
    fragment Loader
    */
    protected void fragmentLoader(int ContainerId, Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(ContainerId, fragment).commit();
    }

    public Fragment setFragment() {
        return fragment;
    }

    protected int setContainerId() {
        return ContainerId;
    }

    /**
     * Font load using Calligraphy
     */
    private void fontConfig() {
        switch (Extras.getInstance().fontConfig()) {
            case Zero:
                Helper.getCalligraphy(this,"RobotoLight.ttf");
                break;
            case One:
                Helper.getCalligraphy(this,"Raleway.ttf");
                break;
            case Two:
                Helper.getCalligraphy(this, "Knul.otf");
                break;
            case Three:
                Helper.getCalligraphy(this,"CutiveMono.ttf");
                break;
            case Four:
                Helper.getCalligraphy(this,"Timber.ttf");
                break;
            case "5":
                Helper.getCalligraphy(this,"Snippet.ttf");
                break;
            case "6":
                Helper.getCalligraphy(this,"Trench.ttf");
                break;
            case "7":
                Helper.getCalligraphy(this, "Monad.otf");
                break;
            case "8":
                Helper.getCalligraphy(this,"Rex.ttf");
                break;
            case "9":
                Helper.getCalligraphy(this,"ExodusStriped.otf");
                break;
            case "10":
                Helper.getCalligraphy(this,"GogiaRegular.otf");
                break;
            case "11":
                Helper.getCalligraphy(this,"MavenPro.ttf");
                break;
            case "12":
                Helper.getCalligraphy(this, "Vow.ttf");
                break;
            case "13":
                Helper.getCalligraphy(this, "Nunito.ttf");
                break;
            case "14":
                Helper.getCalligraphy(this,"Circled.ttf");
                break;
            case "15":
                Helper.getCalligraphy(this,"Franks.otf");
                break;
            case "16":
                Helper.getCalligraphy(this,"Mountain.otf");
                break;
            case "17":
                Helper.getCalligraphy(this,"Jakarta.ttf");
                break;
            case "18":
                Helper.getCalligraphy(this,"Abyssopelagic.otf");
                break;
            case "19":
                Helper.getCalligraphy(this,"Tesla.ttf");
                break;
            case "20":
                Helper.getCalligraphy(this,Typeface.DEFAULT.toString());
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ATE.postApply(this, getATEKey());
    }

    @Override
    protected void onResume() {
        super.onResume();
        ATE.invalidateActivity(this, updateTime, getATEKey());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            ATE.cleanup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ATE.themeOverflow(this, getATEKey());
        return super.onCreateOptionsMenu(menu);
    }

    @Nullable
    @Override
    public String getATEKey() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (Extras.getInstance().getDarkTheme()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(BlackTheme, false);
            editor.putBoolean(LightTheme, false);
            editor.commit();
            return DarkTheme;
        } else if (Extras.getInstance().getBlackTheme()) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DarkTheme, false);
            editor.putBoolean(LightTheme, false);
            editor.commit();
            return BlackTheme;
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DarkTheme, false);
            editor.putBoolean(BlackTheme, false);
            editor.commit();
            return LightTheme;
        }
    }

    public int getStyleTheme() {
        if (Extras.getInstance().getDarkTheme()) {
            return R.style.AppThemeNormalDark;
        } else if (Extras.getInstance().getBlackTheme()) {
            return R.style.AppThemeNormalBlack;
        } else {
            return R.style.AppThemeNormalLight;
        }
    }

    public String returnAteKey() {
        return getATEKey();
    }

    /**
     * Theme Config
     */
    private void themeConfig() {
        if (!ATE.config(this, LightTheme).isConfigured(4)) {
            ATE.config(this, LightTheme)
                    .activityTheme(R.style.AppThemeNormalLight)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .navigationViewSelectedIconRes(R.color.colorAccent)
                    .navigationViewSelectedTextRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .coloredStatusBar(true)
                    .commit();
        }
        if (!ATE.config(this, DarkTheme).isConfigured(4)) {
            ATE.config(this, DarkTheme)
                    .activityTheme(R.style.AppThemeNormalDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDarkTheme)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .navigationViewSelectedIconRes(R.color.colorAccentDarkDefault)
                    .navigationViewSelectedTextRes(R.color.colorAccentDarkDefault)
                    .coloredNavigationBar(true)
                    .coloredStatusBar(true)
                    .commit();
        }
        if (!ATE.config(this, BlackTheme).isConfigured(4)) {
            ATE.config(this, BlackTheme)
                    .activityTheme(R.style.AppThemeNormalBlack)
                    .primaryColorRes(R.color.colorPrimaryBlack)
                    .accentColorRes(R.color.colorAccentBlack)
                    .navigationViewSelectedIconRes(R.color.colorPrimaryBlack)
                    .navigationViewSelectedTextRes(R.color.colorPrimaryBlack)
                    .coloredNavigationBar(true)
                    .coloredStatusBar(true)
                    .commit();
        }
    }

}
