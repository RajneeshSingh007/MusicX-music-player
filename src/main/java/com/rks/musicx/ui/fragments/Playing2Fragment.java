package com.rks.musicx.ui.fragments;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.cleveroad.audiowidget.SmallBang;
import com.cleveroad.play_widget.PlayLayout;
import com.cleveroad.play_widget.VisualizerShadowChanger;
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

public class Playing2Fragment extends BaseFragment implements SimpleItemTouchHelperCallback.OnStartDragListener {

    private static final long UPDATE_INTERVAL = 1000;
    private Handler mHandler = new Handler();
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

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            if (getMusicXService() == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.item_view:
                    getMusicXService().setdataPos(position, true);
                    setSelection(position);
                    break;
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getMusicXService() == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.action_favorite:
                    ImageButton button = (ImageButton) view;
                    if (favhelper.isFavorite(getMusicXService().getsongId())) {
                        favhelper.removeFromFavorites(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                        int outlinecolor = ContextCompat.getColor(getContext(), R.color.white);
                        button.setColorFilter(outlinecolor);
                    } else {
                        favhelper.addFavorite(getMusicXService().getsongId());
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
                            getMusicXService().clearQueue();
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
                        pickupArtwork();
                        isalbumArtChanged = false;
                        break;
                    case R.id.action_playlist:
                        new Helper(getContext()).PlaylistChooser(Playing2Fragment.this, getContext(), getMusicXService().getsongId());
                        break;
                    case R.id.action_lyrics:
                        new Helper(getContext()).searchLyrics(getContext(), getMusicXService().getsongTitle(), getMusicXService().getsongArtistName(), getMusicXService().getsongData(), lrcView);
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), getMusicXService().getsongData());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), getMusicXService().getsongTitle(), getMusicXService().getsongAlbumName(), getMusicXService().getsongArtistName(), getMusicXService().getsongNumber(), getMusicXService().getsongData());
                        break;
                    case R.id.action_share:
                        Helper.shareMusic(getMusicXService().getsongData(), getContext());
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
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), getMusicXService().getsongTitle(), getMusicXService().getsongAlbumID(), new palette() {
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
        playingView();
        updateRepeatButton();
        updateShuffleButton();
        updatePlaylayout();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(getImagePath());
            isalbumArtChanged = true;
        }
        if (getMusicXService().isPlaying()) {
            mHandler.post(updateCurrentProg);
            startTrackingPosition();
        }
    }

    @Override
    protected void playbackConfig() {
        updatePlaylayout();
        if (getMusicXService().isPlaying()) {
            mHandler.post(updateCurrentProg);
            startTrackingPosition();
        } else {
            mHandler.removeCallbacks(updateCurrentProg);
            stopTrackingPosition();
        }
    }

    @Override
    protected void metaConfig() {
        playingView();
        if (getMusicXService().isPlaying()) {
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
            ChangeAlbumCover(getImagePath());
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
    protected void ui(View rootView) {
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

    @Override
    protected void function() {
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

    @Override
    protected int setLayout() {
        return R.layout.fragment_playing2;
    }

    @Override
    protected void playingView() {
        if (getMusicXService() != null) {
            String title = getMusicXService().getsongTitle();
            String artist = getMusicXService().getsongArtistName();
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
                    getMusicXService().playprev(true);
                    if (!mPlayLayout.isOpen()) {
                        mPlayLayout.startRevealAnimation();
                    }
                }

                @Override
                public void onSkipNextClicked() {
                    getMusicXService().playnext(true);
                    if (!mPlayLayout.isOpen()) {
                        mPlayLayout.startRevealAnimation();
                    }
                }

                @Override
                public void onShuffleClicked() {
                    boolean shuffle = getMusicXService().isShuffleEnabled();
                    getMusicXService().setShuffleEnabled(!shuffle);
                    updateShuffleButton();
                }

                @Override
                public void onRepeatClicked() {
                    int mode = getMusicXService().getNextrepeatMode();
                    getMusicXService().setRepeatMode(mode);
                    updateRepeatButton();
                }
            });
            mPlayLayout.setOnProgressChangedListener(new PlayLayout.OnProgressChangedListener() {
                @Override
                public void onPreSetProgress() {
                    // stopTrackingPosition();
                    if (getMusicXService() != null) {
                        startTrackingPosition();
                    }
                }

                @Override
                public void onProgressChanged(float progress) {
                    if (getMusicXService() != null) {
                        int dur = getMusicXService().getDuration();
                        if (dur != -1) {
                            getMusicXService().seekto((int) (dur * progress));
                            startTrackingPosition();
                        }
                    }
                }

            });
            updateQueue("Executed");
            if (favhelper.isFavorite(getMusicXService().getsongId())) {
                favButton.setImageResource(R.drawable.ic_action_favorite);
            } else {
                favButton.setImageResource(R.drawable.ic_action_favorite_outline);
            }
            int dur = getMusicXService().getDuration();
            if (dur != -1) {
                mPlayLayout.getDur().setText(durationCalculator(dur));
                updateCurrentpos();
            }
            new Helper(getContext()).LoadLyrics(title, artist, getMusicXService().getsongData(), lrcView);
        }
    }

    @Override
    protected ImageView shuffleButton() {
        return mPlayLayout.getIvShuffle();
    }

    @Override
    protected ImageView repeatButton() {
        return mPlayLayout.getIvRepeat();
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

    private void updateQueue(String acting) {

        if (getMusicXService() == null) {
            return;
        }
        List<Song> mQueue = getMusicXService().getPlayList();

        if (mQueue != queueAdapter.getSnapshot() && mQueue.size() > 0) {
            queueAdapter.addDataList(mQueue);
        }
        queueAdapter.notifyDataSetChanged();
        setSelection(getMusicXService().returnpos());
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
        if (!getMusicXService().isPlaying()) {
            if (mPlayLayout.isOpen()) {
                mPlayLayout.startDismissAnimation();
            }
        } else {
            if (!mPlayLayout.isOpen()) {
                mPlayLayout.startRevealAnimation();
            }
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
        if (getMusicXService() != null) {
            int pos = getMusicXService().getPlayerPos();
            mPlayLayout.getCurrent().setText(durationCalculator(pos));
        }
    }

    private void playpauseclicked() {
        if (mPlayLayout == null) {
            return;
        }
        if (mPlayLayout.isOpen()) {
            getMusicXService().toggle();
            mPlayLayout.startDismissAnimation();
        } else {
            getMusicXService().toggle();
            mPlayLayout.startRevealAnimation();
        }
    }

    private void startTrackingPosition() {
        if (timer == null) {
            return;
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (getMusicXService() != null) {
                    int dur = getMusicXService().getDuration();
                    int pos = getMusicXService().getPlayerPos();
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


    @Override
    public void onResume() {
        super.onResume();
        if (visualizerShadowChanger != null) {
            visualizerShadowChanger.setEnabledVisualization(true);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    protected void changeArtwork() {
        ChangeAlbumCover(getImagePath());
    }

    private void ChangeAlbumCover(String finalPath) {
        if (getMusicXService() != null) {
            if (getChosenImages() != null){
                new updateAlbumArt(finalPath).execute();
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
                getContext().getContentResolver().delete(ContentUris.withAppendedId(albumCover, getMusicXService().getsongAlbumID()), null, null);
                values = new ContentValues();
                values.put("album_id", getMusicXService().getsongAlbumID());
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
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), getMusicXService().getsongTitle(), path, new palette() {
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
