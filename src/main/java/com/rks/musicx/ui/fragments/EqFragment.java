package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.rks.musicx.R;
import com.rks.musicx.data.Eq.BassBoosts;
import com.rks.musicx.data.Eq.Equalizers;
import com.rks.musicx.data.Eq.Loud;
import com.rks.musicx.data.Eq.Reverb;
import com.rks.musicx.data.Eq.Virtualizers;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.widgets.EqView;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.data.Eq.Equalizers.getBandLevelRange;
import static com.rks.musicx.misc.utils.Constants.SAVEDBASS;
import static com.rks.musicx.misc.utils.Constants.SAVEDLOUD;
import static com.rks.musicx.misc.utils.Constants.SAVEDREVERB;
import static com.rks.musicx.misc.utils.Constants.SAVEDVIRTUALIZER;

/**
 * Created by Coolalien on 1/23/2017.
 */

public class EqFragment extends Fragment {

    private SwitchCompat switchCompat;
    private AppCompatSpinner appCompatSpinner;
    private String ateKey;
    private int accentcolor;
    private VerticalSeekBar[] seekBarFinal = new VerticalSeekBar[5];
    private EqView bassBoost,virtualizerBoost, LoudnessBoost, reverb;
    private VerticalSeekBar seekBar;
    private TextView textView;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager effectpager;
    private List<View> viewList;
    private View blg,rb;
    private PlayingPagerAdapter playingPagerAdapter;
    private int str,gain,virtualstr,presetreverb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.standard_eq, container, false);
        setupInstace(rootView);
        return rootView;
    }

    private void setupInstace(View rootView) {
        switchCompat = (SwitchCompat) rootView.findViewById(R.id.switch_button);
        effectpager = (ViewPager) rootView.findViewById(R.id.effect_pager);
        blg = LayoutInflater.from(getActivity()).inflate(R.layout.blg,null);
        bassBoost = (EqView) blg.findViewById(R.id.bassboost);
        virtualizerBoost = (EqView) blg.findViewById(R.id.virtualizerboost);
        LoudnessBoost = (EqView) blg.findViewById(R.id.loudboost);

        rb = LayoutInflater.from(getActivity()).inflate(R.layout.rb,null);
        reverb = (EqView) rb.findViewById(R.id.reverb);

        /**
         * Pager config
         */
        viewList = new ArrayList<>(2);
        viewList.add(blg);
        viewList.add(rb);
        playingPagerAdapter = new PlayingPagerAdapter(viewList);
        effectpager.setAdapter(playingPagerAdapter);

        switchEq();
        initBassBoost();
        initEq(rootView);
        initPresets(rootView);
        initVirtualizerBoost();
        initLoudnessBoost();
        initPresetReverbBoost();
        ateKey = Helper.getATEKey(getActivity());
        accentcolor = Config.accentColor(getActivity(),ateKey);

        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setShapePadding(20);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "800");
        sequence.setConfig(config);
        sequence.addSequenceItem(switchCompat, "Enabled/Disable equalizer", "GOT IT");
        sequence.addSequenceItem(effectpager, "slide right/left to see effects", "GOT IT");
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                config.setDelay(500);
            }
        });
        sequence.start();
    }

    /**
     * Enabled/Disabled eq
     */
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

    /**
     * Load preset in spinner
     */
    private void initPresets(View rootView) {
        appCompatSpinner = (AppCompatSpinner) rootView.findViewById(R.id.presets_spinner);
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, Equalizers.getEqualizerPresets(getActivity()));
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        appCompatSpinner.setAdapter(arrayAdapter);
        appCompatSpinner.setSelection(Equalizers.getCurrentPreset());
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    Equalizers.usePreset((short) (position - 1));
                    final short[] range = getBandLevelRange();
                    for (short i = 0; i < Equalizers.getNumberOfBands(); i++) {
                        if (range != null){
                            seekBarFinal[i].setProgress(Equalizers.getBandLevel(i) - range[0]);
                        }
                    }
                }else {
                    Log.d("EqFragment", "Error buddy");
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
        bassBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                str = ((int) (float)1000/19 * (progress));
                BassBoosts.setBassBoostStrength( (short) str);
                Extras.getInstance().mPreferences.edit().putInt(SAVEDBASS, str).apply();
            }

        });
        bassBoost.setProgress(Extras.getInstance().mPreferences.getInt(SAVEDBASS, str));
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            bassBoost.getTextPaint().setColor(ContextCompat.getColor(getActivity(),R.color.white));
        }
        bassBoost.setLabel(getString(R.string.Bass));
    }


    /**
     * PresetReverb
     */
    private void initPresetReverbBoost() {
        reverb.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                presetreverb = (progress * 6) / 19;
                Reverb.setPresetReverbStrength((short) presetreverb);
                Extras.getInstance().mPreferences.edit().putInt(SAVEDREVERB, presetreverb).apply();
            }

        });
        reverb.setProgress(Extras.getInstance().mPreferences.getInt(SAVEDREVERB, presetreverb));
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            reverb.getTextPaint().setColor(ContextCompat.getColor(getActivity(),R.color.white));
        }
        reverb.setLabel(getString(R.string.reverb));
    }


    /**
     * virtual boost
     */
    private void initVirtualizerBoost() {
        virtualizerBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                virtualstr = ((int) (float)1000/19 * progress);
                Virtualizers.setVirtualizerStrength((short) virtualstr);
                Extras.getInstance().mPreferences.edit().putInt(SAVEDVIRTUALIZER,virtualstr).apply();
            }
        });
        virtualizerBoost.setLabel(getString(R.string.Virtual));
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            virtualizerBoost.getTextPaint().setColor(ContextCompat.getColor(getActivity(),R.color.white));
        }
        virtualizerBoost.setProgress(Extras.getInstance().mPreferences.getInt(SAVEDVIRTUALIZER,virtualstr));
    }


    /**
     * LoudnessBoost
     */
    private void initLoudnessBoost() {
        LoudnessBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                gain = (int) (float)1000/19 * (progress);
                Loud.setLoudnessEnhancerGain(gain);
                Extras.getInstance().mPreferences.edit().putInt(SAVEDLOUD,gain).apply();
            }

        });
        LoudnessBoost.setProgress(Loud.getGain());
        LoudnessBoost.setLabel(getString(R.string.loudness));
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            LoudnessBoost.getTextPaint().setColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
        LoudnessBoost.setProgress(Extras.getInstance().mPreferences.getInt(SAVEDLOUD,virtualstr));
    }

    /**
     * Equalizer work
     */
    private void initEq(View rootView){
        try {
            for (short i = 0; i < Equalizers.getNumberOfBands(); i++) {
                final short eqbands = i;
                short[] bandLevel = Equalizers.getBandLevelRange();
                seekBar = new VerticalSeekBar(getActivity());
                textView = new TextView(getActivity());
                switch (i) {
                    case 0:
                        seekBar = (VerticalSeekBar) rootView.findViewById(R.id.seek_bar1);
                        textView = (TextView) rootView.findViewById(R.id.level1);
                        break;
                    case 1:
                        seekBar = (VerticalSeekBar) rootView.findViewById(R.id.seek_bar2);
                        textView = (TextView) rootView.findViewById(R.id.level2);
                        break;
                    case 2:
                        seekBar = (VerticalSeekBar) rootView.findViewById(R.id.seek_bar3);
                        textView = (TextView) rootView.findViewById(R.id.level3);
                        break;
                    case 3:
                        seekBar = (VerticalSeekBar) rootView.findViewById(R.id.seek_bar4);
                        textView = (TextView) rootView.findViewById(R.id.level4);
                        break;
                    case 4:
                        seekBar = (VerticalSeekBar) rootView.findViewById(R.id.seek_bar5);
                        textView = (TextView) rootView.findViewById(R.id.level5);
                        break;
                }
                if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                    textView.setTextColor(Color.WHITE);
                    seekBar.setThumb(ContextCompat.getDrawable(getActivity(), R.drawable.thumb));
                }else {
                    textView.setTextColor(Color.WHITE);
                    seekBar.setThumb(ContextCompat.getDrawable(getActivity(), R.drawable.thumb));
                }
                seekBarFinal[eqbands] = seekBar;
                seekBar.setId(i);
                if (bandLevel != null){
                    seekBar.setMax(bandLevel[1] - bandLevel[0]);
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
                            if (bandLevel != null){
                                Equalizers.setBandLevel(eqbands, (short) (progress + bandLevel[0]));
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
        }catch (Exception e){
            e.printStackTrace();
            Log.d("EqFragment", "Failed to init eq");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Equalizers.savePrefs();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getContext());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }

}
