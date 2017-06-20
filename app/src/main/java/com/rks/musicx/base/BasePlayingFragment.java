package com.rks.musicx.base;

import android.app.Activity;
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
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rks.musicx.R;
import com.rks.musicx.data.model.Song;
import com.rks.musicx.database.CommonDatabase;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.misc.utils.PlaylistHelper;
import com.rks.musicx.services.MusicXService;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.activities.SettingsActivity;
import com.rks.musicx.ui.adapters.QueueAdapter;
import com.rks.musicx.ui.fragments.TagEditorFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;
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

public abstract class BasePlayingFragment extends Fragment implements ImageChooserListener {

    private static Handler handler;
    public boolean isalbumArtChanged;
    private MusicXService musicXService;
    private Intent mServiceIntent;
    private boolean mServiceBound = false;
    private String finalPath;
    private ChosenImage chosenImages;
    private ImageChooserManager imageChooserManager;
    private String mediaPath;
    private Helper helper;
    private int size;
    private CommonDatabase commonDatabase;
    private Drawable shuffleOff, shuffleOn, repeatAll, repeatOne, noRepeat;


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

    public static Handler getHandler() {
        return handler;
    }

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
        helper = new Helper(getContext());
        size = getResources().getDimensionPixelSize(R.dimen.cover_size);
        commonDatabase = new CommonDatabase(getContext(), Queue_TableName, true);
        handler = new Handler();
        shuffleOff = ContextCompat.getDrawable(getContext(), R.drawable.shuf_off);
        shuffleOn = ContextCompat.getDrawable(getContext(), R.drawable.shuf_on);
        repeatOne = ContextCompat.getDrawable(getContext(), R.drawable.rep_one);
        repeatAll = ContextCompat.getDrawable(getContext(), R.drawable.rep_all);
        noRepeat = ContextCompat.getDrawable(getContext(), R.drawable.rep_no);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() == null) {
            return;
        }
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
            mServiceIntent = new Intent(getActivity(), MusicXService.class);
            getActivity().bindService(mServiceIntent, serviceConnection, BIND_AUTO_CREATE);
            getActivity().startService(mServiceIntent);
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
        if (resultCode == Activity.RESULT_OK) {
            if (imageChooserManager == null) {
                imageChooserManager = new ImageChooserManager(this, requestCode, true);
                imageChooserManager.setImageChooserListener(this);
                imageChooserManager.reinitialize(mediaPath);
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
            mediaPath = imageChooserManager.choose();
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

    /**
     * Playing Menu
     *
     * @param view
     */
    public void playingMenu(QueueAdapter queueAdapter, View view, boolean torf) {
        if (getActivity() == null) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.playing_menu, popupMenu.getMenu());
        popupMenu.getMenu().findItem(R.id.action_share).setVisible(torf);
        popupMenu.getMenu().findItem(R.id.action_eq).setVisible(torf);
        popupMenu.getMenu().findItem(R.id.action_savequeue).setVisible(false);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.clear_queue:
                        if (queueAdapter.getSnapshot().size() > 0) {
                            queueAdapter.clear();
                            queueAdapter.notifyDataSetChanged();
                            musicXService.clearQueue();
                            try {
                                commonDatabase.removeAll();
                            }finally {
                                commonDatabase.close();
                            }
                            Toast.makeText(getContext(), "Cleared Queue", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.action_changeArt:
                        pickupArtwork();
                        isalbumArtChanged = false;
                        break;
                    case R.id.action_playlist:
                        PlaylistHelper.PlaylistChooser(BasePlayingFragment.this, getContext(), musicXService.getsongId());
                        break;
                    case R.id.action_lyrics:
                        helper.searchLyrics(getContext(), musicXService.getsongTitle(), musicXService.getsongArtistName(), musicXService.getsongData(), lyricsView());
                        break;
                    case R.id.action_edit_tags:
                        Extras.getInstance().saveMetaData(musicXService.getCurrentSong());
                        ((PlayingActivity) getActivity()).setFragment(TagEditorFragment.getInstance());
                        queueAdapter.notifyDataSetChanged();
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), musicXService.getsongData());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), musicXService.getsongTitle(), musicXService.getsongAlbumName(), musicXService.getsongArtistName(), musicXService.getsongNumber(), musicXService.getsongData());
                        break;
                    case R.id.action_eq:
                        Intent i = new Intent(getContext(), EqualizerActivity.class);
                        getContext().startActivity(i);
                        break;
                    case R.id.action_share:
                        Helper.shareMusic(musicXService.getsongData(), getContext());
                        break;
                    case R.id.action_settings:
                        Intent intent = new Intent(getContext(), SettingsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getActivity().startActivity(intent);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    /**
     * for next update
     */
    private void saveQueue(){
        List<String> queueName = new ArrayList<>();
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
                            CommonDatabase commonDatabase = new CommonDatabase(getContext(), editText.getText().toString(), true);
                            commonDatabase.add(musicXService.getPlayList());
                            commonDatabase.close();
                            queueName.add(editText.getText().toString());
                            Extras.getInstance().saveQueueName(queueName);
                            Toast.makeText(getContext(), "Saved Queue", Toast.LENGTH_SHORT).show();
                            editText.getText().clear();
                        }
                    }
                })
                .customView(view1, false)
                .build()
                .show();
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
                        ((PlayingActivity) getActivity()).setFragment(TagEditorFragment.getInstance());
                        queueAdapter.notifyDataSetChanged();
                        break;
                    case R.id.action_ringtone:
                        Helper.setRingTone(getContext(), queue.getmSongPath());
                        break;
                    case R.id.action_trackdetails:
                        Helper.detailMusic(getContext(), queue.getTitle(), queue.getAlbum(), queue.getArtist(), queue.getTrackNumber(), queue.getmSongPath());
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

    public void seekbarProgress() {
        ProgressRunnable progressRunnable = new ProgressRunnable(BasePlayingFragment.this);
        handler.post(progressRunnable);
    }

    public void removeCallback() {
        ProgressRunnable progressRunnable = new ProgressRunnable(BasePlayingFragment.this);
        handler.removeCallbacks(progressRunnable);
        handler.removeCallbacksAndMessages(null);
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
