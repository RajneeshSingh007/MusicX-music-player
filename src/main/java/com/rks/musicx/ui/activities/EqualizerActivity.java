package com.rks.musicx.ui.activities;

import static com.rks.musicx.misc.utils.Constants.DarkTheme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.ui.fragments.EqFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
 * Created by Coolalien on 06/01/2017.
 */

public class EqualizerActivity extends BaseActivity implements ATEActivityThemeCustomizer {


    @Override
    protected int setLayout() {
        return R.layout.activity_equalizer;
    }

    @Override
    protected void setUi() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
    }

    @Override
    protected void function() {
        EqFragment eqFragment = new EqFragment();
        setFragment(eqFragment);
    }

    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_forward, R.anim.slide_out_down);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(R.anim.fadein, R.anim.slide_out_down);
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
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalDark;
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
