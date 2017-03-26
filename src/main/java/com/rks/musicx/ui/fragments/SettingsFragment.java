package com.rks.musicx.ui.fragments;

import static com.rks.musicx.misc.utils.Constants.BlurView;
import static com.rks.musicx.misc.utils.Constants.ClearFav;
import static com.rks.musicx.misc.utils.Constants.ClearRecently;
import static com.rks.musicx.misc.utils.Constants.PlayingView;
import static com.rks.musicx.misc.utils.Constants.SaveHeadset;
import static com.rks.musicx.misc.utils.Constants.SaveTelephony;
import static com.rks.musicx.misc.utils.Constants.TextFonts;
import static com.rks.musicx.misc.utils.Constants.Three;
import static com.rks.musicx.misc.utils.Constants.Zero;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.View;
import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATECheckBoxPreference;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.appthemeengine.prefs.ATEListPreference;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.codekidlabs.storagechooser.StorageChooser;
import com.codekidlabs.storagechooser.StorageChooserView;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.FavoritesLoader;
import com.rks.musicx.data.loaders.RecentlyPlayedLoader;
import com.rks.musicx.misc.utils.ATEUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.widgets.DailogPref;
import com.rks.musicx.ui.activities.SettingsActivity;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private String mAteKey;
    private ATEListPreference fontsPref, playingScreenPref, blurseek;
    private DailogPref dialogPreferenceFav, dailogPreferenceRecent;
    private FavoritesLoader favoritesLoader;
    private RecentlyPlayedLoader recentlyPlayedLoader;
    private int accentcolor;
    private ATECheckBoxPreference headsetConfig, phoneConfig;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settingspref);
        fontsPref = (ATEListPreference) findPreference(TextFonts);
        if (fontsPref.getValue() == null)
            fontsPref.setValue(Zero);
        playingScreenPref = (ATEListPreference) findPreference(PlayingView);
        if (playingScreenPref.getValue() == null)
            playingScreenPref.setValue(Zero);
        blurseek = (ATEListPreference) findPreference(BlurView);
        if (blurseek.getValue() == null)
            blurseek.setValue(Three);
        dialogPreferenceFav = (DailogPref) findPreference(ClearFav);
        dailogPreferenceRecent = (DailogPref) findPreference(ClearRecently);
        favoritesLoader = new FavoritesLoader(getActivity());
        recentlyPlayedLoader = new RecentlyPlayedLoader(getActivity(), -1);
        headsetConfig = (ATECheckBoxPreference) findPreference(SaveHeadset);
        headsetConfig.setChecked(true);
        phoneConfig = (ATECheckBoxPreference) findPreference(SaveTelephony);
        phoneConfig.setChecked(true);
        accentcolor = Config.accentColor(getActivity(), Helper.getATEKey(getActivity()));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invalidateSettings();
        dialogPreferenceFav.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                favoritesLoader.clearDb();
                return true;
            }
        });
        dailogPreferenceRecent.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                recentlyPlayedLoader.clearDb();
                return true;
            }
        });
    }

    public void invalidateSettings() {
        mAteKey = ((SettingsActivity) getActivity()).getATEKey();
        ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
        primaryColorPref.setColor(Config.primaryColor(getActivity(), mAteKey), Color.BLACK);
        primaryColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.primary_color)
                        .preselect(Config.primaryColor(getActivity(), mAteKey))
                        .show();
                return true;
            }
        });

        ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
        accentColorPref.setColor(Config.accentColor(getActivity(), mAteKey), Color.BLACK);
        accentColorPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new ColorChooserDialog.Builder((SettingsActivity) getActivity(), R.string.accent_color)
                        .preselect(Config.accentColor(getActivity(), mAteKey))
                        .show();
                return true;
            }
        });

        findPreference("dark_theme").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Marks both theme configs as changed so MainActivity restarts itself on return
                Config.markChanged(getActivity(), "light_theme");
                Config.markChanged(getActivity(), "dark_theme");
                // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                // It's used in getATEKey() of both the Activities.
                getActivity().recreate();
                return true;
            }
        });

        final MaterialListPreference lightStatusMode = (MaterialListPreference) findPreference("light_status_bar_mode");
        final MaterialListPreference lightToolbarMode = (MaterialListPreference) findPreference("light_toolbar_mode");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            lightStatusMode.setEnabled(true);
            lightStatusMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    @Config.LightStatusBarMode
                    int constant = Integer.parseInt((String) newValue);
                    ATE.config(getActivity(), mAteKey)
                            .lightStatusBarMode(constant)
                            .apply(getActivity());
                    return true;
                }
            });
        } else {
            lightStatusMode.setEnabled(false);
            lightStatusMode.setSummary(R.string.not_available_below_m);
        }

        lightToolbarMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                @Config.LightToolbarMode
                int constant = Integer.parseInt((String) newValue);
                ATE.config(getActivity(), mAteKey)
                        .lightToolbarMode(constant)
                        .apply(getActivity());
                return true;
            }
        });


      findPreference("directory_picker").setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          StorageChooserView.setScSecondaryActionColor(accentcolor);
          StorageChooser chooser = new StorageChooser.Builder()
              .withActivity((SettingsActivity) getActivity())
              .withFragmentManager(((SettingsActivity) getActivity()).getSupportFragmentManager())
              .allowCustomPath(false)
              .allowAddFolder(false)
              .setDialogTitle("Directory Chooser")
              .setType(StorageChooser.DIRECTORY_CHOOSER)
              .skipOverview(true)
              .showHidden(false)
              .build();
          chooser.show();
          chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
              Log.e("PATH", path);
              Extras.getInstance().saveFolderPath(path);
              Extras.getInstance().trackFolderPath(true);
            }
          });
          return true;
        }
      });
        final ATECheckBoxPreference statusBarPref = (ATECheckBoxPreference) findPreference("colored_status_bar");
        final ATECheckBoxPreference navBarPref = (ATECheckBoxPreference) findPreference("colored_nav_bar");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBarPref.setChecked(Config.coloredStatusBar(getActivity(), mAteKey));
            statusBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ATE.config(getActivity(), mAteKey)
                            .coloredStatusBar((Boolean) newValue)
                            .apply(getActivity());
                    return true;
                }
            });


            navBarPref.setChecked(Config.coloredNavigationBar(getActivity(), mAteKey));
            navBarPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    ATE.config(getActivity(), mAteKey)
                            .coloredNavigationBar((Boolean) newValue)
                            .apply(getActivity());
                    return true;
                }
            });
        } else {
            statusBarPref.setEnabled(false);
            statusBarPref.setSummary(R.string.not_available_below_lollipop);
            navBarPref.setEnabled(false);
            navBarPref.setSummary(R.string.not_available_below_lollipop);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = Helper.getATEKey(getActivity());
        ATEUtils.setStatusBarColor(getActivity(), ateKey, Config.primaryColor(getActivity(), ateKey));
    }


  @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }
}