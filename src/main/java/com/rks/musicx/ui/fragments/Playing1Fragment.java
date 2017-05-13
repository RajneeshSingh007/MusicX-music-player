package com.rks.musicx.ui.fragments;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.cleveroad.audiowidget.SmallBang;
import com.rks.musicx.R;
import com.rks.musicx.data.loaders.QueueLoaders;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.rks.musicx.misc.widgets.CircularSeekBar;
import com.rks.musicx.misc.widgets.changeAlbumArt;
import com.rks.musicx.misc.widgets.updateAlbumArt;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;


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

public class Playing1Fragment extends BaseFragment implements SimpleItemTouchHelperCallback.OnStartDragListener, LoaderManager.LoaderCallbacks<List<Song>> {


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
    private PlayingPagerAdapter pagerAdapter;
    private List<View> Playing3PagerDetails;
    private ItemTouchHelper mItemTouchHelper;
    private SlidingPaneLayout slidingpanelayout;
    private View coverView;
    private Helper helper;
    private int queueLoader = -1;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (getMusicXService() != null){
                position = getMusicXService().getPlayerPos();
                mSeekBar.setProgress(position);
                CurrentDur.setText(Helper.durationCalculator(position));
                mHandler.postDelayed(runnable, 1000);
            }
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (getMusicXService() == null) {
                return;
            }
            switch (v.getId()) {
                case R.id.play_pause_toggle:
                    getMusicXService().toggle();
                    break;
                case R.id.shuffle_song:
                    boolean shuffle = getMusicXService().isShuffleEnabled();
                    getMusicXService().setShuffleEnabled(!shuffle);
                    updateShuffleButton();
                    break;
                case R.id.repeat_song:
                    int mode = getMusicXService().getNextrepeatMode();
                    getMusicXService().setRepeatMode(mode);
                    updateRepeatButton();
                    break;
                case R.id.action_favorite:
                    ImageButton button = (ImageButton) v;
                    if (favhelper.isFavorite(getMusicXService().getsongId())) {
                        favhelper.removeFromFavorites(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                    } else {
                        favhelper.addFavorite(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(v);
                    }
                    break;
                case R.id.action_share:
                    Helper.shareMusic(getMusicXService().getsongData(), getContext());
                    break;
                case R.id.menu_button:
                    playingMenu(queueAdapter,v, false);
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
            if (getMusicXService() == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.item_view:
                    getMusicXService().setdataPos(position, true);
                    setSelection(position);
                    break;
                case R.id.menu_button:
                    qeueMenu(queueAdapter,view, position);
                    break;
            }
        }
    };


    private void updateQueue(String acting) {
        getLoaderManager().restartLoader(queueLoader, null, this);
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

    @Override
    protected void reload() {
        playingView();
        setButtonDrawable();
        updateShuffleButton();
        updateRepeatButton();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(getImagePath());
            isalbumArtChanged = true;
        }
        if (getMusicXService().isPlaying()) {
            mHandler.post(runnable);
        }
    }

    @Override
    protected void playbackConfig() {
        setButtonDrawable();
    }

    @Override
    protected void metaConfig() {
        playingView();
        if (getMusicXService().isPlaying()) {
            mHandler.post(runnable);
        }else {
            mHandler.removeCallbacks(runnable);
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
    }

    @Override
    protected void ui(View rootView) {
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
        pagerAdapter = new PlayingPagerAdapter(Playing3PagerDetails);
        Pager.setAdapter(pagerAdapter);
        /**
         * Swipe Listerner
         */
        final GestureDetector gesture = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 300;
            private static final int SWIPE_VELOCITY_THRESHOLD = 200;

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
                                if (getMusicXService().isPlaying()) {
                                    getMusicXService().playprev(true);
                                    Log.d("Aloha !!!", "Down swipe..");
                                }
                            } else {
                                if (getMusicXService().isPlaying()) {
                                    getMusicXService().playnext(true);
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
        sequence.addSequenceItem(slidingpanelayout, "Slide right/tap to view QueueView", "GOT IT");
        sequence.addSequenceItem(queuerv, "Drag ,Drop to change queue, Slide right to remove song", "GOT IT");
        sequence.start();
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                config.setDelay(1000);
            }
        });
    }

    @Override
    protected void function() {
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
        slidingpanelayout.setSliderFadeColor(Color.TRANSPARENT);
        slidingpanelayout.setCoveredFadeColor(Color.TRANSPARENT);
        Playing3view.setBackgroundColor(accentColor);
        if (getActivity() == null) {
            return;
        }
        getActivity().getWindow().setStatusBarColor(accentColor);
        helper = new Helper(getContext());
        getLoaderManager().initLoader(queueLoader, null, this);
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_playing1;
    }

    @Override
    protected void playingView() {
        if (getMusicXService() != null) {
            String title = getMusicXService().getsongTitle();
            String artist = getMusicXService().getsongArtistName();
            isalbumArtChanged = true;
            SongTitle.setText(title);
            SongTitle.setSelected(true);
            SongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            SongArtist.setText(artist);
            Helper.rotationAnim(mAlbumCoverView);
            mSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
                @Override
                public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                    if (fromUser && getMusicXService() != null && (getMusicXService().isPlaying() || getMusicXService().isPaused())) {
                        getMusicXService().seekto(circularSeekBar.getProgress());
                    }
                }

                @Override
                public void onStopTrackingTouch(CircularSeekBar seekBar) {
                }

                @Override
                public void onStartTrackingTouch(CircularSeekBar seekBar) {
                }
            });
            int duration = getMusicXService().getDuration();
            if (duration != -1) {
                mSeekBar.setMax(duration);
                TotalDur.setText(Helper.durationCalculator(duration));
            }
            if (favhelper.isFavorite(getMusicXService().getsongId())) {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite);
                }
            } else {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite_outline);
                }
            }
            helper.LoadLyrics(title, artist, getMusicXService().getsongData(), lrcView);
            updateQueue("Executed");
        }
    }

    @Override
    protected ImageView shuffleButton() {
        return shuffleButton;
    }

    @Override
    protected ImageView repeatButton() {
        return repeatButton;
    }


    @Override
    protected void changeArtwork() {
        ChangeAlbumCover(getImagePath());
    }

    @Override
    protected TextView lyricsView() {
        return lrcView;
    }


    private void ChangeAlbumCover(String finalPath) {
        if (getMusicXService() != null) {
            if (getChosenImages() != null){
                new updateAlbumArt(finalPath, getMusicXService().getsongData(), getContext(), getMusicXService().getsongAlbumID(), new changeAlbumArt() {
                    @Override
                    public void onPostWork() {
                        ArtworkUtils.ArtworkLoader(getContext(), getMusicXService().getsongAlbumName(), finalPath, mAlbumCoverView);
                        ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), getMusicXService().getsongAlbumName(), finalPath, new palette() {
                            @Override
                            public void palettework(Palette palette) {
                                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                                Playing3view.setBackgroundColor(colors[0]);
                                mAlbumCoverView.setBorderColor(colors[0]);
                                new Helper(getContext()).animateViews(Playing3view, colors[0]);
                                if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
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
                            }

                            @Override
                            public void bitmapfailed(Bitmap bitmap) {
                                Playing3view.setBackgroundColor(accentColor);
                                ArtworkUtils.getBlurArtwork(getContext(), 25, ArtworkUtils.getDefaultArtwork(getContext()), blur_artowrk, 1.0f);
                            }
                        });
                        queueAdapter.notifyDataSetChanged();
                    }
                }).execute();
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
                ArtworkUtils.ArtworkLoader(getContext(), getMusicXService().getsongAlbumName(), getMusicXService().getsongAlbumID(), mAlbumCoverView);
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), getMusicXService().getsongAlbumName(), getMusicXService().getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        Playing3view.setBackgroundColor(colors[0]);
                        mAlbumCoverView.setBorderColor(colors[0]);
                        new Helper(getContext()).animateViews(Playing3view, colors[0]);
                        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
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
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        Playing3view.setBackgroundColor(accentColor);
                        ArtworkUtils.getBlurArtwork(getContext(), 25, ArtworkUtils.getDefaultArtwork(getContext()), blur_artowrk, 1.0f);
                    }
                });
            }
        });
        isalbumArtChanged = true;
    }


    private void colorMode(int color) {
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
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

    private void setButtonDrawable() {
        if (getMusicXService() != null) {
            if (getMusicXService().isPlaying()) {
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
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        QueueLoaders queueLoaders = new QueueLoaders(getContext(), getMusicXService());
        if (id == queueLoader){
            return queueLoaders;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<Song>> loader, List<Song> data) {
        if (data == null){
            return;
        }
        queueAdapter.addDataList(data);
        queueAdapter.notifyDataSetChanged();
        setSelection(getMusicXService().returnpos());
    }

    @Override
    public void onLoaderReset(Loader<List<Song>> loader) {
        loader.reset();
        queueAdapter.notifyDataSetChanged();
    }
}



