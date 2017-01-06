package com.rks.musicx.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.rks.musicx.services.MusicXService;
import com.rks.musicx.services.PlayingRequestListerner;

import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;

/**
 * Created by Coolalien on 8/16/2016.
 */

public abstract class BaseFragment extends android.support.v4.app.Fragment {

    private Intent mServiceIntent;
    public MusicXService musicXService;
    private boolean mServiceBound;

    protected abstract void reload();
    protected abstract void playbackConfig();
    protected abstract void metaConfig();
    protected abstract void queueConfig(String action);

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicXService.MusicXBinder binder = (MusicXService.MusicXBinder) service;
            musicXService = binder.getService();
            PlayingRequestListerner.sendRequests(musicXService);
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

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (musicXService == null) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(PLAYSTATE_CHANGED)) {

                playbackConfig();

            } else if (action.equals(META_CHANGED)) {

                metaConfig();

            } else if (action.equals(QUEUE_CHANGED) || action.equals(POSITION_CHANGED) || action.equals(ITEM_ADDED) || action.equals(ORDER_CHANGED)) {

                queueConfig(action);
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        if (!mServiceBound) {
            mServiceIntent = new Intent(getActivity(), MusicXService.class);
            getActivity().bindService(mServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            getActivity().startService(mServiceIntent);
            IntentFilter filter = new IntentFilter();
            filter.addAction(META_CHANGED);
            filter.addAction(PLAYSTATE_CHANGED);
            filter.addAction(POSITION_CHANGED);
            filter.addAction(ITEM_ADDED);
            filter.addAction(ORDER_CHANGED);
            getActivity().registerReceiver(broadcastReceiver, filter);
        } else {
            if (musicXService != null) {
                reload();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        musicXService = null;
        if (mServiceBound) {
            getActivity().unbindService(serviceConnection);
            getActivity().unregisterReceiver(broadcastReceiver);
            mServiceBound = false;
        }
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

}
