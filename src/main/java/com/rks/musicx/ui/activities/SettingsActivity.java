package com.rks.musicx.ui.activities;

import android.content.Context;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.MenuItem;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.rks.musicx.R;
import com.rks.musicx.misc.widgets.TextView;
import com.rks.musicx.ui.fragments.SettingsFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.DarkTheme;

public class SettingsActivity extends BaseActivity implements ColorChooserDialog.ColorCallback, ATEActivityThemeCustomizer {

    private TextView settingstitle;

    @Override
    protected int setLayout() {
        return R.layout.activity_settings;
    }

    @Override
    protected void setUi() {
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
        settingstitle = (TextView) findViewById(R.id.settings_title);
    }

    @Override
    protected void function() {
        settingstitle.setText("Settings");
        PreferenceFragment fragment = new SettingsFragment();
        getFragmentManager().beginTransaction().replace(R.id.settings_container, fragment).commit();
    }


    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        final Config config = ATE.config(this, getATEKey());
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                config.primaryColor(selectedColor);
                break;
            case R.string.accent_color:
                config.accentColor(selectedColor);
                break;
        }
        config.commit();
        recreate();
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
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
