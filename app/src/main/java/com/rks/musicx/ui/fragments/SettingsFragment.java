package com.rks.musicx.ui.fragments;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.prefs.ATECheckBoxPreference;
import com.afollestad.appthemeengine.prefs.ATEColorPreference;
import com.afollestad.appthemeengine.prefs.ATEListPreference;
import com.afollestad.appthemeengine.prefs.ATEPreference;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.prefs.MaterialListPreference;
import com.codekidlabs.storagechooser.StorageChooser;
import com.codekidlabs.storagechooser.StorageChooserView;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.FavoritesLoader;
import com.rks.musicx.data.loaders.RecentlyPlayedLoader;
import com.rks.musicx.database.SaveQueueDatabase;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.SettingsActivity;

import java.io.File;
import java.util.List;

import static android.os.Build.VERSION_CODES.M;
import static com.rks.musicx.misc.utils.Constants.BlackTheme;
import static com.rks.musicx.misc.utils.Constants.BlurView;
import static com.rks.musicx.misc.utils.Constants.ClearFav;
import static com.rks.musicx.misc.utils.Constants.ClearQueue;
import static com.rks.musicx.misc.utils.Constants.ClearRecently;
import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.FADETRACK;
import static com.rks.musicx.misc.utils.Constants.HQ_ARTISTARTWORK;
import static com.rks.musicx.misc.utils.Constants.LightTheme;
import static com.rks.musicx.misc.utils.Constants.PlayingView;
import static com.rks.musicx.misc.utils.Constants.REMOVETABS;
import static com.rks.musicx.misc.utils.Constants.REMOVE_TABLIST;
import static com.rks.musicx.misc.utils.Constants.SaveHeadset;
import static com.rks.musicx.misc.utils.Constants.SaveTelephony;
import static com.rks.musicx.misc.utils.Constants.Three;
import static com.rks.musicx.misc.utils.Constants.Zero;


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

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private String mAteKey;
    private ATEListPreference /*fontsPref,*/ playingScreenPref, blurseek;
    private FavoritesLoader favoritesLoader;
    private RecentlyPlayedLoader recentlyPlayedLoader;
    private int accentcolor;
    private ATECheckBoxPreference headsetConfig, phoneConfig;
    private Helper helper;
    private SaveQueueDatabase queueDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settingspref);
        playingScreenPref = (ATEListPreference) findPreference(PlayingView);
        if (playingScreenPref.getValue() == null)
            playingScreenPref.setValue(Zero);
        blurseek = (ATEListPreference) findPreference(BlurView);
        if (blurseek.getValue() == null)
            blurseek.setValue(Three);
        favoritesLoader = new FavoritesLoader(getActivity(), -1);
        recentlyPlayedLoader = new RecentlyPlayedLoader(getActivity(), -1);
        headsetConfig = (ATECheckBoxPreference) findPreference(SaveHeadset);
        headsetConfig.setChecked(true);
        phoneConfig = (ATECheckBoxPreference) findPreference(SaveTelephony);
        phoneConfig.setChecked(true);
        accentcolor = Config.accentColor(getActivity(), Helper.getATEKey(getActivity()));
        helper = new Helper(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        invalidateSettings();
    }

    public void invalidateSettings() {
        mAteKey = ((SettingsActivity) getActivity()).getATEKey();
        clearStuff();
        uiChanges();
        ATEPreference picker = (ATEPreference) findPreference("directory_picker");
        picker.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                StorageChooserView.setScSecondaryActionColor(accentcolor);
                StorageChooser chooser = new StorageChooser.Builder()
                        .withActivity((SettingsActivity) getActivity())
                        .withFragmentManager(((SettingsActivity) getActivity()).getSupportFragmentManager())
                        .setDialogTitle("Storage Chooser")
                        .build();
                chooser.show();
                chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                    @Override
                    public void onSelect(String path) {
                        Log.e("PATH", path);
                        Extras.getInstance().saveFolderPath(path);
                    }
                });
                return true;
            }
        });

        ATEPreference removeTabs = (ATEPreference) findPreference(REMOVETABS);
        removeTabs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Toast.makeText(((SettingsActivity) getActivity()), "slide left to remove tab/Select ok without selection to restore all tabs", Toast.LENGTH_LONG).show();
                    Extras.getInstance().getmPreferences().edit().remove(REMOVE_TABLIST).commit();
                } finally {
                    TabRemoveFragment tabRemoveFragment = new TabRemoveFragment();
                    tabRemoveFragment.show(getActivity().getFragmentManager(), null);
                }
                return true;
            }
        });


        ATECheckBoxPreference hqArtwork = (ATECheckBoxPreference) findPreference(HQ_ARTISTARTWORK);
        File file = new File(Helper.getAlbumArtworkLocation());
        File file1 = new File(Helper.getArtistArtworkLocation());

        if (hqArtwork.isChecked()) {
            try {
                Helper.deleteRecursive(getActivity(), file);
                Helper.deleteRecursive(getActivity(), file1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Helper.createAppDir(".AlbumArtwork");
                Helper.createAppDir(".ArtistArtwork");
            }
        } else {
            try {
                Helper.deleteRecursive(getActivity(), file);
                Helper.deleteRecursive(getActivity(), file1);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Helper.createAppDir(".AlbumArtwork");
                Helper.createAppDir(".ArtistArtwork");
            }
        }
        ATECheckBoxPreference fadePref = (ATECheckBoxPreference) findPreference(FADETRACK);
        if (fadePref.isChecked()) {
            Extras.getInstance().saveFadeTrack(true);
        } else {
            Extras.getInstance().saveFadeTrack(false);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        return false;
    }


    /**
     * Clear db
     */
    private void clearStuff() {

        ATEPreference atePreference = (ATEPreference) findPreference(ClearFav);
        atePreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.clear_favdb)
                        .autoDismiss(true)
                        .content(R.string.clear_favdb_summary)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                favoritesLoader.clearDb();
                            }
                        })
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .buttonRippleColor(accentcolor)
                        .build()
                        .show();
                return true;
            }
        });

        ATEPreference atesPreference = (ATEPreference) findPreference(ClearQueue);
        atesPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.clear_squeue)
                        .autoDismiss(true)
                        .content(R.string.clear_squeue_summary)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                List<String> queueList = Helper.getSavedQueueList(getActivity());
                                if (queueList != null) {
                                    for (String name : queueList) {
                                        Log.e("Settings", name);
                                        queueDatabase = new SaveQueueDatabase(getActivity(), name);
                                        queueDatabase.removeAll();
                                        queueDatabase.close();
                                        boolean delete = getActivity().deleteDatabase(name);
                                        if (delete) {
                                            Log.e("Settings", name + " : deleted");
                                        } else {
                                            Log.e("Settings", name + " : failed");
                                        }
                                    }
                                }
                            }
                        })
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .buttonRippleColor(accentcolor)
                        .build()
                        .show();
                return true;
            }
        });

        ATEPreference atePreferences = (ATEPreference) findPreference(ClearRecently);
        atePreferences.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.clear_recentplayeddb)
                        .autoDismiss(true)
                        .content(R.string.clear_recentplayeddb)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                recentlyPlayedLoader.clearDb();
                            }
                        })
                        .positiveText(android.R.string.ok)
                        .negativeText(android.R.string.cancel)
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .buttonRippleColor(accentcolor)
                        .build()
                        .show();
                return true;
            }
        });
    }


    /**
     * Ui Changes
     */
    private void uiChanges() {
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

        findPreference(DarkTheme).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Marks both theme configs as changed so MainActivity restarts itself on return
                Config.markChanged(getActivity(), LightTheme);
                Config.markChanged(getActivity(), DarkTheme);
                Config.markChanged(getActivity(), BlackTheme);
                // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                // It's used in getATEKey() of both the Activities.
                getActivity().recreate();
                return true;
            }
        });
        findPreference(BlackTheme).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // Marks both theme configs as changed so MainActivity restarts itself on return
                Config.markChanged(getActivity(), LightTheme);
                Config.markChanged(getActivity(), DarkTheme);
                Config.markChanged(getActivity(), BlackTheme);
                // The dark_theme preference value gets saved by Android in the default PreferenceManager.
                // It's used in getATEKey() of both the Activities.
                getActivity().recreate();
                return true;
            }
        });

        final MaterialListPreference lightStatusMode = (MaterialListPreference) findPreference("light_status_bar_mode");
        final MaterialListPreference lightToolbarMode = (MaterialListPreference) findPreference("light_toolbar_mode");

        if (Build.VERSION.SDK_INT >= M) {
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
}