package com.rks.musicx.ui.fragments.PlayingViews;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cleveroad.play_widget.PlayLayout;
import com.cleveroad.play_widget.VisualizerShadowChanger;
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
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.GestureListerner;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.LyricsHelper;
import com.rks.musicx.misc.utils.PlayingPagerAdapter;
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.StartSnapHelper;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.misc.widgets.updateAlbumArt;
import com.rks.musicx.services.MediaPlayerSingleton;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import static com.rks.musicx.R.id.song_artist;
import static com.rks.musicx.R.id.song_title;
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

public class Playing2Fragment extends BasePlayingFragment implements SimpleItemTouchHelperCallback.OnStartDragListener {

    private PlayLayout mPlayLayout;
    private ImageView blur_artowrk;
    private TextView songTitle, songArtist;
    private TextView lrcView;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private String ateKey;
    private int accentColor, pos, duration;
    private VisualizerShadowChanger visualizerShadowChanger;
    private FavHelper favhelper;
    private ImageButton favButton, moreMenu;
    private ViewPager Pager;
    private PlayingPagerAdapter PlayingPagerAdapter;
    private List<View> Playing4PagerDetails;
    private ItemTouchHelper mItemTouchHelper;
    private List<Song> queueList = new ArrayList<>();
    private updateAlbumArt updatealbumArt;
    private Helper helper;
    private bitmap bitmap;
    private palette palette;
    private changeAlbumArt changeAlbumArt;
    private Queue queue;

    private BaseRecyclerViewAdapter.OnItemClickListener onClick = new BaseRecyclerViewAdapter.OnItemClickListener() {
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
                        getMusicXService().updateService(Constants.META_CHANGED);
                    } else {
                        favhelper.addFavorite(getMusicXService().getsongId());
                        button.setImageResource(R.drawable.ic_action_favorite);
                        like(view);
                        getMusicXService().updateService(Constants.META_CHANGED);
                    }
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
            }
        }
    };


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
    protected void reload() {
        playingView();
        updateRepeatButton();
        updateShuffleButton();
        updatePlaylayout();
        coverArtView();
        seekbarProgress();
        updateQueue();
        favButton();
    }

    @Override
    protected void playbackConfig() {
        updatePlaylayout();
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
        if (getActivity() == null || getActivity().getWindow() == null) {
            return;
        }
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        favhelper = new FavHelper(getContext());
        favButton.setOnClickListener(mOnClickListener);
        CustomLayoutManager customlayoutmanager = new CustomLayoutManager(getContext());
        customlayoutmanager.setOrientation(LinearLayoutManager.HORIZONTAL);
        queuerv.setLayoutManager(customlayoutmanager);
        SnapHelper startSnapHelper = new StartSnapHelper();
        startSnapHelper.attachToRecyclerView(queuerv);
        queuerv.setHasFixedSize(true);
        queueAdapter = new QueueAdapter(getContext(), this);
        queueAdapter.setLayoutId(R.layout.gridqueue);
        queuerv.setAdapter(queueAdapter);
        queueAdapter.setOnItemClickListener(onClick);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(queuerv);
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
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
        helper = new Helper(getContext());
        startVisualiser();
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
                    if (getMusicXService() != null) {
                        try {
                            removeCallback();
                        } catch (Exception c) {
                            c.printStackTrace();
                        } finally {
                            seekbarProgress();
                        }
                    }
                }

                @Override
                public void onProgressChanged(float progress) {
                    if (getMusicXService() != null) {
                        int dur = getMusicXService().getDuration();
                        if (dur != -1) {
                            getMusicXService().seekto((int) (dur * progress));
                        }
                    }
                }

            });
            updateQueuePos(getMusicXService().returnpos());
            int dur = getMusicXService().getDuration();
            if (dur != -1) {
                mPlayLayout.getDur().setText(Helper.durationCalculator(dur));
            }
            LyricsHelper.LoadLyrics(getContext(), title, artist, getMusicXService().getsongAlbumName(), getMusicXService().getsongData(), lrcView);
            bitmap = new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    mPlayLayout.setImageBitmap(bitmap);
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    mPlayLayout.setImageBitmap(bitmap);
                    ArtworkUtils.blurPreferances(getContext(), bitmap, blur_artowrk);
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
                }
            };
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
    protected ImageView shuffleButton() {
        return mPlayLayout.getIvShuffle();
    }

    @Override
    protected ImageView repeatButton() {
        return mPlayLayout.getIvRepeat();
    }

    private void startVisualiser() {
        if (permissionManager.isAudioRecordGranted(getContext())) {
            visualizerShadowChanger = VisualizerShadowChanger.newInstance(audioSessionID());
            visualizerShadowChanger.setEnabledVisualization(true);
            mPlayLayout.setShadowProvider(visualizerShadowChanger);
            Log.i("startVisualiser", "startVisualiser " + MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId());
        } else {
            Toast.makeText(getContext(), "AudioRecord permission not granted for visualizer", Toast.LENGTH_SHORT).show();
            Log.d("PlayingFragment2", "Permission not granted");
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
        if (getActivity() == null || getActivity().getWindow() == null) {
            return;
        }
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
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

    private void playpauseclicked() {
        if (mPlayLayout == null) {
            return;
        }
        if (getMusicXService() == null) {
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


    @Override
    public void onDestroy() {
        if (visualizerShadowChanger != null) {
            visualizerShadowChanger.release();
        }
        if (updatealbumArt != null) {
            updatealbumArt.cancel(true);
        }
        removeCallback();
        super.onDestroy();
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

    @Override
    protected TextView lyricsView() {
        return lrcView;
    }

    @Override
    protected void updateProgress() {
        if (getMusicXService() != null && getMusicXService().isPlaying()) {
            pos = getMusicXService().getPlayerPos();
            duration = getMusicXService().getDuration();
            mPlayLayout.setPostProgress((float) pos / duration);
            mPlayLayout.getCurrent().setText(Helper.durationCalculator(pos));
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
