package com.rks.musicx.ui.fragments;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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
import com.rks.musicx.misc.utils.SimpleItemTouchHelperCallback;
import com.rks.musicx.misc.utils.bitmap;
import com.rks.musicx.misc.utils.palette;
import com.rks.musicx.misc.widgets.DiagonalLayout;
import com.rks.musicx.misc.widgets.changeAlbumArt;
import com.rks.musicx.misc.widgets.updateAlbumArt;
import com.rks.musicx.ui.adapters.BaseRecyclerViewAdapter;
import com.rks.musicx.ui.adapters.QueueAdapter;

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

public class Playing3Fragment extends BaseFragment implements SimpleItemTouchHelperCallback.OnStartDragListener,LoaderManager.LoaderCallbacks<List<Song>> {

    private TextView songArtist, songTitle, currentDur, totalDur;
    private String ateKey;
    private int accentColor,pos;
    private TextView lrcView;
    private SeekBar seekbar;
    private Handler handler = new Handler();
    private RecyclerView queuerv;
    private QueueAdapter queueAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private ImageButton favButton, moreMenu;
    private SmallBang mSmallBang;
    private FavHelper favhelper;
    private ImageView albumArt, repeatButton, shuffleButton, playpausebutton, next, prev;
    private BottomSheetBehavior bottomSheetBehavior;
    private FrameLayout bottomsheetLyrics;
    private DiagonalLayout diagonalLayout;
    private int queueLoader = -1;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (getMusicXService() != null) {
                pos = getMusicXService().getPlayerPos();
                seekbar.setProgress(pos);
                currentDur.setText(Helper.durationCalculator(pos));
                handler.postDelayed(runnable, 1000);
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
                    playingMenu(queueAdapter,view, true);
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
                    getMusicXService().setdataPos(position, true);
                    setSelection(position);
                    break;
                case R.id.menu_button:
                    qeueMenu(queueAdapter,view, position);
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

            private static final int SWIPE_THRESHOLD = 300;
            private static final int SWIPE_VELOCITY_THRESHOLD = 200;

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
        getLoaderManager().initLoader(queueLoader, null, this);
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
            isalbumArtChanged = true;
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
            if (favhelper.isFavorite(getMusicXService().getsongId())) {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite);
                }
            } else {
                if (favButton != null) {
                    favButton.setImageResource(R.drawable.ic_action_favorite_outline);
                }
            }
            int dur = getMusicXService().getDuration();
            if (dur != -1) {
                seekbar.setMax(dur);
                totalDur.setText(Helper.durationCalculator(dur));
            }
            updateQueue("Executed");
            new Helper(getContext()).LoadLyrics(title, artist, getMusicXService().getsongData(), lrcView);

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
        getLoaderManager().restartLoader(queueLoader, null, this);
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
                        ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), getMusicXService().getsongAlbumName(), finalPath, new palette() {
                            @Override
                            public void palettework(Palette palette) {
                                final int color[] = Helper.getAvailableColor(getContext(), palette);
                                if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                                    getActivity().getWindow().setStatusBarColor(color[0]);
                                } else {
                                    getActivity().getWindow().setStatusBarColor(color[0]);
                                }
                                if (Extras.getInstance().artworkColor()) {
                                    colorMode(color[0]);
                                } else {
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
                    }
                }).execute();
            }
        }
    }

    private void colorMode(int color) {
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            getActivity().getWindow().setNavigationBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            getActivity().getWindow().setNavigationBarColor(color);
            seekbar.setBackgroundTintList(ColorStateList.valueOf(color));
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
        if (getMusicXService().isPlaying()) {
            handler.post(runnable);
        }
        setButtonDrawable();
        if (isalbumArtChanged) {
            coverArtView();
            isalbumArtChanged = false;
        } else {
            ChangeAlbumCover(getImagePath());
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
                ArtworkUtils.ArtworkLoaderBitmapPalette(getContext(), getMusicXService().getsongAlbumName(), getMusicXService().getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        final int color[] = Helper.getAvailableColor(getContext(), palette);
                        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
                            getActivity().getWindow().setStatusBarColor(color[0]);
                        } else {
                            getActivity().getWindow().setStatusBarColor(color[0]);
                        }
                        if (Extras.getInstance().artworkColor()) {
                            colorMode(color[0]);
                        } else {
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
    }

    @Override
    protected void metaConfig() {
        playingView();
        if (getMusicXService().isPlaying()) {
            handler.post(runnable);
        }else {
            handler.removeCallbacks(runnable);
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
