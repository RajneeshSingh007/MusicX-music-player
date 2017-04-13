package com.rks.musicx.ui.activities;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.palette.BitmapPalette;
import com.palette.GlidePalette;
import com.rks.musicx.R;
import com.rks.musicx.misc.utils.ArtworkUtils;
import com.rks.musicx.misc.utils.Helper;
import com.rks.musicx.services.MusicXService;

import static com.rks.musicx.misc.utils.Constants.ACTION_NEXT;
import static com.rks.musicx.misc.utils.Constants.ACTION_PLAYINGVIEW;
import static com.rks.musicx.misc.utils.Constants.ACTION_PREVIOUS;
import static com.rks.musicx.misc.utils.Constants.ACTION_TOGGLE;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class MusicxWidget extends AppWidgetProvider {

    public static void musicxWidget(int updatdeID[], MusicXService musicXService) {
        if (musicXService == null) {
            return;
        }
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(musicXService);
        RemoteViews remoteViews = new RemoteViews(musicXService.getPackageName(), R.layout.widget);
        remoteViews.setTextViewText(R.id.title, musicXService.getsongTitle());
        remoteViews.setTextViewText(R.id.artist, musicXService.getsongArtistName());

        Target<Bitmap> bitmapTarget = new BaseTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                if (resource != null) {
                    remoteViews.setBitmap(R.id.artwork, "", resource);
                } else {
                    remoteViews.setBitmap(R.id.artwork, "", ArtworkUtils.getDefaultArtwork(musicXService));
                }
            }

            @Override
            public void getSize(SizeReadyCallback cb) {

            }
        };
        Glide.with(musicXService)
                .load(ArtworkUtils.uri(musicXService.getsongAlbumID()))
                .asBitmap()
                .error(R.mipmap.ic_launcher)
                .placeholder(R.mipmap.ic_launcher)
                .centerCrop()
                .override(300, 300)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(GlidePalette.with(ArtworkUtils.uri(musicXService.getsongAlbumID()).toString()).intoCallBack(new BitmapPalette.CallBack() {
                    @Override
                    public void onPaletteLoaded(@Nullable Palette palette) {
                        final int color[] = Helper.getAvailableColor(musicXService, palette);
                        remoteViews.setInt(R.id.artist, "setTextColor", color[0]);
                    }
                }))
                .into(bitmapTarget);
        remoteViews.setInt(R.id.item_view, "setBackgroundColor", Color.WHITE);
        if (musicXService.isPlaying()) {
            remoteViews.setImageViewResource(R.id.toggle, R.drawable.aw_ic_pause);
        } else {
            remoteViews.setImageViewResource(R.id.toggle, R.drawable.aw_ic_play);
        }
        controls(remoteViews, musicXService);
        widgetManager.updateAppWidget(updatdeID, remoteViews);

    }

    private static void controls(RemoteViews remoteViews, Context context) {
        PendingIntent clickedview = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_PLAYINGVIEW), 0);
        PendingIntent nextIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_NEXT), 0);
        PendingIntent previousIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_PREVIOUS), 0);
        PendingIntent toggleIntent = PendingIntent.getService(context, 0, new Intent(context, MusicXService.class).setAction(ACTION_TOGGLE), 0);

        remoteViews.setOnClickPendingIntent(R.id.item_view, clickedview);
        remoteViews.setOnClickPendingIntent(R.id.toggle, toggleIntent);
        remoteViews.setOnClickPendingIntent(R.id.next, nextIntent);
        remoteViews.setOnClickPendingIntent(R.id.prev, previousIntent);
    }

    private void updateWidget(int id[], AppWidgetManager widgetManager, Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        controls(remoteViews, context);
        widgetManager.updateAppWidget(id, remoteViews);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        updateWidget(appWidgetIds, appWidgetManager, context);
        context.startService(new Intent(context, MusicXService.class));
    }

}
