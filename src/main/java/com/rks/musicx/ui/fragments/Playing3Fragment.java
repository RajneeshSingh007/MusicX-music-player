package com.rks.musicx.ui.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.ATE;
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
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.DiagonalLayout;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.io.File;
import java.util.List;
import java.util.Locale;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class Playing3Fragment extends BaseFragment implements SimpleItemTouchHelperCallback.OnStartDragListener, ImageChooserListener {

    String finalPath;
    ChosenImage chosenImages;
    private TextView songArtist, songTitle, currentDur, totalDur;
    private String ateKey;
    private int accentColor;
    private TextView lrcView;
    private SeekBar seekbar;
    private Handler handler = new Handler();
    private List<Song> queueList;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ImageButton favButton, moreMenu;
    private SmallBang mSmallBang;
    private FavHelper favhelper;
    private ImageView albumArt, repeatButton, shuffleButton, playpausebutton, next, prev;
    private boolean isalbumArtChanged;
    private BottomSheetBehavior bottomSheetBehavior;
    private FrameLayout bottomsheetLyrics;
    private DiagonalLayout diagonalLayout;
    private ImageChooserManager imageChooserManager;
    private String mediaPath;

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (musicXService == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.play_pause_toggle:
                    musicXService.toggle();
                    break;
                case R.id.action_favorite:
                    ImageButton button = (ImageButton) view;
                    if (favhelper.isFavorite(musicXService.getsongId())) {
                        favhelper.removeFromFavorites(musicXService.getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                    } else {
                        favhelper.addFavorite(musicXService.getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(view);
                    }
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
                case R.id.menu_button:
                    ShowMoreMenu(view);
                    break;
                case R.id.next:
                    musicXService.playnext(true);
                    break;
                case R.id.prev:
                    musicXService.playprev(true);
                    break;
            }
        }
    };
    private Runnable seekbarrunnable = new Runnable() {
        @Override
        public void run() {
            updateSeekbar();
            handler.postDelayed(seekbarrunnable, 200);
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_playing3, container, false);
        ui(rootview);
        function();
        return rootview;
    }

    private void ui(View rootview) {
        albumArt = (ImageView) rootview.findViewById(R.id.albumArt);
        queuerv = (RecyclerView) rootview.findViewById(R.id.commonrv);
        favButton = (ImageButton) rootview.findViewById(R.id.action_favorite);
        moreMenu = (ImageButton) rootview.findViewById(R.id.menu_button);
        shuffleButton = (ImageView) rootview.findViewById(R.id.shuffle_song);
        repeatButton = (ImageView) rootview.findViewById(R.id.repeat_song);
        playpausebutton = (ImageView) rootview.findViewById(R.id.play_pause_toggle);
        next = (ImageView) rootview.findViewById(R.id.next);
        prev = (ImageView) rootview.findViewById(R.id.prev);
        currentDur = (TextView) rootview.findViewById(R.id.currentDur);
        totalDur = (TextView) rootview.findViewById(R.id.totalDur);
        songArtist = (TextView) rootview.findViewById(R.id.song_artist);
        songTitle = (TextView) rootview.findViewById(R.id.song_title);
        seekbar = (SeekBar) rootview.findViewById(R.id.seekbar);
        lrcView= (TextView) rootview.findViewById(R.id.lyrics);
        bottomsheetLyrics = (FrameLayout) rootview.findViewById(R.id.bottomsheetLyrics);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLyrics);
        diagonalLayout = (DiagonalLayout) rootview.findViewById(R.id.diagonalLayout);
    }

    private void function() {
        ateKey = Helper.getATEKey(getContext());
        accentColor = Config.accentColor(getContext(), ateKey);
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
        getActivity().getWindow().setStatusBarColor(accentColor);
        CustomLayoutManager customlayoutmanager = new CustomLayoutManager(getActivity());
        customlayoutmanager.setOrientation(LinearLayoutManager.VERTICAL);
        customlayoutmanager.setSmoothScrollbarEnabled(true);
        queuerv.setLayoutManager(customlayoutmanager);
        queuerv.addItemDecoration(new DividerItemDecoration(getContext(), 75, false));
        queuerv.setHasFixedSize(true);
        queueAdapter = new QueueAdapter(getContext(), this);
        queueAdapter.setLayoutId(R.layout.queue_songlist);
        queuerv.setAdapter(queueAdapter);
        queueAdapter.setOnItemClickListener(mOnClick);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(queuerv);
        moreMenu.setOnClickListener(onClick);
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        favhelper = new FavHelper(getActivity());
        mSmallBang = new SmallBang(getContext());
        shuffleButton.setOnClickListener(onClick);
        shuffleButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        repeatButton.setOnClickListener(onClick);
        repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        playpausebutton.setOnClickListener(onClick);
        next.setOnClickListener(onClick);
        prev.setOnClickListener(onClick);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.thumb);
        seekbar.setThumb(drawable);
        favButton.setOnClickListener(onClick);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        /**
         * GestureView
         */
        final GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.OnGestureListener() {

            private static final int SWIPE_THRESHOLD = 200;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
                try {
                    float diffy = motionEvent1.getY() - motionEvent.getY();
                    float diffx = motionEvent1.getX() - motionEvent.getX();
                    if (Math.abs(diffx) > Math.abs(diffy)) {
                        if (Math.abs(diffx) > SWIPE_THRESHOLD && Math.abs(v) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffx > 0) {
                                Log.d("Aloha !!!", "no left swipe..");
                            } else {
                                Log.d("Aloha !!!", "no right swipe..");
                            }
                        }
                    } else {
                        if (Math.abs(diffy) > SWIPE_THRESHOLD && Math.abs(v1) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffy > 0) {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            } else {
                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                                    @Override
                                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                        }
                                    }

                                    @Override
                                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });
        albumArt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            bottomsheetLyrics.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
           // lrcview.setTextColor(Color.WHITE);
        } else {
            bottomsheetLyrics.setBackgroundColor(Color.WHITE);
//            lrcview.setTextColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
        }
        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setShapePadding(20);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "600");
        sequence.setConfig(config);
        sequence.addSequenceItem(queuerv, "Drag ,Drop to change queue, Slide right to remove song", "GOT IT");
        sequence.addSequenceItem(lrcView, "slide up/down to show/hide view  available lyrics", "GOT IT");
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                config.setDelay(1000);
            }
        });
        sequence.start();
    }

    private void ShowMoreMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.playing_menu, popupMenu.getMenu());
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
                    case R.id.action_eq:
                        Intent i = new Intent(getActivity(), EqualizerActivity.class);
                        getActivity().startActivity(i);
                        break;
                    case R.id.action_changeArt:
                        pickNupdateArtwork();
                        isalbumArtChanged = false;
                        break;
                    case R.id.action_playlist:
                        new Helper(getContext()).PlaylistChooser(Playing3Fragment.this, getContext(), musicXService.getsongId());
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
                    case R.id.action_share:
                        Helper.shareMusic(musicXService.getsongData(), getContext());
                        break;

                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void pickNupdateArtwork() {
        int chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this, chooserType, true);
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
                        new Helper(getContext()).PlaylistChooser(Playing3Fragment.this, getContext(), queue.getId());
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

        if (position >= 0 && position < queueAdapter.data.size()) {
            queuerv.scrollToPosition(position);
        }

        int newselection;

        newselection = position;

        if (newselection >= 0 && position < queueAdapter.data.size()) {
            queueAdapter.notifyItemChanged(newselection);
            queueAdapter.notifyDataSetChanged();
        }
        queuerv.scrollToPosition(position);
    }

    private void updateSeekbar() {
        if (musicXService != null) {
            int pos = musicXService.getPlayerPos();
            seekbar.setProgress(pos);
            currentDur.setText(durationCalculator(pos));
        }
    }

    public String durationCalculator(long id) {
        return String.format(Locale.getDefault(), "%d:%02d", id / 60000,
                (id % 60000) / 1000);
    }

    private void ChangeAlbumCover(String finalPath) {
        if (musicXService != null) {
            if (chosenImages != null) {
                new updateAlbumArt(finalPath).execute();
            }
        }
    }

    private void PlayingView() {
        if (musicXService != null) {
            String title = musicXService.getsongTitle();
            String artist = musicXService.getsongArtistName();
            songTitle.setText(title);
            songTitle.setSelected(true);
            songTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            songArtist.setText(artist);
            isalbumArtChanged = true;
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b && musicXService != null && (musicXService.isPlaying() || musicXService.isPaused())) {
                        musicXService.seekto(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            if (favhelper.isFavorite(musicXService.getsongId())) {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite);
                }
            } else {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite_outline);
                }
            }
            int dur = musicXService.getDuration();
            if (dur != -1) {
                seekbar.setMax(dur);
                totalDur.setText(durationCalculator(dur));
            }
            updateQueue("Executed");
            new Helper(getContext()).LoadLyrics(musicXService.getsongTitle(), musicXService.getsongArtistName(), lrcView);

        }
    }

    private void colorMode(int color){
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            getActivity().getWindow().setNavigationBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            getActivity().getWindow().setNavigationBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
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
        } else if (mode == musicXService.getRepeatAll()){
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

    @Override
    protected void reload() {
        PlayingView();
        updateRepeatButton();
        updateShuffleButton();
        if (musicXService.isPlaying()) {
            handler.post(seekbarrunnable);
        }
        setButtonDrawable();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(finalPath);
            isalbumArtChanged = true;
        }
    }

    private void coverArtView() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), musicXService.getsongTitle(), musicXService.getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int color[] = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(color[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(color[0]);
                        }
                        if (Extras.getInstance().artworkColor()){
                            colorMode(color[0]);
                        }else {
                            colorMode(accentColor);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        albumArt.setImageBitmap(bitmap);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        albumArt.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    @Override
    protected void playbackConfig() {
        setButtonDrawable();
        if (musicXService.isPlaying()) {
            handler.post(seekbarrunnable);
        }else {
            handler.removeCallbacks(seekbarrunnable);
        }
    }

    @Override
    protected void metaConfig() {
        PlayingView();
        if (musicXService.isPlaying()) {
            handler.post(seekbarrunnable);
        }else {
            handler.removeCallbacks(seekbarrunnable);
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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
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
            Uri newUri = getContext().getContentResolver().insert(albumCover, values);
            if (newUri != null) {
                File file = new File("content://media/external/audio/albumart");
                Toast.makeText(getContext(), "AlbumArt Changed", Toast.LENGTH_LONG).show();
                Log.d("updateAlbumCover", "success hurray !!!");
                getContext().getContentResolver().notifyChange(albumCover, null);
                getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), musicXService.getsongTitle(), path, new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int color[] = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(color[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(color[0]);
                        }
                        if (Extras.getInstance().artworkColor()){
                            colorMode(color[0]);
                        }else {
                            colorMode(accentColor);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        albumArt.setImageBitmap(bitmap);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        albumArt.setImageBitmap(bitmap);
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
