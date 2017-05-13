package com.rks.musicx.services;

import android.content.Context;
import android.content.Intent;
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

public class MediaButtonReceiver extends android.support.v4.media.session.MediaButtonReceiver {

    private final String TAG = "MediaButtonReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        String command = null;
        KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (intent.getAction() != null){
            if (intentAction.equals(Intent.ACTION_MEDIA_BUTTON) && event.getAction() == KeyEvent.ACTION_UP) {
                switch (event.getKeyCode()) {
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
                startServices(context, command);
            }
        }
    }

    public void startServices(Context context, String action){
        Intent intent1 = new Intent(action);
        context.sendBroadcast(intent1);
    }
}
