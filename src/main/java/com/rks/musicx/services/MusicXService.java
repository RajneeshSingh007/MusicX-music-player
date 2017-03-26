package com.rks.musicx.services;

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
import static com.rks.musicx.misc.utils.Constants.CLOSE_EFFECTS;
import static com.rks.musicx.misc.utils.Constants.CURRENTPOS;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.OPEN_EFFECTS;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYINGSTATE;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.rks.musicx.data.eq.AudioEffects;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.Queue;
import com.rks.musicx.database.RecentlyPlayed;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.ui.activities.MainActivity;
import com.rks.musicx.ui.activities.MusicxWidget;
import com.rks.musicx.ui.activities.PlayingActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class MusicXService extends Service implements playInterface, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener,
    AudioWidget.OnControlsClickListener, AudioWidget.OnWidgetStateChangedListener {


  private static final long UPDATE_INTERVAL = 1000;
  private final int NO_REPEAT = 1;
  private final int REPEAT_ALL = 2;
  private final int REPEAT_CURRENT = 3;
  private final long msgDelay = 6000;
  private List<Song> playList = new ArrayList<>();
  private List<Song> ogList = new ArrayList<>();
  private Song CurrentSong;
  private AudioWidget audioWidget;
  private int playingIndex;
  private boolean paused;
  private Timer timer;
  private MusicXBinder musicXBinder = new MusicXBinder();
  private SharedPreferences saveData;
  private AudioManager audioManager;
  private boolean fastplay = false;
  private boolean isPlaying = false;
  private TelephonyManager telephonyManager;
  private boolean autoPhoneState = false;
  private boolean loosypaused;
  private int notificationID = 4;
  private MediaSessionCompat mediaSessionLockscreen;
  private int repeatMode = NO_REPEAT;
  private boolean isShuffled = false;
  private boolean isBound = false;
  private int startID, coloraccent;
  private String atkey, songTitle, songArtist;
  private long albumID, songID;

  /**
   * handle message
   */
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

  /**
   * AudioManager
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
            MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
          }
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

  /**
   * Phone state Listerner
   */
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

  /**
   * HeadsetListener
   */
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

  /**
   * Oncreate
   */
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
    } catch (Exception e) {
      Log.d("MusicXService", "initMedia_error", e);
    }
  }

  private void initAudioWidgetData() {
    audioWidget = new AudioWidget.Builder(this).build();
    audioWidget.controller().onControlsClickListener(this);
    audioWidget.controller().onWidgetStateChangedListener(this);
  }

  private void otherstuff() {
    saveData = PreferenceManager.getDefaultSharedPreferences(this);
    audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    autoPhoneState = saveData.getBoolean(PREF_AUTO_PAUSE, false);
    checkTelephonyState();
    headsetState();
    mediaLockscreen();
    restoreState();
    atkey = Helper.getATEKey(this);
    coloraccent = Config.accentColor(this, atkey);
    if (permissionManager.isAudioRecordGranted(this)) {
      Intent intent = new Intent(this, AudioEffects.class);
      intent.setAction(OPEN_EFFECTS);
      intent.putExtra(AUDIO_ID, MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
      sendBroadcast(intent);
    } else {
      Log.d(TAG, "permission not granted");
    }
  }

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
          if (permissionManager.isSystemAlertGranted(this)) {
            if (!Extras.getInstance().floatingWidget()) {
              audioWidget.show(Extras.getInstance().getwidgetPositionX(),
                  Extras.getInstance().getwidgetPositionY());
            } else {
              audioWidget.hide();
            }
          } else {
            Log.d(TAG, "Overlay not detected");
          }
          break;
        }
      }
    }
    return START_STICKY;
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
    if (permissionManager.isSystemAlertGranted(this)) {
      if (!Extras.getInstance().floatingWidget()) {
        audioWidget.controller().stop();
        audioWidget.controller().position(0);
      } else {
        audioWidget.hide();
      }
    } else {
      Log.d(TAG, "Overlay Permission failed");
    }
    if (playingIndex == -1) {
      if (permissionManager.isSystemAlertGranted(this)) {
        if (!Extras.getInstance().floatingWidget()) {
          audioWidget.controller().stop();
        } else {
          audioWidget.hide();
        }
      } else {
        Log.d(TAG, "Overlay Permission failed");
      }
    }
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {
    Log.d(TAG, String.valueOf(what) + String.valueOf(extra));
    return true;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {
    updateService(META_CHANGED);
    if (fastplay) {
      play();
      fastplay = false;
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
      if (!Extras.getInstance().hideNotify()) {
        removeNotification();
      }
      pause();
    }
  }

  @Override
  public void onWidgetPositionChanged(int cx, int cy) {
    Extras.getInstance().setwidgetPosition(cx);
  }

  @Override
  public void play() {
    int audiogain = audioManager.requestAudioFocus(audioListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    if (audiogain == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
      if (CurrentSong != null) {
        MediaPlayerSingleton.getInstance().getMediaPlayer().start();
        paused = false;
        isPlaying = true;
        updateService(PLAYSTATE_CHANGED);
        if (permissionManager.isSystemAlertGranted(this)) {
          if (!Extras.getInstance().floatingWidget()) {
            audioWidget.show(Extras.getInstance().getwidgetPositionX(), Extras.getInstance().getwidgetPositionY());
            audioWidget.controller().duration(getDuration());
            widgetCover(CurrentSong);
            trackingstart();
            audioWidget.controller().start();
          } else {
            audioWidget.hide();
          }
        } else {
          Log.d(TAG, "Overlay Permission failed");
        }
        RecentlyPlayed recentlyPlayed = new RecentlyPlayed(this);
        recentlyPlayed.add(CurrentSong);
      } else {
        Log.d(TAG, "Currentsong is null");
      }
    }
  }

  private void widgetCover(Song song) {
    int size = getResources().getDimensionPixelSize(R.dimen.cover_size);
    Glide.with(MusicXService.this)
        .load(ArtworkUtils.uri(song.getAlbumId()))
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .placeholder(R.mipmap.ic_launcher)
        .error(R.mipmap.ic_launcher)
        .format(DecodeFormat.PREFER_ARGB_8888)
        .override(size, size)
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
          public void onResourceReady(Bitmap resource,
              GlideAnimation<? super Bitmap> glideAnimation) {
            audioWidget.controller().albumCoverBitmap(resource);
          }

          @Override
          public void onLoadCleared(Drawable placeholder) {

          }

          @Override
          public void getSize(SizeReadyCallback cb) {

          }

          @Override
          public Request getRequest() {
            return null;
          }

          @Override
          public void setRequest(Request request) {

          }
        });
  }

  @Override
  public void pause() {
    if (CurrentSong != null) {
      MediaPlayerSingleton.getInstance().getMediaPlayer().pause();
      paused = true;
      isPlaying = false;
      updateService(PLAYSTATE_CHANGED);
      if (permissionManager.isSystemAlertGranted(this)) {
        if (!Extras.getInstance().floatingWidget()) {
          trackingstop();
          audioWidget.controller().pause();
        } else {
          audioWidget.hide();
        }
      } else {
        Log.d(TAG, "Overlay Permission failed");
      }
    } else {
      Log.d(TAG, "CurrentSong is null");
    }
  }

  @Override
  public int getnextPos(boolean yorn) {
    if (repeatMode == REPEAT_CURRENT && !yorn) {
      return returnpos();
    }
    if (returnpos() + 1 > playList.size()) {
      if (repeatMode == REPEAT_ALL) {
        return 0;
      }

      return -1;
    }
    return returnpos() + 1;
  }

  @Override
  public int getprevPos(boolean yorn) {
    if ((repeatMode == REPEAT_CURRENT && !yorn) || (isPlaying() && getPlayerPos() >= 1500)) {
      return returnpos();
    }
    if (returnpos() - 1 < 0) {
      if (repeatMode == REPEAT_ALL) {
        return playList.size() - 1;
      }
      return -1;
    }
    return returnpos() - 1;
  }

  @Override
  public void playnext(boolean torf) {
    int position = getnextPos(torf);
    if (position != -1 && position < playList.size()) {
      CurrentSong = playList.get(position);
      paused = false;
      fastplay = true;
      fastplay(true, CurrentSong);
    } else {
      isPlaying = false;
      paused = true;
      updateService(PLAYSTATE_CHANGED);
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
  public int returnpos() {
    return playList.indexOf(CurrentSong) != -1 ? playList.indexOf(CurrentSong) : -1;
  }

  @Override
  public void refreshWidget() {
    AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
    int updateId[] = widgetManager.getAppWidgetIds(new ComponentName(this, MusicxWidget.class));
    if (updateId.length != -1 && playingIndex != -1) {
      MusicxWidget.musicxWidget(updateId, MusicXService.this);
    }
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
        playList.clear();
        playList.addAll(ogList);
      }
      updateService(ORDER_CHANGED);
    }
  }

  @Override
  public void shuffle() {
    Random rand = new Random();
    rand.setSeed(System.currentTimeMillis());
    Collections.shuffle(playList, rand);
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
    if (isPlaying() && playingIndex != -1 || playList.size() > 0 && paused) {
      return MediaPlayerSingleton.getInstance().getMediaPlayer().getDuration();
    }
    return 0;
  }

  @Override
  public int getPlayerPos() {
    if (isPlaying() && playingIndex != -1 || playList.size() > 0 && paused) {
      return MediaPlayerSingleton.getInstance().getMediaPlayer().getCurrentPosition();
    }
    return 0;
  }

  @Override
  public void seekto(int seek) {
    if (isPlaying() && playingIndex != -1 || playList.size() > 0 && paused) {
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
    playingIndex = pos;
    if (pos != -1) {
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
    timer = new Timer(TAG);
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        AudioWidget audioWidgets = audioWidget;
        if (audioWidgets != null && isPlaying() && playingIndex != -1) {
          audioWidget.controller()
              .position(MediaPlayerSingleton.getInstance().getMediaPlayer().getCurrentPosition());
        }
      }
    }, UPDATE_INTERVAL, UPDATE_INTERVAL);
  }

  @Override
  public void trackingstop() {
    if (timer == null) {
      return;
    }
    timer.cancel();
    timer.purge();
    timer = null;
  }

  @Override
  public void startCurrentTrack(Song song) {
    if (returnpos() != -1) {
      MediaPlayerSingleton.getInstance().getMediaPlayer().reset();
      Uri dataLoader = ContentUris
          .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.getId());
      try {
        MediaPlayerSingleton.getInstance().getMediaPlayer()
            .setDataSource(getApplicationContext(), dataLoader);
        MediaPlayerSingleton.getInstance().getMediaPlayer().prepareAsync();
      } catch (IOException e) {
        e.printStackTrace();
        stopSelf();
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
      setdataPos(0, true);
    }
  }

  @Override
  public void addToQueue(Song song) {
    if (playList != null && playingIndex != -1) {
      ogList.add(song);
      playList.add(song);
      updateService(ITEM_ADDED);
    }
  }

  @Override
  public void setAsNextTrack(Song song) {
    if (playList != null && playingIndex != -1) {
      ogList.add(song);
      playList.add(playingIndex + 1, song);
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
    if (permissionManager.isSystemAlertGranted(this)) {
      if (!Extras.getInstance().floatingWidget()) {
        audioWidget.controller().onControlsClickListener(null);
        audioWidget.controller().onWidgetStateChangedListener(null);
        audioWidget.hide();
        audioWidget.controller().albumCover(null);
        audioWidget.controller().position(0);
        audioWidget = null;
        trackingstop();
      }else {
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
    unregisterReceiver(headsetListener);
    if (telephonyManager != null) {
      telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
    audioManager.abandonAudioFocus(audioListener);
    if (permissionManager.isAudioRecordGranted(this)) {
      Intent intent = new Intent(this, AudioEffects.class);
      intent.setAction(CLOSE_EFFECTS);
      sendBroadcast(intent);
    } else {
      Log.d(TAG, "permission not granted");
    }
    if (!Extras.getInstance().hideNotify()) {
      removeNotification();
    }
    MediaPlayerSingleton.getInstance().getMediaPlayer().reset();
    if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
      MediaPlayerSingleton.getInstance().getMediaPlayer().stop();
    }
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
    if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
      MediaPlayerSingleton.getInstance().getMediaPlayer().stop();
    }
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
        if (!Extras.getInstance().hideNotify()) {
          removeNotification();
        }
        stopSelf();
      }

      @Override
      public void onSeekTo(long pos) {
        seekto((int) pos);
      }
    });
  }

  @Override
  public void updatemediaLockscreen(String update) {

    if (!mediaSessionLockscreen.isActive()) {
      mediaSessionLockscreen.setActive(true);
    }

    if (update.equals(PLAYSTATE_CHANGED)) {

      int playState =
          isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED;
      mediaSessionLockscreen.setPlaybackState(new PlaybackStateCompat.Builder()
          .setState(playState, getPlayerPos(), 1.0F)
          .setActions(PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE
              | PlaybackStateCompat.ACTION_PLAY_PAUSE |
              PlaybackStateCompat.ACTION_SKIP_TO_NEXT | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS)
          .build());
    }
    if (update.equals(META_CHANGED)) {
      MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
          .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, getsongArtistName())
          .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, getsongAlbumName())
          .putString(MediaMetadataCompat.METADATA_KEY_TITLE, getsongTitle())
          .putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
              MediaPlayerSingleton.getInstance().getMediaPlayer().getDuration());

      ArtworkUtils
          .ArtworkLoaderBitmapPalette(this, getsongTitle(), getsongAlbumID(), new palette() {
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
      Log.d(TAG, "SavingData");
      editor.putInt(CURRENTPOS, playingIndex);
      editor.putInt(REPEATMODE, repeatMode);
      editor.putBoolean(SHUFFLEMODE, isShuffled);
      editor.putString(SONG_TITLE, songTitle);
      editor.putString(SONG_ARTIST, songArtist);
      editor.putLong(SONG_ID, songID);
      editor.putLong(SONG_ALBUM_ID, albumID);
      editor.apply();
    }
  }

  private void restoreState() {
    if (saveData.getBoolean(PLAYINGSTATE, false)) {
      Queue queue = new Queue(this);
      List<Song> queueList = queue.read();
      queue.close();
      int restorepos = saveData.getInt(CURRENTPOS, playingIndex);
      repeatMode = saveData.getInt(REPEATMODE, repeatMode);
      isShuffled = saveData.getBoolean(SHUFFLEMODE, isShuffled);
      String songname = saveData.getString(SONG_TITLE, songTitle);
      String songartist = saveData.getString(SONG_ARTIST, songArtist);
      long albumId = saveData.getLong(SONG_ALBUM_ID, albumID);
      long songId = saveData.getLong(SONG_ID, songID);
      if (restorepos != -1 && queueList.size() > 0 || !isPlaying() && isPaused()) {
        smartplaylist(queueList);
        setdataPos(restorepos, false);
        setSongTitle(songname);
        setSongArtist(songartist);
        setAlbumID(albumId);
        setSongID(songId);
        Log.d(TAG, "restoring data");
      } else {
        Log.d(TAG, "Failed to restore data");
      }
    }
  }

  @Override
  public void updateService(String updateservices) {
    if (!Extras.getInstance().hideLockscreen()) {
      updatemediaLockscreen(updateservices);
    }
    saveState(
        QUEUE_CHANGED.equals(updateservices) || ITEM_ADDED.equals(updateservices) || ORDER_CHANGED
            .equals(updateservices));
    if (PLAYSTATE_CHANGED.equals(updateservices) || META_CHANGED.equals(updateservices)) {
      if (CurrentSong == null || playingIndex == -1){
        removeNotification();
      }
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
      Log.d(TAG, "broadcast song metadata");
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
    refreshWidget();
  }

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

  public void setCurrentSong(Song currentSong) {
    CurrentSong = currentSong;
  }

  @Override
  public String getsongTitle() {
    if (CurrentSong != null) {
      return CurrentSong.getTitle();
    }
    return null;
  }

  @Override
  public long getsongId() {
    if (CurrentSong != null) {
      return CurrentSong.getId();
    }
    return 0;
  }

  @Override
  public String getsongAlbumName() {
    if (CurrentSong != null) {
      return CurrentSong.getAlbum();
    }
    return null;
  }

  @Override
  public String getsongArtistName() {
    if (CurrentSong != null) {
      return CurrentSong.getArtist();
    }
    return null;
  }

  @Override
  public String getsongData() {
    if (CurrentSong != null) {
      return CurrentSong.getmSongPath();
    }
    return null;
  }

  @Override
  public long getsongAlbumID() {
    if (CurrentSong != null) {
      return CurrentSong.getAlbumId();
    }
    return 0;
  }

  @Override
  public int getsongNumber() {
    if (CurrentSong != null) {
      return CurrentSong.getTrackNumber();
    }
    return 0;
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

  public MediaSessionCompat getMediaSession() {
    return mediaSessionLockscreen;
  }

  @Override
  public void buildNotification() {
    if (!Extras.getInstance().hideNotify()) {
      NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
      PendingIntent nextIntent = PendingIntent
          .getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_NEXT), 0);
      PendingIntent previousIntent = PendingIntent
          .getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_PREVIOUS), 0);
      PendingIntent toggleIntent = PendingIntent
          .getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_TOGGLE), 0);
      PendingIntent stopIntent = PendingIntent
          .getService(this, 0, new Intent(this, MusicXService.class).setAction(ACTION_STOP), 0);
      Intent intent = new Intent(this, PlayingActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      PendingIntent pendInt = PendingIntent
          .getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      builder.addAction(R.drawable.aw_ic_prev, "", previousIntent);
      if (isPlaying()) {
        builder.addAction(R.drawable.aw_ic_pause, "", toggleIntent);
      } else {
        builder.addAction(R.drawable.aw_ic_play, "", toggleIntent);
      }
      builder.addAction(R.drawable.aw_ic_next, "", nextIntent);
      builder
          .setWhen(System.currentTimeMillis())
          .setCategory(Intent.CATEGORY_APP_MUSIC)
          .setPriority(Notification.PRIORITY_DEFAULT)
          .setContentIntent(pendInt)
          .setShowWhen(false)
          .setSmallIcon(R.mipmap.ic_launcher)
          .setContentTitle(getsongTitle())
          .setContentText(getsongArtistName())
          .setStyle(new NotificationCompat.MediaStyle()
              .setMediaSession(getMediaSession().getSessionToken())
              .setShowActionsInCompactView(0, 1, 2));
      ArtworkUtils.ArtworkLoaderBitmapPalette(MusicXService.this, getsongTitle(), getsongAlbumID(),
          new palette() {
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
      ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
          .notify(notificationID, builder.build());
    }

  }

    /*
    *Notification _/\_
    */

  private void removeNotification() {
    NotificationManager notificationManager = (NotificationManager) getSystemService(
        Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(notificationID);
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
