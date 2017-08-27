package com.rks.musicx.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.widget.RemoteViews;

import com.rks.musicx.R;
import com.rks.musicx.database.FavHelper;
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.ui.activities.PlayingActivity;

import static com.rks.musicx.misc.utils.Constants.ACTION_FAV;
import static com.rks.musicx.misc.utils.Constants.ACTION_NEXT;
import static com.rks.musicx.misc.utils.Constants.ACTION_PREVIOUS;
import static com.rks.musicx.misc.utils.Constants.ACTION_TOGGLE;
import static com.rks.musicx.misc.utils.Constants.PLAYSTATE_CHANGED;

/*
 * Created by Coolalien on 02/05/2017.
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

public class NotificationHandler {

    public static final int notificationID = 1127;
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void buildNotification(MusicXService musicXService, String what) {
        if (musicXService == null) {
            return;
        }
        RemoteViews remoteViews = new RemoteViews(musicXService.getPackageName(), R.layout.widget);
        RemoteViews smallremoteView = new RemoteViews(musicXService.getPackageName(), R.layout.small_notification);

        Intent intent = new Intent(musicXService, PlayingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(musicXService, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(musicXService)
                .setWhen(System.currentTimeMillis())
                .setCategory(Intent.CATEGORY_APP_MUSIC)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setShowWhen(false)
                .setAutoCancel(true)
                .setCustomBigContentView(remoteViews)
                .setContent(smallremoteView)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        remoteViews.setOnClickPendingIntent(R.id.item_view, pendInt);
        smallremoteView.setOnClickPendingIntent(R.id.small_item_view, pendInt);
        remoteViews.setTextViewText(R.id.title, musicXService.getsongTitle());
        remoteViews.setTextViewText(R.id.artist, musicXService.getsongArtistName());
        smallremoteView.setTextViewText(R.id.small_title, musicXService.getsongTitle());
        smallremoteView.setTextViewText(R.id.small_artist, musicXService.getsongArtistName());
        FavHelper favHelper = new FavHelper(musicXService);
        if (favHelper.isFavorite(musicXService.getsongId())) {
            remoteViews.setImageViewResource(R.id.action_favorite, R.drawable.ic_action_favorite);
        } else {
            remoteViews.setImageViewResource(R.id.action_favorite, R.drawable.ic_action_favorite_outline);
        }
        if (musicXService.isPlaying()) {
            builder.setSmallIcon(R.drawable.aw_ic_play);
            builder.setOngoing(true);
        } else {
            builder.setSmallIcon(R.drawable.aw_ic_pause);
            builder.setOngoing(false);
        }
        if (what.equals(PLAYSTATE_CHANGED)) {
            if (MediaPlayerSingleton.getInstance().getMediaPlayer().isPlaying()) {
                remoteViews.setImageViewResource(R.id.toggle, R.drawable.aw_ic_play);
                smallremoteView.setImageViewResource(R.id.small_toggle, R.drawable.aw_ic_play);
            } else {
                remoteViews.setImageViewResource(R.id.toggle, R.drawable.aw_ic_pause);
                smallremoteView.setImageViewResource(R.id.small_toggle, R.drawable.aw_ic_pause);
            }
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                ArtworkUtils.ArtworkLoader(musicXService, 300, 300, musicXService.getsongAlbumName(), musicXService.getsongAlbumID(), new palette() {
                    @Override
                    public void palettework(Palette palette) {
                        int colors[] = Helper.getAvailableColor(musicXService, palette);
                        remoteViews.setInt(R.id.item_view, "setBackgroundColor", colors[0]);
                        remoteViews.setInt(R.id.title, "setTextColor", Color.WHITE);
                        remoteViews.setInt(R.id.artist, "setTextColor", Color.WHITE);
                        smallremoteView.setInt(R.id.small_item_view, "setBackgroundColor", colors[0]);
                        smallremoteView.setInt(R.id.small_title, "setTextColor", Color.WHITE);
                        smallremoteView.setInt(R.id.small_artist, "setTextColor", Color.WHITE);
                    }
                }, new bitmap() {
                    @Override
                    public void bitmapwork(Bitmap bitmap) {
                        remoteViews.setImageViewBitmap(R.id.artwork, bitmap);
                        smallremoteView.setImageViewBitmap(R.id.small_artwork, bitmap);
                        NotificationManagerCompat.from(musicXService).notify(notificationID, builder.build());
                    }

                    @Override
                    public void bitmapfailed(Bitmap bitmap) {
                        remoteViews.setImageViewBitmap(R.id.artwork, bitmap);
                        smallremoteView.setImageViewBitmap(R.id.small_artwork, bitmap);
                        NotificationManagerCompat.from(musicXService).notify(notificationID, builder.build());
                    }
                });
            }
        });
        controls(remoteViews, smallremoteView, musicXService);
    }

    private static void controls(RemoteViews remoteViews, RemoteViews smallViews, MusicXService musicXService) {
        PendingIntent toggleIntent = PendingIntent.getService(musicXService, 0, new Intent(musicXService, MusicXService.class).setAction(ACTION_TOGGLE), 0);
        PendingIntent nextIntent = PendingIntent.getService(musicXService, 0, new Intent(musicXService, MusicXService.class).setAction(ACTION_NEXT), 0);
        PendingIntent previousIntent = PendingIntent.getService(musicXService, 0, new Intent(musicXService, MusicXService.class).setAction(ACTION_PREVIOUS), 0);
        PendingIntent savefavIntent = PendingIntent.getService(musicXService, 0, new Intent(musicXService, MusicXService.class).setAction(ACTION_FAV), 0);

        remoteViews.setOnClickPendingIntent(R.id.toggle, toggleIntent);
        remoteViews.setOnClickPendingIntent(R.id.next, nextIntent);
        remoteViews.setOnClickPendingIntent(R.id.prev, previousIntent);
        remoteViews.setOnClickPendingIntent(R.id.action_favorite, savefavIntent);

        smallViews.setOnClickPendingIntent(R.id.small_toggle, toggleIntent);
        smallViews.setOnClickPendingIntent(R.id.small_next, nextIntent);
        smallViews.setOnClickPendingIntent(R.id.small_prev, previousIntent);
    }

    public static int[] getAvailableColor(Context context, Palette palette) {
        int[] temp = new int[3]; //array with size 3
        if (palette.getDarkVibrantSwatch() != null) {
            temp[0] = palette.getDarkVibrantSwatch().getRgb();
            temp[1] = palette.getDarkVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getDarkVibrantSwatch().getBodyTextColor();
        } else if (palette.getDarkMutedSwatch() != null) {
            temp[0] = palette.getDarkMutedSwatch().getRgb();
            temp[1] = palette.getDarkMutedSwatch().getTitleTextColor();
            temp[2] = palette.getDarkMutedSwatch().getBodyTextColor();
        } else if (palette.getVibrantSwatch() != null) {
            temp[0] = palette.getVibrantSwatch().getRgb();
            temp[1] = palette.getVibrantSwatch().getTitleTextColor();
            temp[2] = palette.getVibrantSwatch().getBodyTextColor();
        } else if (palette.getDominantSwatch() != null) {
            temp[0] = palette.getDominantSwatch().getRgb();
            temp[1] = palette.getDominantSwatch().getTitleTextColor();
            temp[2] = palette.getDominantSwatch().getBodyTextColor();
        } else if (palette.getMutedSwatch() != null) {
            temp[0] = palette.getMutedSwatch().getRgb();
            temp[1] = palette.getMutedSwatch().getTitleTextColor();
            temp[2] = palette.getMutedSwatch().getBodyTextColor();
        } else {
            temp[0] = ContextCompat.getColor(context, R.color.MaterialGrey);
            temp[1] = Color.WHITE;
            temp[2] = Color.WHITE;
        }
        return temp;
    }

}
