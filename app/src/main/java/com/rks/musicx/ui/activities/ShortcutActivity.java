package com.rks.musicx.ui.activities;

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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.widget.Toast;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.services.MusicXService;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**
 * Created by Coolalien on 6/4/2017.
 */
public class ShortcutActivity extends BaseActivity implements ATEActivityThemeCustomizer{

    @Override
    protected int setLayout() {
        return R.layout.shortcuts;
    }

    @Override
    protected void setUi() {
    }

    @Override
    protected void function() {
        int shortcutType = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            shortcutType = extras.getInt(Constants.SHORTCUTS_TYPES, 0);
        }
        switch (shortcutType) {
            case 1:
                startService(Constants.ACTION_PLAY);
                break;
            case 2:
                startService(Constants.ACTION_PAUSE);
                break;
            default:
                Toast.makeText(this, "Unknown shortcut", Toast.LENGTH_SHORT).show();
        }

        finish();
    }


    private void startService(final String action) {
        final Intent intent = new Intent(this, MusicXService.class);
        intent.setAction(action);
        startService(intent);
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return getStyleTheme();
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
