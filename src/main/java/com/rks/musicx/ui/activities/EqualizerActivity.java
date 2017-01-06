package com.rks.musicx.ui.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.rks.musicx.R;
import com.rks.musicx.data.Eq.BassBoosts;
import com.rks.musicx.data.Eq.Equalizers;
import com.rks.musicx.data.Eq.Loud;
import com.rks.musicx.data.Eq.Virtualizers;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.widgets.EqView;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.DarkTheme;


public class EqualizerActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    private SwitchCompat switchCompat;
    private AppCompatSpinner appCompatSpinner;
    private String ateKey;
    private int accentcolor;
    private VerticalSeekBar[] seekBarFinal = new VerticalSeekBar[5];
    private EqView bassBoost,virtualizerBoost, LoudnessBoost;
    private VerticalSeekBar seekBar;
    private TextView textView;

    @Override
    protected int setLayout() {
        return R.layout.activity_equalizer;
    }

    @Override
    protected void setUi() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        overridePendingTransition(R.anim.slide_in_up, R.anim.fade_back);
        switchCompat = (SwitchCompat) findViewById(R.id.switch_button);
        bassBoost = (EqView) findViewById(R.id.bassboost);
        virtualizerBoost = (EqView) findViewById(R.id.virtualizerboost);
        LoudnessBoost = (EqView) findViewById(R.id.loudboost);
    }

    @Override
    protected void function() {
        switchEq();
        initBassBoost();
        initEq();
        initPresets();
        initVirtualizerBoost();
        initLoudnessBoost();
        ateKey = Helper.getATEKey(this);
        accentcolor = Config.accentColor(this,ateKey);
    }

    @Override
    public void onPause() {
        super.onPause();
        BassBoosts.saveBass();
        Equalizers.savePrefs();
        Virtualizers.saveVirtual();
        Loud.saveLoudnessEnhancer();
    }

    private void switchEq() {
        if (switchCompat != null) {
            switchCompat.setChecked(Equalizers.isEnabled());
            switchCompat.setChecked(BassBoosts.isEnabled());
            switchCompat.setChecked(Virtualizers.isEnabled());
            switchCompat.setChecked(Loud.isEnabled());
            switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    Equalizers.setEnabled(b);
                    BassBoosts.setEnabled(b);
                    Virtualizers.setEnabled(b);
                    Loud.setEnabled(b);
                }
            });
        }
    }

    private void initPresets() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Equalizers.getEqualizerPresets(this));
        appCompatSpinner = (AppCompatSpinner) findViewById(R.id.presets_spinner);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appCompatSpinner.setAdapter(adapter);
        appCompatSpinner.setSelection(Equalizers.getCurrentPreset());
        appCompatSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 1) {
                    Equalizers.usePreset((short) (position - 1));
                }
                short numberOfFreqBands = 5;
                final short[] range = Equalizers.getBandLevelRange();
                for (short i = 0; i < numberOfFreqBands; i++) {
                    if (range !=null){
                        seekBarFinal[i].setProgress(Equalizers.getBandLevel(i) - range[0]);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * bassbost
     */
    private void initBassBoost() {
       if (bassBoost != null){
           bassBoost.setProgress(BassBoosts.getStr());
           bassBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
               @Override
               public void onProgressChanged(int progress) {
                   short bassStrength = (short)  ((float) 1000/19 * (progress));
                   BassBoosts.setBassBoostStrength(bassStrength);
               }
           });
           bassBoost.setProgressColor(accentcolor);
           if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
               bassBoost.getTextPaint().setColor(ContextCompat.getColor(this,R.color.white));
           }
           bassBoost.setLabel("BASS");
       }else {
           bassBoost.setProgress(0);
           bassBoost.setProgressColor(accentcolor);
           if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
               bassBoost.getTextPaint().setColor(ContextCompat.getColor(this,R.color.white));
           }
           bassBoost.setLabel("BASS");
       }
    }

    /**
     * virtual boost
     */
    private void initVirtualizerBoost() {
        if (virtualizerBoost != null){
            virtualizerBoost.setProgress(Virtualizers.getVirtualStrength());
            virtualizerBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
                @Override
                public void onProgressChanged(int progress) {
                    short virtualStrength = (short) (((float) 1000 / 19) * (progress));
                    Virtualizers.setVirtualizerStrength(virtualStrength);
                }
            });
            virtualizerBoost.setProgressColor(accentcolor);
            virtualizerBoost.setLabel("Virtual");
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
                virtualizerBoost.getTextPaint().setColor(ContextCompat.getColor(this,R.color.white));
            }
        }else {
            virtualizerBoost.setProgress(0);
            virtualizerBoost.setProgressColor(accentcolor);
            virtualizerBoost.setLabel("Virtual");
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
                virtualizerBoost.getTextPaint().setColor(ContextCompat.getColor(this,R.color.white));
            }
        }

    }

    /**
     * LoudnessBoost
     */
    private void initLoudnessBoost() {
        if (LoudnessBoost != null){
            LoudnessBoost.setProgress(Loud.getGain());
            LoudnessBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
                @Override
                public void onProgressChanged(int progress) {
                    short loudGain = (short) (((float) 1000 / 19) * (progress));
                    Loud.setLoudnessEnhancerGain(loudGain);
                }
            });
            LoudnessBoost.setProgressColor(accentcolor);
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
                LoudnessBoost.getTextPaint().setColor(ContextCompat.getColor(this,R.color.white));
            }
            LoudnessBoost.setLabel("LOUDNESS");
        }else {
            LoudnessBoost.setProgress(0);
            LoudnessBoost.setProgressColor(accentcolor);
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
                LoudnessBoost.getTextPaint().setColor(ContextCompat.getColor(this,R.color.white));
            }
            LoudnessBoost.setLabel("LOUDNESS");
        }

    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalDark;
    }

    /**
     * Equalizer work
     */
    private void initEq(){
        new AsyncTask<Void, Void, Void>() {

            final short[] range = Equalizers.getBandLevelRange();
            short bands = Equalizers.getNumberOfBands();
            @Override
            protected Void doInBackground(Void... voids) {
                for (short i = 0; i < bands; i++) {
                    final short eqbands = i;
                    seekBar = new VerticalSeekBar(EqualizerActivity.this);
                    textView = new TextView(EqualizerActivity.this);
                    switch (i) {
                        case 0:
                            seekBar = (VerticalSeekBar) findViewById(R.id.seek_bar1);
                            textView = (TextView) findViewById(R.id.level1);
                            break;
                        case 1:
                            seekBar = (VerticalSeekBar) findViewById(R.id.seek_bar2);
                            textView = (TextView) findViewById(R.id.level2);
                            break;
                        case 2:
                            seekBar = (VerticalSeekBar) findViewById(R.id.seek_bar3);
                            textView = (TextView) findViewById(R.id.level3);
                            break;
                        case 3:
                            seekBar = (VerticalSeekBar) findViewById(R.id.seek_bar4);
                            textView = (TextView) findViewById(R.id.level4);
                            break;
                        case 4:
                            seekBar = (VerticalSeekBar) findViewById(R.id.seek_bar5);
                            textView = (TextView) findViewById(R.id.level5);
                            break;
                    }
                    if (PreferenceManager.getDefaultSharedPreferences(EqualizerActivity.this).getBoolean("dark_theme", false)) {
                        Drawable drawable = ContextCompat.getDrawable(EqualizerActivity.this,R.drawable.thumb);
                        drawable.setColorFilter(accentcolor, PorterDuff.Mode.SRC_ATOP);
                        seekBar.setThumb(drawable);
                        textView.setTextColor(Color.WHITE);
                    }else {
                        Drawable drawable = ContextCompat.getDrawable(EqualizerActivity.this,R.drawable.thumb);
                        drawable.setColorFilter(accentcolor, PorterDuff.Mode.SRC_ATOP);
                        seekBar.setThumb(drawable);
                        textView.setTextColor(Color.WHITE);
                    }
                    seekBarFinal[eqbands] = seekBar;
                    seekBar.setId(i);
                    if (range !=null){
                        seekBar.setMax(range[1] - range[0]);
                    }
                    int frequency = Equalizers.getCenterFreq(eqbands);
                    if (frequency < 1000 * 1000) {
                        textView.setText((frequency / 1000) + "Hz");
                    } else {
                        textView.setText((frequency / (1000 * 1000)) + "kHz");
                    }
                    seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            if (fromUser) {
                                if (range != null){
                                    Equalizers.setBandLevel(eqbands, (short) (progress + range[0]));
                                }
                                appCompatSpinner.setSelection(0);
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }
                    });

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        }.execute();


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
