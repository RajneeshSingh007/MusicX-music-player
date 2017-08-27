package com.rks.musicx.ui.fragments.PlayingViews;


import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.audiofx.Visualizer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatSeekBar;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rks.musicx.R;
import com.rks.musicx.base.BasePlayingFragment;
import com.rks.musicx.base.BaseRecyclerViewAdapter;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.interfaces.Queue;
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.changeAlbumArt;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.CustomLayoutManager;
import com.rks.musicx.misc.utils.DividerItemDecoration;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.GestureListerner;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.LyricsHelper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.misc.widgets.CircleImageView;
import com.rks.musicx.misc.widgets.CircleVisualizerFFTView;
import com.rks.musicx.misc.widgets.updateAlbumArt;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.misc.utils.Constants.Queue_TableName;

/*
 * Created by Coolalien on 23/07/2017.
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

    private int accentColor, pos, primaryColor;
    private String ateKey;
    private CircleImageView albumArtwork;
    private CircleVisualizerFFTView vizualview;
    private Visualizer mVisualizer;
    private FloatingActionButton playpausebutton, queueClick;
    private TextView songArtist, songTitle, currentDur, totalDur;
    private TextView lrcview;
    private AppCompatSeekBar seekbar;
    private ImageButton favButton, moreMenu;
    private FavHelper favhelper;
    private ImageView repeatButton, shuffleButton, next, prev, blurArtwork, blurQArtwork;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private FrameLayout queueViews;
    private ItemTouchHelper mItemTouchHelper;
    private ViewPager Pager;
    private PlayingPagerAdapter pagerAdapter;
    private List<View> Playing4PagerDetails;
    private List<Song> queueList = new ArrayList<>();
    private updateAlbumArt updatealbumArt;
    private Helper helper;
    private bitmap bitmap;
    private palette palette;
    private changeAlbumArt changeAlbumArt;
    private boolean flag = true;
    private LinearLayout controlsBg;
    private Queue queue;

    private BaseRecyclerViewAdapter.OnItemClickListener mOnClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View view) {
            if (getMusicXService() == null) {
                return;
            }
            switch (view.getId()) {
                case R.id.item_view:
                    if (queueAdapter.getSnapshot().size() > 0) {
                        queueAdapter.setSelection(position);
                        getMusicXService().setdataPos(position, true);
                        Extras.getInstance().saveSeekServices(0);
                    }
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
                        getMusicXService().updateService(Constants.META_CHANGED);
                    } else {
                        favhelper.addFavorite(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(view);
                        getMusicXService().updateService(Constants.META_CHANGED);
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
                    queue = new Queue() {
                        @Override
                        public void clearStuff() {
                            if (queueAdapter.getSnapshot().size() > 0) {
                                CommonDatabase commonDatabase = new CommonDatabase(getContext(), Queue_TableName, true);
                                ;
                                queueAdapter.clear();
                                queueAdapter.notifyDataSetChanged();
                                getMusicXService().clearQueue();
                                try {
                                    commonDatabase.removeAll();
                                } finally {
                                    commonDatabase.close();
                                }
                                Toast.makeText(getContext(), "Cleared Queue", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };
                    playingMenu(queue, view, true);
                    break;
                case R.id.next:
                    getMusicXService().playnext(true);
                    break;
                case R.id.prev:
                    getMusicXService().playprev(true);
                    break;
                case R.id.show_queue:
                    if (flag) {
                        Helper.getCircularShowAnimtion(queueViews).start();
                        queueViews.setVisibility(View.VISIBLE);
                        if (vizualview != null) {
                            vizualview.setVisibility(View.GONE);
                        }
                        flag = false;
                    } else {
                        Helper.getCircularHideAnimtion(queueViews).start();
                        queueViews.setVisibility(View.GONE);
                        if (vizualview != null) {
                            vizualview.setVisibility(View.VISIBLE);
                        }
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
        primaryColor = Config.primaryColor(getContext(), ateKey);
        playpausebutton.setOnClickListener(onClick);
        Drawable bg = ContextCompat.getDrawable(getContext(), R.drawable.transparent);
        if (seekbar != null) {
            seekbar.setSplitTrack(false);
            if (seekbar.getThumb() != null) {
                seekbar.getThumb().mutate().setAlpha(0);
            }
            seekbar.setBackground(bg);
        }
        favButton.setOnClickListener(onClick);
        queueClick.setOnClickListener(onClick);
        moreMenu.setOnClickListener(onClick);
        favhelper = new FavHelper(getContext());
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
        if (getActivity() == null && getActivity().getWindow() == null) {
            return;
        }
        getActivity().getWindow().setStatusBarColor(accentColor);
        initVisualizer();
        helper = new Helper(getContext());
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_queue);
        drawable.setTint(Color.WHITE);
        queueClick.setImageDrawable(drawable);
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
            Helper.rotationAnim(albumArtwork);
            Helper.rotationAnim(playpausebutton);
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
            updateQueuePos(getMusicXService().returnpos());
            LyricsHelper.LoadLyrics(getContext(), title, artist, getMusicXService().getsongAlbumName(), getMusicXService().getsongData(), lrcview);
            bitmap = new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    albumArtwork.setImageBitmap(bitmap);
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blurArtwork);
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blurQArtwork);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    albumArtwork.setImageBitmap(bitmap);
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blurArtwork);
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blurQArtwork);
                }
            };
            palette = new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(), palette);
                    if (Extras.getInstance().artworkColor()) {
                        colorMode(colors[0]);
                    } else {
                        colorMode(accentColor);
                    }
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), ((ColorDrawable) controlsBg.getBackground()).getColor(), colors[0]);
                    colorAnimation.setDuration(250); // milliseconds
                    colorAnimation.addUpdateListener(animator -> controlsBg.setBackgroundColor((int) animator.getAnimatedValue()));
                    colorAnimation.start();
                }
            };
            if (mVisualizer != null) {
                if (vizualview != null) {
                    vizualview.setEnabled(true);
                }
                mVisualizer.setEnabled(true);
            }
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

    @Override
    protected void updateProgress() {
        if (getMusicXService() != null && getMusicXService().isPlaying()) {
            pos = getMusicXService().getPlayerPos();
            seekbar.setProgress(pos);
            currentDur.setText(Helper.durationCalculator(pos));
        }
    }


    private void updateQueue() {
        if (getMusicXService() == null) {
            return;
        }
        queueList = getMusicXService().getPlayList();
        int pos = getMusicXService().returnpos();
        if (queueList != queueAdapter.getSnapshot() && queueList.size() > 0) {
            queueAdapter.addDataList(queueList);
        }
        updateQueuePos(pos);
        queueAdapter.notifyDataSetChanged();
    }

    private void updateQueuePos(int pos) {
        queueAdapter.notifyDataSetChanged();
        queueAdapter.setSelection(pos);
        if (pos >= 0 && pos < queueList.size()) {
            queuerv.scrollToPosition(pos);
        }
    }

    private void colorMode(int color) {
        if (getActivity() == null || getActivity().getWindow() == null) {
            return;
        }
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            getActivity().getWindow().setNavigationBarColor(color);
            getActivity().getWindow().setStatusBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
            if (vizualview != null) {
                vizualview.setmCakeColor(color);
            }
            playpausebutton.setBackgroundTintList(ColorStateList.valueOf(color));
            queueClick.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            getActivity().getWindow().setNavigationBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
            getActivity().getWindow().setStatusBarColor(color);
            if (vizualview != null) {
                vizualview.setmCakeColor(color);
            }
            playpausebutton.setBackgroundTintList(ColorStateList.valueOf(color));
            queueClick.setBackgroundTintList(ColorStateList.valueOf(color));
        }

    }

    private void coverArtView() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArtworkUtils.ArtworkLoader(getContext(), 300, 600, getMusicXService().getsongAlbumName(), getMusicXService().getsongAlbumID(), palette, bitmap);
            }
        });
    }


    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }


    private void initVisualizer() {
        if (permissionManager.isAudioRecordGranted(getContext())) {
            mVisualizer = new Visualizer(audioSessionID());
            mVisualizer.setEnabled(false);
            mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    vizualview.updateVisualizer(fft);
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, true);
        } else {
            Toast.makeText(getContext(), "AudioRecord permission not granted for visualizer", Toast.LENGTH_SHORT).show();
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
    protected void reload() {
        playingView();
        setButtonDrawable();
        updateRepeatButton();
        updateShuffleButton();
        coverArtView();
        seekbarProgress();
        updateQueue();
        favButton();
    }

    @Override
    protected void playbackConfig() {
        setButtonDrawable();
    }

    @Override
    protected void metaConfig() {
        playingView();
        seekbarProgress();
        coverArtView();
        favButton();
    }


    private void ChangeAlbumCover(String finalPath) {
        if (getMusicXService() != null) {
            if (getChosenImages() != null) {
                changeAlbumArt = new changeAlbumArt() {
                    @Override
                    public void onPostWork() {
                        ArtworkUtils.ArtworkLoader(getContext(), 300, 600, finalPath, getMusicXService().getsongAlbumID(), palette, bitmap);
                        queueAdapter.notifyDataSetChanged();
                    }
                };
                updatealbumArt = new updateAlbumArt(finalPath, getMusicXService().getsongData(), getContext(), getMusicXService().getsongAlbumID(), changeAlbumArt);
                updatealbumArt.execute();
                if (permissionManager.isAudioRecordGranted(getContext())) {
                    Glide.with(getContext())
                            .load(finalPath)
                            .asBitmap()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(R.mipmap.ic_launcher)
                            .error(R.mipmap.ic_launcher)
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(getSize(), getSize())
                            .transform(new CropCircleTransformation(getContext()))
                            .into(new SimpleTarget<Bitmap>() {
                                @Override
                                public void onLoadStarted(Drawable placeholder) {
                                }

                                @Override
                                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                    if (getMusicXService().getAudioWidget() != null) {
                                        getMusicXService().getAudioWidget().controller().albumCoverBitmap(ArtworkUtils.drawableToBitmap(errorDrawable));
                                    }
                                }

                                @Override
                                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                    if (getMusicXService().getAudioWidget() != null) {
                                        getMusicXService().getAudioWidget().controller().albumCoverBitmap(resource);
                                    }
                                }

                            });
                }
                metaChangedBroadcast();
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        if (context instanceof palette) {
            palette = (com.rks.musicx.interfaces.palette) context;
        }
        if (context instanceof bitmap) {
            bitmap = (com.rks.musicx.interfaces.bitmap) context;
        }
        if (context instanceof changeAlbumArt) {
            changeAlbumArt = (com.rks.musicx.interfaces.changeAlbumArt) context;
        }
        if (context instanceof Queue) {
            queue = (Queue) context;
        }
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        bitmap = null;
        palette = null;
        changeAlbumArt = null;
        queue = null;
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
        blurQArtwork = (ImageView) rootView.findViewById(R.id.blurQArtwork);
        currentDur = (TextView) rootView.findViewById(R.id.currentDur);
        totalDur = (TextView) rootView.findViewById(R.id.totalDur);
        songArtist = (TextView) rootView.findViewById(R.id.song_artist);
        songTitle = (TextView) rootView.findViewById(R.id.song_title);
        seekbar = (AppCompatSeekBar) rootView.findViewById(R.id.seekbar);
        favButton = (ImageButton) rootView.findViewById(R.id.action_favorite);
        moreMenu = (ImageButton) rootView.findViewById(R.id.menu_button);
        shuffleButton = (ImageView) rootView.findViewById(R.id.shuffle_song);
        repeatButton = (ImageView) rootView.findViewById(R.id.repeat_song);
        queuerv = (RecyclerView) rootView.findViewById(R.id.commonrv);
        queueViews = (FrameLayout) rootView.findViewById(R.id.queue_views);
        queueClick = (FloatingActionButton) rootView.findViewById(R.id.show_queue);
        Pager = (ViewPager) rootView.findViewById(R.id.pager);
        next = (ImageView) rootView.findViewById(R.id.next);
        prev = (ImageView) rootView.findViewById(R.id.prev);
        playpausebutton = (FloatingActionButton) rootView.findViewById(R.id.play_pause_toggle);
        controlsBg = (LinearLayout) rootView.findViewById(R.id.controls);

        View coverView = LayoutInflater.from(getContext()).inflate(R.layout.playing4_coverview, new LinearLayout(getContext()), false);
        View lyricsView = LayoutInflater.from(getContext()).inflate(R.layout.lyricsview, new LinearLayout(getContext()), false);

        /**
         * Album,playpause,Vizualiser View
         */
        albumArtwork = (CircleImageView) coverView.findViewById(R.id.album_cover);
        vizualview = (CircleVisualizerFFTView) coverView.findViewById(R.id.vizualview);
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
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(getActivity(), "1800");
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
        coverView.setOnTouchListener(new GestureListerner() {
            @Override
            public void onRightToLeft() {

            }

            @Override
            public void onLeftToRight() {

            }

            @Override
            public void onBottomToTop() {
            }

            @Override
            public void onTopToBottom() {
            }

            @Override
            public void doubleClick() {
                if (getActivity() == null) {
                    return;
                }
                ((PlayingActivity) getActivity()).onBackPressed();
            }

            @Override
            public void singleClick() {

            }

            @Override
            public void otherFunction() {

            }
        });
    }

    @Override
    public void onDestroy() {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
        if (updatealbumArt != null) {
            updatealbumArt.cancel(true);
        }
        removeCallback();
        super.onDestroy();
    }

    private void favButton() {
        if (getMusicXService() == null) {
            return;
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
    }
}
