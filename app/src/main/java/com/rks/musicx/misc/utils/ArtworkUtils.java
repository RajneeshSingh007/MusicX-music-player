package com.rks.musicx.misc.utils;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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


    private static ArtworkUtils sInstance = null;

    public ArtworkUtils() {
        if (sInstance == null){
            sInstance = new ArtworkUtils();
        }
    }

    public static ArtworkUtils getInstance() {
        return sInstance;
    }

    /**
     * Artwork
     * @param key
     * @return
     */
    public static Uri uri(long key) {
        Uri albumCover = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(albumCover, key);
    }

    /**
     * Artwork from musicx/.albumArtwork folder
     * @param context
     * @param album
     * @return
     */
    public static File getAlbumCoverPath(Context context, String album) {
        String albumImagePath = new Helper(context).loadAlbumImage(album);
        File file = new File(albumImagePath);
        return file;
    }

    /**
     * Artist artwork from musicx/.artistArtwork folder
     * @param context
     * @param artist
     * @return
     */
    public static File getArtistCoverPath(Context context, String artist) {
        String artistImagePath = new Helper(context).loadArtistImage(artist);
        File file = new File(artistImagePath);
        return file;
    }

    /**
     * Return albumArtwork name
     * @param context
     * @param album
     * @return
     */
    public static String getAlbumFileName(Context context, String album){
        String filename = getAlbumCoverPath(context, album).getName();
        int pos = filename.lastIndexOf(".");
        String justName = pos > 0 ? filename.substring(0, pos) : filename;
        return justName;
    }

    /**
     * Return artistArtwork name
     * @param context
     * @param artist
     * @return
     */
    public static String getArtistFileName(Context context, String artist){
        String filename = getArtistCoverPath(context, artist).getName();
        int pos = filename.lastIndexOf(".");
        String justName = pos > 0 ? filename.substring(0, pos) : filename;
        return justName;
    }

    /**
     * Bitmap Artwork with path
     * @param context
     * @param size
     * @param bigsize
     * @param path
     * @param palettework
     * @param bitmapwork
     * @return
     */
    private static Target loadArtwork(Context context, int size, int bigsize, String path, palette palettework, bitmap bitmapwork) {
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
                    .override(bigsize, bigsize)
                    .listener(GlidePalette.with(path).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmapwork.bitmapwork(optimizeBitmap(resource, bigsize));
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
                    .override(size, size)
                    .listener(GlidePalette.with(path).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            bitmapwork.bitmapwork(optimizeBitmap(resource, size));
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            bitmapwork.bitmapfailed(drawableToBitmap(errorDrawable));
                        }

                    });
        }
    }

    /**
     * Bitmap Artwork with albumID
     * @param context
     * @param size
     * @param bigsize
     * @param key
     * @param palettework
     * @param bitmapwork
     * @return
     */
    private static Target loadArtwork(Context context, int size, int bigsize, long key, palette palettework, bitmap bitmapwork) {
        if (Extras.getInstance().getHdArtwork()){
            return Glide.with(context)
                    .load(uri(key))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(bigsize, bigsize)
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
                            bitmapwork.bitmapwork(optimizeBitmap(resource, bigsize));
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
                    .skipMemoryCache(true)
                    .override(size, size)
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
                            bitmapwork.bitmapwork(optimizeBitmap(resource, size));
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            bitmapwork.bitmapfailed(drawableToBitmap(errorDrawable));
                        }

                    });
        }
    }


    /**
     * Load Artwork
     * @param context
     * @param size
     * @param bigsize
     * @param key
     * @param palettework
     * @param imageView
     * @return
     */
    private static Target loadArtwork(Context context, int size, int bigsize, long key, palette palettework, ImageView imageView) {
        if (Extras.getInstance().getHdArtwork()){
            return Glide.with(context)
                    .load(uri(key))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(bigsize, bigsize)
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
                    .into(imageView);
        }else {
            return Glide.with(context)
                    .load(uri(key))
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .override(size, size)
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
                    .into(imageView);
        }
    }


    /**
     * Load Artwork
     * @param context
     * @param size
     * @param bigsize
     * @param path
     * @param palettework
     * @param imageView
     * @return
     */
    private static Target loadArtwork(Context context, int size, int bigsize, String path, palette palettework, ImageView imageView) {
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
                    .override(bigsize, bigsize)
                    .listener(GlidePalette.with(path).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(imageView);
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
                    .override(size, size)
                    .listener(GlidePalette.with(path).intoCallBack(new BitmapPalette.CallBack() {
                        @Override
                        public void onPaletteLoaded(@Nullable Palette palette) {
                            palettework.palettework(palette);
                        }
                    }))
                    .into(imageView);
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
    public static void ArtworkLoader(Context context, int size, int bigsize, String album, long key, palette palettework, bitmap bitmapwork) {
        if (Extras.getInstance().getDownloadedArtwork()){
            Helper helper = new Helper(context);
            loadArtwork(context, size, bigsize, helper.loadAlbumImage(album), palettework, bitmapwork);
        }else {
            loadArtwork(context, size, bigsize, key, palettework, bitmapwork);
        }
    }

    /**
     * Album Artwork Loader
     *
     * @param context
     * @param album
     * @param key
     * @param palettework
     * @param imageView
     */
    public static void ArtworkLoader(Context context, int size, int bigsize, String album, long key, palette palettework, ImageView imageView) {
        if (Extras.getInstance().getDownloadedArtwork()){
            Helper helper = new Helper(context);
            loadArtwork(context, size, bigsize, helper.loadAlbumImage(album), palettework, imageView);
        }else {
            loadArtwork(context, size, bigsize, key, palettework, imageView);
        }
    }


    /**
     * Artist Artwork Loader
     *
     * @param context
     * @param path
     * @param palettework
     * @param imageView
     */
    public static void ArtworkLoader(Context context, int size, int bigsize,String path, palette palettework, ImageView imageView) {
        loadArtwork(context, size, bigsize, path, palettework, imageView);
    }

    private static AsyncTask<Drawable, Void, Drawable> getBlurArtwork(Context context, int radius, Bitmap bitmap, ImageView imageView, int scale) {
        return new BlurArtwork(context, radius, bitmap, imageView, scale).execute();
    }

    public static void blurPreferances(Context context, Bitmap blurBitmap, ImageView imageView) {
        switch (Extras.getInstance().getBlurView()) {
            case Constants.Zero:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 1);
                break;
            case Constants.One:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 2);
                break;
            case Constants.Two:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 3);
                break;
            case Constants.Three:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 4);
                break;
            case Constants.Four:
                getBlurArtwork(context, radius(), blurBitmap, imageView, 5);
                break;
            default:
                getBlurArtwork(context, 25, blurBitmap, imageView, 6);
                break;
        }
    }


    public static int radius() {
        int radius = 1;
        switch (Extras.getInstance().getBlurView()) {
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

    /**
     * optimize bitmap
     * @param bitmap
     * @return
     */
    public static Bitmap optimizeBitmap(@NonNull Bitmap bitmap, int size) {
        if (!bitmap.isRecycled()) {
            // convert bitmap into inputStream
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // bitmap compress
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            // bytes into array
            InputStream is = new ByteArrayInputStream(stream.toByteArray());
            BufferedInputStream imageFileStream = new BufferedInputStream(is);
            try {
                // Phase 1: Get a reduced size image. In this part we will do a rough scale down
                int sampleSize = 1;
                if (size > 0 && size > 0) {
                    final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
                    decodeBoundsOptions.inJustDecodeBounds = true;
                    imageFileStream.mark(64 * 1024);
                    BitmapFactory.decodeStream(imageFileStream, null, decodeBoundsOptions);
                    imageFileStream.reset();
                    final int originalWidth = decodeBoundsOptions.outWidth;
                    final int originalHeight = decodeBoundsOptions.outHeight;
                    // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
                    sampleSize = Math.max(1, Math.max(originalWidth / size, originalHeight / size));
                }
                BitmapFactory.Options decodeBitmapOptions = new BitmapFactory.Options();
                decodeBitmapOptions.inSampleSize = sampleSize;
                //decodeBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888; // Uses 2-bytes instead of default 4 per pixel

                // Get the roughly scaled-down image
                Bitmap bmp = BitmapFactory.decodeStream(imageFileStream, null, decodeBitmapOptions);

                // Phase 2: Get an exact-size image - no dimension will exceed the desired value
                float ratio = Math.min((float) size / (float) bmp.getWidth(), (float) size / (float) bmp.getHeight());
                int w = (int) ((float) bmp.getWidth() * ratio);
                int h = (int) ((float) bmp.getHeight() * ratio);

                // finally scaled bitmap
                return Bitmap.createScaledBitmap(bmp, w, h, true);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    imageFileStream.close();
                } catch (IOException ignored) {
                }
            }
            return null;
        } else {
            return null;
        }

    }


}

