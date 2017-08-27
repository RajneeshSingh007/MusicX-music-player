package com.rks.musicx.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceFragment;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.ui.fragments.SettingsFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


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

public class SettingsActivity extends BaseActivity implements ColorChooserDialog.ColorCallback, ATEActivityThemeCustomizer {

    private TextView settingstitle;

    @Override
    protected int setLayout() {
        return R.layout.activity_settings;
    }

    @Override
    protected void setUi() {
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

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {

    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return getStyleTheme();
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
        if (Extras.getInstance().getNavSettings()) {
            Intent intent = new Intent(this, PlayingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
