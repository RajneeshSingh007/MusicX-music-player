package com.rks.musicx.base;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.cleveroad.audiowidget.SmallBang;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.database.SaveQueueDatabase;
import com.rks.musicx.interfaces.Queue;
import com.rks.musicx.misc.utils.Constants;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.LyricsHelper;
import com.rks.musicx.misc.utils.PlaylistHelper;
import com.rks.musicx.services.MediaPlayerSingleton;
import com.rks.musicx.services.MusicXService;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.adapters.QueueAdapter;

import java.lang.ref.WeakReference;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;
import static com.rks.musicx.misc.utils.Constants.ALBUM_ARTIST;
import static com.rks.musicx.misc.utils.Constants.ALBUM_ID;
import static com.rks.musicx.misc.utils.Constants.ALBUM_NAME;
import static com.rks.musicx.misc.utils.Constants.ALBUM_TRACK_COUNT;
import static com.rks.musicx.misc.utils.Constants.ARTIST_ARTIST_ID;
import static com.rks.musicx.misc.utils.Constants.ARTIST_NAME;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.Queue_Store_TableName;
import static com.rks.musicx.misc.utils.Constants.SHOW_ALBUM;
import static com.rks.musicx.misc.utils.Constants.SHOW_ARTIST;
import static com.rks.musicx.misc.utils.Constants.SHOW_TAG;

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

public abstract class BasePlayingFragment extends Fragment implements ImageChooserListener {

    private static Handler handler;
    private MusicXService musicXService;
    private boolean mServiceBound = false;
    private String finalPath;
    private ChosenImage chosenImages;
    private ImageChooserManager imageChooserManager;
    private int size;
    private Drawable shuffleOff, shuffleOn, repeatAll, repeatOne, noRepeat;
    private SmallBang mSmallBang;


    /**
     * Service Connection
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicXService.MusicXBinder binder = (MusicXService.MusicXBinder) service;
            musicXService = binder.getService();
            mServiceBound = true;
            if (musicXService != null) {
                reload();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    /**
     * BroadCast
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (musicXService == null) {
                return;
            }
            String action = intent.getAction();
            switch (action) {
                case PLAYSTATE_CHANGED:
                    playbackConfig();
                    break;
                case META_CHANGED:
                    metaConfig();
                    break;
                case QUEUE_CHANGED:
                case POSITION_CHANGED:
                case ITEM_ADDED:
                case ORDER_CHANGED:
                    queueConfig();
                    break;
            }
        }
    };

    protected abstract void reload();

    protected abstract void playbackConfig();

    protected abstract void metaConfig();

    protected abstract void queueConfig();

    protected abstract void onPaused();

    protected abstract void ui(View rootView);

    protected abstract void function();

    protected abstract int setLayout();

    protected abstract void playingView();

    protected abstract ImageView shuffleButton();

    protected abstract ImageView repeatButton();

    protected abstract void changeArtwork();

    protected abstract TextView lyricsView();

    protected abstract void updateProgress();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(setLayout(), container, false);
        ui(rootView);
        function();
        size = getResources().getDimensionPixelSize(R.dimen.cover_size);
        handler = new Handler(Looper.getMainLooper());
        shuffleOff = ContextCompat.getDrawable(getContext(), R.drawable.shuf_off);
        shuffleOn = ContextCompat.getDrawable(getContext(), R.drawable.shuf_on);
        repeatOne = ContextCompat.getDrawable(getContext(), R.drawable.rep_one);
        repeatAll = ContextCompat.getDrawable(getContext(), R.drawable.rep_all);
        noRepeat = ContextCompat.getDrawable(getContext(), R.drawable.rep_no);
        if (getActivity() == null) {
            return null;
        }
        mSmallBang = SmallBang.attach2Window(getActivity());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(getActivity(), MusicXService.class);
        getActivity().bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null) {
            return;
        }
        if (!mServiceBound) {
            Intent intent = new Intent(getActivity(), MusicXService.class);
            getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(intent);
            IntentFilter filter = new IntentFilter();
            filter.addAction(META_CHANGED);
            filter.addAction(PLAYSTATE_CHANGED);
            filter.addAction(POSITION_CHANGED);
            filter.addAction(ITEM_ADDED);
            filter.addAction(ORDER_CHANGED);
            try {
                getActivity().registerReceiver(broadcastReceiver, filter);
            } catch (Exception e) {
                // already registered
            }
        } else {
            if (musicXService != null) {
                reload();
            }
        }
        Glide.get(getContext()).clearMemory();
    }

    @Override
    public void onPause() {
        super.onPause();
        onPaused();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (getActivity() == null) {
            return;
        }
        if (mServiceBound) {
            musicXService = null;
            getActivity().unbindService(serviceConnection);
            mServiceBound = false;
            try {
                getActivity().unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                // already unregistered
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Glide.get(getContext()).clearMemory();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() == null) {
            return;
        }
        Extras.getInstance().getThemevalue(getActivity());
    }


    @Override
    public void onImageChosen(ChosenImage chosenImage) {
        chosenImages = chosenImage;
        finalPath = chosenImages.getFilePathOriginal();
        Log.d("BaseFragment", finalPath);
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                changeArtwork();
            }
        });
    }


    @Override
    public void onError(String s) {
        Log.d("BaseFragment", s);
    }

    @Override
    public void onImagesChosen(ChosenImages chosenImages) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(getClass().getName(), requestCode + "");
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (imageChooserManager == null) {
                imageChooserManager = new ImageChooserManager(this, requestCode, true);
                imageChooserManager.setImageChooserListener(this);
                imageChooserManager.reinitialize(finalPath);
            }
            imageChooserManager.submit(requestCode, data);
        }
    }

    public String getImagePath() {
        return finalPath;
    }

    public MusicXService getMusicXService() {
        return musicXService;
    }

    public ChosenImage getChosenImages() {
        return chosenImages;
    }

    public int getSize() {
        return size;
    }

    /**
     * Pick Up Artwork
     */
    public void pickupArtwork() {
        imageChooserManager = new ImageChooserManager(this, ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(this);
        try {
            finalPath = imageChooserManager.choose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shuffle Button Update
     */
    public void updateShuffleButton() {
        boolean shuffle = musicXService.isShuffleEnabled();
        if (shuffle) {
            shuffleButton().setImageDrawable(shuffleOn);
        } else {
            shuffleButton().setImageDrawable(shuffleOff);
        }
    }

    /**
     * Repeat Button Update
     */
    public void updateRepeatButton() {
        int mode = musicXService.getRepeatMode();
        if (mode == getMusicXService().getNoRepeat()) {
            repeatButton().setImageDrawable(noRepeat);
        } else if (mode == getMusicXService().getRepeatCurrent()) {
            repeatButton().setImageDrawable(repeatAll);
        } else if (mode == getMusicXService().getRepeatAll()) {
            repeatButton().setImageDrawable(repeatOne);
        }
    }


    private void goToMain(String action, Bundle data) {
        if (getActivity() == null) {
            return;
        }
        Intent i = new Intent(action);
        if (data != null) {
            i.putExtras(data);
        }
        getActivity().setResult(RESULT_OK, i);
        getActivity().finish();
    }

    /**
     * Playing Menu
     *
     * @param view
     */
    public void playingMenu(Queue queue, View view, boolean torf) {
        if (getActivity() == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.playing_menu, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.action_share).setVisible(torf);
        popupMenu.getMenu().findItem(R.id.action_eq).setVisible(torf);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.clear_queue:
                        queue.clearStuff();
                        break;
                    case R.id.action_changeArt:
                        pickupArtwork();
                        break;
                    case R.id.action_playlist:
                        PlaylistHelper.PlaylistChooser(BasePlayingFragment.this, getContext(), musicXService.getsongId());
                        break;
                    case R.id.action_lyrics:
                        LyricsHelper.searchLyrics(getContext(), musicXService.getsongTitle(), musicXService.getsongArtistName(), musicXService.getsongAlbumName(), musicXService.getsongData(), lyricsView());
                        break;
                    case R.id.action_edit_tags:
                        Extras.getInstance().saveMetaData(musicXService.getCurrentSong());
                        Bundle tag = new Bundle();
                        goToMain(SHOW_TAG, tag);
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), musicXService.getsongData());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), musicXService.getsongTitle(), musicXService.getsongAlbumName(), musicXService.getsongArtistName(), musicXService.getsongNumber(), musicXService.getsongData());
                        break;
                    case R.id.action_eq:
                        ((PlayingActivity) getActivity()).returnEq();
                        break;
                    case R.id.action_share:
                        Helper.shareMusic(musicXService.getsongId(), getContext());
                        break;
                    case R.id.action_settings:
                        ((PlayingActivity) getActivity()).returnSettings();
                        break;
                    case R.id.action_savequeue:
                        saveQueue();
                        break;
                    case R.id.go_to_album:
                        Bundle data = new Bundle();
                        data.putLong(ALBUM_ID, getMusicXService().getsongAlbumID());
                        data.putString(ALBUM_NAME, getMusicXService().getsongAlbumName());
                        data.putString(ALBUM_ARTIST, getMusicXService().getsongArtistName());
                        data.putInt(ALBUM_TRACK_COUNT, getMusicXService().getsongNumber());
                        goToMain(SHOW_ALBUM, data);
                        Log.e("Move", "Go_to_Main");
                        break;
                    case R.id.go_to_artist:
                        Bundle data1 = new Bundle();
                        data1.putLong(ARTIST_ARTIST_ID, getMusicXService().getArtistID());
                        data1.putString(ARTIST_NAME, getMusicXService().getsongArtistName());
                        goToMain(SHOW_ARTIST, data1);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    /**
     * Save Queue
     */
    private void saveQueue(){
        View view1 = LayoutInflater.from(getContext()).inflate(R.layout.create_playlist, new LinearLayout(getContext()), false);
        TextInputEditText editText = (TextInputEditText) view1.findViewById(R.id.playlist_name);
        TextInputLayout inputLayout = (TextInputLayout) view1.findViewById(R.id.inputlayout);
        inputLayout.setHint("Enter queue name");
        new MaterialDialog.Builder(getContext())
                .title("Save Queue")
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .autoDismiss(true)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (editText.getText() != null) {
                            String tableName = editText.getText().toString();
                            tableName = tableName.replace(" ", "");
                            tableName = tableName.trim();
                            saveQueueInfo(tableName);
                        } else {
                            dialog.dismiss();
                        }
                    }
                })
                .customView(view1, false)
                .build()
                .show();
    }

    /**
     * Save info in the db
     * @param tableName
     */
    private void saveQueueInfo(String tableName) {
        if (musicXService == null) {
            return;
        }
        if (musicXService.getPlayList().size() > 0) {
            SaveQueueDatabase saveQueueDatabase = new SaveQueueDatabase(getContext(), Queue_Store_TableName);
            boolean checkExistance = saveQueueDatabase.isExist(tableName);
            if (checkExistance) {
                Toast.makeText(getContext(), "Already " + tableName + " queue" + " exists", Toast.LENGTH_SHORT).show();
            } else {
                if (musicXService.getPlayList().size() > 0) {
                    CommonDatabase commonDatabase = new CommonDatabase(getContext(), tableName, true);
                    commonDatabase.add(musicXService.getPlayList());
                    commonDatabase.close();
                    saveQueueDatabase.addQueueName(tableName);
                    saveQueueDatabase.close();
                    Toast.makeText(getContext(), "Saved Queue", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Queue is empty", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * Queue Menu
     *
     * @param view
     * @param position
     */
    public void qeueMenu(QueueAdapter queueAdapter, View view, int position) {
        if (getActivity() == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.playing_menu, popupMenu.getMenu());
        Song queue = queueAdapter.getItem(position);
        popupMenu.getMenu().findItem(R.id.action_lyrics).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_eq).setVisible(false);
        popupMenu.getMenu().findItem(R.id.clear_queue).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_changeArt).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_settings).setVisible(false);
        popupMenu.getMenu().findItem(R.id.action_savequeue).setVisible(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playlist:
                        PlaylistHelper.PlaylistChooser(BasePlayingFragment.this, getContext(), queue.getId());
                        break;
                    case R.id.action_edit_tags:
                        Extras.getInstance().saveMetaData(queue);
                        Bundle tag = new Bundle();
                        goToMain(SHOW_TAG, tag);
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), queue.getmSongPath());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), queue.getTitle(), queue.getAlbum(), queue.getArtist(), queue.getTrackNumber(), queue.getmSongPath());
                        break;
                    case R.id.action_share:
                        Helper.shareMusic(musicXService.getsongId(), getContext());
                        break;
                    case R.id.go_to_album:
                        Bundle data = new Bundle();
                        data.putLong(ALBUM_ID, queue.getAlbumId());
                        data.putString(ALBUM_NAME, queue.getAlbum());
                        data.putString(ALBUM_ARTIST, queue.getArtist());
                        data.putInt(ALBUM_TRACK_COUNT, queue.getTrackNumber());
                        goToMain(SHOW_ALBUM, data);
                        break;
                    case R.id.go_to_artist:
                        Bundle data1 = new Bundle();
                        data1.putLong(ARTIST_ARTIST_ID, queue.getArtistId());
                        data1.putString(ARTIST_NAME, queue.getArtist());
                        goToMain(SHOW_ARTIST, data1);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    public void seekbarProgress() {
        ProgressRunnable progressRunnable = new ProgressRunnable(BasePlayingFragment.this);
        handler.post(progressRunnable);
    }

    public void removeCallback() {
        ProgressRunnable progressRunnable = new ProgressRunnable(BasePlayingFragment.this);
        handler.removeCallbacks(progressRunnable);
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * Like Animation
     *
     * @param view
     */
    public void like(View view) {
        mSmallBang.bang(view);
        mSmallBang.setmListener(new SmallBang.SmallBangListener() {
            @Override
            public void onAnimationStart() {
                Helper.rotationAnim(view);
            }

            @Override
            public void onAnimationEnd() {
            }
        });
    }

    public void metaChangedBroadcast() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(Constants.META_CHANGED);
        if (Constants.META_CHANGED.equals(intent.getAction())) {
            getActivity().sendBroadcast(intent);
            Log.e("BasePlay", "BroadCast");
        }
    }

    public int audioSessionID() {
        int audioID = MediaPlayerSingleton.getInstance().getMediaPlayer().getAudioSessionId();
        if (audioID == 0) {
            return 0;
        }
        return audioID;
    }

    private static class ProgressRunnable implements Runnable {

        private final WeakReference<BasePlayingFragment> baseLoaderWeakReference;

        public ProgressRunnable(BasePlayingFragment myClassInstance) {
            baseLoaderWeakReference = new WeakReference<BasePlayingFragment> (myClassInstance);
        }

        @Override
        public void run () {
            BasePlayingFragment basePlayingFragment = baseLoaderWeakReference.get();
            if (basePlayingFragment != null){
                basePlayingFragment.updateProgress();
            }
            handler.postDelayed(ProgressRunnable.this,1000);
        }
    }

}
