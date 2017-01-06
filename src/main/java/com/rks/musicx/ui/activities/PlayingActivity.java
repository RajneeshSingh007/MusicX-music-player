package com.rks.musicx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.ui.fragments.Playing1Fragment;
import com.rks.musicx.ui.fragments.Playing2Fragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.One;
import static com.rks.musicx.misc.utils.Constants.PlayingView;
import static com.rks.musicx.misc.utils.Constants.Zero;


public class PlayingActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    private SharedPreferences sharedPreferences;

    @Override
    protected int setLayout() {
        return R.layout.activity_playing;
    }

    @Override
    protected void setUi() {
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences (this);
    }

    @Override
    protected void function() {
        String playing1 = sharedPreferences.getString(PlayingView,"0");
        String playing2 = sharedPreferences.getString(PlayingView,"1");
        Playing1Fragment playing1Fragment = new Playing1Fragment();
        Playing2Fragment playing2Fragment = new Playing2Fragment();
        if (playing1.equals(Zero)){
            setFragment(playing1Fragment);
        }else if (playing2.equals(One)) {
            setFragment(playing2Fragment);
        }else {
            setFragment(playing1Fragment);
        }
    }


    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_down);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.fadein, R.anim.slide_out_down);
        startActivity(intent);
    }


	@StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
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


