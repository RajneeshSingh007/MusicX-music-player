package com.rks.musicx.ui.activities;

import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.TextView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Helper;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.DEVELOPER_NAME;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;

public class AboutActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    TextView guide_detail,support,licenses,licenses_detail,about_app_title,about_app_disc,about_app_ver,developer,developer_name,contact_detail,changeslog;
    CardView AboutApp,AboutDev;

    @Override
    protected int setLayout() {
        return R.layout.activity_about;
    }

    @Override
    protected void setUi() {
        licenses = (TextView) findViewById(R.id.licenses);
        about_app_title = (TextView) findViewById(R.id.about_app_title);
        about_app_disc = (TextView) findViewById(R.id.about_app_disc);
        about_app_ver = (TextView) findViewById(R.id.about_app_ver);
        developer = (TextView) findViewById(R.id.developer);
        developer_name = (TextView) findViewById(R.id.developer_name);
        contact_detail = (TextView) findViewById(R.id.contact_detail);
        AboutApp = (CardView) findViewById(R.id.about_app_card);
        AboutDev = (CardView) findViewById(R.id.about_developer);
        licenses_detail = (TextView) findViewById(R.id.licenses_detail);
        changeslog  = (TextView) findViewById(R.id.changelogs_detail);
        support = (TextView) findViewById(R.id.Support);
        guide_detail = (TextView) findViewById(R.id.guide_detail);
    }

    @Override
    protected void function() {
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
        setSupportActionBar(toolbar);
        licenses_detail.setOnClickListener(v -> Helper.Licenses(AboutActivity.this));
        about_app_title.setText(getString(R.string.app_name));
        about_app_disc.setText("Elegant Material Music Player");
        developer.setText("Developer : ");
        guide_detail.setOnClickListener(view -> Helper.GuidLines(AboutActivity.this));
        contact_detail.setText(Html.fromHtml("<a href=\"mailto:developerrajneeshsingh@gmail.com\">Send Feedback</a>"));
        contact_detail.setMovementMethod(LinkMovementMethod.getInstance());
        about_app_ver.setText("v 1.0");
        developer_name.setText(DEVELOPER_NAME);
        licenses_detail.setText("Licenses");
        changeslog.setOnClickListener(view -> Helper.Changelogs(AboutActivity.this));
        if (!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
            about_app_title.setTextColor(Color.BLACK);
            about_app_disc.setTextColor(Color.BLACK);
            about_app_ver.setTextColor(Color.BLACK);
            developer.setTextColor(Color.BLACK);
            developer_name.setTextColor(Color.BLACK);
            contact_detail.setTextColor(Color.BLACK);
            licenses.setTextColor(Color.BLACK);
            licenses_detail.setTextColor(Color.BLACK);
            changeslog.setTextColor(Color.BLACK);
            support.setTextColor(Color.BLACK);
            guide_detail.setTextColor(Color.BLACK);
        }else {
            AboutDev.setCardBackgroundColor(ContextCompat.getColor(this,R.color.MaterialGrey));
            AboutApp.setCardBackgroundColor(ContextCompat.getColor(this,R.color.MaterialGrey));
        }
    }


    @StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
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
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
