package com.rks.musicx.services;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cleveroad.audiowidget.AudioWidget;
import com.rks.musicx.R;
import com.rks.musicx.data.eq.BassBoosts;
import com.rks.musicx.data.eq.Equalizers;
import com.rks.musicx.data.eq.Loud;
import com.rks.musicx.data.eq.Reverb;
import com.rks.musicx.data.eq.Virtualizers;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.interfaces.playInterface;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.homeWidget.MusicXWidget5x5;
import com.rks.musicx.ui.homeWidget.MusicXwidget4x4;
import com.rks.musicx.ui.homeWidget.MusicxWidget4x2;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static com.rks.musicx.misc.utils.Constants.ACTION_CHANGE_STATE;
import static com.rks.musicx.misc.utils.Constants.ACTION_CHOOSE_SONG;
import static com.rks.musicx.misc.utils.Constants.ACTION_COMMAND;
import static com.rks.musicx.misc.utils.Constants.ACTION_COMMAND1;
import static com.rks.musicx.misc.utils.Constants.ACTION_COMMAND2;
import static com.rks.musicx.misc.utils.Constants.ACTION_FAV;
import static com.rks.musicx.misc.utils.Constants.ACTION_NEXT;
import static com.rks.musicx.misc.utils.Constants.ACTION_PAUSE;
import static com.rks.musicx.misc.utils.Constants.ACTION_PLAY;
import static com.rks.musicx.misc.utils.Constants.ACTION_PREVIOUS;
import static com.rks.musicx.misc.utils.Constants.ACTION_STOP;
import static com.rks.musicx.misc.utils.Constants.ACTION_TOGGLE;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYER_POS;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.REPEAT_MODE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SONG_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_PATH;
import static com.rks.musicx.misc.utils.Constants.SONG_TITLE;
import static com.rks.musicx.misc.utils.Constants.SONG_TRACK_NUMBER;
import static com.rks.musicx.services.NotificationHandler.notificationID;

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

public class MusicXService extends Service implements playInterface, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
        AudioWidget.OnControlsClickListener, AudioWidget.OnWidgetStateChangedListener, AudioManager.OnAudioFocusChangeListener{


    private static Handler handler;
    public final int NO_REPEAT = 1;
    public final int REPEAT_ALL = 2;
    public final int REPEAT_CURRENT = 3;
    public final String TAG = MusicXService.class.getSimpleName();
    private List<Song> playList = new ArrayList<>();
    private List<Song> ogList = new ArrayList<>();
    private Song CurrentSong;
    private AudioWidget audioWidget;
    private int playingIndex;
    private boolean paused;
    private MusicXBinder musicXBinder = new MusicXBinder();
    private boolean fastplay = false;
    private boolean isPlaying = false;
    private TelephonyManager telephonyManager;
    private MediaSessionCompat mediaSessionLockscreen;
    private int repeatMode = NO_REPEAT;
    private boolean isShuffled = false;
    private String songTitle, songArtist, songPath;
    private long albumID, songID;
    private AudioManager audioManager;
    private MusicxWidget4x2 musicxWidget = MusicxWidget4x2.getInstance(); //4x2 widget
    private MusicXwidget4x4 musicXwidget4x4 = MusicXwidget4x4.getInstance(); // 4x4 widget
    private MusicXWidget5x5 musicXWidget5x5 = MusicXWidget5x5.getInstance(); // jumbo widget
    private FavHelper favHelper;
    private boolean mLostAudioFocus = false;
    private boolean mIsDucked = false;
    private int trackDuration;
    private CommonDatabase recent, queue;
    private List<Song> queueList = new ArrayList<>();
    private boolean onPlayNotify = false;
    private Helper helper;
    private MediaButtonReceiver mediaButtonReceiver = null;
    private ControlReceiver controlReceiver = null;
    private boolean widgetPermission;
    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    if (Extras.getInstance().phonecallConfig()) {
                        pause();
                    } else {
                        play();
                    }
                    break;
            }
        }
    };
    private BroadcastReceiver headsetListener = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG) && isPlaying()) {
                if (Extras.getInstance().headsetConfig()) {
                    pause();
                } else {
                    play();
                }
            }

        }
    };

    private void widgetProgress() {
        handler.post(new ProgressRunnable(this));
    }

    private void removeProgress() {
        handler.removeCallbacks(new ProgressRunnable(this));
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaData();
        initAudioWidgetData();
        otherstuff();
    }

    private void initMediaData() {
        try {
            MediaPlayerSingleton.getInstance().getMediaPlayer();
            MediaPlayerSingleton.getInstance().getMediaPlayer().setOnPreparedListener(this);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setOnCompletionListener(this);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setOnErrorListener(this);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setAudioStreamType(AudioManager.STREAM_MUSIC);
            MediaPlayerSingleton.getInstance().getMediaPlayer().setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
            Log.d(TAG, "MediaInit");
        } catch (Exception e) {
            Log.d(TAG, "initMedia_error", e);
        }
    }

    private void initAudioWidgetData() {
        audioWidget = new AudioWidget.Builder(this).build();
        audioWidget.controller().onControlsClickListener(this);
        audioWidget.controller().onWidgetStateChangedListener(this);
    }

    private void otherstuff() {
        recent = new CommonDatabase(this, Constants.RecentlyPlayed_TableName, true);
        queue = new CommonDatabase(this, Constants.Queue_TableName, true);
        CurrentSong = new Song();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        checkTelephonyState();
        headsetState();
        mediaLockscreen();
        restoreState();
        mediaButtonReceiver = new MediaButtonReceiver();
        if (controlReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY);
            intentFilter.addAction(ACTION_PREVIOUS);
            intentFilter.addAction(ACTION_PAUSE);
            intentFilter.addAction(ACTION_PLAY);
            intentFilter.addAction(ACTION_TOGGLE);
            intentFilter.addAction(ACTION_NEXT);
            intentFilter.addAction(ACTION_CHANGE_STATE);
            intentFilter.addAction(ACTION_COMMAND1);
            intentFilter.addAction(ACTION_COMMAND);
            intentFilter.addAction(ACTION_FAV);
            registerReceiver(controlReceiver, intentFilter);
            registerReceiver(mediaButtonReceiver, intentFilter);
            Log.d(TAG, "Broadcast");
        }
        if (permissionManager.isAudioRecordGranted(this)) {
            int audioID = audioSession();
            Equalizers.initEq(audioID);
            BassBoosts.initBass(audioID);
            Virtualizers.initVirtualizer(audioID);
            Loud.initLoudnessEnhancer(audioID);
            Reverb.initReverb();
        } else {
            Log.d(TAG, "permission not granted");
        }
        favHelper = new FavHelper(this);
        handler = new Handler();
        helper = new Helper(this);
        if (permissionManager.isSystemAlertGranted(MusicXService.this)) {
            widgetPermission = true;
        } else {
            widgetPermission = false;
            Log.d(TAG, "Overlay permission not detected");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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
                    stopSelf();
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
                    if (widgetPermission) {
                        if (!Extras.getInstance().floatingWidget()) {
                            audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
                        } else {
                            audioWidget.hide();
                        }
                    }
                    break;
                }
                case ACTION_COMMAND: {
                    int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    musicxWidget.musicxWidgetUpdate(MusicXService.this, appWidgetIds, null);
                }
                case ACTION_COMMAND1: {
                    int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    musicXwidget4x4.musicxWidgetUpdate(MusicXService.this, appWidgetIds, null);
                }
                case ACTION_COMMAND2: {
                    int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    musicXWidget5x5.musicxWidgetUpdate(MusicXService.this, appWidgetIds, null);
                }
                case ACTION_FAV: {
                    if (favHelper.isFavorite(getsongId())) {
                        favHelper.removeFromFavorites(getsongId());
                        updateService(META_CHANGED);
                    } else {
                        favHelper.addFavorite(getsongId());
                        updateService(META_CHANGED);
                    }
                }

            }
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBind");
        return musicXBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        saveState(true);
        Extras.getInstance().saveSeekServices(getPlayerPos());
        if (isPlaying() && playList.size() > 0) {
            //return false;
            return true;
        }
        Log.d(TAG, "Unbind");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playList.size() == 0 || returnpos() == -1) {
            audioWidget.controller().stop();
            return;
        }
        playnext(true);
        int pos = playList.size() - 1;
        if (playList.get(pos) != null) {
            updateService(PLAYSTATE_CHANGED);
            if (widgetPermission) {
                if (!Extras.getInstance().floatingWidget()) {
                    audioWidget.Stop();
                    audioWidget.Pos(0);
                    trackingstop();
                } else {
                    audioWidget.hide();
                }
            }
        }
        Extras.getInstance().saveSeekServices(0);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.d(TAG, String.valueOf(what) + String.valueOf(extra));
        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        updateService(META_CHANGED);
        restorePos();
        if (fastplay) {
            play();
            fastplay = false;
        }
        trackDuration = MediaPlayerSingleton.getInstance().getMediaPlayer().getDuration();
        if (widgetPermission) {
            if (!Extras.getInstance().floatingWidget()) {
                audioWidget.Pos(0);
                trackingstop();
                audioWidget.Dur(getDuration());
                widgetCover();
                trackingstart();
            } else {
                audioWidget.hide();
            }
        }
        Log.d(TAG, "Prepared");
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
        if (returnpos() == -1) {
            Toast.makeText(this, "No track selected", Toast.LENGTH_SHORT).show();
            return true;
        }
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
            forceStop();
            Log.d(TAG, "WidgetRemoved");
        }
    }

    @Override
    public void onWidgetPositionChanged(int cx, int cy) {
        Extras.getInstance().setwidgetPosition(cx);
        Log.d(TAG, "Widget_Position_Changed");
    }

    private boolean successfullyRetrievedAudioFocus() {
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    /**
     * FadeOut
     *
     * @param _player
     * @param duration
     */
    public void fadeOut(final MediaPlayer _player, final int duration) {
        final float deviceVolume = getDeviceVolume();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private float time = duration;
            private float volume = 0.0f;

            @Override
            public void run() {
                // can call h again after work!
                time -= 100;
                volume = (deviceVolume * time) / duration;
                _player.setVolume(volume, volume);
                if (time > 0)
                    h.postDelayed(this, 100);
                else {
                    _player.pause();
                }
            }
        }, 100); // 1 second delay (takes millis)
    }

    /**
     * Fade In
     *
     * @param _player
     * @param duration
     */
    public void fadeIn(final MediaPlayer _player, final int duration) {
        final float deviceVolume = getDeviceVolume();
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            private float time = 0.0f;
            private float volume = 0.0f;

            @Override
            public void run() {
                _player.start();
                // can call h again after work!
                time += 100;
                volume = (deviceVolume * time) / duration;
                _player.setVolume(volume, volume);
                if (time < duration)
                    h.postDelayed(this, 100);
            }
        }, 100); // 1 second delay (takes millis)

    }

    public float getDeviceVolume() {
        int volumeLevel = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        return (float) volumeLevel / maxVolume;
    }

    @Override
    public void play() {
        if (CurrentSong == null) {
            return;
        }
        if (returnpos() != -1) {
            if (Extras.getInstance().getFadeTrack()) {
                finalPlay();
                fadeIn(MediaPlayerSingleton.getInstance().getMediaPlayer(), fadeDurationValue());
            } else {
                finalPlay();
                MediaPlayerSingleton.getInstance().getMediaPlayer().start();
            }
        }
    }

    private void finalPlay(){
        if (!successfullyRetrievedAudioFocus()) {
            return;
        }
        onPlayNotify = true;
        updateService(PLAYSTATE_CHANGED);
        paused = false;
        isPlaying = true;
        Log.d(TAG, "Play");
        if (repeatMode == REPEAT_CURRENT && getDuration() > 2000 && getPlayerPos() >= getDuration() - 2000) {
            playnext(true);
        }
        if (widgetPermission){
            if (!Extras.getInstance().floatingWidget()) {
                if (!audioWidget.isShown()){
                    audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
                }
                audioWidget.Start();
                trackingstart();
            } else {
                audioWidget.hide();
            }
        }
        recent.add(CurrentSong);
        recent.close();
    }

    private void widgetCover() {
        int size = getResources().getDimensionPixelSize(R.dimen.cover_size);
        if (Extras.getInstance().getDownloadedArtwork()){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(MusicXService.this)
                            .load(helper.loadAlbumImage(getsongAlbumName()))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(size, size)
                            .transform(new CropCircleTransformation(MusicXService.this))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onLoadStarted(Drawable placeholder) {
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    audioWidget.setAlbumArt(ArtworkUtils.drawableToBitmap(errorDrawable));
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    audioWidget.setAlbumArt(resource);
                                }

                            });
                }
            });
        }else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(MusicXService.this)
                            .load(ArtworkUtils.uri(getsongAlbumID()))
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(size, size)
                            .transform(new CropCircleTransformation(MusicXService.this))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onLoadStarted(Drawable placeholder) {
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    audioWidget.setAlbumArt(ArtworkUtils.drawableToBitmap(errorDrawable));
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    audioWidget.setAlbumArt(resource);
                                }

                            });
                }
            });
        }
    }

    @Override
    public void pause() {
        if (CurrentSong == null) {
            return;
        }
        if (Extras.getInstance().getFadeTrack()) {
            finalPause();
            fadeOut(MediaPlayerSingleton.getInstance().getMediaPlayer(), fadeDurationValue());
        } else {
            finalPause();
            MediaPlayerSingleton.getInstance().getMediaPlayer().pause();
        }
    }

    private void finalPause(){
        Log.d(TAG, "Pause");
        paused = true;
        isPlaying = false;
        updateService(PLAYSTATE_CHANGED);
        if (widgetPermission){
            if (!Extras.getInstance().floatingWidget()) {
                trackingstop();
                audioWidget.Pause();
            } else {
                audioWidget.hide();
            }
        }
    }

    @Override
    public int getnextPos(boolean yorn) {
        int incPos = returnpos() + 1;
        if (repeatMode == REPEAT_ALL) {
            Log.d(TAG, "Repeat --> All");
            int pos = playList.size() - 1;
            if (pos == returnpos()){
                return 0;
            } else {
                return incPos;
            }
        } else if (repeatMode == REPEAT_CURRENT) {
            Log.d(TAG, "Repeat --> CURRENT");
            return returnpos();
        } else if (repeatMode == NO_REPEAT & yorn) {
            Log.d(TAG, "Repeat --> NO REPEAT");
            return incPos;
        }
        return -1;
    }

    @Override
    public int getNextrepeatMode() {
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
    public int getprevPos(boolean yorn) {
        int pos = returnpos();

        if (repeatMode == REPEAT_CURRENT) {
            if (pos != -1 && pos < playList.size()) {
                return pos;
            } else {
                return -1;
            }
        } else if (yorn) {
            if (pos != -1 && pos < playList.size()) {
                return pos - 1;
            } else {
                return -1;
            }
        }
        return -1;
    }

    @Override
    public void playnext(boolean torf) {
        int position = getnextPos(torf);
        if (position != -1 && position < playList.size()) {
            paused = false;
            fastplay = true;
            CurrentSong = playList.get(position);
            fastplay(true, CurrentSong);
            Log.d(TAG, "PlayNext");
            Extras.getInstance().saveSeekServices(0);
        } else {
            fastplay = false;
            paused = true;
            isPlaying = false;
        }
    }

    @Override
    public void playprev(boolean torf) {
        int position = getprevPos(torf);
        if (position != -1 && position < playList.size()) {
            fastplay = true;
            paused = false;
            CurrentSong = playList.get(position);
            fastplay(true, CurrentSong);
            Log.d(TAG, "PlayPrev");
            Extras.getInstance().saveSeekServices(0);
        } else {
            fastplay = false;
            paused = true;
            isPlaying = false;
        }
    }

    public int getNoRepeat() {
        return NO_REPEAT;
    }

    public int getRepeatAll() {
        return REPEAT_ALL;
    }

    public int getRepeatCurrent() {
        return REPEAT_CURRENT;
    }

    @Override
    public int returnpos() {
        return playList.indexOf(CurrentSong) != -1 && playList.indexOf(CurrentSong) < playList.size() ? playList.indexOf(CurrentSong) : -1;
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
        if (isShuffled != enable) {
            isShuffled = enable;
            if (enable) {
                shuffle();
                isShuffled = true;
            } else {
                clearQueue();
                playList.addAll(ogList);
            }
            updateService(ORDER_CHANGED);
        }
    }

    @Override
    public void clearQueue() {
        if (playList.size() < 0) {
            return;
        }
        playList.clear();
    }

    @Override
    public void forceStop() {
        if (isPlaying()) {
            paused = false;
            isPlaying = false;
            fastplay = false;
            updateService(PLAYSTATE_CHANGED);
            stopSelf();
        }
    }

    @Override
    public void receiverCleanup() {
        if (controlReceiver != null) {
            try {
                unregisterReceiver(controlReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            controlReceiver = null;
        }
        if (headsetListener != null) {
            try {
                unregisterReceiver(headsetListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
            headsetListener = null;
        }
        if (mediaButtonReceiver != null) {
            try {
                unregisterReceiver(mediaButtonReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaButtonReceiver = null;
        }
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }

    @Override
    public int audioSession() {
        return MediaPlayerSingleton.getInstance().getMediaPlayer() != null ? MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId() : 0;
    }

    @Override
    public void restorePos() {
        int seekpos = Extras.getInstance().getmPreferences().getInt(PLAYER_POS, 0);
        seekto(seekpos);
    }

    @Override
    public int fadeDurationValue() {
        int fadeDuration = 0;
        String savedvalue = Extras.getInstance().getFadeDuration();
        if ( savedvalue != null){
            switch (savedvalue) {
                case "0":
                    fadeDuration = 1000;
                    return fadeDuration;
                case "1":
                    fadeDuration = 3000;
                    return fadeDuration;
                case "2":
                    fadeDuration = 5000;
                    return fadeDuration;
            }
        }
        return fadeDuration;
    }

    @Override
    public void shuffle() {
        if (playList.size() > 0) {
            Random rand = new Random();
            long speed = System.nanoTime();
            rand.setSeed(speed);
            Collections.shuffle(playList, rand);
            Log.d(TAG, "shuffle playlist");
        }
    }

    @Override
    public void toggle() {
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() == null) {
            return;
        }
        if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    @Override
    public void returnHome() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void forwardPlayingView() {
        Intent intent = new Intent(this, PlayingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public int getDuration() {
        if (returnpos() == -1) {
            return 0;
        }
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() != null && returnpos() < playList.size()) {
            Log.d(TAG, "ReturnDuration");
            return trackDuration;
        }else {
            return -1;
        }
    }

    @Override
    public int getPlayerPos() {
        if (returnpos() == -1) {
            return 0;
        }
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() != null && returnpos() < playList.size()) {
            return MediaPlayerSingleton.getInstance().getMediaPlayer().getCurrentPosition();
        } else {
            return -1;
        }
    }

    @Override
    public void seekto(int seek) {
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() != null) {
            MediaPlayerSingleton.getInstance().getMediaPlayer().seekTo(seek);
        } else {
            MediaPlayerSingleton.getInstance().getMediaPlayer().seekTo(0);
        }

    }

    @Override
    public void setPlaylist(List<Song> songList, int pos, boolean play) {
        smartplaylist(songList);
        setdataPos(pos, play);
        if (isShuffled) {
            shuffle();
        }
        updateService(QUEUE_CHANGED);
    }

    @Override
    public void smartplaylist(List<Song> smartplaylists) {
        if (smartplaylists != null && smartplaylists.size() > 0) {
            ogList = smartplaylists;
            playList.clear();
            playList.addAll(ogList);
        } else {
            Log.d(TAG, "smartplaylist error");
        }
    }

    @Override
    public void setdataPos(int pos, boolean play) {
        if (pos != -1 && pos < playList.size()) {
            playingIndex = pos;
            CurrentSong = playList.get(pos);
            if (play) {
                fastplay(true, CurrentSong);
            } else {
                fastplay(false, CurrentSong);
            }
        } else {
            Log.d(TAG, "null value");
        }
    }

    @Override
    public void trackingstart() {
        widgetProgress();
    }

    @Override
    public void trackingstop() {
        removeProgress();
    }

    @Override
    public void startCurrentTrack(Song song) {
        if (returnpos() != -1 && playList.size() > 0) {
            if (MediaPlayerSingleton.getInstance().getMediaPlayer() == null) {
                return;
            }
            Uri dataLoader = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
            if (dataLoader == null) {
                return;
            }
            try {
                MediaPlayerSingleton.getInstance().getMediaPlayer().reset();
                MediaPlayerSingleton.getInstance().getMediaPlayer().setDataSource(MusicXService.this, dataLoader);
                MediaPlayerSingleton.getInstance().getMediaPlayer().prepareAsync();
                MediaPlayerSingleton.getInstance().getMediaPlayer().setAuxEffectSendLevel(1.0f);
                Log.d(TAG, "Prepared");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "position error");
        }
    }

    @Override
    public void setPlaylistandShufle(List<Song> songList, boolean play) {
        if (play) {
            smartplaylist(songList);
            isShuffled = true;
            updateService(QUEUE_CHANGED);
            shuffle();
            Extras.getInstance().saveSeekServices(0);
            setdataPos(0, true);
        }
    }

    @Override
    public void addToQueue(Song song) {
        if (playList.size() > 0 || playList.size() == 0) {
            ogList.add(song);
            playList.add(song);
            updateService(ITEM_ADDED);
        }
    }

    @Override
    public void setAsNextTrack(Song song) {
        if (playList.size() > 0 || playList.size() == 0) {
            ogList.add(song);
            playList.add(returnpos() + 1, song);
            updateService(ITEM_ADDED);
        }
    }

    @Override
    public void fastplay(boolean torf, Song song) {
        fastplay = torf;
        startCurrentTrack(song);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Extras.getInstance().setwidgetPosition(100);
        audioWidget.cleanUp();
        audioWidget = null;
        Equalizers.EndEq();
        BassBoosts.EndBass();
        Virtualizers.EndVirtual();
        Loud.EndLoudnessEnhancer();
        Reverb.EndReverb();
        receiverCleanup();
        Extras.getInstance().eqSwitch(false);
        audioManager.abandonAudioFocus(this);
        removeProgress();
        fastplay = false;
        isPlaying = false;
        paused = false;
        stopMediaplayer();
        if (!Extras.getInstance().hideLockscreen()) {
            if (mediaSessionLockscreen != null) {
                mediaSessionLockscreen.release();
                mediaSessionLockscreen = null;
            }
        }
        Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        if (Helper.isActivityPresent(this, i)) {
            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSession());
            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, this.getPackageName());
            sendBroadcast(i);
        } else {
            Log.d(TAG, "no activity found");
        }
        if (!Extras.getInstance().hideNotify()) {
            removeNotification();
        }
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public void checkTelephonyState() {
        if (Extras.getInstance().getHeadset()) {
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            }
        }
    }

    @Override
    public void headsetState() {
        if (headsetListener != null) {
            IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(headsetListener, receiverFilter);
        }
    }

    @Override
    public void stopMediaplayer() {
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() == null) {
            return;
        }
        MediaPlayerSingleton.getInstance().getMediaPlayer().reset();
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
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                seekto((int) pos);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                if (mediaButtonReceiver != null) {
                    mediaButtonReceiver.onReceive(MusicXService.this, mediaButtonEvent);
                }
                return true;
            }
        });
        mediaSessionLockscreen.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        ComponentName buttonCom = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(buttonCom);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSessionLockscreen.setMediaButtonReceiver(pendingIntent);
        mediaSessionLockscreen.setActive(true);
    }

    public void saveState(boolean yorno) {
        if (playList.size() > 0) {
            if (yorno) {
                queue.removeAll();
                queue.add(playList);
                queue.close();
            }
            songTitle = getsongTitle() == null ? SONG_TITLE : getsongTitle();
            songArtist = getsongTitle() == null ? SONG_ARTIST : getsongArtistName();
            albumID = getsongAlbumID() == 0 ? 0 : getsongAlbumID();
            songID = getsongId() == 0 ? 0 : getsongId();
            songPath = getsongData() == null ? SONG_PATH : getsongData();
            Log.d(TAG, "SavingData");
            Extras.getInstance().saveServices(true, returnpos(), repeatMode, isShuffled, songTitle, songArtist, songPath, songID, albumID);
        }
    }

    public void restoreState() {
        if (Extras.getInstance().getState(false)) {
            queueList = queue.readLimit(-1, null);
            queue.close();
            int restorepos = Extras.getInstance().getCurrentpos();
            String songname = Extras.getInstance().getSongTitle(songTitle);
            String songartist = Extras.getInstance().getSongArtist(songArtist);
            long albumId = Extras.getInstance().getAlbumId(albumID);
            long songId = Extras.getInstance().getSongId(songID);
            String songpath = Extras.getInstance().getSongPath(songPath);
            repeatMode = Extras.getInstance().getRepeatMode(repeatMode);
            isShuffled = Extras.getInstance().getShuffle(isShuffled);
            if (queueList.size() > 0 || !isPlaying() && isPaused()) {
                smartplaylist(queueList);
                setSongTitle(songname);
                setSongArtist(songartist);
                setAlbumID(albumId);
                setSongID(songId);
                setSongPath(songpath);
                if (restorepos != -1 && restorepos < playList.size()) {
                    setdataPos(restorepos, false);
                }
                Log.d(TAG, "restoring data");
            } else {
                Log.d(TAG, "Failed to restore data");
            }
        }
    }

    @Override
    public void updateService(String updateservices) {
        Intent intent = new Intent(updateservices);
        if (updateservices.equals(PLAYSTATE_CHANGED) && intent.getAction().equals(PLAYSTATE_CHANGED)) {
            sendBroadcast(intent);
        } else if (updateservices.equals(META_CHANGED) && intent.getAction().equals(META_CHANGED)) {
            Bundle bundle = new Bundle();
            bundle.putString(SONG_TITLE, getsongTitle());
            bundle.putString(SONG_ALBUM, getsongAlbumName());
            bundle.putLong(SONG_ALBUM_ID, getsongAlbumID());
            bundle.putString(SONG_ARTIST, getsongArtistName());
            bundle.putLong(SONG_ID, getsongId());
            bundle.putString(SONG_PATH, getsongData());
            bundle.putInt(SONG_TRACK_NUMBER, getsongNumber());
            bundle.putInt(POSITION_CHANGED, returnpos());
            intent.putExtras(bundle);
            Log.d(TAG, "broadcast song metadata");
            sendBroadcast(intent);
        } else if ((updateservices.equals(QUEUE_CHANGED) || updateservices.equals(ORDER_CHANGED) || updateservices.equals(ITEM_ADDED)) && (intent.getAction().equals(QUEUE_CHANGED) || intent.getAction().equals(ORDER_CHANGED) || intent.getAction().equals(ITEM_ADDED))) {
            sendBroadcast(intent);
            saveState(true);
        }
        if (onPlayNotify) {
            if (!Extras.getInstance().hideNotify()) {
                NotificationHandler.buildNotification(MusicXService.this, updateservices);
            }
        }
        musicxWidget.notifyChange(this, updateservices);
        musicXwidget4x4.notifyChange(this, updateservices);
        musicXWidget5x5.notifyChange(this, updateservices);
        if (!Extras.getInstance().hideLockscreen()) {
            MediaSession.lockscreenMedia(getMediaSession(), MusicXService.this, updateservices);
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public String getsongTitle() {
        if (CurrentSong != null) {
            return CurrentSong.getTitle();
        } else {
            return null;
        }
    }

    @Override
    public long getsongId() {
        if (CurrentSong != null) {
            return CurrentSong.getId();
        } else {
            return 0;
        }
    }

    @Override
    public String getsongAlbumName() {
        if (CurrentSong != null) {
            return CurrentSong.getAlbum();
        } else {
            return null;
        }
    }

    @Override
    public String getsongArtistName() {
        if (CurrentSong != null) {
            return CurrentSong.getArtist();
        } else {
            return null;
        }
    }

    @Override
    public String getsongData() {
        if (CurrentSong != null) {
            return CurrentSong.getmSongPath();
        } else {
            return null;
        }
    }

    @Override
    public long getsongAlbumID() {
        if (CurrentSong != null) {
            return CurrentSong.getAlbumId();
        } else {
            return 0;
        }
    }

    @Override
    public int getsongNumber() {
        if (CurrentSong != null) {
            return CurrentSong.getTrackNumber();
        } else {
            return 0;
        }
    }

    @Override
    public long getArtistID() {
        if (CurrentSong != null) {
            return CurrentSong.getArtistId();
        } else {
            return 0;
        }
    }

    @Override
    public String getYear() {
        if (CurrentSong != null) {
            return CurrentSong.getYear();
        } else {
            return null;
        }
    }

    public List<Song> getPlayList() {
        return playList;
    }

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

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSessionLockscreen;
    }

    public Song getCurrentSong() {
        return CurrentSong;
    }

    public AudioWidget getAudioWidget() {
        return audioWidget;
    }

    /*
     * Remove Notification _/\_
    */
    private void removeNotification() {
        NotificationManagerCompat.from(this).cancel(notificationID);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                Log.d(TAG, "AudioFocus Loss");
                if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                    pause();
                    //service.stopSelf();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                    MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.3f, 0.3f);
                    mIsDucked = true;
                }
                Log.d(TAG, "AudioFocus Loss Can Duck Transient");
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.d(TAG, "AudioFocus Loss Transient");
                if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                    pause();
                    mLostAudioFocus = true;
                }
                break;
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.d(TAG, "AudioFocus Gain");
                if (mIsDucked) {
                    MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
                    mIsDucked = false;
                } else if (mLostAudioFocus) {
                    // If we temporarily lost the audio focus we can resume playback here
                    if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                        play();
                    }
                    mLostAudioFocus = false;
                }
                break;
            default:
                Log.d(TAG, "Unknown focus");
        }
    }

    private static class ProgressRunnable implements Runnable {

        private final WeakReference<MusicXService> musicxService;

        public ProgressRunnable(MusicXService musicXService) {
            musicxService = new WeakReference<MusicXService>(musicXService);
        }

        @Override
        public void run () {
            MusicXService musicXService = musicxService.get();
            if (musicXService != null) {
                if (musicXService.audioWidget != null && musicXService.isPlaying() && musicXService.returnpos() < musicXService.playList.size()) {
                    musicXService.audioWidget.Pos(musicXService.getPlayerPos());
                }
            }
            handler.postDelayed(this,1000);
        }
    }

    /**
     * BroadCast controls
     */
    private class ControlReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                if (mLostAudioFocus) {
                    mLostAudioFocus = false;
                }
                pause();
                Log.d(TAG, "noisyAudio");
            } else if (intent.getAction().equals(ACTION_PLAY)) {
                play();
            } else if (intent.getAction().equals(ACTION_PAUSE)) {
                pause();
            } else if (intent.getAction().equals(ACTION_NEXT)) {
                playnext(true);
            } else if (intent.getAction().equals(ACTION_PREVIOUS)) {
                playprev(true);
            } else if (intent.getAction().equals(ACTION_STOP)) {
                stopSelf();
            } else if (intent.getAction().equals(ACTION_TOGGLE)) {
                toggle();
            } else if (intent.getAction().equals(ACTION_CHANGE_STATE)) {
                if (widgetPermission) {
                    if (!Extras.getInstance().floatingWidget()) {
                        audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
                    } else {
                        audioWidget.hide();
                    }
                }
            } else if (intent.getAction().equals(ACTION_FAV)) {
                if (favHelper.isFavorite(Extras.getInstance().getSongId(getsongId()))) {
                    favHelper.removeFromFavorites(Extras.getInstance().getSongId(getsongId()));
                    updateService(META_CHANGED);
                } else {
                    favHelper.addFavorite(Extras.getInstance().getSongId(getsongId()));
                    updateService(META_CHANGED);
                }
            } else if (intent.getAction().equals(ACTION_COMMAND)) {
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                musicxWidget.musicxWidgetUpdate(MusicXService.this, appWidgetIds, null);
            } else if (intent.getAction().equals(ACTION_COMMAND1)) {
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                musicXwidget4x4.musicxWidgetUpdate(MusicXService.this, appWidgetIds, null);
            } else if (intent.getAction().equals(ACTION_COMMAND2)) {
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                musicXWidget5x5.musicxWidgetUpdate(MusicXService.this, appWidgetIds, null);
            }
        }

    }

    /*
    *Binding services
    */
    public class MusicXBinder extends Binder {

        public MusicXService getService() {
            return MusicXService.this;
        }
    }

}
