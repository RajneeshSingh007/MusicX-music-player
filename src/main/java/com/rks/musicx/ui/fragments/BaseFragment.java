package com.rks.musicx.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.afollestad.appthemeengine.ATE;
import com.rks.musicx.misc.utils.Extras;
import com.rks.musicx.services.MusicXService;

import static com.rks.musicx.misc.utils.Constants.ITEM_ADDED;
import static com.rks.musicx.misc.utils.Constants.META_CHANGED;
import static com.rks.musicx.misc.utils.Constants.ORDER_CHANGED;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;
import static com.rks.musicx.misc.utils.Constants.POSITION_CHANGED;
import static com.rks.musicx.misc.utils.Constants.QUEUE_CHANGED;

/*
 * Created by Coolalien on 6/28/2016.
 */

public abstract class BaseFragment extends android.support.v4.app.Fragment {

    public MusicXService musicXService;
    private Intent mServiceIntent;
    private boolean mServiceBound;

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
                    queueConfig(action);
                    break;
            }
        }
    };

    protected abstract void reload();

    protected abstract void playbackConfig();

    protected abstract void metaConfig();

    protected abstract void queueConfig(String action);

    protected abstract void onPaused();

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() == null) {
            return;
        }
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() == null) {
            return;
        }
        musicXService = null;
        if (mServiceBound) {
            getActivity().unbindService(serviceConnection);
            mServiceBound = false;
            try {
                getActivity().unregisterReceiver(broadcastReceiver);
            } catch (Exception e) {
                // already unregistered
            }
        }
        onPaused();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Extras.getInstance().mPreferences.getBoolean("dark_theme", false)) {
            ATE.postApply(getActivity(), "dark_theme");
        } else {
            ATE.postApply(getActivity(), "light_theme");
        }
    }

}
