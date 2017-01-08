package com.rks.musicx.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.PermissionChecker;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.cleveroad.audiowidget.AudioWidget;
import com.rks.musicx.R;
import com.rks.musicx.data.Eq.AudioEffects;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.Queue;
import com.rks.musicx.database.RecentlyPlayed;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.activities.PlayingActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.os.Build.VERSION_CODES.N;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;
import static com.rks.musicx.misc.utils.Constants.ACTION_CHANGE_STATE;
import static com.rks.musicx.misc.utils.Constants.ACTION_CHOOSE_SONG;
import static com.rks.musicx.misc.utils.Constants.ACTION_NEXT;
import static com.rks.musicx.misc.utils.Constants.ACTION_PAUSE;
import static com.rks.musicx.misc.utils.Constants.ACTION_PLAY;
import static com.rks.musicx.misc.utils.Constants.ACTION_PREVIOUS;
import static com.rks.musicx.misc.utils.Constants.ACTION_STOP;
import static com.rks.musicx.misc.utils.Constants.ACTION_TOGGLE;
import static com.rks.musicx.misc.utils.Constants.AUDIO_ID;
import static com.rks.musicx.misc.utils.Constants.CURRENTPOS;
import static com.rks.musicx.misc.utils.Constants.EXTRA_CHANGE_STATE;
import static com.rks.musicx.misc.utils.Constants.EXTRA_POSITION;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_X;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_Y;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.OPEN_EFFECTS;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYINGSTATE;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PREF_AUTO_PAUSE;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.REPEATMODE;
import static com.rks.musicx.misc.utils.Constants.REPEAT_MODE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.SHUFFLEMODE;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SONG_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_PATH;
import static com.rks.musicx.misc.utils.Constants.SONG_TITLE;
import static com.rks.musicx.misc.utils.Constants.SONG_TRACK_NUMBER;

/**
 * Created by Coolalien on 10/23/2016.
 */

public class MusicXService extends Service implements playInterface, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioWidget.OnControlsClickListener, AudioWidget.OnWidgetStateChangedListener{


    private List<Song> playList = new ArrayList<>();
    private List<Song> ogList = new ArrayList<>();
    private Song CurrentSong;
    private AudioWidget audioWidget;
    private int playingIndex;
    private boolean paused;
    private Timer timer;
    private MusicXBinder musicXBinder = new MusicXBinder();
    private static final long UPDATE_INTERVAL = 1000;
    private SharedPreferences saveData;
    private AudioManager audioManager;
    private boolean fastplay = false;
    private boolean isPlaying = false;
    private TelephonyManager telephonyManager;
    private boolean autoPhoneState = false;
    private boolean loosypaused;
    private int notificationID = 4;
    private MediaSessionCompat mediaSessionLockscreen;
    private final int NO_REPEAT = 1;
    private final int REPEAT_ALL = 2;
    private final int REPEAT_CURRENT = 3;
    private int repeatMode = NO_REPEAT;
    private boolean isShuffled = false;
    private boolean isBound = false;
    private int startID,coloraccent;
    private final long msgDelay = 6000;
    private String atkey;
    private String songTitle, songArtist;
    private long albumID, songID;
    /**
     * Oncreate method
     */
    @Override
    public void onCreate() {
        super.onCreate();
        initMediaData();
        initAudioWidgetData();
        otherstuff();
    }

    /**
     * Init mediaplayer
     */
    private void initMediaData() {
        try {
            MediaPlayerSingleton.getInstance().getMediaPlayer();
            MediaPlayerSingleton.getInstance().getMediaPlayer().setOnPreparedListener(this);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setOnCompletionListener(this);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setOnErrorListener(this);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        }catch (Exception e){
            Log.d("MusicXService","initMedia_error", e);
        }
    }

    /**
     * init audiowidget
     */
    private void initAudioWidgetData() {
        audioWidget = new AudioWidget.Builder(this).build();
        audioWidget.controller().onControlsClickListener(this);
        audioWidget.controller().onWidgetStateChangedListener(this);
    }

    /**
     * other stuff
     */
    private void otherstuff() {
        saveData = PreferenceManager.getDefaultSharedPreferences(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        autoPhoneState = saveData.getBoolean(PREF_AUTO_PAUSE, false);
        checkTelephonyState();
        headsetState();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaLockscreen();
        }
        restoreState();
        Intent i = new Intent(this, AudioEffects.class);
        i.setAction(OPEN_EFFECTS);
        i.putExtra(AUDIO_ID,  MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
        sendBroadcast(i);
        atkey = Helper.getATEKey(this);
        coloraccent = Config.accentColor(this,atkey);
    }

    private Handler msgDelayHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (isPlaying() || isBound) {
                return false;
            }
            stopSelf(startID);
            return true;
        }
    });

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startID = startId;
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case ACTION_CHOOSE_SONG: {
                    returnHome();
                }
                case ACTION_TOGGLE: {
                    toggle();
                    break;
                }
                case ACTION_PAUSE: {
                    pause();
                    break;
                }
                case ACTION_PLAY: {
                    play();
                    break;
                }
                case ACTION_STOP: {
                    if (!isBound) {
                        stopSelf(startID);
                    }
                    break;
                }
                case ACTION_NEXT: {
                    playnext(true);
                    break;
                }
                case ACTION_PREVIOUS: {
                    playprev(true);
                    break;
                }
                case ACTION_CHANGE_STATE: {
                    if (PermissionChecker.checkSelfPermission(this,Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED){
                        if (!(Build.VERSION.SDK_INT >= N && !Settings.canDrawOverlays(this))) {
                            boolean show = intent.getBooleanExtra(EXTRA_CHANGE_STATE, false);
                            if (show) {
                                if (Extras.getInstance().floatingWidget()){
                                    audioWidget.hide();
                                }
                            } else {
                                if (!Extras.getInstance().floatingWidget()){
                                    audioWidget.show(saveData.getInt(KEY_POSITION_X, 100), saveData.getInt(KEY_POSITION_Y, 100));
                                }
                            }
                        } else {
                            Log.w("MusicXService", "Can't change audio widget state! Device does not have drawOverlays permissions!");
                        }
                    }
                    break;
                }
            }
        }
        return START_STICKY;
    }

    public static void setState(@NonNull Context context, boolean isShowing) {
        Intent intent = new Intent(ACTION_CHANGE_STATE, null, context, MusicXService.class);
        intent.putExtra(EXTRA_CHANGE_STATE, isShowing);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        isBound = true;
        return musicXBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBound = false;
        if (isPlaying()) {
            return true;
        }
        if (playList.size() > 0) {
            Message msg = msgDelayHandler.obtainMessage();
            msgDelayHandler.sendMessageDelayed(msg, msgDelay);
            return true;
        }
        stopSelf(startID);
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playnext(false);
        if (playingIndex == -1) {
            audioWidget.controller().stop();
        }
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG , String.valueOf(what) + String.valueOf(extra));
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        updateService(META_CHANGED);
        if (fastplay) {
            play();
            fastplay = false;
        }
        if (PermissionChecker.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED) {
            if (!Extras.getInstance().floatingWidget()){
                audioWidget.show(saveData.getInt(KEY_POSITION_X, 100), saveData.getInt(KEY_POSITION_Y, 100));
                audioWidget.controller().start();
                audioWidget.controller().position(0);
                audioWidget.controller().duration(MediaPlayerSingleton.getInstance().getMediaPlayer().getDuration());
                trackingstart();
                trackingstop();
            }else {
                audioWidget.hide();
            }
        }
    }

    @Override
    public boolean onPlaylistClicked() {
        returnHome();
        return true;
    }

    @Override
    public void onPlaylistLongClicked() {

    }

    @Override
    public void onPreviousClicked() {
        playprev(true);
    }

    @Override
    public void onPreviousLongClicked() {

    }

    @Override
    public boolean onPlayPauseClicked() {
        toggle();
        return true;
    }

    @Override
    public void onPlayPauseLongClicked() {
    }

    @Override
    public void onNextClicked() {
        playnext(true);
    }

    @Override
    public void onNextLongClicked() {

    }

    @Override
    public void onAlbumClicked() {
        forwardPlayingView();
    }

    @Override
    public void onAlbumLongClicked() {

    }

    @Override
    public void onWidgetStateChanged(@NonNull AudioWidget.State state) {
        if (state == AudioWidget.State.REMOVED) {
            stopSelf();
            forceStop();
        }
    }

    @Override
    public void onWidgetPositionChanged(int cx, int cy) {
        saveData.edit()
                .putInt(KEY_POSITION_X, cx)
                .putInt(KEY_POSITION_Y, cy)
                .apply();
    }

    @Override
    public void play() {
        int audiogain = audioManager.requestAudioFocus(audioListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (audiogain == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
           MediaPlayerSingleton.getInstance().getMediaPlayer().start();
            paused = false;
            isPlaying = true;
            updateService(PLAYSTATE_CHANGED);
            if (PermissionChecker.checkSelfPermission(this,Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED){
                if (!Extras.getInstance().floatingWidget()){
                    trackingstart();
                    audioWidget.controller().start();
                }else {
                    audioWidget.hide();
                }
            }
            if (CurrentSong != null){
                RecentlyPlayed recentlyPlayed = new RecentlyPlayed(this);
                recentlyPlayed.add(CurrentSong);
            }
        }
    }

    private void widgetCover(Song song){
        new Runnable() {
            @Override
            public void run() {
                int size = getResources().getDimensionPixelSize(R.dimen.cover_size);
                Glide.with(MusicXService.this)
                        .load(ArtworkUtils.uri(song.getAlbumId()))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .placeholder(R.mipmap.ic_launcher)
                        .error(R.mipmap.ic_launcher)
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(size,size)
                        .transform(new CropCircleTransformation(MusicXService.this))
                        .into(new Target<Bitmap>() {
                            @Override
                            public void onStart() {

                            }

                            @Override
                            public void onStop() {

                            }

                            @Override
                            public void onDestroy() {

                            }

                            @Override
                            public void onLoadStarted(Drawable placeholder) {

                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                audioWidget.controller().albumCover(errorDrawable);

                            }

                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                audioWidget.controller().albumCoverBitmap(resource);
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {

                            }

                            @Override
                            public void getSize(SizeReadyCallback cb) {

                            }

                            @Override
                            public void setRequest(Request request) {

                            }

                            @Override
                            public Request getRequest() {
                                return null;
                            }
                        });
            }
        }.run();
    }

    @Override
    public void pause() {
        MediaPlayerSingleton.getInstance().getMediaPlayer().pause();
        paused = true;
        isPlaying = false;
        updateService(PLAYSTATE_CHANGED);
        if (PermissionChecker.checkSelfPermission(this,Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED){
            if (!Extras.getInstance().floatingWidget()){
                trackingstop();
                audioWidget.controller().pause();
            }
        }
    }

    @Override
    public int getnextPos(boolean yorn) {
        returnpos();
        if (repeatMode == REPEAT_CURRENT && !yorn) {
            return playingIndex;
        }
        if (playingIndex + 1 >= playList.size()) {
            if (repeatMode == REPEAT_ALL) {
                return 0;
            }

            return -1;
        }
        return playingIndex + 1;
    }

    @Override
    public int getprevPos(boolean yorn) {
        returnpos();
        if ((repeatMode == REPEAT_CURRENT && !yorn) || (isPlaying() && MediaPlayerSingleton.getInstance().getMediaPlayer().getCurrentPosition() >= 1500)) {
            return playingIndex;
        }
        if (playingIndex - 1 < 0) {
            if (repeatMode == REPEAT_ALL) {
                return playList.size() - 1;
            }
            return -1;
        }
        return playingIndex - 1;
    }

    @Override
    public void playnext(boolean torf) {
        int position = getnextPos(torf);
        if (position >= 0 && position < playList.size()) {
            playingIndex = position;
            CurrentSong = playList.get(playingIndex);
            fastplay();
        } else {
            isPlaying = false;
            paused = true;
            updateService(PLAYSTATE_CHANGED);
        }
    }

    @Override
    public void playprev(boolean torf) {
        int position = getprevPos(torf);
        if (position >= 0 && position < playList.size()) {
            playingIndex = position;
            CurrentSong = playList.get(playingIndex);
            fastplay();
        }
    }

    @Override
    public int getNextRepeatMode() {
        switch (repeatMode) {
            case NO_REPEAT:
                return REPEAT_ALL;
            case REPEAT_ALL:
                return REPEAT_CURRENT;
            case REPEAT_CURRENT:
                return NO_REPEAT;
        }
        return NO_REPEAT;
    }

    @Override
    public void setdataPos(int pos, boolean play) {
        if (pos != -1){
            playingIndex = pos;
            CurrentSong = playList.get(playingIndex);
            if (play){
                fastplay();
            }else {
                startCurrentTrack();
                paused = true;
            }
            if (PermissionChecker.checkSelfPermission(this, Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED) {
                widgetCover(CurrentSong);
            }
        }
    }

    @Override
    public int returnpos() {
        if (playingIndex != -1){
            return playList.indexOf(CurrentSong) != -1 ? playList.indexOf(CurrentSong) : -1;
        }
        return -1;
    }


    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int mode) {
        repeatMode = mode;
        updateService(REPEAT_MODE_CHANGED);
    }

    public boolean isShuffleEnabled() {
        return isShuffled;
    }

    public void setShuffleEnabled(boolean enable) {

        if (isShuffled != enable){
            isShuffled = enable;
            if (enable){
                shuffle();
            }else {
                playList.clear();
                playList.addAll(ogList);
            }
            returnpos();
            updateService(ORDER_CHANGED);
        }
    }


    @Override
    public void shuffle() {
        Collections.shuffle(playList);
    }

    @Override
    public void toggle() {
        if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void returnHome() {
        Intent intent = new Intent(this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void forwardPlayingView() {
        Intent intent = new Intent(this,PlayingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public int getDuration() {
        if (isPlaying() && playingIndex != -1 || playList.size() > 0 && paused){
            return MediaPlayerSingleton.getInstance().getMediaPlayer().getDuration();
        }
        return 0;
    }

    @Override
    public int getPlayerPos() {
        if (isPlaying() && playingIndex != -1 || playList.size() > 0 && paused){
            return MediaPlayerSingleton.getInstance().getMediaPlayer().getCurrentPosition();
        }
        return 0;
    }

    @Override
    public void seekto(int seek) {
        if (isPlaying() && playingIndex != -1 || playList.size() > 0 && paused){
            MediaPlayerSingleton.getInstance().getMediaPlayer().seekTo(seek);
        }else {
            MediaPlayerSingleton.getInstance().getMediaPlayer().seekTo(0);
        }
    }

    @Override
    public void setPlaylist(List<Song> songList, int pos, boolean play) {
        smartplaylist(songList);
        setdataPos(pos,play);
        if (isShuffled) {
            shuffle();
        }
        updateService(QUEUE_CHANGED);
    }


    @Override
    public void smartplaylist(List<Song> smartplaylist) {
        if (smartplaylist == null || smartplaylist.size() <= 0) {
            return;
        }
        ogList = smartplaylist;
        playList.clear();
        playList.addAll(ogList);
    }

    @Override
    public void trackingstart() {
        timer = new Timer(TAG);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                AudioWidget widget = audioWidget;
                if (widget != null && isPlaying()) {
                    widget.controller().position(MediaPlayerSingleton.getInstance().getMediaPlayer().getCurrentPosition());
                }
            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    @Override
    public void trackingstop() {
        if (timer == null)
            return;
        timer.cancel();
        timer.purge();
        timer = null;
    }


    @Override
    public void startCurrentTrack() {
        int pos = playList.indexOf(CurrentSong);
        if (playingIndex != -1 && pos != -1){
            Bundle extras = new Bundle();
            extras.putInt(EXTRA_POSITION, pos);
            sendBroadcast(POSITION_CHANGED, extras);
        }
        MediaPlayerSingleton.getInstance().getMediaPlayer().reset();
        Uri dataLoader = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                getsongId());
        try {
            MediaPlayerSingleton.getInstance().getMediaPlayer().setDataSource(this,dataLoader);
            MediaPlayerSingleton.getInstance().getMediaPlayer().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setPlaylistandShufle(List<Song> songList, boolean play) {
        smartplaylist(songList);
        isShuffled = true;
        updateService(QUEUE_CHANGED);
        boolean b = playList.remove(CurrentSong);
        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        Collections.shuffle(playList,rand);
        if (b){
            playList.add(0, CurrentSong);
        }
        setdataPos(0, true);
        if (play) {
            play();
        }
    }

    @Override
    public void addToQueue(Song song) {
        if (playList != null) {
            ogList.add(song);
            playList.add(song);
            updateService(ITEM_ADDED);
        }
    }

    @Override
    public void setAsNextTrack(Song song) {
        if (playList != null) {
            ogList.add(song);
            playList.add(playingIndex + 1, song);
            updateService(ITEM_ADDED);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        audioWidget.controller().onControlsClickListener(null);
        audioWidget.controller().onWidgetStateChangedListener(null);
        audioWidget.hide();
        audioWidget = null;
        if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
            MediaPlayerSingleton.getInstance().getMediaPlayer().stop();
        }
        MediaPlayerSingleton.getInstance().getMediaPlayer().reset();
        //MediaPlayerSingleton.getInstance().getMediaPlayer().release();
        trackingstop();
        if (telephonyManager !=null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(headsetListener);
        audioManager.abandonAudioFocus(audioListener);
        mediaSessionLockscreen = null;
        saveData = null;
    }

    public boolean isPaused() {
        return paused;
    }

    /*
    *Binding services
    */
    public class MusicXBinder extends Binder {
        public MusicXService getService() {
            return MusicXService.this;
        }
    }

    @Override
    public void fastplay() {
        fastplay = true;
        startCurrentTrack();
    }

    @Override
    public void checkTelephonyState() {
        if (autoPhoneState) {
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    @Override
    public void setAutoPauseEnabled(boolean yorn) {
        if (yorn == !autoPhoneState) {
            autoPhoneState = yorn;
            if (yorn) {
                checkTelephonyState();
            }
        }
    }

    @Override
    public void headsetState() {
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetListener, receiverFilter);
    }

    @Override
    public void mediaLockscreen() {
        mediaSessionLockscreen = new MediaSessionCompat(this, TAG);
        mediaSessionLockscreen.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                playnext(true);
            }

            @Override
            public void onSkipToPrevious() {
                playprev(true);
            }

            @Override
            public void onStop() {
                forceStop();
            }

            @Override
            public void onSeekTo(long pos) {
                seekto((int) pos);
            }
        });
    }

    @Override
    public void forceStop() {
        MediaPlayerSingleton.getInstance().getMediaPlayer().stop();
        isPlaying = false;
        audioWidget.controller().stop();
        trackingstop();
        updateService(PLAYSTATE_CHANGED);
    }

    @Override
    public void updatemediaLockscreen(String update) {

        if (!mediaSessionLockscreen.isActive()) {
            mediaSessionLockscreen.setActive(true);
        }

        if (update.equals(PLAYSTATE_CHANGED)) {

            int playState = isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
            mediaSessionLockscreen.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(playState, getPlayerPos(), 1.0F)
                    .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_PLAY_PAUSE |
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
                    .build());
        }
        if (update.equals(META_CHANGED)) {
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getsongArtistName())
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getsongAlbumName())
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getsongTitle())
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, MediaPlayerSingleton.getInstance().getMediaPlayer().getDuration());

            ArtworkUtils.ArtworkLoaderBitmapPalette(this, getsongAlbumID(), new palette() {
                @Override
                public void palettework(Palette palette) {
                }
            }, new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
                }

            });
            mediaSessionLockscreen.setMetadata(builder.build());
        }
    }

    /**
     * Save State
     * @param yorno
     */
    private void saveState(boolean yorno) {
        SharedPreferences.Editor editor = saveData.edit();
        if (playList.size() > 0) {
            editor.putBoolean(PLAYINGSTATE, true);
            if (yorno) {
                Queue queue = new Queue(this);
                queue.removeAll();
                queue.add(playList);
                queue.close();
            }
            playingIndex = returnpos();
            songTitle = getsongTitle() == null ? SONG_TITLE : getsongTitle();
            songArtist = getsongTitle() == null ? SONG_ARTIST : getsongArtistName();
            albumID = getsongAlbumID() == 0 ? 0 : getsongAlbumID();
            songID = getsongId() == 0 ? 0 : getsongId();
            Log.d(TAG,"SavingData");
            editor.putInt(CURRENTPOS, playingIndex);
            editor.putInt(REPEATMODE, repeatMode);
            editor.putBoolean(SHUFFLEMODE, isShuffled);
            editor.putString(SONG_TITLE,songTitle);
            editor.putString(SONG_ARTIST,songArtist);
            editor.putLong(SONG_ID,songID);
            editor.putLong(SONG_ALBUM_ID, albumID);
            editor.apply();
        }
    }

    /**
     * Restore state
     */
    private void restoreState(){
        if (saveData.getBoolean(PLAYINGSTATE, false)) {
            Queue queue = new Queue(this);
            List<Song> queueList = queue.read();
            queue.close();
            int restorepos = saveData.getInt(CURRENTPOS, playingIndex);
            repeatMode = saveData.getInt(REPEATMODE, repeatMode);
            isShuffled = saveData.getBoolean(SHUFFLEMODE, isShuffled);
            String songname = saveData.getString(SONG_TITLE,songTitle);
            String songartist = saveData.getString(SONG_ARTIST,songArtist);
            long albumId = saveData.getLong(SONG_ALBUM_ID, albumID);
            long songId = saveData.getLong(SONG_ID,songID);
            if (playingIndex != -1 && queueList.size() >0 || !isPlaying() && isPaused()) {
                smartplaylist(queueList);
                setdataPos(restorepos, false);
                setSongTitle(songname);
                setSongArtist(songartist);
                setAlbumID(albumId);
                setSongID(songId);
                Log.d(TAG,"restoring data");
            }
        }
    }

    /**
     * Broadcast changes
     * @param updateservices
     */
    @Override
    public void updateService(String updateservices) {
        updatemediaLockscreen(updateservices);
        saveState(QUEUE_CHANGED.equals(updateservices) || ITEM_ADDED.equals(updateservices) || ORDER_CHANGED.equals(updateservices));
        if (PLAYSTATE_CHANGED.equals(updateservices) || META_CHANGED.equals(updateservices)) {
            buildNotification();
            Intent intent = new Intent();
            intent.setAction(PLAYSTATE_CHANGED.equals(updateservices) ? PLAYSTATE_CHANGED : META_CHANGED);
            Bundle bundle = new Bundle();
            bundle.putString(SONG_TITLE, getsongTitle());
            bundle.putString(SONG_ALBUM, getsongAlbumName());
            bundle.putLong(SONG_ALBUM_ID, getsongAlbumID());
            bundle.putString(SONG_ARTIST, getsongArtistName());
            bundle.putLong(SONG_ID, getsongId());
            bundle.putString(SONG_PATH, getsongData());
            bundle.putInt(SONG_TRACK_NUMBER, getsongNumber());
            intent.putExtras(bundle);
            Log.d(TAG,"broadcast song metadata");
            sendBroadcast(intent);
        }
        sendBroadcast(updateservices, null);
    }

    private void sendBroadcast(String action, Bundle data) {
        Intent i = new Intent(action);
        if (data != null) {
            i.putExtras(data);
        }
        sendBroadcast(i);
    }

    /**
     * Getter
     * @return
     */
    public List<Song> getPlayList() {
        return playList;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getNO_REPEAT() {
        return NO_REPEAT;
    }

    public int getREPEAT_ALL() {
        return REPEAT_ALL;
    }

    public int getREPEAT_CURRENT() {
        return REPEAT_CURRENT;
    }

    public Song getCurrentSong() {
        return CurrentSong;
    }

    @Override
    public String getsongTitle() {
        if (CurrentSong !=null){
            return CurrentSong.getTitle();
        }
        return null;
    }

    @Override
    public long getsongId() {
        if (CurrentSong !=null){
            return CurrentSong.getId();
        }
        return 0;
    }

    @Override
    public String getsongAlbumName() {
        if (CurrentSong !=null){
            return CurrentSong.getAlbum();
        }
        return null;
    }

    @Override
    public String getsongArtistName() {
        if (CurrentSong !=null){
            return CurrentSong.getArtist();
        }
        return null;
    }

    @Override
    public String getsongData() {
        if (CurrentSong !=null){
            return CurrentSong.getmSongPath();
        }
        return null;
    }

    @Override
    public long getsongAlbumID() {
        if (CurrentSong !=null){
            return CurrentSong.getAlbumId();
        }
        return 0;
    }

    @Override
    public int getsongNumber() {
        if (CurrentSong !=null){
            return CurrentSong.getTrackNumber();
        }
        return 0;
    }

    /**
     * Setter
     */

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public void setAlbumID(long albumID) {
        this.albumID = albumID;
    }

    public void setSongID(long songID) {
        this.songID = songID;
    }

    public void setCurrentSong(Song currentSong) {
        CurrentSong = currentSong;
    }

    /**
     * Phone StateListener
     */
    private PhoneStateListener phoneStateListener= new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    if (Extras.getInstance().phonecallConfig()){
                        pause();
                    }else {
                        play();
                    }
                    break;
            }

        }
    };

    /**
     * headset State Listener
     */
    private BroadcastReceiver headsetListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) && isPlaying()) {
                if (Extras.getInstance().headsetConfig()){
                    pause();
                }else {
                    play();
                }
            }

        }
    };

    /**
     * AudioListerner
     */
    private AudioManager.OnAudioFocusChangeListener audioListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (isPlaying()) {
                        pause();
                        loosypaused = true;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!isPlaying() && loosypaused) {
                        play();
                        loosypaused = false;
                    }
                    MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (isPlaying()) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.1f, 0.1f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    audioManager.abandonAudioFocus(audioListener);
                    pause();
                    loosypaused = false;
                    break;
            }
        }
    };

    public MediaSessionCompat getMediaSession() {
        return mediaSessionLockscreen;
    }

    /*
    *Notification _/\_
    */

    @Override
    public void buildNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(getsongTitle());
        builder.setContentText(getsongArtistName());
        builder.setAutoCancel(true);
        PendingIntent nextIntent = PendingIntent.getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_NEXT), 0);
        PendingIntent previousIntent = PendingIntent.getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_PREVIOUS), 0);
        PendingIntent pauseIntent = PendingIntent.getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_TOGGLE), 0);
        PendingIntent playIntent = PendingIntent.getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_TOGGLE), 0);
        builder.addAction(R.drawable.aw_ic_prev, "", previousIntent);
        if (isPlaying()){
            builder.addAction(R.drawable.aw_ic_pause, "", pauseIntent);
        }else {
            builder.addAction(R.drawable.aw_ic_play, "", playIntent);
        }
        builder.addAction(R.drawable.aw_ic_next, "",nextIntent);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
                    .setStyle(new NotificationCompat.MediaStyle()
                            .setMediaSession(getMediaSession().getSessionToken())
                            .setShowActionsInCompactView(0, 1, 2));
        }
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendInt);
        builder.setShowWhen(false);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        ArtworkUtils.ArtworkLoaderBitmapPalette(this, getsongAlbumID(), new palette() {
            @Override
            public void palettework(Palette palette) {
                final int color[] = Helper.getAvailableColor(getApplicationContext(), palette);
                builder.setColor(color[0]);
            }
        }, new bitmap() {
            @Override
            public void bitmapwork(Bitmap bitmap) {
                builder.setLargeIcon(bitmap);
            }

            @Override
            public void bitmapfailed(Bitmap bitmap) {
                builder.setLargeIcon(bitmap);
            }

        });
        android.app.Notification notification = builder.build();
        if (isPlaying()) {
            startForeground(notificationID, notification);
        }else {
            stopForeground(true);
        }
    }


}
