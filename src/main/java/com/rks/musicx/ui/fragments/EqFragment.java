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

import static com.rks.musicx.data.eq.Equalizers.getBandLevelRange;
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

    private SwitchCompat switchCompat;
    private AppCompatSpinner appCompatSpinner;
    private String ateKey;
    private int accentcolor;
    private VerticalSeekBar[] seekBarFinal = new VerticalSeekBar[5];
    private EqView bassBoost, virtualizerBoost, LoudnessBoost, reverb;
    private VerticalSeekBar seekBar;
    private TextView textView;
    private ArrayAdapter<String> arrayAdapter;
    private ViewPager effectpager;
    private List<View> viewList;
    private View blg, rb;
    private PlayingPagerAdapter playingPagerAdapter;
    private int str, gain, virtualstr, presetreverb;

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
        blg = LayoutInflater.from(getContext()).inflate(R.layout.blg, null);
        bassBoost = (EqView) blg.findViewById(R.id.bassboost);
        virtualizerBoost = (EqView) blg.findViewById(R.id.virtualizerboost);
        LoudnessBoost = (EqView) blg.findViewById(R.id.loudboost);

        rb = LayoutInflater.from(getContext()).inflate(R.layout.rb, null);
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

    private void enableDisable(Boolean onoroff) {
        Equalizers.setEnabled(onoroff);
        BassBoosts.setEnabled(onoroff);
        Virtualizers.setEnabled(onoroff);
        Loud.setEnabled(onoroff);
        Reverb.setEnabled(onoroff);
    }

    private void initPresets(View rootView) {
        appCompatSpinner = (AppCompatSpinner) rootView.findViewById(R.id.presets_spinner);
        arrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item);
        if (getPresetNames() == null){
            return;
        }
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (String presets : getPresetNames()){
            arrayAdapter.add(presets);
            appCompatSpinner.setAdapter(arrayAdapter);
        }
        appCompatSpinner.setSelection(Equalizers.getCurrentPreset());
        appCompatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 1) {
                    Equalizers.usePreset((short) (position - 1));
                    final short[] range = Equalizers.getBandLevelRange();
                    for (short i = 0; i < Equalizers.getNumberOfBands(); i++) {
                        if (range != null) {
                            seekBarFinal[i].setProgress(Equalizers.getBandLevel(i) - range[0]);
                        }
                    }
                } else {
                    Log.d("EqFragment", "Error buddy");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private List<String> getPresetNames(){
        List<String> presets = new ArrayList<>();
        presets.add(getString(R.string.custom));
        Log.d("EqFragment", String.valueOf(presets.get(0)));
        //short size = (short) (Equalizers.getPresenNo()+ no);
        for (short n = 0; n <Equalizers.getPresenNo(); n++) {
            presets.add(Equalizers.getPresetNames(n));
            Log.d("EqFragment", String.valueOf(n) + String.valueOf(presets.get(n)));
        }
        return presets;
    }

    private void initBassBoost() {
        bassBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                str = ((int) (float) 1000 / 19 * (bassBoost.getProgress()));
                BassBoosts.setBassBoostStrength((short) str);
            }

        });
        if (Extras.getInstance().saveEq().getInt(BASS_BOOST, 0) != 0) {
            bassBoost.setProgress(Extras.getInstance().saveEq().getInt(BASS_BOOST, 0));
        } else {
            bassBoost.setProgress(1);
        }
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            bassBoost.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        bassBoost.setLabel(getString(R.string.Bass));
    }


    private void initPresetReverbBoost() {
        reverb.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                presetreverb = (progress * 6) / 19;
                Reverb.setPresetReverbStrength((short) presetreverb);
            }

        });
        if (Extras.getInstance().saveEq().getInt(PRESET_BOOST, 0) != 0) {
            reverb.setProgress(Extras.getInstance().saveEq().getInt(PRESET_BOOST, 0));
        } else {
            reverb.setProgress(1);
        }
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            reverb.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        reverb.setLabel(getString(R.string.reverb));
    }


    private void initVirtualizerBoost() {
        virtualizerBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                virtualstr = ((int) (float) 1000 / 19 * (progress));
                Virtualizers.setVirtualizerStrength((short) virtualstr);
            }
        });
        virtualizerBoost.setLabel(getString(R.string.Virtual));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            virtualizerBoost.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        if (Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0) != 0) {
            virtualizerBoost.setProgress(Extras.getInstance().saveEq().getInt(VIRTUAL_BOOST, 0));
        } else {
            virtualizerBoost.setProgress(1);
        }
    }


    private void initLoudnessBoost() {
        LoudnessBoost.setOnProgressChangedListener(new EqView.onProgressChangedListener() {
            @Override
            public void onProgressChanged(int progress) {
                gain = ((int) (float) 1000 / 19 * (progress));
                Loud.setLoudnessEnhancerGain(gain);
            }

        });
        LoudnessBoost.setProgress(Loud.getGain());
        LoudnessBoost.setLabel(getString(R.string.loudness));
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            LoudnessBoost.getTextPaint().setColor(ContextCompat.getColor(getContext(), R.color.white));
        }
        if (Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0) != 0) {
            LoudnessBoost.setProgress(Extras.getInstance().saveEq().getInt(LOUD_BOOST, 0));
        } else {
            LoudnessBoost.setProgress(1);
        }
    }


    private void initEq(View rootView) {
        try {
            for (short i = 0; i < Equalizers.getNumberOfBands(); i++) {
                final short eqbands = i;
                short[] bandLevel = getBandLevelRange();
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
                if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                    textView.setTextColor(Color.WHITE);
                    seekBar.setThumb(ContextCompat.getDrawable(getContext(), R.drawable.thumb));
                } else {
                    textView.setTextColor(Color.WHITE);
                    seekBar.setThumb(ContextCompat.getDrawable(getContext(), R.drawable.thumb));
                }
                seekBarFinal[eqbands] = seekBar;
                seekBar.setId(i);
                if (bandLevel != null) {
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
                        try {
                            if (bandLevel != null) {
                                short level = (short) (progress + bandLevel[0]);
                                if (fromUser) {
                                    Equalizers.setBandLevel(eqbands, level);
                                    appCompatSpinner.setSelection(0);
                                    Equalizers.savePrefs(Equalizers.getCurrentPreset(), level);
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
            Log.d("EqFragment", "Failed to init eq");
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
