package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.h6ah4i.android.widget.verticalseekbar.VerticalSeekBar;
import com.rks.musicx.R;
import com.rks.musicx.data.eq.BassBoosts;
import com.rks.musicx.data.eq.Equalizers;
import com.rks.musicx.data.eq.Loud;
import com.rks.musicx.data.eq.Reverb;
import com.rks.musicx.data.eq.Virtualizers;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.widgets.EqView;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.misc.utils.Constants.BAND_LEVEL;
import static com.rks.musicx.misc.utils.Constants.BASS_BOOST;
import static com.rks.musicx.misc.utils.Constants.LOUD_BOOST;
import static com.rks.musicx.misc.utils.Constants.PRESET_BOOST;
import static com.rks.musicx.misc.utils.Constants.VIRTUAL_BOOST;

/*
 * Created by Coolalien on 06/01/2017.
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

public class EqFragment extends Fragment {

    private final String TAG = EqFragment.class.getSimpleName();
    private SwitchCompat switchCompat;
    private AppCompatSpinner appCompatSpinner;
    private String ateKey;
    private int accentcolor;
    private VerticalSeekBar[] seekBarFinal = new VerticalSeekBar[5];
    private EqView bassBoost, virtualizerBoost, loudnessBoost, reverb;
    private VerticalSeekBar seekBar;
    private TextView textView;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager effectpager;
    private List<View> viewList;
    private View eqView, eqViews;
    private PlayingPagerAdapter playingPagerAdapter;
    private int gain;
    private float inc;
    private short str,virtualstr,presetreverb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eq, container, false);
        setupInstace(rootView);
        return rootView;
    }

    private void setupInstace(View rootView) {
        switchCompat = (SwitchCompat) rootView.findViewById(R.id.switch_button);
        effectpager = (ViewPager) rootView.findViewById(R.id.effect_pager);
        eqView = LayoutInflater.from(getContext()).inflate(R.layout.eq_view, new LinearLayout(getContext()), false);
        bassBoost = (EqView) eqView.findViewById(R.id.bassboost);
        virtualizerBoost = (EqView) eqView.findViewById(R.id.virtualizerboost);
        loudnessBoost = (EqView) eqView.findViewById(R.id.loudboost);

        eqViews = LayoutInflater.from(getContext()).inflate(R.layout.eq_views, new LinearLayout(getContext()), false);
        reverb = (EqView) eqViews.findViewById(R.id.reverb);

        /**
         * Pager config
         */
        viewList = new ArrayList<>(2);
        viewList.add(eqView);
        viewList.add(eqViews);
        playingPagerAdapter = new PlayingPagerAdapter(viewList);
        effectpager.setAdapter(playingPagerAdapter);
        switchEq();
        initBassBoost();
        initEq(rootView);
        initPresets(rootView);
        initVirtualizerBoost();
        initLoudnessBoost();
        initPresetReverbBoost();

        ateKey = Helper.getATEKey(getContext());
        accentcolor = Config.accentColor(getContext(), ateKey);

        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setShapePadding(20);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "800");
        sequence.setConfig(config);
        sequence.addSequenceItem(switchCompat, "Enabled/Disable equalizer", "GOT IT");
        sequence.addSequenceItem(effectpager, "Slide right/left to see effects", "GOT IT");
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                config.setDelay(500);
            }
        });
        sequence.start();
    }

    /**
     * Switch Config
     */
    private void switchEq() {
        if (switchCompat != null) {
            switchCompat.setChecked(Extras.getInstance().geteqSwitch());
            switchCompat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (switchCompat.isChecked()) {
                        Extras.getInstance().eqSwitch(true);
                        enableDisable(true);
                    } else {
                        Extras.getInstance().eqSwitch(false);
                        enableDisable(false);

                    }
                }
            });
        }
    }

    /**
     * Enable/Disable
     *
     * @param onoroff
     */
    private void enableDisable(Boolean onoroff) {
        Equalizers.setEnabled(onoroff);
        BassBoosts.setEnabled(onoroff);
        Virtualizers.setEnabled(onoroff);
        Loud.setEnabled(onoroff);
        Reverb.setEnabled(onoroff);
    }

    /**
     * Presets
     * @param rootView
     */
    private void initPresets(View rootView) {
        appCompatSpinner = (AppCompatSpinner) rootView.findViewById(R.id.presets_spinner);
        List<String> presetList = getPresetNames();
        if (presetList == null || presetList.size() == 0) {
            return;
        }
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int presets = 0; presets < presetList.size(); presets++) {
            arrayAdapter.add(presetList.get(presets));
            appCompatSpinner.setAdapter(arrayAdapter);
        }
        int presetPos = Extras.getInstance().getPresetPos();
        if (presetPos < presetList.size()){
            appCompatSpinner.setSelection(presetPos);
        }
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < presetList.size()){
                    Extras.getInstance().savePresetPos(position);
                    short presetNo = Equalizers.getPresetNo();
                    short bandNo = Equalizers.getNumberOfBands();
                    if (presetNo != -1 && bandNo != -1) {
                        if (position < presetNo) {
                            short presetPosition = (short) position;
                            Equalizers.usePreset(presetPosition);
                            for (short i = 0; i < bandNo; i++) {
                                final short[] range = Equalizers.getBandLevelRange();
                                short level = Equalizers.getBandLevel(i);
                                if (range != null) {
                                    if (i < seekBarFinal.length) {
                                        seekBarFinal[i].setProgress(level - range[0]);
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Error buddy");
                            for (int i = 0; i < bandNo; i++) {
                                final short[] range = Equalizers.getBandLevelRange();
                                if (range != null) {
                                    if (i < seekBarFinal.length) {
                                        seekBarFinal[i].setProgress(Extras.getInstance().saveEq().getInt(BAND_LEVEL + i, 0) - range[0]);
                                    }
                                }
                            }
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    private List<String> getPresetNames() {
        List<String> presets = new ArrayList<>();
        short presetsNo = Equalizers.getPresetNo();
        if (presetsNo != 0) {
            for (short n = 0; n < presetsNo; n++) {
                presets.add(Equalizers.getPresetNames(n));
                Log.d(TAG, String.valueOf(n) + String.valueOf(presets.get(n)));
            }
            presets.add(getString(R.string.custom));
        }
        return presets;
    }

    private void initBassBoost() {
        bassBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                str = (short) (((float) 1000 / 20) * (progress));
                BassBoosts.setBassBoostStrength(str);

            }

        });
        int savedBass = (Extras.getInstance().saveEq().getInt(BASS_BOOST, 0) * 20) / 1000;
        if (savedBass > 0) {
            bassBoost.setProgress(savedBass);
            BassBoosts.setBassBoostStrength((short) savedBass);
        }else {
            bassBoost.setProgress(1);
        }
        bassBoost.getTextPaint().setTypeface(Helper.getFont(getContext()));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            bassBoost.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        bassBoost.setLabel(getString(R.string.Bass));
    }


    private void initPresetReverbBoost() {
        reverb.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                presetreverb = (short) (((float) 6 / 20) * (progress));
                Reverb.setPresetReverbStrength(presetreverb);
            }

        });
        int saveReverb = (Extras.getInstance().saveEq().getInt(PRESET_BOOST, 0) * 20 ) / 6;
        if (saveReverb > 0) {
            reverb.setProgress(saveReverb);
            Reverb.setPresetReverbStrength((short) saveReverb);
        } else {
            reverb.setProgress(1);
        }
        reverb.getTextPaint().setTypeface(Helper.getFont(getContext()));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            reverb.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        reverb.setLabel(getString(R.string.reverb));
    }


    private void initVirtualizerBoost() {
        virtualizerBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                virtualstr =  (short) (((float) 1000 / 20) * (progress));
                Virtualizers.setVirtualizerStrength(virtualstr);
            }
        });
        virtualizerBoost.setLabel(getString(R.string.Virtual));
        int saveVirtual = (Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0)  * 20) / 1000;
        if ( saveVirtual > 0) {
            virtualizerBoost.setProgress(saveVirtual);
            Virtualizers.setVirtualizerStrength((short) saveVirtual);
        } else {
            virtualizerBoost.setProgress(1);
        }
        virtualizerBoost.getTextPaint().setTypeface(Helper.getFont(getContext()));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            virtualizerBoost.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
    }


    private void initLoudnessBoost() {
        loudnessBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                gain =   (short) (((float) 100 / 20) * (progress));
                Loud.setLoudnessEnhancerGain(gain);
            }

        });
        int saveLoud = (Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0) * 20 ) / 100;
        if (saveLoud > 0) {
            loudnessBoost.setProgress(saveLoud);
            Loud.setLoudnessEnhancerGain(saveLoud);
        } else {
            loudnessBoost.setProgress(1);
        }
        loudnessBoost.getTextPaint().setTypeface(Helper.getFont(getContext()));
        loudnessBoost.setLabel(getString(R.string.loudness));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            loudnessBoost.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
    }


    private void initEq(View rootView) {
        try {
            for (short i = 0; i < Equalizers.getNumberOfBands(); i++) {
                short eqbands = i;
                short[] bandLevel = Equalizers.getBandLevelRange();
                seekBar = new VerticalSeekBar(getContext());
                textView = new TextView(getContext());
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
                seekBarFinal[eqbands] = seekBar;
                seekBar.setId(i);
                if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()){
                    textView.setTextColor(Color.WHITE);
                    seekBar.setThumb(ContextCompat.getDrawable(getContext(), R.drawable.thumb));
                    seekBar.getThumb().setTint(accentcolor);
                    seekBar.setProgressTintMode(PorterDuff.Mode.SRC_ATOP);
                    seekBar.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                }else {
                    textView.setTextColor(Color.WHITE);
                    seekBar.setThumb(ContextCompat.getDrawable(getContext(), R.drawable.thumb));
                    seekBar.getThumb().setTint(accentcolor);
                    seekBar.setProgressTintMode(PorterDuff.Mode.SRC_ATOP);
                    seekBar.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
                }
                if (bandLevel != null){
                    seekBar.setMax(bandLevel[1] - bandLevel[0]);
                    int presetPos = Extras.getInstance().getPresetPos();
                    if (presetPos < Equalizers.getPresetNo()){
                        seekBarFinal[eqbands].setProgress(Equalizers.getBandLevel(eqbands) -  bandLevel[0]);
                    }else {
                        seekBarFinal[i].setProgress(Extras.getInstance().saveEq().getInt(BAND_LEVEL + i, 0) - bandLevel[0]);
                    }
                }
                int frequency = Equalizers.getCenterFreq(eqbands);
                if (frequency < 1000 * 1000) {
                    textView.setText((frequency / 1000) + "Hz");
                } else {
                    textView.setText((frequency / (1000 * 1000)) + "kHz");
                }
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                        try {
                            if (fromUser) {
                                if (bandLevel != null) {
                                    int  level = seekbar.getProgress() + bandLevel[0];
                                    Equalizers.setBandLevel(eqbands, (short) level);
                                    int presetNo = Equalizers.getPresetNo();
                                    if (presetNo != 0) {
                                        appCompatSpinner.setSelection(Equalizers.getPresetNo());
                                    } else {
                                        appCompatSpinner.setSelection(0);
                                    }
                                    Equalizers.savePrefs(eqbands, level);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Failed to init eq");
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }

}
