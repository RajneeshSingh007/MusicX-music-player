package com.rks.musicx.ui.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.palette.BitmapPalette;
import com.palette.GlidePalette;
import com.rks.musicx.R;
import com.rks.musicx.base.BaseActivity;
import com.rks.musicx.data.model.Album;
import com.rks.musicx.data.model.Artist;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.interfaces.MetaDatas;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.Sleeptimer;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.misc.widgets.ProgressBar;
import com.rks.musicx.services.MusicXService;
import com.rks.musicx.services.ShortcutsHandler;
import com.rks.musicx.ui.fragments.AlbumFragment;
import com.rks.musicx.ui.fragments.ArtistFragment;
import com.rks.musicx.ui.fragments.FavFragment;
import com.rks.musicx.ui.fragments.MainFragment;
import com.rks.musicx.ui.fragments.TagEditorFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.os.Build.VERSION_CODES.M;
import static com.rks.musicx.misc.utils.Constants.ALBUM_ARTIST;
import static com.rks.musicx.misc.utils.Constants.ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.ALBUM_NAME;
import static com.rks.musicx.misc.utils.Constants.ALBUM_TRACK_COUNT;
import static com.rks.musicx.misc.utils.Constants.EQ;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.NAV;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.OVERLAY_REQ;
import static com.rks.musicx.misc.utils.Constants.PERMISSIONS_REQ;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.SHOW_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SHOW_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SHOW_TAG;
import static com.rks.musicx.misc.utils.Constants.WRITESETTINGS;

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

public class MainActivity extends BaseActivity implements MetaDatas, ATEActivityThemeCustomizer, NavigationView.OnNavigationItemSelectedListener {

    private static Handler songProgressHandler;
    private MusicXService musicXService;
    private int primarycolor, accentcolor;
    private String ateKey;
    private boolean mService = false;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private View mNavigationHeader;
    private int count = 0;
    private FloatingActionButton playToggle;
    private ProgressBar songProgress;
    private TextView SongTitle, SongArtist;
    private RelativeLayout songDetail;
    private ImageView BackgroundArt;
    private RequestManager mRequestManager;
    private Helper helper;
    private Drawable pause, play;
    private RelativeLayout logoLayout;
    private Intent intent;
    private ProgressRunnable progressRunnable;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicXService.MusicXBinder binder = (MusicXService.MusicXBinder) service;
            musicXService = binder.getService();
            mService = true;
            if (musicXService != null) {
                PlayingView();
            }
            Intent filterIntent = getIntent();
            if (filterIntent != null) {
                Uri data = filterIntent.getData();
                if (data != null) {
                    try {
                        openFile(data);
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = false;
        }
    };
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (musicXService == null) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(PLAYSTATE_CHANGED)) {
                playpausetoggle();
            } else if (action.equals(META_CHANGED)) {
                miniplayerview();
                finalProgress();
            }
        }
    };
    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (musicXService == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.quick_play_pause_toggle:
                    musicXService.toggle();
                    break;
            }
        }
    };

    private void finalProgress() {
        songProgressHandler.post(progressRunnable);
    }

    private void removeProgress() {
        songProgressHandler.removeCallbacks(progressRunnable);
        songProgressHandler.removeCallbacksAndMessages(null);
    }

    /**
     * play song from outside of the app
     * @param data
     */
    private void openFile(Uri data) {
        List<Song> playList = Helper.getSongMetaData(MainActivity.this, data.getPath());
        if (playList.size() > 0) {
            onSongSelected(playList, 0);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MusicXService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(META_CHANGED);
        filter.addAction(PLAYSTATE_CHANGED);
        filter.addAction(POSITION_CHANGED);
        filter.addAction(ITEM_ADDED);
        filter.addAction(ORDER_CHANGED);
        registerReceiver(broadcastReceiver, filter);
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void setUi() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        playToggle = (FloatingActionButton) findViewById(R.id.quick_play_pause_toggle);
        songDetail = (RelativeLayout) findViewById(R.id.songDetail);
        songProgress = (ProgressBar) findViewById(R.id.songProgress);
        SongTitle = (TextView) findViewById(R.id.song_title);
        SongArtist = (TextView) findViewById(R.id.song_artist);
        mNavigationHeader = mNavigationView.inflateHeaderView(R.layout.navigation_header);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.getMenu().findItem(R.id.action_library).setChecked(true);
        BackgroundArt = (ImageView) findViewById(R.id.BackgroundArt);
    }

    @Override
    protected void function() {
        fragmentLoader(setContainerId(), setFragment());
        permissionManager.checkPermissions(MainActivity.this);
        if (!Extras.getInstance().getWidgetTrack()) {
            permissionManager.widgetPermission(MainActivity.this);
        }
        if (!Extras.getInstance().getSettings()){
            permissionManager.settingPermission(MainActivity.this);
        }
        ateKey = returnAteKey();
        accentcolor = Config.accentColor(this, ateKey);
        primarycolor = Config.primaryColor(this, ateKey);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(MainActivity.this);
        }
        count = Extras.getInstance().getInitValue(MainActivity.this, "first", "last");
        if (count == 0) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            count++;
            Extras.getInstance().setInitValue(MainActivity.this, count, "first", "last");
        }
        songDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PlayingActivity.class);
                startActivityForResult(i, NAV);
            }
        });
        logoLayout = (RelativeLayout) mNavigationHeader.findViewById(R.id.logolayout);
        logoLayout.setBackgroundColor(primarycolor);
        mRequestManager = Glide.with(MainActivity.this);
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            SongArtist.setTextColor(Color.WHITE);
            SongTitle.setTextColor(Color.WHITE);
        } else {
            SongArtist.setTextColor(Color.WHITE);
            SongTitle.setTextColor(Color.WHITE);
        }
        ShortcutsHandler.create(MainActivity.this);
        SongTitle.setTypeface(Helper.getFont(MainActivity.this));
        SongArtist.setTypeface(Helper.getFont(MainActivity.this));
        songProgressHandler = new Handler(Looper.getMainLooper());
        progressRunnable = new ProgressRunnable(MainActivity.this);
        helper = new Helper(this);
        pause = ContextCompat.getDrawable(MainActivity.this, R.drawable.aw_ic_pause);
        play = ContextCompat.getDrawable(MainActivity.this, R.drawable.aw_ic_play);
        playToggle.setOnClickListener(onClick);
    }

    @Override
    public Fragment setFragment() {
        return MainFragment.newInstance();
    }

    @Override
    protected int setContainerId() {
        return R.id.container;
    }

    @Override
    protected void fragmentLoader(int ContainerId, Fragment fragment) {
        super.fragmentLoader(ContainerId, fragment);
    }

    public void showFavorites() {
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.getMenu().findItem(R.id.action_favorites).setChecked(true);
        setFragment(FavFragment.newFavoritesFragment());
    }

    /*
    Global fragment loader in this activity
     */
    public void setFragment(Fragment f) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).addToBackStack(null).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(MainActivity.this);
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                } else {
                    fragmentLoader(setContainerId(), setFragment());
                }
                return true;
            case R.id.system_eq:
                Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                if (intent.getAction() != null && Helper.isActivityPresent(MainActivity.this, intent)){
                    intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicXService.audioSession());
                    startActivityForResult(intent, EQ);
                }else {
                    Toast.makeText(this, "No app found to handle equalizer", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.play_save_queue:
                multiQueuePlay();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * paly Multi Saved Queue
     */
    private void multiQueuePlay() {
        List<Song> mixList = new ArrayList<>();
        List<String> savedQueue = new ArrayList<>();
        savedQueue = Helper.getSavedQueueList(MainActivity.this);
        if (savedQueue == null) {
            return;
        }
        new MaterialDialog.Builder(this)
                .title("Play Saved Queue")
                .items(savedQueue)
                .typeface(Helper.getFont(this), Helper.getFont(this))
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                        if (dialog.getItems() != null) {
                            List<Song> queueList = new ArrayList<>();
                            for (CharSequence name : text) {
                                CommonDatabase commonDatabase = new CommonDatabase(MainActivity.this, name.toString(), true);
                                queueList.clear();
                                queueList = commonDatabase.readLimit(-1, null);
                                commonDatabase.close();
                            }
                            for (Song song : queueList) {
                                mixList.add(song);
                            }
                        }
                        return true;
                    }
                })
                .alwaysCallMultiChoiceCallback()
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        dialog.clearSelectedIndices();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mixList.size() > 0) {
                            onShuffleRequested(mixList, true);
                        }
                    }
                })
                .positiveText("Play")
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .dividerColor(accentcolor)
                .build()
                .show();
    }

    @Override
    public void onSongSelected(List<Song> songList, int pos) {
        if (musicXService == null) {
            return;
        }
        Extras.getInstance().saveSeekServices(0);
        musicXService.setPlaylist(songList, pos, true);
    }

    @Override
    public void onShuffleRequested(List<Song> songList, boolean play) {
        if (musicXService == null) {
            return;
        }
        Extras.getInstance().saveSeekServices(0);
        musicXService.setPlaylistandShufle(songList, play);
    }

    @Override
    public void addToQueue(Song song) {
        if (musicXService != null) {
            musicXService.addToQueue(song);
        }
    }

    @Override
    public void setAsNextTrack(Song song) {
        if (musicXService != null) {
            musicXService.setAsNextTrack(song);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQ: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Granted", "hurray");
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_REQ) {
            if (Build.VERSION.SDK_INT >= M) {
                if (Settings.canDrawOverlays(this)) {
                    Log.d("MainActivity", "Granted");
                } else {
                    Extras.getInstance().setWidgetTrack(true);
                    Log.d("MainActivity", "Denied or Grant permission Manually");
                }
            }
        }
        if (requestCode == WRITESETTINGS) {
            if (Build.VERSION.SDK_INT >= M) {
                if (!Settings.System.canWrite(this)) {
                    Log.d("MainActivity", "Granted");
                } else {
                    Extras.getInstance().setSettings(true);
                    Log.d("MainActivity", "Denied or Grant permission Manually");
                }
            }
        }
        if (requestCode == EQ) {
            Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            if (intent.getAction() != null && Helper.isActivityPresent(MainActivity.this, intent)) {
                if (musicXService == null) {
                    return;
                }
                intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicXService.audioSession());
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, this.getPackageName());
                intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                sendBroadcast(intent);
            } else {
                Log.d("MainActivity", "Error");
            }
        }
        if (requestCode == NAV && resultCode == RESULT_OK) {
            intent = data;
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (intent.getAction().equals(SHOW_ALBUM)) {
                long id = bundle.getLong(ALBUM_ID);
                String title = bundle.getString(ALBUM_NAME);
                String artist = bundle.getString(ALBUM_ARTIST);
                int trackCount = bundle.getInt(ALBUM_TRACK_COUNT);
                Album album = new Album();
                album.setId(id);
                album.setArtistName(artist);
                album.setTrackCount(trackCount);
                album.setAlbumName(title);
                album.setYear(0);
                Log.e("Move", "Go_to_AlbumFrag");
                setFragment(AlbumFragment.newInstance(album));
            } else if (intent.getAction().equals(SHOW_ARTIST)) {
                long id = bundle.getLong(Constants.ARTIST_ARTIST_ID);
                String name = bundle.getString(Constants.ARTIST_NAME);
                Artist artist = new Artist(id, name, 0, 0);
                Log.e("Move", "Go_to_ArtistFrag");
                setFragment(ArtistFragment.newInstance(artist));
            } else if (intent.getAction().equals(SHOW_TAG)) {
                setFragment(TagEditorFragment.getInstance());
                Log.e("Move", "Go_to_TagFrag");
            }
            intent = null;
        }
    }

    private void updateProgress() {
        if (musicXService != null) {
            int pos = musicXService.getPlayerPos();
            songProgress.setProgressWithAnim(pos);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mService) {
            Intent intent = new Intent(this, MusicXService.class);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            startService(intent);
            /*IntentFilter filter = new IntentFilter();
            filter.addAction(META_CHANGED);
            filter.addAction(PLAYSTATE_CHANGED);
            filter.addAction(POSITION_CHANGED);
            filter.addAction(ITEM_ADDED);
            filter.addAction(ORDER_CHANGED);
            registerReceiver(broadcastReceiver, filter);*/
        } else {
            if (musicXService != null) {
                PlayingView();
            }
        }
        Glide.get(this).clearMemory();
    }

    private void PlayingView() {
        miniplayerview();
        playpausetoggle();
        finalProgress();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mService) {
            unbindService(mServiceConnection);
            mService = false;
            musicXService = null;
            unregisterReceiver(broadcastReceiver);
            removeProgress();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Glide.get(this).clearMemory();
    }

    @Override
    public String returnAteKey() {
        return super.returnAteKey();
    }

    @StyleRes
    @Override
    public int getActivityTheme() {
        return getStyleTheme();
    }

    public void libraryLoader() {
        fragmentLoader(setContainerId(), setFragment());
    }

    /*
     NavigationView Selection
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        mDrawerLayout.closeDrawers();
        mDrawerLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch (item.getItemId()) {
                    case R.id.action_library:
                        libraryLoader();
                        break;
                    case R.id.action_favorites:
                        showFavorites();
                        break;
                    case R.id.action_sleep_timer:
                        if (!Sleeptimer.running) {
                            Sleeptimer.showSleepTimer(MainActivity.this);
                        } else {
                            Sleeptimer.showTimerInfo(MainActivity.this);
                        }
                        break;
                    case R.id.action_eq:
                        Helper.startActivity(MainActivity.this, EqualizerActivity.class);
                        break;
                    case R.id.action_settings:
                        Extras.getInstance().setNaviSettings(false);
                        Helper.startActivity(MainActivity.this, SettingsActivity.class);
                        break;
                    case R.id.action_donation:
                        Helper.startActivity(MainActivity.this, DonationActivity.class);
                        break;
                    case R.id.about:
                        Helper.startActivity(MainActivity.this, AboutActivity.class);
                        break;
                    case R.id.shares:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, "Hey check out MusicX music player app at: https://play.google.com/store/apps/details?id=com.rks.musicx");
                        intent.setType("text/plain");
                        startActivity(Intent.createChooser(intent, getString(R.string.Shares)));
                        break;
                    case R.id.rateus:
                        Helper.showRateDialog(MainActivity.this);
                        break;

                }
            }
        }, 75);
        return true;
    }

    private void backgroundArt() {
        if (musicXService == null){
            return;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Extras.getInstance().getDownloadedArtwork()) {
                    mRequestManager.load(helper.loadAlbumImage(musicXService.getsongAlbumName()))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(300, 300)
                            .listener(GlidePalette.with(ArtworkUtils.getAlbumCoverPath(MainActivity.this, musicXService.getsongAlbumName()).getAbsolutePath())
                                    .intoCallBack(new BitmapPalette.CallBack() {
                                        @Override
                                        public void onPaletteLoaded(@Nullable Palette palette) {
                                            int color[] = Helper.getAvailableColor(MainActivity.this, palette);
                                            if (Extras.getInstance().artworkColor()) {
                                                colorMode(color[0]);
                                            } else {
                                                colorMode(accentcolor);
                                            }
                                        }
                                    }))
                            .into(new SimpleTarget<Bitmap>() {

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    ArtworkUtils.blurPreferances(MainActivity.this, ArtworkUtils.drawableToBitmap(errorDrawable), BackgroundArt);
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    ArtworkUtils.blurPreferances(MainActivity.this, resource, BackgroundArt);
                                }
                            });
                } else {
                    mRequestManager.load(ArtworkUtils.uri(musicXService.getsongAlbumID()))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .centerCrop()
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(300, 300)
                            .listener(GlidePalette.with(ArtworkUtils.uri(musicXService.getsongAlbumID()).toString()).intoCallBack(new BitmapPalette.CallBack() {
                                @Override
                                public void onPaletteLoaded(@Nullable Palette palette) {
                                    int color[] = Helper.getAvailableColor(MainActivity.this, palette);
                                    if (Extras.getInstance().artworkColor()) {
                                        colorMode(color[0]);
                                    } else {
                                        colorMode(accentcolor);
                                    }
                                }
                            }))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    ArtworkUtils.blurPreferances(MainActivity.this, ArtworkUtils.drawableToBitmap(errorDrawable), BackgroundArt);
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    ArtworkUtils.blurPreferances(MainActivity.this, resource, BackgroundArt);
                                }

                            });
                }
            }
        });
    }

    private void colorMode(int color){
        playToggle.setBackgroundTintList(ColorStateList.valueOf(color));
        songProgress.setProgressColor(color);
        songProgress.setDefaultProgressBackgroundColor(Color.TRANSPARENT);
    }

    private void miniplayerview() {
        if (musicXService != null) {
            String title = musicXService.getsongTitle();
            String artist = musicXService.getsongArtistName();
            SongTitle.setText(title);
            SongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            SongArtist.setText(artist);
            int duration = musicXService.getDuration();
            if (duration != -1) {
                songProgress.setMax(duration);
            }
            backgroundArt();
            Helper.rotateFab(playToggle);
        }
    }

    private void playpausetoggle() {
        if (musicXService != null) {
            if (musicXService.isPlaying()) {
                playToggle.setImageDrawable(pause);
            } else {
                playToggle.setImageDrawable(play);
            }
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private static class ProgressRunnable implements Runnable {

        private final WeakReference<MainActivity> activityWeakReference;

        public ProgressRunnable(MainActivity myClassInstance) {
            activityWeakReference = new WeakReference<MainActivity>(myClassInstance);
        }

        @Override
        public void run() {
            MainActivity mainActivity = activityWeakReference.get();
            if (mainActivity != null)
                mainActivity.updateProgress();
            songProgressHandler.postDelayed(ProgressRunnable.this, 1000);
        }
    }

}
