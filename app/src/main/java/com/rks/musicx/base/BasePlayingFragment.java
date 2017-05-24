package com.rks.musicx.base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.TextView;
import android.widget.Toast;

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
import com.rks.musicx.services.MusicXService;
import com.rks.musicx.ui.activities.EqualizerActivity;
import com.rks.musicx.ui.activities.PlayingActivity;
import com.rks.musicx.ui.adapters.QueueAdapter;
import com.rks.musicx.ui.fragments.TagEditorFragment;

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

    public boolean isalbumArtChanged;
    private MusicXService musicXService;
    private Intent mServiceIntent;
    private boolean mServiceBound = false;
    private String finalPath;
    private ChosenImage chosenImages;
    private ImageChooserManager imageChooserManager;
    private String mediaPath;
    private Helper helper;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(setLayout(), container, false);
        ui(rootView);
        function();
        helper = new Helper(getContext());
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
            shuffleButton().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_on));
        } else {
            shuffleButton().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shuf_off));
        }
    }

    /**
     * Repeat Button Update
     */
    public void updateRepeatButton() {
        int mode = musicXService.getRepeatMode();
        if (mode == getMusicXService().getNoRepeat()) {
            repeatButton().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_no));
        } else if (mode == getMusicXService().getRepeatCurrent()) {
            repeatButton().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_all));
        } else if (mode == getMusicXService().getRepeatAll()) {
            repeatButton().setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.rep_one));
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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.clear_queue:
                        if (queueAdapter.getSnapshot().size() > 0) {
                            queueAdapter.clear();
                            queueAdapter.notifyDataSetChanged();
                            musicXService.clearQueue();
                            CommonDatabase commonDatabase = new CommonDatabase(getContext(), Queue_TableName);
                            commonDatabase.removeAll();
                            Toast.makeText(getContext(), "Cleared Queue", Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case R.id.action_changeArt:
                        pickupArtwork();
                        isalbumArtChanged = false;
                        break;
                    case R.id.action_playlist:
                        helper.PlaylistChooser(BasePlayingFragment.this, getContext(), musicXService.getsongId());
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

                }
                return false;
            }
        });
        popupMenu.show();
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
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_playlist:
                        helper.PlaylistChooser(BasePlayingFragment.this, getContext(), queue.getId());
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

}
