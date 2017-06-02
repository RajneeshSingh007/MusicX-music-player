package com.rks.musicx.services;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

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
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.homeWidget.MusicXWidget5x5;
import com.rks.musicx.ui.homeWidget.MusicXwidget4x4;
import com.rks.musicx.ui.homeWidget.MusicxWidget4x2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_X;
import static com.rks.musicx.misc.utils.Constants.KEY_POSITION_Y;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYER_POS;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PREF_AUTO_PAUSE;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.REPEAT_MODE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SONG_ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SONG_ID;
import static com.rks.musicx.misc.utils.Constants.SONG_PATH;
import static com.rks.musicx.misc.utils.Constants.SONG_TITLE;
import static com.rks.musicx.misc.utils.Constants.SONG_TRACK_NUMBER;

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


    public final int NO_REPEAT = 1;
    public final int REPEAT_ALL = 2;
    public final int REPEAT_CURRENT = 3;
    public final String TAG = "MusicX";
    private final int maxVol = 100;
    private final int minVol = 0;
    private final float floatMax = 1;
    private final float floatMin = 0;
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
    private boolean autoPhoneState = false;
    private MediaSessionCompat mediaSessionLockscreen;
    private int repeatMode = NO_REPEAT;
    private boolean isShuffled = false;
    private String songTitle, songArtist, songPath;
    private long albumID, songID;
    private AudioManager audioManager;
    private MusicxWidget4x2 musicxWidget = MusicxWidget4x2.getInstance(); //4x2 widget
    private MusicXwidget4x4 musicXwidget4x4 = MusicXwidget4x4.getInstance(); // 4x4 widget
    private MusicXWidget5x5 musicXWidget5x5 = MusicXWidget5x5.getInstance(); // jumbo widget
    private MediaButtonReceiver mediaButtonReceiver = null;
    private ControlReceiver controlReceiver = null;
    private FavHelper favHelper;
    private int volume;
    private boolean mLostAudioFocus = false;
    private boolean mIsDucked = false;
    private Handler handler;
    private int trackDuration;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            AudioWidget audioWidgets = audioWidget;
            if (audioWidgets != null && isPlaying && returnpos() < playList.size()) {
                audioWidget.controller().position(getPlayerPos());
            }
            handler.postDelayed(runnable, 1000);
        }
    };
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
        autoPhoneState = Extras.getInstance().getmPreferences().getBoolean(PREF_AUTO_PAUSE, false);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        checkTelephonyState();
        headsetState();
        mediaLockscreen();
        restoreState();
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
                    if (permissionManager.isSystemAlertGranted(MusicXService.this)) {
                        if (!Extras.getInstance().floatingWidget()) {
                            audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
                        } else {
                            audioWidget.hide();
                        }
                    } else {
                        Log.d(TAG, "Overlay not detected");
                    }
                    break;
                }
                case ACTION_COMMAND: {
                    int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    musicxWidget.musicxWidgetUpdate(MusicXService.this, appWidgetIds);
                }
                case ACTION_COMMAND1: {
                    int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    musicXwidget4x4.musicxWidgetUpdate(MusicXService.this, appWidgetIds);
                }
                case ACTION_COMMAND2: {
                    int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                    musicXWidget5x5.musicxWidgetUpdate(MusicXService.this, appWidgetIds);
                }
                case ACTION_FAV: {
                    if (favHelper.isFavorite(Extras.getInstance().getSongId(getsongId()))) {
                        favHelper.removeFromFavorites(Extras.getInstance().getSongId(getsongId()));
                        updateService(PLAYSTATE_CHANGED);
                    } else {
                        favHelper.addFavorite(Extras.getInstance().getSongId(getsongId()));
                        updateService(PLAYSTATE_CHANGED);
                    }
                }
            }
            return START_STICKY;
        } else {
            return START_NOT_STICKY;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBind");
        return musicXBinder;
    }


    @Override
    public boolean onUnbind(Intent intent) {
        saveState(true);
        Extras.getInstance().saveSeekServices(getPlayerPos());
        if (isPlaying() || playList.size() > 0) {
            return true;
        } else {
            stopSelf();
        }
        Log.d(TAG, "Unbind");
        return true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playList.size() == 0) {
            return;
        }
        playnext(false);
        if (playList.get(playList.size() - 1) != null) {
            if (permissionManager.isSystemAlertGranted(this)) {
                if (!Extras.getInstance().floatingWidget()) {
                    audioWidget.controller().stop();
                } else {
                    audioWidget.hide();
                }
            } else {
                Log.d(TAG, "Overlay Permission failed");
            }
            updateService(PLAYSTATE_CHANGED);
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
        if (permissionManager.isSystemAlertGranted(this)) {
            if (!Extras.getInstance().floatingWidget()) {
                audioWidget.controller().position(0);
                audioWidget.controller().duration(getDuration());
                trackingstop();
                trackingstart();
                widgetCover();
            } else {
                audioWidget.hide();
            }
        } else {
            Log.d(TAG, "Overlay Permission failed");
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

    private void updateVolume(int change) {
        volume = volume + change;
        if (volume < minVol) {
            volume = minVol;
        } else if (volume > maxVol) {
            volume = maxVol;
        }
        //convert to float value
        float fVolume = 1 - ((float) Math.log(maxVol - volume) / (float) Math.log(maxVol));
        //ensure fVolume within boundaries
        if (fVolume < floatMin) {
            fVolume = floatMin;
        } else if (fVolume > floatMax) {
            fVolume = floatMax;
        }
        //finally set volume
        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(fVolume, fVolume);
    }

    @Override
    public void play() {
        if (CurrentSong == null) {
            return;
        }
        if (returnpos() != -1) {
            if (Extras.getInstance().getFadeTrack()) {
                // volume
                if (fadeDurationValue() > 0) {
                    volume = minVol;
                } else {
                    volume = maxVol;
                }
                //update volume to zero
                updateVolume(0);
                //play
                finalplay();
                //fade
                if (fadeDurationValue() > 0) {
                    final Timer timer = new Timer(true);
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            updateVolume(1); // update volume
                            if (volume == maxVol) {
                                timer.cancel();
                                timer.purge();
                            }
                        }
                    };
                    // calculate delay, cannot be zero, set to 1 if zero
                    int delay = fadeDurationValue() / maxVol;
                    if (delay == 0) {
                        delay = 1;
                    }
                    timer.schedule(timerTask, delay, delay);
                }
            } else {
                finalplay();
            }
        }
    }

    public void finalplay() {
        if (!successfullyRetrievedAudioFocus()) {
            return;
        }
        Log.d(TAG, "Play");
        if (repeatMode != REPEAT_CURRENT && getDuration() > 2000 && getPlayerPos() >= getDuration() - 2000) {
            playnext(true);
        }
        MediaPlayerSingleton.getInstance().getMediaPlayer().start();
        if (permissionManager.isSystemAlertGranted(MusicXService.this)) {
            if (!Extras.getInstance().floatingWidget()) {
                if (!audioWidget.isShown()){
                    audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
                }
                audioWidget.controller().start();
                trackingstart();
            } else {
                audioWidget.hide();
            }
        } else {
            Log.d(TAG, "Overlay Permission failed");
        }
        CommonDatabase commonDatabase = new CommonDatabase(this, Constants.RecentlyPlayed_TableName);
        commonDatabase.add(CurrentSong);
        updateService(PLAYSTATE_CHANGED);
        paused = false;
        isPlaying = true;
    }

    private void widgetCover() {
        int size = getResources().getDimensionPixelSize(R.dimen.cover_size);
        if (ArtworkUtils.getAlbumCoverPath(MusicXService.this, getsongAlbumName()).exists()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(MusicXService.this)
                            .load(ArtworkUtils.getAlbumCoverPath(MusicXService.this, getsongAlbumName()))
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
                                    audioWidget.controller().albumCoverBitmap(ArtworkUtils.drawableToBitmap(errorDrawable));
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    audioWidget.controller().albumCoverBitmap(resource);
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
                                    audioWidget.controller().albumCoverBitmap(ArtworkUtils.drawableToBitmap(errorDrawable));
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    audioWidget.controller().albumCoverBitmap(resource);
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
            // volume
            if (fadeDurationValue() > 0) {
                volume = maxVol;
            } else {
                volume = minVol;
            }
            //update volume 0
            updateVolume(0);
            if (fadeDurationValue() > 0) {
                final Timer timer = new Timer(true);
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        //update volume
                        updateVolume(-1);
                        if (volume == minVol) {
                            //Pause music
                            finalPause();
                            timer.cancel();
                            timer.purge();
                        }
                    }
                };
                // calculate delay, cannot be zero, set to 1 if zero
                int delay = fadeDurationValue() / maxVol;
                if (delay == 0) {
                    delay = 1;
                }
                timer.schedule(timerTask, delay, delay);
            }
        } else {
            finalPause();
        }
    }

    private void finalPause() {
        MediaPlayerSingleton.getInstance().getMediaPlayer().pause();
        Log.d(TAG, "Pause");
        paused = true;
        isPlaying = false;
        updateService(PLAYSTATE_CHANGED);
        if (permissionManager.isSystemAlertGranted(MusicXService.this)) {
            if (!Extras.getInstance().floatingWidget()) {
                trackingstop();
                audioWidget.controller().pause();
            } else {
                audioWidget.hide();
            }
        } else {
            Log.d(TAG, "Overlay Permission failed");
        }
    }

    @Override
    public int getnextPos(boolean yorn) {
        if (repeatMode == REPEAT_CURRENT) {
            if (returnpos() < 0) {
                return 0;
            }
            return returnpos();
        } else {
            if (returnpos() >= playList.size() - 1) {
                if (repeatMode == NO_REPEAT && !yorn) {
                    return -1;
                } else if (repeatMode == REPEAT_ALL || yorn) {
                    return 0;
                }
                return -1;
            } else {
                return returnpos() + 1;
            }
        }
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
        if (returnpos() != -1 && yorn) {
            return returnpos() - 1;
        } else {
            return -1;
        }
    }

    @Override
    public void playnext(boolean torf) {
        int position = getnextPos(torf);
        if (position != -1 && position < playList.size()) {
            CurrentSong = playList.get(position);
            paused = false;
            fastplay = true;
            fastplay(true, CurrentSong);
            Log.d(TAG, "PlayNext");
            Extras.getInstance().saveSeekServices(0);
        } else {
            isPlaying = false;
            fastplay = false;
        }
    }

    @Override
    public void playprev(boolean torf) {
        int position = getprevPos(torf);
        if (position != -1 && position < playList.size()) {
            CurrentSong = playList.get(position);
            fastplay = true;
            fastplay(true, CurrentSong);
            paused = false;
            Log.d(TAG, "PlayPrev");
            Extras.getInstance().saveSeekServices(0);
        } else {
            isPlaying = false;
            fastplay = false;
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
            stopMediaplayer();
            if (permissionManager.isSystemAlertGranted(this)) {
                if (!Extras.getInstance().floatingWidget()) {
                    audioWidget.controller().stop();
                    audioWidget.controller().position(0);
                    audioWidget.hide();
                    trackingstop();
                } else {
                    audioWidget.hide();
                }
            } else {
                Log.d(TAG, "Overlay Permission failed");
            }
            if (!Extras.getInstance().hideNotify()) {
                removeNotification();
            }
            try {
                Extras.getInstance().getmPreferences().edit().remove(KEY_POSITION_X).apply();
                Extras.getInstance().getmPreferences().edit().remove(KEY_POSITION_Y).apply();
            } finally {
                Extras.getInstance().setwidgetPosition(100);
            }
            updateService(PLAYSTATE_CHANGED);
        }
    }

    @Override
    public void receiverCleanup() {
        if (controlReceiver != null) {
            unregisterReceiver(controlReceiver);
            controlReceiver = null;
        }
        if (headsetListener != null) {
            unregisterReceiver(headsetListener);
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
        if (isPlaying()) {
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
        Log.d(TAG, "ReturnDuration");
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() != null && returnpos() < playList.size()) {
            return trackDuration;
        }else {
            return -1;
        }
    }

    @Override
    public int getPlayerPos() {
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
                stopMediaplayer();
                fastplay(true, CurrentSong);
            } else {
                stopMediaplayer();
                fastplay(false, CurrentSong);
            }
        } else {
            Log.d(TAG, "null value");
        }
    }


    @Override
    public void trackingstart() {
        handler.post(runnable);
    }

    @Override
    public void trackingstop() {
        handler.removeCallbacks(runnable);
    }

    @Override
    public void startCurrentTrack(Song song) {
        if (MediaPlayerSingleton.getInstance().getMediaPlayer() == null) {
            return;
        }
        if (returnpos() != -1 && playList.size() > 0) {
            Uri dataLoader = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
            stopMediaplayer();
            try {
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
        if (permissionManager.isSystemAlertGranted(MusicXService.this)) {
            if (!Extras.getInstance().floatingWidget()) {
                audioWidget.controller().onControlsClickListener(null);
                audioWidget.controller().onWidgetStateChangedListener(null);
                audioWidget.hide();
                audioWidget.controller().position(0);
                audioWidget.controller().stop();
                audioWidget = null;
                trackingstop();
            } else {
                audioWidget.hide();
            }
        } else {
            Log.d(TAG, "Overlay Permission failed");
        }
        if (!Extras.getInstance().hideLockscreen()) {
            if (mediaSessionLockscreen != null) {
                mediaSessionLockscreen.release();
                mediaSessionLockscreen = null;
            }
        }
        if (permissionManager.isAudioRecordGranted(this)) {
            Equalizers.EndEq();
            BassBoosts.EndBass();
            Virtualizers.EndVirtual();
            Loud.EndLoudnessEnhancer();
            Reverb.EndReverb();
        } else {
            Log.d(TAG, "permission not granted");
        }
        if (!Extras.getInstance().hideNotify()) {
            removeNotification();
        }
        receiverCleanup();
        Extras.getInstance().eqSwitch(false);
        audioManager.abandonAudioFocus(this);
        stopMediaplayer();
    }


    public boolean isPaused() {
        return paused;
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
    public void headsetState() {
        IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetListener, receiverFilter);
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

        });
        mediaSessionLockscreen.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(this, MediaButtonReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSessionLockscreen.setMediaButtonReceiver(pendingIntent);
        mediaSessionLockscreen.setActive(true);
    }

    public void saveState(boolean yorno) {
        if (playList.size() > 0) {
            if (yorno) {
                CommonDatabase commonDatabase = new CommonDatabase(this, Constants.Queue_TableName);
                commonDatabase.removeAll();
                commonDatabase.add(playList);
                commonDatabase.close();
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
            CommonDatabase commonDatabase = new CommonDatabase(this, Constants.Queue_TableName);
            List<Song> queueList = commonDatabase.readLimit(-1, null);
            commonDatabase.close();
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
        saveState(QUEUE_CHANGED.equals(updateservices) || ITEM_ADDED.equals(updateservices) || ORDER_CHANGED.equals(updateservices));
        Intent intent = new Intent(updateservices);
        Bundle bundle = new Bundle();
        bundle.putString(SONG_TITLE, getsongTitle());
        bundle.putString(SONG_ALBUM, getsongAlbumName());
        bundle.putLong(SONG_ALBUM_ID, getsongAlbumID());
        bundle.putString(SONG_ARTIST, getsongArtistName());
        bundle.putLong(SONG_ID, getsongId());
        bundle.putString(SONG_PATH, getsongData());
        bundle.putInt(SONG_TRACK_NUMBER, getsongNumber());
        intent.putExtras(bundle);
        Log.d(TAG, "broadcast song metadata");
        sendBroadcast(intent);
        if (META_CHANGED.equals(updateservices) || PLAYSTATE_CHANGED.equals(updateservices)) {
            musicxWidget.notifyChange(this, updateservices);
            musicXwidget4x4.notifyChange(this, updateservices);
            musicXWidget5x5.notifyChange(this, updateservices);
            if (!Extras.getInstance().hideNotify()) {
                NotificationHandler.buildNotification(MusicXService.this, updateservices);
            }
            if (!Extras.getInstance().hideLockscreen()) {
                MediaSession.lockscreenMedia(getMediaSession(), MusicXService.this, updateservices);
            }
        }
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
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
        NotificationManagerCompat.from(this).cancel(NotificationHandler.notificationID);
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
                if (permissionManager.isSystemAlertGranted(MusicXService.this)) {
                    if (!Extras.getInstance().floatingWidget()) {
                        audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
                    } else {
                        audioWidget.hide();
                    }
                } else {
                    Log.d(TAG, "Overlay not detected");
                }
            } else if (intent.getAction().equals(ACTION_FAV)) {
                if (favHelper.isFavorite(Extras.getInstance().getSongId(getsongId()))) {
                    favHelper.removeFromFavorites(Extras.getInstance().getSongId(getsongId()));
                    updateService(PLAYSTATE_CHANGED);
                } else {
                    favHelper.addFavorite(Extras.getInstance().getSongId(getsongId()));
                    updateService(PLAYSTATE_CHANGED);
                }
            } else if (intent.getAction().equals(ACTION_COMMAND)) {
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                musicxWidget.musicxWidgetUpdate(MusicXService.this, appWidgetIds);
            } else if (intent.getAction().equals(ACTION_COMMAND1)) {
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                musicXwidget4x4.musicxWidgetUpdate(MusicXService.this, appWidgetIds);
            } else if (intent.getAction().equals(ACTION_COMMAND2)) {
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                musicXWidget5x5.musicxWidgetUpdate(MusicXService.this, appWidgetIds);
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
