package com.rks.musicx.misc.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.rks.musicx.R;
import com.rks.musicx.misc.widgets.CircularSeekBar;
import com.rks.musicx.services.MediaPlayerSingleton;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.rks.musicx.R.string.minutes;

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

public class Sleeptimer {

    public static boolean running;
    static CircularSeekBar mcircularSeekBar;
    static TextView mSeekArcProgress;
    private static ScheduledFuture mTask;
    private static boolean timer = false;
    static private CountDownTimer countDownTimer;
    private static String ateKey;
    private static int accentColor;

    public static void showSleepTimer(Activity c) {
        View view = LayoutInflater.from(c).inflate(R.layout.sleeptimer, null);
        mcircularSeekBar = (CircularSeekBar) view.findViewById(R.id.timer_circle);
        mSeekArcProgress = (TextView) view.findViewById(R.id.timer_progress);
        final String start = c.getString(R.string.start);
        final String cancel = c.getString(R.string.cancel);
        ateKey = Helper.getATEKey(c);
        accentColor = Config.accentColor(c, ateKey);
        int primary = Config.primaryColor(c, ateKey);
        if (Extras.getInstance().getDarkTheme() || Extras.getInstance().getBlackTheme()) {
            mcircularSeekBar.setPointerHaloColor(accentColor);
            mcircularSeekBar.setCircleProgressColor(accentColor);
            mcircularSeekBar.setPointerHaloColor(accentColor);
            mSeekArcProgress.setTextColor(ContextCompat.getColor(c, R.color.white));
        } else {
            mcircularSeekBar.setPointerHaloColor(accentColor);
            mcircularSeekBar.setCircleProgressColor(accentColor);
            mSeekArcProgress.setTextColor(ContextCompat.getColor(c, R.color.colorPrimaryText));
            mcircularSeekBar.setCircleColor(primary);
        }
        mcircularSeekBar.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                String minute;
                if (progress < 10) {
                    minute = c.getString(R.string.minute);
                } else {
                    minute = c.getString(minutes);
                }
                String temps = String.valueOf(progress) + " " + minute;
                mSeekArcProgress.setText(temps);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });
        MaterialDialog.Builder sleeptimer = new MaterialDialog.Builder(c);
        sleeptimer.title("SleepTimer");
        sleeptimer.positiveText(start);
        sleeptimer.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                int mins = mcircularSeekBar.getProgress();
                startTimer(view, mins, c);
            }
        });
        sleeptimer.negativeText(cancel);
        sleeptimer.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                sleeptimer.autoDismiss(true);
            }
        });
        sleeptimer.customView(view, false);
        sleeptimer.show();
    }

    public static void showTimerInfo(Activity c) {
        final String Continue = c.getString(R.string.Continue);
        final String cancelTimer = c.getString(R.string.cancel_timer);
        if (mTask.getDelay(TimeUnit.MILLISECONDS) < 0) {
            Stop();
            return;
        }
        View view = LayoutInflater.from(c).inflate(R.layout.timer_info, null);
        final TextView timeLeft = ((TextView) view.findViewById(R.id.time_left));
        if (PreferenceManager.getDefaultSharedPreferences(c).getBoolean("dark_theme", false)) {
            timeLeft.setTextColor(Color.WHITE);
        } else {
            timeLeft.setTextColor(Color.BLACK);
        }
        final String stopTimer = c.getString(R.string.stop_timer);

        MaterialDialog.Builder sleepdialog = new MaterialDialog.Builder(c);
        sleepdialog.title("SleepTimer");
        sleepdialog.positiveText(Continue);
        sleepdialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                sleepdialog.autoDismiss(true);
            }
        });
        sleepdialog.negativeText(cancelTimer);
        sleepdialog.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                Stop();
                Toast.makeText(c, stopTimer, Toast.LENGTH_LONG).show();
            }
        });
        sleepdialog.customView(view, false);
        new CountDownTimer(mTask.getDelay(TimeUnit.MILLISECONDS), 1000) {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onTick(long seconds) {
                long miliseconds = seconds;
                miliseconds = miliseconds / 1000;
                timeLeft.setText(String.format(c.getString(R.string.timer_info), ((miliseconds % 3600) / 60), ((miliseconds % 3600) % 60)));
            }

            @Override
            public void onFinish() {
                sleepdialog.autoDismiss(true);
            }
        }.start();
        sleepdialog.show();
    }

    private static void startTimer(View v, final int minutes, Context c) {
        final String impossible = c.getString(R.string.impossible);
        final String minute = c.getString(R.string.minute);
        final String minutess = c.getString(R.string.minutes);
        final String stop = c.getString(R.string.stop);
        final String minuteTxt;
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final int delay = (minutes * 60) * 1000;
        if (delay == 0) {
            Toast.makeText(c, impossible, Toast.LENGTH_LONG).show();
            return;
        }
        if (minutes < 10) {
            minuteTxt = minute;
        } else {
            minuteTxt = minutess;
        }
        mTask = scheduler.schedule(new runner(c), delay, TimeUnit.MILLISECONDS);
        Toast.makeText(c, stop + " " + minutes + " " + minuteTxt, Toast.LENGTH_LONG).show();
        running = true;
        setState(true);
        reduceVolume(delay);
    }

    public static void setState(boolean onOff) {
        timer = onOff;
    }

    public static void stopTimer() {
        if (running) mTask.cancel(true);
        running = false;
    }

    private static void Stop() {
        if (running) {
            mTask.cancel(true);
            countDownTimer.cancel();
            countDownTimer.onFinish();
        }
        running = false;
        setState(false);
    }

    public static void reduceVolume(final int delay) {

        final short minutes = (short) (((delay / 1000) % 3600) / 60);
        final boolean tempsMinute = minutes > 10;
        int cycle;
        if (tempsMinute) {
            cycle = 60000;
        } else {
            cycle = 1000;
        }
        countDownTimer = new CountDownTimer(delay, cycle) {
            @Override
            public void onTick(long mseconds) {
                long temps1 = ((mTask.getDelay(TimeUnit.MILLISECONDS) / 1000) % 3600) / 60;
                long temps2 = mTask.getDelay(TimeUnit.MILLISECONDS) / 1000;
                if (tempsMinute) {
                    if (temps1 < 1) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.1f, 0.1f);
                    } else if (temps1 < 2) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.2f, 0.2f);
                    } else if (temps1 < 3) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.3f, 0.3f);
                    } else if (temps1 < 4) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.4f, 0.4f);
                    } else if (temps1 < 5) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.5f, 0.5f);
                    } else if (temps1 < 6) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.6f, 0.6f);
                    } else if (temps1 < 7) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.7f, 0.7f);
                    } else if (temps1 < 8) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.8f, 0.8f);
                    } else if (temps1 < 9) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.9f, 0.9f);
                    } else if (temps1 < 10) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
                    }
                } else {
                    if (temps2 < 6) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.1f, 0.1f);
                    } else if (temps2 < 12) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.2f, 0.2f);
                    } else if (temps2 < 18) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.3f, 0.3f);
                    } else if (temps2 < 24) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.4f, 0.4f);
                    } else if (temps2 < 30) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.5f, 0.5f);
                    } else if (temps2 < 36) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.6f, 0.6f);
                    } else if (temps2 < 42) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.7f, 0.7f);
                    } else if (temps2 < 48) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.8f, 0.8f);
                    } else if (temps2 < 54) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(0.9f, 0.9f);
                    } else if (temps2 < 60) {
                        MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
                    }
                }
            }

            @Override
            public void onFinish() {
                MediaPlayerSingleton.getInstance().getMediaPlayer().setVolume(1.0f, 1.0f);
            }

        }.start();
    }

    private static class runner implements Runnable {
        Context c;

        public runner(Context c) {
            this.c = c;
        }

        @Override
        public void run() {
            ((AudioManager) c.getSystemService(Context.AUDIO_SERVICE)).requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            stopTimer();
        }
    }
}
