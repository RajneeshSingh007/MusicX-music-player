package com.rks.musicx.ui.activities;

import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.Four;
import static com.rks.musicx.misc.utils.Constants.LightTheme;
import static com.rks.musicx.misc.utils.Constants.One;
import static com.rks.musicx.misc.utils.Constants.Three;
import static com.rks.musicx.misc.utils.Constants.Two;
import static com.rks.musicx.misc.utils.Constants.Zero;

import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.ATEActivity;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.Extras;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


/*
 * Created by Coolalien on 6/28/2016.
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

    protected Fragment setFragment() {
        return fragment;
    }

    protected int setContainerId() {
        return ContainerId;
    }


    private void fontConfig() {
        switch (Extras.getInstance().fontConfig()) {
            case Zero:
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("RobotoLight.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case One:
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Raleway.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case Two:
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("CormorantGaramond.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case Three:
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("CutiveMono.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case Four:
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Timber.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case "5":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Snippet.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case "6":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Trench.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case "7":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Espacio.ttf")
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .setFontAttrId(R.attr.fontPath)
                        .build());
                break;
            case "8":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Rex.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "9":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("ExodusStriped.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "10":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("GogiaRegular.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "11":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("MavenPro.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "12":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Vetka.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "13":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Lombok.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "14":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Circled.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "15":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Franks.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "16":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Mountain.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "17":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Jakarta.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "18":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Abyssopelagic.otf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
            case "19":
                CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("Tesla.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                        .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                        .build());
                break;
          case "20":
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(Typeface.DEFAULT.toString())
                .setFontAttrId(R.attr.fontPath)
                .addCustomStyle(AppCompatTextView.class, android.R.attr.textViewStyle)
                .addCustomStyle(TextView.class, android.R.attr.textViewStyle)
                .addCustomStyle(EditText.class, android.R.attr.editTextStyle)
                .build());
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
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ? DarkTheme : LightTheme;
    }

    public String returnAteKey() {
        return getATEKey();
    }

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
    }

}
