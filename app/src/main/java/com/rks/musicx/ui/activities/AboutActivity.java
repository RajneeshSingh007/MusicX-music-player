package com.rks.musicx.ui.activities;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.StyleRes;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.DEVELOPER_NAME;

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

public class AboutActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    private TextView privacy, appcr, tester, testerName, guide_detail, support, licenses_detail, about_app_title, about_app_disc, about_app_ver, developer, developer_name, contact_detail, changeslog;
    private CardView AboutApp, AboutDev;
    private int accentcolor;

    @Override
    protected int setLayout() {
        return R.layout.activity_about;
    }

    @Override
    protected void setUi() {
        about_app_title = (TextView) findViewById(R.id.about_app_title);
        about_app_disc = (TextView) findViewById(R.id.about_app_disc);
        about_app_ver = (TextView) findViewById(R.id.about_app_ver);
        developer = (TextView) findViewById(R.id.developer);
        developer_name = (TextView) findViewById(R.id.developer_name);
        contact_detail = (TextView) findViewById(R.id.contact_detail);
        AboutApp = (CardView) findViewById(R.id.about_app_card);
        AboutDev = (CardView) findViewById(R.id.about_developer);
        licenses_detail = (TextView) findViewById(R.id.licenses_detail);
        changeslog = (TextView) findViewById(R.id.changelogs_detail);
        support = (TextView) findViewById(R.id.Support);
        guide_detail = (TextView) findViewById(R.id.guide_detail);
        testerName = (TextView) findViewById(R.id.testerName);
        tester = (TextView) findViewById(R.id.tester);
        appcr = (TextView) findViewById(R.id.appcr);
        privacy = (TextView) findViewById(R.id.privacy_detail);
    }

    @Override
    protected void function() {
        accentcolor = Config.accentColor(this, Helper.getATEKey(this));
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
        setSupportActionBar(toolbar);
        licenses_detail.setOnClickListener(v -> Helper.Licenses(AboutActivity.this));
        about_app_title.setText(getString(R.string.app_name));
        about_app_disc.setText("Elegant Material Music Player");
        guide_detail.setOnClickListener(view -> Helper.GuidLines(AboutActivity.this));
        contact_detail.setText(Html.fromHtml("<a href=\"mailto:developerrajneeshsingh@gmail.com\">Mail us</a>"));
        contact_detail.setMovementMethod(LinkMovementMethod.getInstance());
        about_app_ver.setText("v 1.4.1");
        developer_name.setText(DEVELOPER_NAME);
        licenses_detail.setText("Licenses");
        testerName.setText(getString(R.string.testerName));
        privacy.setText(Html.fromHtml("<a href=http://musicxplayer.tk/privacy.html> Privacy</a> "));
        privacy.setMovementMethod(LinkMovementMethod.getInstance());
        changeslog.setOnClickListener(view -> Helper.Changelogs(AboutActivity.this));
        guide_detail.setTextColor(accentcolor);
        contact_detail.setTextColor(accentcolor);
        licenses_detail.setTextColor(accentcolor);
        changeslog.setTextColor(accentcolor);
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            about_app_title.setTextColor(Color.WHITE);
            about_app_disc.setTextColor(Color.WHITE);
            about_app_ver.setTextColor(Color.WHITE);
            developer.setTextColor(Color.WHITE);
            developer_name.setTextColor(Color.WHITE);
            support.setTextColor(Color.WHITE);
            tester.setTextColor(Color.WHITE);
            appcr.setTextColor(Color.WHITE);
            testerName.setTextColor(Color.WHITE);
            privacy.setTextColor(Color.WHITE);
        } else {
            about_app_title.setTextColor(Color.BLACK);
            about_app_disc.setTextColor(Color.BLACK);
            about_app_ver.setTextColor(Color.BLACK);
            developer.setTextColor(Color.BLACK);
            developer_name.setTextColor(Color.BLACK);
            support.setTextColor(Color.BLACK);
            tester.setTextColor(Color.BLACK);
            appcr.setTextColor(Color.BLACK);
            testerName.setTextColor(Color.BLACK);
            privacy.setTextColor(Color.BLACK);
        }
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
