package com.rks.musicx.ui.fragments;


import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.cleveroad.audiowidget.SmallBang;
import com.cleveroad.play_widget.PlayLayout;
import com.cleveroad.play_widget.VisualizerShadowChanger;
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
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.services.MediaPlayerSingleton;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.R.id.song_artist;
import static com.rks.musicx.R.id.song_title;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class Playing2Fragment extends BaseFragment implements SimpleItemTouchHelperCallback.OnStartDragListener, ImageChooserListener {

    private static final long UPDATE_INTERVAL = 1000;
    private Handler mHandler = new Handler();
    private String finalPath;
    private ChosenImage chosenImages;
    private PlayLayout mPlayLayout;
    private ImageView blur_artowrk;
    private TextView songTitle, songArtist;
    private TextView lrcView;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private String ateKey;
    private int accentColor;
    private VisualizerShadowChanger visualizerShadowChanger;
    private FavHelper favhelper;
    private SmallBang mSmallBang;
    private ImageButton favButton, moreMenu;
    private ViewPager Pager;
    private PlayingPagerAdapter PlayingPagerAdapter;
    private List<View> Playing4PagerDetails;
    private ItemTouchHelper mItemTouchHelper;
    private boolean isalbumArtChanged;
    private Timer timer;
    private ImageChooserManager imageChooserManager;
    private String mediaPath;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            if (musicXService == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.item_view:
                    musicXService.setdataPos(position, true);
                    setSelection(position);
                    break;
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (musicXService == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.action_favorite:
                    ImageButton button = (ImageButton) view;
                    if (favhelper.isFavorite(musicXService.getsongId())) {
                        favhelper.removeFromFavorites(musicXService.getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                        int outlinecolor = ContextCompat.getColor(getContext(), R.color.white);
                        button.setColorFilter(outlinecolor);
                    } else {
                        favhelper.addFavorite(musicXService.getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        button.setColorFilter(accentColor);
                        like(view);
                    }
                    break;
                case R.id.menu_button:
                    ShowMoreMenu(view);
                    break;
            }
        }
    };
    private Runnable updateCurrentProg = new Runnable() {
        @Override
        public void run() {
            updateCurrentpos();
            mHandler.postDelayed(updateCurrentProg, 200);
        }
    };

    public static String durationCalculator(long msec) {
        return String.format(Locale.getDefault(), "%d:%02d", msec / 60000,
                (msec % 60000) / 1000);
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
                        if (queueAdapter.getSnapshot().size() > 0) {
                            queueAdapter.clear();
                            queueAdapter.notifyDataSetChanged();
                            musicXService.clearQueue();
                            Queue queue = new Queue(getContext());
                            queue.removeAll();
                            Toast.makeText(getContext(), "Cleared Queue", Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.action_eq:
                        Intent intent = new Intent(getActivity(), EqualizerActivity.class);
                        getActivity().startActivity(intent);
                        break;
                    case R.id.action_changeArt:
                        pickNupdateArtwork();
                        break;
                    case R.id.action_playlist:
                        new Helper(getContext()).PlaylistChooser(Playing2Fragment.this, getContext(), musicXService.getsongId());
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
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        }
                        if (Extras.getInstance().artworkColor()) {
                            colorMode(colors[0]);
                        } else {
                            colorMode(accentColor);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                        mPlayLayout.setImageBitmap(bitmap);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        mPlayLayout.setImageBitmap(bitmap);
                        ArtworkUtils.getBlurArtwork(getContext(), 25, ArtworkUtils.getDefaultArtwork(getContext()), blur_artowrk, 1.0f);
                    }
                });
            }
        });
        isalbumArtChanged = true;
    }

    @Override
    protected void reload() {
        PlayingView();
        updateRepeatButton();
        updateShuffleButton();
        updatePlaylayout();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(finalPath);
            isalbumArtChanged = true;
        }
        if (musicXService.isPlaying()) {
            mHandler.post(updateCurrentProg);
            startTrackingPosition();
        }
    }

    @Override
    protected void playbackConfig() {
        updatePlaylayout();
        if (musicXService.isPlaying()) {
            mHandler.post(updateCurrentProg);
            startTrackingPosition();
        } else {
            mHandler.removeCallbacks(updateCurrentProg);
            stopTrackingPosition();
        }
    }

    @Override
    protected void metaConfig() {
        PlayingView();
        if (musicXService.isPlaying()) {
            mHandler.post(updateCurrentProg);
            startTrackingPosition();
        } else {
            mHandler.removeCallbacks(updateCurrentProg);
            stopTrackingPosition();
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
        if (visualizerShadowChanger != null) {
            visualizerShadowChanger.setEnabledVisualization(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_playing2, container, false);
        ui(rootView);
        working();
        return rootView;
    }

    private void ui(View rootView) {
        blur_artowrk = (ImageView) rootView.findViewById(R.id.blur_artwork);
        Pager = (ViewPager) rootView.findViewById(R.id.pagerPlaying4);
        songTitle = (TextView) rootView.findViewById(song_title);
        songArtist = (TextView) rootView.findViewById(song_artist);
        moreMenu = (ImageButton) rootView.findViewById(R.id.menu_button);
        favButton = (ImageButton) rootView.findViewById(R.id.action_favorite);
        queuerv = (RecyclerView) rootView.findViewById(R.id.commonrv);

        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.playing2_coverview, null);
        View lyricsView = LayoutInflater.from(getContext()).inflate(R.layout.lyricsview, null);

        mPlayLayout = (PlayLayout) coverView.findViewById(R.id.revealView);
        lrcView = (TextView) lyricsView.findViewById(R.id.lyrics);

        Playing4PagerDetails = new ArrayList<>(2);
        Playing4PagerDetails.add(coverView);
        Playing4PagerDetails.add(lyricsView);
        PlayingPagerAdapter = new PlayingPagerAdapter(Playing4PagerDetails);
        Pager.setAdapter(PlayingPagerAdapter);
    }

    private void working() {
        mPlayLayout.fastOpen();
        mPlayLayout.getIvSkipNext().setImageResource(R.drawable.aw_ic_next);
        mPlayLayout.getIvSkipPrevious().setImageResource(R.drawable.aw_ic_prev);
        ateKey = Helper.getATEKey(getContext());
        accentColor = Config.accentColor(getContext(), ateKey);
        mPlayLayout.setPlayButtonBackgroundTintList(ColorStateList.valueOf(accentColor));
        moreMenu.setOnClickListener(mOnClickListener);
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        favhelper = new FavHelper(getContext());
        mSmallBang = SmallBang.attach2Window(getActivity());
        favButton.setOnClickListener(mOnClickListener);
        startVisualiser();
        CustomLayoutManager customlayoutmanager = new CustomLayoutManager(getActivity());
        customlayoutmanager.setOrientation(LinearLayoutManager.HORIZONTAL);
        queuerv.setLayoutManager(customlayoutmanager);
        queuerv.setHasFixedSize(true);
        queueAdapter = new QueueAdapter(getContext(), this);
        queueAdapter.setLayoutId(R.layout.gridqueue);
        queuerv.setAdapter(queueAdapter);
        queueAdapter.setOnItemClickListener(onClick);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(queuerv);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            mPlayLayout.setProgressLineColor(ContextCompat.getColor(getContext(), R.color.translucent_white_8p));
        } else {
            mPlayLayout.setProgressLineColor(ContextCompat.getColor(getContext(), R.color.translucent_white_8p));
        }
        getActivity().getWindow().setStatusBarColor(accentColor);
        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "400");
        sequence.setConfig(config);
        sequence.addSequenceItem(queuerv, "Slide right/left to view Lyrics/PlayingView", "GOT IT");
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                materialShowcaseView.hide();
            }
        });
        sequence.start();
        timer = new Timer("PlayingActivity Timer");
    }

    private void startVisualiser() {
        if (permissionManager.isAudioRecordGranted(getContext())) {
            visualizerShadowChanger = VisualizerShadowChanger.newInstance(MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
            visualizerShadowChanger.setEnabledVisualization(true);
            mPlayLayout.setShadowProvider(visualizerShadowChanger);
            Log.i("startVisualiser", "startVisualiser " + MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
        } else {
            Log.d("PlayingFragment2", "Permission not granted");
        }

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

    private void updateQueue(String acting) {

        if (musicXService == null) {
            return;
        }
        List<Song> mQueue = musicXService.getPlayList();

        if (mQueue != queueAdapter.getSnapshot() && mQueue.size() > 0) {
            queueAdapter.addDataList(mQueue);
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

    private void updatePlaylayout() {
        if (!musicXService.isPlaying()) {
            if (mPlayLayout.isOpen()) {
                mPlayLayout.startDismissAnimation();
            }
        } else {
            if (!mPlayLayout.isOpen()) {
                mPlayLayout.startRevealAnimation();
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
            songArtist.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            isalbumArtChanged = true;
            mPlayLayout.setOnButtonsClickListener(new PlayLayout.OnButtonsClickListenerAdapter() {
                @Override
                public void onPlayButtonClicked() {
                    playpauseclicked();
                }

                @Override
                public void onSkipPreviousClicked() {
                    musicXService.playprev(true);
                    if (!mPlayLayout.isOpen()) {
                        mPlayLayout.startRevealAnimation();
                    }
                }

                @Override
                public void onSkipNextClicked() {
                    musicXService.playnext(true);
                    if (!mPlayLayout.isOpen()) {
                        mPlayLayout.startRevealAnimation();
                    }
                }

                @Override
                public void onShuffleClicked() {
                    boolean shuffle = musicXService.isShuffleEnabled();
                    musicXService.setShuffleEnabled(!shuffle);
                    updateShuffleButton();
                }

                @Override
                public void onRepeatClicked() {
                    int mode = musicXService.getNextrepeatMode();
                    musicXService.setRepeatMode(mode);
                    updateRepeatButton();
                }
            });
            mPlayLayout.setOnProgressChangedListener(new PlayLayout.OnProgressChangedListener() {
                @Override
                public void onPreSetProgress() {
                    // stopTrackingPosition();
                    if (musicXService != null) {
                        startTrackingPosition();
                    }
                }

                @Override
                public void onProgressChanged(float progress) {
                    if (musicXService != null) {
                        int dur = musicXService.getDuration();
                        if (dur != -1) {
                            musicXService.seekto((int) (dur * progress));
                            startTrackingPosition();
                        }
                    }
                }

            });
            updateQueue("Executed");
            if (favhelper.isFavorite(musicXService.getsongId())) {
                favButton.setImageResource(R.drawable.ic_action_favorite);
            } else {
                favButton.setImageResource(R.drawable.ic_action_favorite_outline);
            }
            int dur = musicXService.getDuration();
            if (dur != -1) {
                mPlayLayout.getDur().setText(durationCalculator(dur));
                updateCurrentpos();
            }
            new Helper(getContext()).LoadLyrics(title, artist, musicXService.getsongData(), lrcView);
        }
    }

    private void colorMode(int color) {
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            getActivity().getWindow().setNavigationBarColor(color);
            mPlayLayout.setBigDiffuserColor(Helper.getColorWithAplha(color, 0.3f));
            mPlayLayout.setMediumDiffuserColor(Helper.getColorWithAplha(color, 0.4f));
            mPlayLayout.getPlayButton().setBackgroundTintList(ColorStateList.valueOf(accentColor));
            mPlayLayout.setProgressBallColor(color);
            mPlayLayout.setProgressCompleteColor(color);
            getActivity().getWindow().setStatusBarColor(color);
        } else {
            getActivity().getWindow().setNavigationBarColor(color);
            mPlayLayout.setBigDiffuserColor(Helper.getColorWithAplha(color, 0.3f));
            mPlayLayout.setMediumDiffuserColor(Helper.getColorWithAplha(color, 0.4f));
            mPlayLayout.getPlayButton().setBackgroundTintList(ColorStateList.valueOf(accentColor));
            mPlayLayout.setProgressBallColor(color);
            mPlayLayout.setProgressCompleteColor(color);
            getActivity().getWindow().setStatusBarColor(color);
        }

    }

    private void updateCurrentpos() {
        if (musicXService != null) {
            int pos = musicXService.getPlayerPos();
            mPlayLayout.getCurrent().setText(durationCalculator(pos));
        }
    }

    private void playpauseclicked() {
        if (mPlayLayout == null) {
            return;
        }
        if (mPlayLayout.isOpen()) {
            musicXService.toggle();
            mPlayLayout.startDismissAnimation();
        } else {
            musicXService.toggle();
            mPlayLayout.startRevealAnimation();
        }
    }

    private void updateRepeatButton() {
        int mode = musicXService.getRepeatMode();
        if (mode == musicXService.getNoRepeat()) {
            mPlayLayout.getIvRepeat().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        } else if (mode == musicXService.getRepeatCurrent()) {
            mPlayLayout.getIvRepeat().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_all));
        } else if (mode == musicXService.getRepeatAll()) {
            mPlayLayout.getIvRepeat().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_one));
        }
    }

    private void startTrackingPosition() {
        if (timer == null) {
            return;
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (musicXService != null) {
                    int dur = musicXService.getDuration();
                    int pos = musicXService.getPlayerPos();
                    if (dur != -1 && pos != -1) {
                        mPlayLayout.setPostProgress((float) pos / dur);
                    }
                }

            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void stopTrackingPosition() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer.purge();
        timer = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (visualizerShadowChanger != null) {
            visualizerShadowChanger.release();
        }
        stopTrackingPosition();
    }


    private void updateShuffleButton() {
        boolean shuffle = musicXService.isShuffleEnabled();
        if (shuffle) {
            mPlayLayout.getIvShuffle().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_on));
        } else {
            mPlayLayout.getIvShuffle().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (visualizerShadowChanger != null) {
            visualizerShadowChanger.setEnabledVisualization(true);
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
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void ChangeAlbumCover(String mediaPath) {
        if (musicXService != null) {
            if (chosenImages != null) {
                new updateAlbumArt(mediaPath).execute();
            }
        }
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
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(colors[0]);
                        }
                        if (Extras.getInstance().artworkColor()) {
                            colorMode(colors[0]);
                        } else {
                            colorMode(accentColor);
                        }
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        mPlayLayout.setImageBitmap(bitmap);
                        ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        mPlayLayout.setImageBitmap(bitmap);
                        ArtworkUtils.getBlurArtwork(getContext(), 25, ArtworkUtils.getDefaultArtwork(getContext()), blur_artowrk, 1.0f);
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
