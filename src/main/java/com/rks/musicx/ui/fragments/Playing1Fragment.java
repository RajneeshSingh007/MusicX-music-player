package com.rks.musicx.ui.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.cleveroad.audiowidget.SmallBang;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.database.Queue;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.BlurArtwork;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.rks.musicx.misc.widgets.CircularSeekBar;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Playing1Fragment extends BaseFragment implements SimpleItemTouchHelperCallback.OnStartDragListener, ImageChooserListener {


    private String finalPath;
    private ChosenImage chosenImages;
    private FloatingActionButton playpausebutton;
    private String ateKey;
    private int accentColor, position;
    private TextView SongArtist, SongTitle, CurrentDur, TotalDur, Divider;
    private TextView lrcView;
    private CircleImageView mAlbumCoverView;
    private View Playing3view;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private CircularSeekBar mSeekBar;
    private ImageButton favButton, share, moreMenu, eqButton;
    private SmallBang mSmallBang;
    private FavHelper favhelper;
    private ImageView blur_artowrk, repeatButton, shuffleButton;
    private Handler mHandler = new Handler();
    private ViewPager Pager;
    private PlayingPagerAdapter PlayingPagerAdapter;
    private List<View> Playing3PagerDetails;
    private ItemTouchHelper mItemTouchHelper;
    private SlidingPaneLayout slidingpanelayout;
    private List<Song> queueList;
    private boolean isalbumArtChanged;
    private View coverView;
    private Helper helper;
    private ImageChooserManager imageChooserManager;
    private String mediaPath;
    private final ScheduledExecutorService mExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> mScheduleFuture;

    private Runnable seekbarRunnable = new Runnable() {
        @Override
        public void run() {
            updateCircularSeekBar();
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (musicXService == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.play_pause_toggle:
                    musicXService.toggle();
                    break;
                case R.id.shuffle_song:
                    boolean shuffle = musicXService.isShuffleEnabled();
                    musicXService.setShuffleEnabled(!shuffle);
                    updateShuffleButton();
                    break;
                case R.id.repeat_song:
                    int mode = musicXService.getNextrepeatMode();
                    musicXService.setRepeatMode(mode);
                    updateRepeatButton();
                    break;
                case R.id.action_favorite:
                    ImageButton button = (ImageButton) v;
                    if (favhelper.isFavorite(musicXService.getsongId())) {
                        favhelper.removeFromFavorites(musicXService.getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                    } else {
                        favhelper.addFavorite(musicXService.getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(v);
                    }
                    break;
                case R.id.action_share:
                    Helper.shareMusic(musicXService.getsongData(), getContext());
                    break;
                case R.id.menu_button:
                    ShowMoreMenu(v);
                    break;
                case R.id.eq_button:
                    Intent i = new Intent(getActivity(), EqualizerActivity.class);
                    getActivity().startActivity(i);
                    break;

            }

        }
    };
    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            switch (view.getId()) {
                case R.id.item_view:
                    musicXService.setdataPos(position, true);
                    setSelection(position);
                    break;
                case R.id.menu_button:
                    ShowMoreQueueMenu(view, position);
                    break;
            }
        }
    };

    private void ShowMoreMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.playing_menu, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.action_share).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_eq).setVisible(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.clear_queue:
                        if (queueAdapter.getSnapshot().size() > 0){
                            queueAdapter.clear();
                            queueAdapter.notifyDataSetChanged();
                            musicXService.clearQueue();
                            Queue queue = new Queue(getContext());
                            queue.removeAll();
                            Toast.makeText(getContext(), "Cleared Queue", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.action_changeArt:
                        pickNupdateArtwork();
                        isalbumArtChanged = false;
                        break;
                    case R.id.action_playlist:
                        new Helper(getContext()).PlaylistChooser(Playing1Fragment.this, getContext(), musicXService.getsongId());
                        break;
                    case R.id.action_lyrics:
                        new Helper(getContext()).searchLyrics(getContext(), musicXService.getsongTitle(), musicXService.getsongArtistName(), lrcView);
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), musicXService.getsongData());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), musicXService.getsongTitle(), musicXService.getsongAlbumName(), musicXService.getsongArtistName(), musicXService.getsongNumber(), musicXService.getsongData());
                        break;

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void pickNupdateArtwork() {
        imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            mediaPath = imageChooserManager.choose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), requestCode + "");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (imageChooserManager == null) {
                imageChooserManager = new ImageChooserManager(this, requestCode, true);
                imageChooserManager.setImageChooserListener(this);
                imageChooserManager.reinitialize(mediaPath);
            }
            imageChooserManager.submit(requestCode, data);
        }
    }

    private void ShowMoreQueueMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.playing_menu, popupMenu.getMenu());
        Song queue = queueAdapter.getItem(position);
        popupMenu.getMenu().findItem(R.id.action_lyrics).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_eq).setVisible(false);
        popupMenu.getMenu().findItem(R.id.clear_queue).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_changeArt).setVisible(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playlist:
                        new Helper(getContext()).PlaylistChooser(Playing1Fragment.this, getContext(), queue.getId());
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), queue.getmSongPath());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), queue.getTitle(), queue.getAlbum(), queue.getArtist(), queue.getTrackNumber(), queue.getmSongPath());
                        break;
                    case R.id.action_share:
                        Helper.shareMusic(musicXService.getsongData(), getContext());
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playing1, container, false);
        findbyidPart(rootView);
        working();
        return rootView;
    }

    private void findbyidPart(View rootView) {
        SongTitle = (TextView) rootView.findViewById(R.id.song_title);
        SongArtist = (TextView) rootView.findViewById(R.id.song_artist);
        Playing3view = rootView.findViewById(R.id.Playing3view);
        favButton = (ImageButton) rootView.findViewById(R.id.action_favorite);
        share = (ImageButton) rootView.findViewById(R.id.action_share);
        blur_artowrk = (ImageView) rootView.findViewById(R.id.blur_artwork);
        shuffleButton = (ImageView) rootView.findViewById(R.id.shuffle_song);
        repeatButton = (ImageView) rootView.findViewById(R.id.repeat_song);
        moreMenu = (ImageButton) rootView.findViewById(R.id.menu_button);
        queuerv = (RecyclerView) rootView.findViewById(R.id.commonrv);
        Pager = (ViewPager) rootView.findViewById(R.id.pagerPlaying3);
        eqButton = (ImageButton) rootView.findViewById(R.id.eq_button);
        slidingpanelayout = (SlidingPaneLayout) rootView.findViewById(R.id.slidingpanelayout);


        coverView = LayoutInflater.from(getContext()).inflate(R.layout.playing1_coverview, null);
        View lyricsView = LayoutInflater.from(getContext()).inflate(R.layout.lyricsview, null);

        /**
         * Album,playpause View
         */
        playpausebutton = (FloatingActionButton) coverView.findViewById(R.id.play_pause_toggle);
        playpausebutton.setOnClickListener(mOnClickListener);
        mAlbumCoverView = (CircleImageView) coverView.findViewById(R.id.album_cover);
        mSeekBar = (CircularSeekBar) coverView.findViewById(R.id.circular_seekbar);
        CurrentDur = (TextView) coverView.findViewById(R.id.currentDur);
        TotalDur = (TextView) coverView.findViewById(R.id.totalDur);
        Divider = (TextView) coverView.findViewById(R.id.divider);

        /**
         * Lyrics View
         */
        lrcView = (TextView) lyricsView.findViewById(R.id.lyrics);

        /**
         * Pager config
         */
        Playing3PagerDetails = new ArrayList<>(2);
        Playing3PagerDetails.add(coverView);
        Playing3PagerDetails.add(lyricsView);
        PlayingPagerAdapter = new PlayingPagerAdapter(Playing3PagerDetails);
        Pager.setAdapter(PlayingPagerAdapter);
        /**
       * Swipe Listerner
       */
      final GestureDetector gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
        private static final int SWIPE_THRESHOLD = 200;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
          return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
          return super.onSingleTapUp(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {

          return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
          super.onLongPress(e);
        }

        // Determines the fling velocity and then fires the appropriate swipe event accordingly
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
          try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
              if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                  Log.d("Aloha !!!", "no left swipe..");
                } else {
                  Log.d("Aloha !!!", "no right swipe..");
                }
              }
            } else {
              if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                  if (musicXService.isPlaying()) {
                    musicXService.playprev(true);
                    Log.d("Aloha !!!", "Down swipe..");
                  }
                } else {
                  if (musicXService.isPlaying()) {
                    musicXService.playnext(true);
                    Log.d("Aloha !!!", "Up swipe..");
                  }
                }
              }
            }
          } catch (Exception exception) {
            exception.printStackTrace();
          }
          return true;
        }
      });
        coverView.setOnTouchListener((v, event) -> {
            gesture.onTouchEvent(event);
            return true;
        });

        /**
         * SlidingPanel
         */
        if (!slidingpanelayout.isOpen()) {
            slidingpanelayout.closePane();
        } else {
            slidingpanelayout.openPane();
        }
        slidingpanelayout.setSliderFadeColor(ContextCompat.getColor(getContext(), R.color.text_transparent));
        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "200");
        sequence.setConfig(config);
        sequence.addSequenceItem(coverView, "Swipe up/down to play Next/Prev song on PlayingView", "GOT IT");
        sequence.addSequenceItem(Pager, "Slide right/left to view Lyrics/PlayingView", "GOT IT");
        sequence.addSequenceItem(slidingpanelayout, "slide right/tap to view QueueView", "GOT IT");
        sequence.addSequenceItem(queuerv, "Drag ,Drop to change queue, Slide right to remove song", "GOT IT");
        sequence.start();
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                config.setDelay(1000);
            }
        });
    }

    public void working() {
        ateKey = Helper.getATEKey(getContext());
        accentColor = Config.accentColor(getContext(), ateKey);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        favButton.setOnClickListener(mOnClickListener);
        favhelper = new FavHelper(getActivity());
        share.setOnClickListener(mOnClickListener);
        eqButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.eq));
        eqButton.setOnClickListener(mOnClickListener);
        share.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shares));
        mSmallBang = SmallBang.attach2Window(getActivity());
        shuffleButton.setOnClickListener(mOnClickListener);
        shuffleButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        repeatButton.setOnClickListener(mOnClickListener);
        repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        moreMenu.setOnClickListener(mOnClickListener);
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        CustomLayoutManager customlayoutmanager = new CustomLayoutManager(getActivity());
        customlayoutmanager.setOrientation(LinearLayoutManager.VERTICAL);
        customlayoutmanager.setSmoothScrollbarEnabled(true);
        queuerv.setLayoutManager(customlayoutmanager);
        queuerv.addItemDecoration(new DividerItemDecoration(getContext(), 75, true));
        queuerv.setHasFixedSize(true);
        queueAdapter = new QueueAdapter(getContext(), this);
        queueAdapter.setLayoutId(R.layout.song_list);
        queuerv.setAdapter(queueAdapter);
        queueAdapter.setOnItemClickListener(mOnClick);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(queuerv);
        position = 0;
        slidingpanelayout.setSliderFadeColor(Color.TRANSPARENT);
        slidingpanelayout.setCoveredFadeColor(Color.TRANSPARENT);
        Playing3view.setBackgroundColor(accentColor);
        getActivity().getWindow().setStatusBarColor(accentColor);
        helper = new Helper(getContext());

    }

    private void updateQueue(String acting) {
        if (musicXService == null) {
            return;
        }
        queueList = musicXService.getPlayList();
        if (queueList != queueAdapter.getSnapshot() && queueList.size() > 0) {
            queueAdapter.addDataList(queueList);
        }
        queueAdapter.notifyDataSetChanged();
        setSelection(musicXService.returnpos());
    }

    public void setSelection(int position) {

        queueAdapter.setSelection(position);

        if (position >= 0 && position < queueAdapter.getSnapshot().size()) {
            queuerv.scrollToPosition(position);
        }

        int newselection;

        newselection = position;

        if (newselection >= 0 && position < queueAdapter.getSnapshot().size()) {
            queueAdapter.notifyItemChanged(newselection);
            queueAdapter.notifyDataSetChanged();
        }
        queuerv.scrollToPosition(position);
    }

    public void like(View view) {
        favButton.setImageResource(R.drawable.ic_action_favorite);
        mSmallBang.bang(view);
        mSmallBang.setmListener(new SmallBang.SmallBangListener() {
            @Override
            public void onAnimationStart() {
            }

            @Override
            public void onAnimationEnd() {
            }
        });
    }

    public String durationCalculator(long id) {
        return String.format(Locale.getDefault(), "%d:%02d", id / 60000,
                (id % 60000) / 1000);
    }

    private void updateCircularSeekBar() {
        if (musicXService != null) {
            position = musicXService.getPlayerPos();
            mSeekBar.setProgress(position);
            CurrentDur.setText(durationCalculator(position));
        }

    }

    @Override
    protected void reload() {
        PlayingView();
        setButtonDrawable();
        updateShuffleButton();
        updateRepeatButton();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(finalPath);
            isalbumArtChanged = true;
        }
        if (musicXService.isPlaying()) {
            scheduleSeekbarUpdate();
        }
    }

    @Override
    protected void playbackConfig() {
        setButtonDrawable();
    }

    @Override
    protected void metaConfig() {
        PlayingView();
        if (musicXService.isPlaying()) {
            scheduleSeekbarUpdate();
        }
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(finalPath);
            isalbumArtChanged = true;
        }
    }

    @Override
    protected void queueConfig(String action) {
        updateQueue(action);
    }

    @Override
    protected void onPaused() {
    }

    private void ChangeAlbumCover(String finalPath) {
        if (musicXService != null) {
            if (chosenImages != null) {
                new updateAlbumArt(finalPath).execute();
            }
        }
    }

    private void coverArtView() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArtworkUtils.ArtworkLoader(getContext(), musicXService.getsongTitle(), musicXService.getsongAlbumID(), mAlbumCoverView);
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), musicXService.getsongTitle(), musicXService.getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        Playing3view.setBackgroundColor(colors[0]);
                        mAlbumCoverView.setBorderColor(colors[0]);
                        new Helper(getContext()).animateViews(Playing3view, colors[0]);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        }
                        if (Extras.getInstance().artworkColor()){
                            colorMode(colors[0]);
                        }else {
                            colorMode(accentColor);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        Playing3view.setBackgroundColor(accentColor);
                        new BlurArtwork(getContext(), 25, bitmap, blur_artowrk).execute("BlurredArtwork");
                    }
                });
            }
        });
        isalbumArtChanged = true;
    }

    private void PlayingView() {
        if (musicXService != null) {
            String title = musicXService.getsongTitle();
            String artist = musicXService.getsongArtistName();
            isalbumArtChanged = true;
            SongTitle.setText(title);
            SongTitle.setSelected(true);
            SongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            SongArtist.setText(artist);
            Helper.rotationAnim(mAlbumCoverView);
            mSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                    if (fromUser && musicXService != null && (musicXService.isPlaying() || musicXService.isPaused())) {
                        musicXService.seekto(circularSeekBar.getProgress());
                    }
                }

                @Override
                public void onStopTrackingTouch(CircularSeekBar seekBar) {
                    scheduleSeekbarUpdate();
                }

                @Override
                public void onStartTrackingTouch(CircularSeekBar seekBar) {
                    stopSeekbarUpdate();
                }
            });
            int duration = musicXService.getDuration();
            if (duration != -1) {
                mSeekBar.setMax(duration);
                TotalDur.setText(durationCalculator(duration));
            }
            if (favhelper.isFavorite(musicXService.getsongId())) {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite);
                }
            } else {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite_outline);
                }
            }
            new Helper(getContext()).LoadLyrics(musicXService.getsongTitle(), musicXService.getsongArtistName(), lrcView);
            updateQueue("Executed");
        }
    }

    private void scheduleSeekbarUpdate() {
        stopSeekbarUpdate();
        if (!mExecutorService.isShutdown()) {
            mScheduleFuture = mExecutorService.scheduleAtFixedRate(
                    new Runnable() {
                        @Override
                        public void run() {
                            mHandler.post(seekbarRunnable);
                        }
                    }, 100,
                    1000, TimeUnit.MILLISECONDS);
        }
    }

    private void stopSeekbarUpdate() {
        if (mScheduleFuture != null) {
            mScheduleFuture.cancel(false);
        }
    }
    private void colorMode(int color){
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            mSeekBar.setCircleProgressColor(color);
            mSeekBar.setPointerColor(color);
            mSeekBar.setPointerHaloColor(color);
            getActivity().getWindow().setNavigationBarColor(color);
            playpausebutton.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            mSeekBar.setCircleProgressColor(color);
            mSeekBar.setPointerColor(color);
            mSeekBar.setPointerHaloColor(color);
            getActivity().getWindow().setNavigationBarColor(color);
            playpausebutton.setBackgroundTintList(ColorStateList.valueOf(color));
        }

    }

    private void updateShuffleButton() {
        boolean shuffle = musicXService.isShuffleEnabled();
        if (shuffle) {
            shuffleButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_on));
        } else {
            shuffleButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        }
    }

    private void updateRepeatButton() {
        int mode = musicXService.getRepeatMode();
        if (mode == musicXService.getNoRepeat()){
            repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        }else if (mode == musicXService.getRepeatCurrent()){
            repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_all));
        }else if (mode == musicXService.getRepeatAll()){
            repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_one));
        }
    }

    private void setButtonDrawable() {
        if (musicXService != null) {
            if (musicXService.isPlaying()) {
                playpausebutton.setImageResource(R.drawable.aw_ic_pause);
            } else {
                playpausebutton.setImageResource(R.drawable.aw_ic_play);
            }
        }

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void onImageChosen(ChosenImage chosenImage) {
        chosenImages = chosenImage;
        finalPath = chosenImages.getFilePathOriginal();
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                ChangeAlbumCover(finalPath);
            }
        });
    }

    @Override
    public void onError(String s) {

    }

    @Override
    public void onImagesChosen(ChosenImages chosenImages) {

    }

    public class updateAlbumArt extends AsyncTask<Void, Void, Void> {

        private Uri albumCover;
        private ContentValues values;
        private String path;

        public updateAlbumArt(String path) {
            this.path = path;
        }

        @Override
        protected Void doInBackground(Void... params) {
            albumCover = Uri.parse("content://media/external/audio/albumart");
            try {
                getContext().getContentResolver().delete(ContentUris.withAppendedId(albumCover, musicXService.getsongAlbumID()), null, null);
                values = new ContentValues();
                values.put("album_id", musicXService.getsongAlbumID());
                values.put("_data", path);
            } catch (Exception e) {
                Log.d("playing", "error", e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            Uri newUri = Playing1Fragment.this.getContext().getContentResolver().insert(albumCover, values);
            if (newUri != null) {
                File file = new File("content://media/external/audio/albumart");
                Toast.makeText(getContext(), "AlbumArt Changed", Toast.LENGTH_LONG).show();
                Log.d("updateAlbumCover", "success hurray !!!");
                getContext().getContentResolver().notifyChange(albumCover, null);
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                ArtworkUtils.ArtworkLoader(getContext(), musicXService.getsongTitle(), path, mAlbumCoverView);
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), musicXService.getsongTitle(), path, new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        Playing3view.setBackgroundColor(colors[0]);
                        mAlbumCoverView.setBorderColor(colors[0]);
                        new Helper(getContext()).animateViews(Playing3view, colors[0]);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        }
                        if (Extras.getInstance().artworkColor()){
                            colorMode(colors[0]);
                        }else {
                            colorMode(accentColor);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        Playing3view.setBackgroundColor(accentColor);
                        new BlurArtwork(getContext(), 25, bitmap, blur_artowrk).execute("BlurredArtwork");
                    }
                });
                queueAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "AlbumArt Failed", Toast.LENGTH_LONG).show();
                Log.d("updateAlbumCover", "failed lol !!!");
            }
        }
    }
}



