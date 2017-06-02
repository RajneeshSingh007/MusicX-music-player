package com.rks.musicx.misc.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.palette.BitmapPalette;
import com.palette.GlidePalette;
import com.rks.musicx.R;
import com.rks.musicx.interfaces.bitmap;
import com.rks.musicx.interfaces.palette;
import com.rks.musicx.misc.widgets.BlurArtwork;

import java.io.File;

import static com.rks.musicx.misc.utils.Constants.BlurView;

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

public class ArtworkUtils {


    private static ArtworkUtils sInstance;
    private Context mcontext;

    public ArtworkUtils(Context context) {
        this.mcontext = context;
    }

    public static void init(Context context) {
        sInstance = new ArtworkUtils(context);
    }

    public static ArtworkUtils getInstance() {
        return sInstance;
    }

    public static Uri uri(long key) {
        Uri albumCover = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(albumCover, key);
    }


    public static Bitmap getEmbedArtwork(String path) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        byte[] art;
        Bitmap bitmap = null;
        mediaMetadataRetriever.setDataSource(path);
        try {
            art = mediaMetadataRetriever.getEmbeddedPicture();
            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
        } catch (Exception e) {
            Log.d("AlbumArtwork", "oops Error Buddy", e);
        }
        return bitmap;
    }

    public static File getAlbumCoverPath(Context context, String album) {
        String albumImagePath = new Helper(context).loadAlbumImage(album);
        File file = new File(albumImagePath);
        return file;
    }

    public static File getArtistCoverPath(Context context, String artist) {
        String artistImagePath = new Helper(context).loadArtistImage(artist);
        File file = new File(artistImagePath);
        return file;
    }


    private static Target loadArtwork(Context context, String path, palette palettework, bitmap bitmapwork) {
        if (Extras.getInstance().getHdArtwork()){
            return Glide.with(context)
                    .load(path)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .fitCenter()
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .listener(GlidePalette.with(path).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmapwork.bitmapwork(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            bitmapwork.bitmapfailed(drawableToBitmap(errorDrawable));
                        }

                    });
        }else {
            return Glide.with(context)
                    .load(path)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.mipmap.ic_launcher)
                    .error(R.mipmap.ic_launcher)
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .fitCenter()
                    .override(300, 300)
                    .listener(GlidePalette.with(path).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmapwork.bitmapwork(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            bitmapwork.bitmapfailed(drawableToBitmap(errorDrawable));
                        }

                    });
        }
    }

    private static Target<Bitmap> loadArtwork(Context context, long key, palette palettework, bitmap bitmapwork) {
        if (Extras.getInstance().getHdArtwork()){
           return Glide.with(context)
                   .load(uri(key))
                   .asBitmap()
                   .diskCacheStrategy(DiskCacheStrategy.NONE)
                   .skipMemoryCache(true)
                   .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher)
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .listener(GlidePalette.with(uri(key).toString()).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                     }))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmapwork.bitmapwork(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            bitmapwork.bitmapfailed(drawableToBitmap(errorDrawable));
                        }

                    });
        }else {
            return Glide.with(context)
                    .load(uri(key))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true).override(300, 300)
                    .error(R.mipmap.ic_launcher)
                    .placeholder(R.mipmap.ic_launcher)
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .listener(GlidePalette.with(uri(key).toString()).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmapwork.bitmapwork(resource);
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            bitmapwork.bitmapfailed(drawableToBitmap(errorDrawable));
                        }

                    });
        }
    }

    /**
     * Album Artwork Loader
     *
     * @param context
     * @param album
     * @param key
     * @param palettework
     * @param bitmapwork
     */
    public static void ArtworkLoader(Context context, String album, String path, long key, palette palettework, bitmap bitmapwork) {
        if (album != null && key != -1 && path == null){
            if (getAlbumCoverPath(context, album).exists()) {
                loadArtwork(context, getAlbumCoverPath(context, album).getAbsolutePath(), palettework, bitmapwork);
            } else {
                loadArtwork(context, key, palettework, bitmapwork);
            }
        } else {
            loadArtwork(context, path, palettework, bitmapwork);
        }
    }


    public static AsyncTask<String, Void, String> getBlurArtwork(Context context, int radius, Bitmap bitmap, ImageView imageView, float scale) {
        BlurArtwork blurArtwork = new BlurArtwork(context, radius, bitmap, imageView, scale);
        return blurArtwork.execute("Executed");
    }

    public static void blurPreferances(Context context, Bitmap blurBitmap, ImageView imageView) {
        String blurView = Extras.getInstance().getmPreferences().getString(BlurView, Constants.Two);
        switch (blurView) {
            case Constants.Zero:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 1.0f);
                break;
            case Constants.One:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.8f);
                break;
            case Constants.Two:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.6f);
                break;
            case Constants.Three:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.4f);
                break;
            case Constants.Four:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 0.2f);
                break;
            default:
                getBlurArtwork(context, 25, blurBitmap, imageView, 0.2f);
                break;
        }
    }


    public static int radius() {
        int radius = 1;
        String blurView = Extras.getInstance().getmPreferences().getString(BlurView, Constants.Zero);
        switch (blurView) {
            case Constants.Zero:
                radius = 5;
                return radius;
            case Constants.One:
                radius = 10;
                return radius;
            case Constants.Two:
                radius = 15;
                return radius;
            case Constants.Three:
                radius = 20;
                return radius;
            case Constants.Four:
                radius = 25;
                return radius;
        }
        return radius;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }


    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

}

