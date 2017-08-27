package com.rks.musicx.ui.fragments.PlayingViews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.permissionManager;
import com.rks.musicx.misc.widgets.DiagonalLayout;
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

public class Playing3Fragment extends BasePlayingFragment implements SimpleItemTouchHelperCallback.OnStartDragListener {

    private TextView songArtist, songTitle, currentDur, totalDur;
    private String ateKey;
    private int accentColor, pos;
    private TextView lrcView;
    private SeekBar seekbar;
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ImageButton favButton, moreMenu;
    private FavHelper favhelper;
    private ImageView albumArt, repeatButton, shuffleButton, playpausebutton, next, prev;
    private BottomSheetBehavior bottomSheetBehavior;
    private FrameLayout bottomsheetLyrics;
    private DiagonalLayout diagonalLayout;
    private List<Song> queueList = new ArrayList<>();
    private updateAlbumArt updatealbumArt;
    private Helper helper;
    private bitmap bitmap;
    private palette palette;
    private changeAlbumArt changeAlbumArt;
    private Queue queue;

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
                        getMusicXService().updateService(Constants.META_CHANGED);
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


    @Override
    protected void ui(View rootView) {
        albumArt = (ImageView) rootView.findViewById(R.id.albumArt);
        queuerv = (RecyclerView) rootView.findViewById(R.id.commonrv);
        favButton = (ImageButton) rootView.findViewById(R.id.action_favorite);
        moreMenu = (ImageButton) rootView.findViewById(R.id.menu_button);
        shuffleButton = (ImageView) rootView.findViewById(R.id.shuffle_song);
        repeatButton = (ImageView) rootView.findViewById(R.id.repeat_song);
        playpausebutton = (ImageView) rootView.findViewById(R.id.play_pause_toggle);
        next = (ImageView) rootView.findViewById(R.id.next);
        prev = (ImageView) rootView.findViewById(R.id.prev);
        currentDur = (TextView) rootView.findViewById(R.id.currentDur);
        totalDur = (TextView) rootView.findViewById(R.id.totalDur);
        songArtist = (TextView) rootView.findViewById(R.id.song_artist);
        songTitle = (TextView) rootView.findViewById(R.id.song_title);
        seekbar = (SeekBar) rootView.findViewById(R.id.seekbar);
        lrcView = (TextView) rootView.findViewById(R.id.lyrics);
        bottomsheetLyrics = (FrameLayout) rootView.findViewById(R.id.bottomsheetLyrics);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomsheetLyrics);
        diagonalLayout = (DiagonalLayout) rootView.findViewById(R.id.diagonalLayout);
    }

    @Override
    protected void function() {
        ateKey = Helper.getATEKey(getContext());
        if (getActivity() == null) {
            return;
        }
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
        if (Extras.getInstance().getPlayingViewTrack()) {
            queueAdapter.setLayoutId(R.layout.song_list);
        }
        queuerv.setAdapter(queueAdapter);
        queueAdapter.setOnItemClickListener(mOnClick);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(queuerv);
        moreMenu.setOnClickListener(onClick);
        moreMenu.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_menu));
        favhelper = new FavHelper(getActivity());
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
        diagonalLayout.setOnTouchListener(new GestureListerner() {
            @Override
            public void onRightToLeft() {

            }

            @Override
            public void onLeftToRight() {

            }

            @Override
            public void onBottomToTop() {
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

            @Override
            public void onTopToBottom() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
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
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            bottomsheetLyrics.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
            lrcView.setTextColor(Color.WHITE);
        } else {
            bottomsheetLyrics.setBackgroundColor(Color.WHITE);
            lrcView.setTextColor(ContextCompat.getColor(getContext(), R.color.MaterialGrey));
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
        helper = new Helper(getContext());
    }

    @Override
    protected int setLayout() {
        return R.layout.fragment_playing3;
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
            int dur = getMusicXService().getDuration();
            if (dur != -1) {
                seekbar.setMax(dur);
                totalDur.setText(Helper.durationCalculator(dur));
            }
            updateQueuePos(getMusicXService().returnpos());
            LyricsHelper.LoadLyrics(getContext(), title, artist, getMusicXService().getsongAlbumName(), getMusicXService().getsongData(), lrcView);
            bitmap = new bitmap() {
                @Override
                public void bitmapwork(Bitmap bitmap) {
                    albumArt.setImageBitmap(bitmap);
                }

                @Override
                public void bitmapfailed(Bitmap bitmap) {
                    albumArt.setImageBitmap(bitmap);
                }
            };
            palette = new palette() {
                @Override
                public void palettework(Palette palette) {
                    if (Extras.getInstance().artworkColor()) {
                        final int color[] = Helper.getAvailableColor(getContext(), palette);
                        colorMode(color[0]);
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
            seekbar.setProgress(pos);
            currentDur.setText(Helper.durationCalculator(pos));
        }
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

    private void colorMode(int color) {
        if (getActivity() == null || getActivity().getWindow() == null) {
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
        updateRepeatButton();
        updateShuffleButton();
        seekbarProgress();
        setButtonDrawable();
        coverArtView();
        updateQueue();
        favButton();
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

    @Override
    protected void onPaused() {

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
