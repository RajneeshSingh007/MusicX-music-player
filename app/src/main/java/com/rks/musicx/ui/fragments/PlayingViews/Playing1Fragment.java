package com.rks.musicx.ui.fragments.PlayingViews;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.rks.musicx.misc.widgets.CircularSeekBar;
import com.rks.musicx.misc.widgets.updateAlbumArt;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.R.id.lyrics;
import static com.rks.musicx.misc.utils.Constants.Queue_TableName;


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

public class Playing1Fragment extends BasePlayingFragment implements SimpleItemTouchHelperCallback.OnStartDragListener {


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
    private FavHelper favhelper;
    private ImageView blur_artowrk, repeatButton, shuffleButton;
    private ViewPager Pager;
    private PlayingPagerAdapter pagerAdapter;
    private List<View> Playing3PagerDetails;
    private ItemTouchHelper mItemTouchHelper;
    private SlidingPaneLayout slidingpanelayout;
    private View coverView;
    private Helper helper;
    private List<Song> queueList = new ArrayList<>();
    private updateAlbumArt updatealbumArt;
    private bitmap bitmap;
    private palette palette;
    private changeAlbumArt changeAlbumArt;
    private Queue queue;

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
                        getMusicXService().updateService(Constants.META_CHANGED);
                    } else {
                        favhelper.addFavorite(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(v);
                        getMusicXService().updateService(Constants.META_CHANGED);
                    }
                    break;
                case R.id.action_share:
                    Helper.shareMusic(getMusicXService().getsongId(), getContext());
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
                    playingMenu(queue, v, false);
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


    /**
     * Update queue
     */
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

    /**
     * update queue pos
     *
     * @param pos
     */
    private void updateQueuePos(int pos) {
        queueAdapter.notifyDataSetChanged();
        queueAdapter.setSelection(pos);
        if (pos >= 0 && pos < queueList.size()) {
            queuerv.scrollToPosition(pos);
        }
    }


    @Override
    protected void reload() {
        playingView();
        setButtonDrawable();
        updateShuffleButton();
        updateRepeatButton();
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

    @Override
    protected void queueConfig() {
        updateQueue();
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


        coverView = LayoutInflater.from(getContext()).inflate(R.layout.playing1_coverview, new LinearLayout(getContext()), false);
        View lyricsView = LayoutInflater.from(getContext()).inflate(R.layout.lyricsview, new LinearLayout(getContext()), false);

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
        lrcView = (TextView) lyricsView.findViewById(lyrics);

        /**
         * Pager config
         */
        Playing3PagerDetails = new ArrayList<>(2);
        Playing3PagerDetails.add(coverView);
        Playing3PagerDetails.add(lyricsView);
        pagerAdapter = new PlayingPagerAdapter(Playing3PagerDetails);
        Pager.setAdapter(pagerAdapter);
        coverView.setOnTouchListener(new GestureListerner() {
            @Override
            public void onRightToLeft() {

            }

            @Override
            public void onLeftToRight() {

            }

            @Override
            public void onBottomToTop() {
                if (getMusicXService() == null) {
                    return;
                }
                if (getMusicXService().isPlaying()) {
                    getMusicXService().playnext(true);
                }
            }

            @Override
            public void onTopToBottom() {
                if (getMusicXService() == null) {
                    return;
                }
                if (getMusicXService().isPlaying()) {
                    getMusicXService().playprev(true);
                }
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
        favhelper = new FavHelper(getContext());
        share.setOnClickListener(mOnClickListener);
        eqButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.eq));
        eqButton.setOnClickListener(mOnClickListener);
        share.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shares));
        shuffleButton.setOnClickListener(mOnClickListener);
        shuffleButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        repeatButton.setOnClickListener(mOnClickListener);
        repeatButton.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        moreMenu.setOnClickListener(mOnClickListener);
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        CustomLayoutManager customlayoutmanager = new CustomLayoutManager(getContext());
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
            SongTitle.setText(title);
            SongTitle.setSelected(true);
            SongTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            SongArtist.setText(artist);
            Helper.rotationAnim(playpausebutton);
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
            LyricsHelper.LoadLyrics(getContext(), title, artist, getMusicXService().getsongAlbumName(), getMusicXService().getsongData(), lrcView);
            // NetworkHelper.absolutesLyrics(getContext(),artist, title, getMusicXService().getsongAlbumName(), getMusicXService().getsongData(), lrcView);
            updateQueuePos(getMusicXService().returnpos());
            bitmap = new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                    mAlbumCoverView.setImageBitmap(bitmap);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                    mAlbumCoverView.setImageBitmap(bitmap);
                }
            };
            palette = new palette() {
                @Override
                public void palettework(Palette palette) {
                    final int[] colors = Helper.getAvailableColor(getContext(), palette);
                    if (getActivity() == null || getActivity().getWindow() == null) {
                        return;
                    }
                    getActivity().getWindow().setStatusBarColor(colors[0]);
                    getActivity().getWindow().setNavigationBarColor(colors[0]);
                    ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), ((ColorDrawable) Playing3view.getBackground()).getColor(), colors[0]);
                    colorAnimation.setDuration(250); // milliseconds
                    colorAnimation.addUpdateListener(animator -> Playing3view.setBackgroundColor((int) animator.getAnimatedValue()));
                    colorAnimation.start();
                    if (Extras.getInstance().artworkColor()) {
                        colorMode(colors[0]);
                    } else {
                        colorMode(accentColor);
                    }
                }
            };

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

    @Override
    protected void updateProgress() {
        if (getMusicXService() != null && getMusicXService().isPlaying()) {
            position = getMusicXService().getPlayerPos();
            mSeekBar.setProgress(position);
            CurrentDur.setText(Helper.durationCalculator(position));
        }
    }


    /**
     * On Change Album Work
     *
     * @param finalPath
     */
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

    private void colorMode(int color) {
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            mSeekBar.setCircleProgressColor(color);
            mSeekBar.setPointerColor(color);
            mSeekBar.setPointerHaloColor(color);
            playpausebutton.setBackgroundTintList(ColorStateList.valueOf(color));
            Playing3view.setBackgroundColor(color);
            mAlbumCoverView.setBorderColor(color);
        } else {
            mSeekBar.setCircleProgressColor(color);
            mSeekBar.setPointerColor(color);
            mSeekBar.setPointerHaloColor(color);
            playpausebutton.setBackgroundTintList(ColorStateList.valueOf(color));
            Playing3view.setBackgroundColor(color);
            mAlbumCoverView.setBorderColor(color);
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
    public void onDestroy() {
        if (updatealbumArt != null) {
            updatealbumArt.cancel(true);
        }
        removeCallback();
        super.onDestroy();
    }

}



