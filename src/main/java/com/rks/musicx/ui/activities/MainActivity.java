package com.rks.musicx.ui.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.MetaDatas;
import com.rks.musicx.misc.utils.Sleeptimer;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.misc.widgets.BlurArtwork;
import com.rks.musicx.misc.widgets.ProgressBar;
import com.rks.musicx.services.MusicXService;
import com.rks.musicx.services.PlayingRequestListerner;
import com.rks.musicx.ui.fragments.FavFragment;
import com.rks.musicx.ui.fragments.MainFragment;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.rks.musicx.misc.utils.Constants.DarkTheme;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PERMISSIONS_REQ;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;

public class MainActivity extends BaseActivity implements MetaDatas, ATEActivityThemeCustomizer, NavigationView.OnNavigationItemSelectedListener{

    private MusicXService musicXService;
    private int primarycolor, accentcolor;
    private Intent mServiceIntent;
    private String ateKey;
    private boolean mService = false;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;
    private View mNavigationHeader;
    private int count = 0;
    private int tempInt = 0;
    private FloatingActionButton playToggle;
    private ProgressBar songProgress;
    private TextView SongTitle, SongArtist;
    private RelativeLayout songDetail;
    private ImageView BackgroundArt;
    RequestManager mRequestManager;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicXService.MusicXBinder binder = (MusicXService.MusicXBinder) service;
            musicXService = binder.getService();
            mService = true;
            PlayingRequestListerner.sendRequests(musicXService);
            if (musicXService != null) {
                MiniPlayerUpdate();
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
                updateconfig();
            } else if (action.equals(META_CHANGED)) {
                miniplayerview();
            }
        }
    };



    /**
     * Drawer Layout
     * @return
     */
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    /**
     * Layout
     * @return
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    /**
     * Ui
     */
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

    /**
     * function
     */
    @Override
    protected void function() {
        fragmentLoader(setContainerId(),setFragment());
        permissionManager.checkPermissions(MainActivity.this);
        permissionManager.widgetPermission(MainActivity.this);
        ateKey = returnAteKey();
        accentcolor = Config.accentColor(this,ateKey);
        primarycolor = Config.primaryColor(this, ateKey);
        if (mNavigationView != null) {
            mNavigationView.setNavigationItemSelectedListener(this);
        }
        count = readSharedPreferenceInt("first", "last");
        if (count == 0) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, IntroActivity.class);
            startActivity(intent);
            count++;
            writeSharedPreference(count, "first", "last");
        }
        songDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, PlayingActivity.class);
                startActivity(i);
            }
        });
        RelativeLayout logolayout = (RelativeLayout) mNavigationHeader.findViewById(R.id.logolayout);
        if (logolayout != null) {
            logolayout.setBackgroundColor(primarycolor);
        }else {
            logolayout.setBackgroundColor(primarycolor);
        }
        mRequestManager = Glide.with(this);
    }

    /**
     * set fragment
     * @return
     */
    @Override
    protected Fragment setFragment() {
        return MainFragment.newInstance();
    }

    /**
     * ID
     * @return
     */
    @Override
    protected int setContainerId() {
        return R.id.container;
    }

    /**
     * Load Fragment
     * @param ContainerId
     * @param fragment
     */
    @Override
    protected void fragmentLoader(int ContainerId, Fragment fragment) {
        super.fragmentLoader(ContainerId, fragment);
    }

    /**
     * Read Preferences
     * @param spName
     * @param key
     * @return
     */
    public int readSharedPreferenceInt(String spName, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(spName, Context.MODE_PRIVATE);
        return tempInt = sharedPreferences.getInt(key, 0);
    }

    /**
     * Write Prefernces
     * @param ammount
     * @param spName
     * @param key
     */
    public void writeSharedPreference(int ammount, String spName, String key) {
        SharedPreferences sharedPreferences = getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, ammount);
        editor.commit();
    }

    /**
     * Fav Fragment Loader
     */
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
       // getMenuInflater().inflate(R.menu.main, menu);
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
                    fragmentLoader(setContainerId(),setFragment());
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Song Selection with metadata
     * @param songList
     * @param pos
     */
    @Override
    public void onSongSelected(List<Song> songList, int pos) {
        if (musicXService == null) {
            return;
        }
        musicXService.setPlaylist(songList, pos, true);
    }

    /**
     * On Shuffle Selection
     * @param songList
     * @param play
     */
    @Override
    public void onShuffleRequested(List<Song> songList, boolean play) {
        if (musicXService == null) {
            return;
        }
        musicXService.setPlaylistandShufle(songList, play);
    }

    /**
     * Add to Queue
     * @param song
     */
    @Override
    public void addToQueue(Song song) {
        if (musicXService != null){
            musicXService.addToQueue(song);
        }
    }

    /**
     * Set as Next Song
     * @param song
     */
    @Override
    public void setAsNextTrack(Song song) {
        if (musicXService != null) {
            musicXService.setAsNextTrack(song);
        }
    }

    /**
     * Permission Resultsa
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQ:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.d("Granted","hurray");
                }
            }
        }
    }

    /**
     * Update progress
     */
    private void updateProgress() {
        if (musicXService !=null){
            int pos = musicXService.getPlayerPos();
            songProgress.setProgressWithAnim(pos);
        }
    }

    /**
     * onPause
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mService) {
            musicXService = null;
            unbindService(mServiceConnection);
            mService = false;
            unregisterReceiver(broadcastReceiver);
        }
        musicXService.setState(this,true);
    }

    /**
     * onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
        musicXService.setState(this,true);
        if (!mService) {
            mServiceIntent = new Intent(this, MusicXService.class);
            bindService(mServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            startService(mServiceIntent);
            IntentFilter filter = new IntentFilter();
            filter.addAction(META_CHANGED);
            filter.addAction(PLAYSTATE_CHANGED);
            filter.addAction(POSITION_CHANGED);
            filter.addAction(ITEM_ADDED);
            filter.addAction(ORDER_CHANGED);
            registerReceiver(broadcastReceiver,filter);
        } else {
            if (musicXService != null) {
                MiniPlayerUpdate();
            }
        }
        Glide.get(this).clearMemory();
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
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(DarkTheme, false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }


    /**
     * Load Fragment
     */
    public void libraryLoader(){
        fragmentLoader(setContainerId(),setFragment());
    }
    /*
     NavigationView Selection
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
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
                   // case R.id.action_folder:
                   //     setFragment(FolderFragment.newInstance());
                     //   break;
                    case R.id.action_eq:
                        Intent intents = new Intent(MainActivity.this,EqualizerActivity.class);
                        startActivity(intents);
                        break;
                    case R.id.action_settings:
                        Intent settings = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case R.id.about:
                        Intent about = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(about);
                        break;
                    case R.id.shares:
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT,"Hey check out my app at: https://play.google.com/store/apps/details?id=com.rks.musicx");
                        intent.setType("text/plain");
                        startActivity(Intent.createChooser(intent,getString(R.string.Shares)));
                        break;

                }
            }
        }, 75);
        return true;
    }

    Handler songProgressHandler = new Handler();

    /**
     * UpdateProgress
     */
    private Runnable mUpdateProgress = new Runnable() {

        @Override
        public void run() {
            updateProgress();
            songProgressHandler.postDelayed(mUpdateProgress, 1000);

        }
    };

    /**
     * BackgroundArt
     */
    private void backgroundArt(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRequestManager.load(ArtworkUtils.uri(musicXService.getsongAlbumID()))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(300,300)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (resource != null){
                                    ArtworkUtils.blurPreferances(MainActivity.this,resource,BackgroundArt);
                                }else {
                                    new BlurArtwork(MainActivity.this,25,ArtworkUtils.getDefaultArtwork(MainActivity.this),BackgroundArt).execute("");
                                }
                            }
                        });
            }
        });
    }

    /**
     * MiniPlayer View
     */
    private void miniplayerview() {
        if (musicXService != null) {
            String title = musicXService.getsongTitle();
            String artist = musicXService.getsongArtistName();
            playToggle.setOnClickListener(v -> musicXService.toggle());
            SongTitle.setText(title);
            SongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            SongArtist.setText(artist);
            int duration = musicXService.getDuration();
            if (duration != -1) {
                songProgress.setMax(duration);
                updateProgress();
            }
            backgroundArt();
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false)) {
                SongTitle.setTextColor(Color.WHITE);
                SongArtist.setTextColor(ContextCompat.getColor(this,R.color.darkthemeTextColor));
                songProgress.setDefaultProgressColor(accentcolor);
                songProgress.setDefaultProgressBackgroundColor(Color.TRANSPARENT);
            }else {
                SongTitle.setTextColor(Color.WHITE);
                SongArtist.setTextColor(Color.LTGRAY);
                songProgress.setDefaultProgressColor(accentcolor);
                songProgress.setDefaultProgressBackgroundColor(Color.TRANSPARENT);
            }
        }
    }

    /**
     * MiniPlayer Update
     */
    private  void MiniPlayerUpdate(){
        miniplayerview();
        playpausetoggle();
        if (musicXService.isPlaying()) {
            songProgressHandler.post(mUpdateProgress);
        }
    }

    /**
     * play/pause FAB
     */
    private void playpausetoggle() {
        if (musicXService != null) {
            if (musicXService.isPlaying()) {
                playToggle.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.aw_ic_pause));
            } else {
                playToggle.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,R.drawable.aw_ic_play));
            }
        }

    }


    /**
     * update toggles,progress
     */
    private void updateconfig(){
        playpausetoggle();
        if (musicXService.isPlaying()) {
            songProgressHandler.post(mUpdateProgress);
        }else {
            songProgressHandler.removeCallbacks(mUpdateProgress);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
