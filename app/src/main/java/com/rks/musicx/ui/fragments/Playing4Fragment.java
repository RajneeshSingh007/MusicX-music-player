package com.rks.musicx.ui.fragments;


import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.cleveroad.audiowidget.SmallBang;
import com.rks.musicx.R;
import com.rks.musicx.base.BasePlayingFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.rks.musicx.misc.widgets.CircleVisualizerFFTView;
import com.rks.musicx.misc.widgets.changeAlbumArt;
import com.rks.musicx.misc.widgets.updateAlbumArt;
import com.rks.musicx.services.MediaPlayerSingleton;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.List;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/*
 * Created by Coolalien on 3/13/2017.
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

public class Playing4Fragment extends BasePlayingFragment implements SimpleItemTouchHelperCallback.OnStartDragListener {

    private int accentColor, pos;
    private String ateKey;
    private CircleImageView albumArtwork;
    private CircleVisualizerFFTView vizualview;
    private Visualizer mVisualizer;
    private FloatingActionButton playpausebutton;
    private TextView songArtist, songTitle, currentDur, totalDur;
    private TextView lrcview;
    private SeekBar seekbar;
    private Handler handler = new Handler();
    private ImageButton favButton, moreMenu;
    private SmallBang mSmallBang;
    private FavHelper favhelper;
    private boolean isalbumArtChanged;
    private ImageView repeatButton, shuffleButton, next, prev, queueViewArt, blurArtwork, queueblurArtwork;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private FrameLayout queueViews;
    private boolean flag = false;
    private ViewPager Pager;
    private PlayingPagerAdapter pagerAdapter;
    private List<View> Playing4PagerDetails;
    private List<Song> queueList = new ArrayList<>();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (getMusicXService() != null && getMusicXService().isPlaying()) {
                pos = getMusicXService().getPlayerPos();
                seekbar.setProgress(pos);
                currentDur.setText(Helper.durationCalculator(pos));
            }
            handler.postDelayed(runnable, 1000);
        }
    };

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            if (getMusicXService() == null) {
                return;
            }
            queuerv.scrollToPosition(position);
            switch (view.getId()) {
                case R.id.item_view:
                    getMusicXService().setdataPos(position, true);
                    break;

                case R.id.menu_button:
                    qeueMenu(queueAdapter, view, position);
                    break;
            }
        }
    };
    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getMusicXService() == null) {
                return;
            }
            switch (view.getId()) {

                case R.id.play_pause_toggle:
                    getMusicXService().toggle();
                    break;
                case R.id.action_favorite:
                    ImageButton button = (ImageButton) view;
                    if (favhelper.isFavorite(getMusicXService().getsongId())) {
                        favhelper.removeFromFavorites(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite_outline);
                    } else {
                        favhelper.addFavorite(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(view);
                    }
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
                case R.id.menu_button:
                    playingMenu(queueAdapter, view, true);
                    break;
                case R.id.next:
                    getMusicXService().playnext(true);
                    break;
                case R.id.prev:
                    getMusicXService().playprev(true);
                    break;
                case R.id.queuelist:
                    if (flag) {
                        queueViews.setVisibility(View.GONE);
                        flag = false;

                    } else {
                        queueViews.setVisibility(View.VISIBLE);
                        flag = true;
                    }
                    break;

            }
        }
    };


    @Override
    protected void function() {
        ateKey = Helper.getATEKey(getContext());
        accentColor = Config.accentColor(getContext(), ateKey);
        playpausebutton.setOnClickListener(onClick);
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.thumb);
        seekbar.setThumb(drawable);
        vizualview.setmCakeColor(accentColor);
        favButton.setOnClickListener(onClick);
        moreMenu.setOnClickListener(onClick);
        queueViewArt.setOnClickListener(onClick);
        favhelper = new FavHelper(getContext());
        mSmallBang = new SmallBang(getContext());
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        shuffleButton.setOnClickListener(onClick);
        shuffleButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        repeatButton.setOnClickListener(onClick);
        repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        playpausebutton.setOnClickListener(onClick);
        next.setOnClickListener(onClick);
        prev.setOnClickListener(onClick);
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
        getActivity().getWindow().setStatusBarColor(accentColor);
        initVisualizer();
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_playing4;
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
            isalbumArtChanged = true;
            Helper.rotationAnim(albumArtwork);
            if (permissionManager.isAudioRecordGranted(getContext())) {
                mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform,
                                                      int samplingRate) {
                    }

                    @Override
                    public void onFftDataCapture(Visualizer visualizer, byte[] fft,
                                                 int samplingRate) {
                        vizualview.updateVisualizer(fft);
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true);
                mVisualizer.setEnabled(true);
            } else {
                Log.d("Playing4Fragment", "permission not granted");
            }
            int dur = getMusicXService().getDuration();
            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (b && getMusicXService() != null && (getMusicXService().isPlaying() || getMusicXService().isPaused())) {
                        getMusicXService().seekto(seekBar.getProgress());
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });
            if (dur != -1) {
                seekbar.setMax(dur);
                totalDur.setText(Helper.durationCalculator(dur));
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
            updateQueue();
            new Helper(getContext()).LoadLyrics(title, artist, getMusicXService().getsongData(), lrcview);
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
        return lrcview;
    }


    private void updateQueue() {
        if (getMusicXService() == null) {
            return;
        }
        queueList = getMusicXService().getPlayList();
        if (queueList != queueAdapter.getSnapshot() && queueList.size() > 0) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    queueAdapter.addDataList(queueList);
                }
            });
        }
        queueAdapter.setSelection(getMusicXService().returnpos());
        if (getMusicXService().returnpos() >=0 && getMusicXService().returnpos() < queueAdapter.getSnapshot().size()){
            queuerv.scrollToPosition(getMusicXService().returnpos());
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


    private void colorMode(int color) {
        if (getActivity() == null) {
            return;
        }
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            getActivity().getWindow().setNavigationBarColor(color);
            getActivity().getWindow().setStatusBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            getActivity().getWindow().setNavigationBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
            getActivity().getWindow().setStatusBarColor(color);
        }

    }

    private void coverArtView() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArtworkUtils.ArtworkLoader(getContext(), getMusicXService().getsongAlbumName(), null, getMusicXService().getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int[] colors = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().vizColor()) {
                            vizualview.setmCakeColor(colors[0]);
                        } else {
                            vizualview.setmCakeColor(accentColor);
                        }
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
                        ArtworkUtils.blurPreferances(getContext(), bitmap, blurArtwork);
                        ArtworkUtils.blurPreferances(getContext(), bitmap, queueblurArtwork);
                        albumArtwork.setImageBitmap(bitmap);
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        ArtworkUtils.blurPreferances(getContext(), bitmap, blurArtwork);
                        ArtworkUtils.blurPreferances(getContext(), bitmap, queueblurArtwork);
                        albumArtwork.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    private void initVisualizer() {
        if (permissionManager.isAudioRecordGranted(getContext())) {
            mVisualizer = new Visualizer(MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
            mVisualizer.setEnabled(false);
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        } else {
            Log.d("Playing4Fragment", "permission not granted");
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
    public void onDestroy() {
        super.onDestroy();
        if (mVisualizer != null) {
            mVisualizer.release();
        }
        mVisualizer = null;
    }

    @Override
    protected void reload() {
        playingView();
        setButtonDrawable();
        updateRepeatButton();
        updateShuffleButton();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(getImagePath());
            isalbumArtChanged = true;
        }
        handler.post(runnable);
    }

    @Override
    protected void playbackConfig() {
        setButtonDrawable();
    }

    @Override
    protected void metaConfig() {
        playingView();
        handler.post(runnable);
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(getImagePath());
            isalbumArtChanged = true;
        }
    }


    private void ChangeAlbumCover(String finalPath) {
        if (getMusicXService() != null) {
            if (getChosenImages() != null) {
                new updateAlbumArt(finalPath, getMusicXService().getsongData(), getContext(), getMusicXService().getsongAlbumID(), new changeAlbumArt() {
                    @Override
                    public void onPostWork() {
                        ArtworkUtils.ArtworkLoader(getContext(), getMusicXService().getsongAlbumName(), finalPath, getMusicXService().getsongAlbumID(), new palette() {
                            @Override
                            public void palettework(Palette palette) {
                                final int[] colors = Helper.getAvailableColor(getContext(), palette);
                                if (Extras.getInstance().vizColor()) {
                                    vizualview.setmCakeColor(colors[0]);
                                } else {
                                    vizualview.setmCakeColor(accentColor);
                                }
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
                                ArtworkUtils.blurPreferances(getContext(), bitmap, blurArtwork);
                                ArtworkUtils.blurPreferances(getContext(), bitmap, queueblurArtwork);
                                albumArtwork.setImageBitmap(bitmap);
                            }

                            @Override
                            public void bitmapfailed(Bitmap bitmap) {
                                ArtworkUtils.blurPreferances(getContext(), bitmap, blurArtwork);
                                ArtworkUtils.blurPreferances(getContext(), bitmap, queueblurArtwork);
                                albumArtwork.setImageBitmap(bitmap);
                            }
                        });
                        queueAdapter.notifyDataSetChanged();
                    }
                }).execute();
            }
        }
    }

    @Override
    protected void queueConfig() {
        updateQueue();
    }

    @Override
    protected void onPaused() {
        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
        }
    }

    @Override
    protected void ui(View rootView) {
        blurArtwork = (ImageView) rootView.findViewById(R.id.blurArtwork);
        queueblurArtwork = (ImageView) rootView.findViewById(R.id.queue_blurArtwork);
        currentDur = (TextView) rootView.findViewById(R.id.currentDur);
        totalDur = (TextView) rootView.findViewById(R.id.totalDur);
        songArtist = (TextView) rootView.findViewById(R.id.song_artist);
        songTitle = (TextView) rootView.findViewById(R.id.song_title);
        seekbar = (SeekBar) rootView.findViewById(R.id.seekbar);
        favButton = (ImageButton) rootView.findViewById(R.id.action_favorite);
        moreMenu = (ImageButton) rootView.findViewById(R.id.menu_button);
        shuffleButton = (ImageView) rootView.findViewById(R.id.shuffle_song);
        repeatButton = (ImageView) rootView.findViewById(R.id.repeat_song);
        queuerv = (RecyclerView) rootView.findViewById(R.id.commonrv);
        queueViews = (FrameLayout) rootView.findViewById(R.id.queueview);
        queueViewArt = (ImageView) rootView.findViewById(R.id.queuelist);
        Pager = (ViewPager) rootView.findViewById(R.id.pager);

        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.playing4_coverview, null);
        View lyricsView = LayoutInflater.from(getContext()).inflate(R.layout.lyricsview, null);

        /**
         * Album,playpause,Vizualiser View
         */
        albumArtwork = (CircleImageView) coverView.findViewById(R.id.album_cover);
        vizualview = (CircleVisualizerFFTView) coverView.findViewById(R.id.vizualview);
        playpausebutton = (FloatingActionButton) coverView.findViewById(R.id.play_pause_toggle);
        next = (ImageView) coverView.findViewById(R.id.next);
        prev = (ImageView) coverView.findViewById(R.id.prev);
        lrcview = (TextView) lyricsView.findViewById(R.id.lyrics);

        /**
         * Pager config
         */
        Playing4PagerDetails = new ArrayList<>(2);
        Playing4PagerDetails.add(coverView);
        Playing4PagerDetails.add(lyricsView);
        pagerAdapter = new PlayingPagerAdapter(Playing4PagerDetails);
        Pager.setAdapter(pagerAdapter);
        /**
         * Show Case
         */
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "1000");
        sequence.setConfig(config);
        sequence.addSequenceItem(next, "Slide right/left to view Lyrics/PlayingView", "GOT IT");
        sequence.addSequenceItem(queueViews, "Tap to view QueueView", "GOT IT");
        sequence.addSequenceItem(queuerv, "Drag ,Drop to change queue, Slide right to remove song", "GOT IT");
        sequence.start();
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
                config.setDelay(1000);
            }
        });
    }


}
