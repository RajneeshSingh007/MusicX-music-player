package com.rks.musicx.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.KeyEvent;

import com.rks.musicx.misc.utils.Constants;


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

/**
 * @ Thanks to phonograph for button fixes
 */

public class MediaButtonReceiver extends WakefulBroadcastReceiver {

    private static final int MSG_HEADSET_DOUBLE_CLICK_TIMEOUT = 2;
    private static final int DOUBLE_CLICK = 400;
    private static PowerManager.WakeLock mWakeLock = null;
    private static int mClickCounter = 0;
    private static long mLastClickTime = 0;
    @SuppressLint("HandlerLeak") // false alarm, handler is already static
    private static Handler mHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case MSG_HEADSET_DOUBLE_CLICK_TIMEOUT:
                    final int clickCount = msg.arg1;
                    final String command;

                    Log.e("MediaButton", "Handling headset click, count = " + clickCount);
                    switch (clickCount) {
                        case 1:
                            command = Constants.ACTION_TOGGLE;
                            break;
                        case 2:
                            command = Constants.ACTION_NEXT;
                            break;
                        case 3:
                            command = Constants.ACTION_PREVIOUS;
                            break;
                        default:
                            command = null;
                            break;
                    }

                    if (command != null) {
                        final Context context = (Context) msg.obj;
                        startServices(context, command);
                    }
                    break;
            }
            releaseWakeLockIfHandlerIdle();
        }
    };
    private final String TAG = "MediaButtonReceiver";

    /**
     * Release
     */
    private static void releaseWakeLockIfHandlerIdle() {
        if (mHandler.hasMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT)) {
            return;
        }

        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private static void acquireWakeLockAndSendMessage(Context context, Message msg, long delay) {
        if (mWakeLock == null) {
            Context appContext = context.getApplicationContext();
            PowerManager pm = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Musicx headset button");
            mWakeLock.setReferenceCounted(false);
        }
        // Make sure we don't indefinitely hold the wake lock under any circumstances
        mWakeLock.acquire(10000);
        mHandler.sendMessageDelayed(msg, delay);
    }

    /**
     * Start service
     *
     * @param context
     * @param action
     */
    public static void startServices(Context context, String action) {
        Intent intent1 = new Intent(context, MusicXService.class);
        intent1.setAction(action);
        context.startService(intent1);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        String command = null;
        if (intent.getAction() != null) {
            if (intentAction.equals(Intent.ACTION_MEDIA_BUTTON)) {
                KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (event == null) {
                    return;
                }

                int keycode = event.getKeyCode();
                final int action = event.getAction();
                final long eventTime = event.getEventTime();


                switch (keycode) {
                    case KeyEvent.KEYCODE_MEDIA_STOP:
                        Log.d(TAG, "stop");
                        command = Constants.ACTION_STOP;
                        break;
                    case KeyEvent.KEYCODE_HEADSETHOOK:
                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                        Log.d(TAG, "toggle");
                        command = Constants.ACTION_TOGGLE;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        Log.d(TAG, "next");
                        command = Constants.ACTION_NEXT;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        Log.d(TAG, "prev");
                        command = Constants.ACTION_PREVIOUS;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        Log.d(TAG, "pause");
                        command = Constants.ACTION_PAUSE;
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        Log.d(TAG, "play");
                        command = Constants.ACTION_PLAY;
                        break;
                }
                // startServices(context, command);
                if (command != null) {
                    if (action == KeyEvent.ACTION_DOWN) {
                        if (event.getRepeatCount() == 0) {
                            if (keycode == KeyEvent.KEYCODE_HEADSETHOOK) {
                                if (eventTime - mLastClickTime >= DOUBLE_CLICK) {
                                    mClickCounter = 0;
                                }

                                mClickCounter++;
                                Log.e("MediaButton", "Got headset click, count = " + mClickCounter);
                                mHandler.removeMessages(MSG_HEADSET_DOUBLE_CLICK_TIMEOUT);

                                Message msg = mHandler.obtainMessage(
                                        MSG_HEADSET_DOUBLE_CLICK_TIMEOUT, mClickCounter, 0, context);

                                long delay = mClickCounter < 3 ? DOUBLE_CLICK : 0;
                                if (mClickCounter >= 3) {
                                    mClickCounter = 0;
                                }
                                mLastClickTime = eventTime;
                                acquireWakeLockAndSendMessage(context, msg, delay);
                            } else {
                                startServices(context, command);
                            }
                        }
                    }
                }
            }
        }
    }
}
