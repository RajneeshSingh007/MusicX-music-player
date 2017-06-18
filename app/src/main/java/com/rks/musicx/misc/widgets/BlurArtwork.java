package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class BlurArtwork extends AsyncTask<Drawable, Void, Drawable> {

    private Context context;
    private int radius;
    private Bitmap bitmap;
    private ImageView imageView;
    private Bitmap finalResult;
    //private final int height;
    //private final int width;
    private BitmapFactory.Options options;
    private ByteArrayOutputStream stream;
    private byte[] imageInByte;
    private ByteArrayInputStream bis;
    private BitmapDrawable bitmapDrawable;
    private int scale;
    private RenderScript renderScript = null;
    private Allocation allocationIn  = null;
    private Allocation allocationOut = null;
    private ScriptIntrinsicBlur scriptIntrinsicBlur = null;

    public BlurArtwork(Context contexts, int radius, Bitmap bitmaps, ImageView imageView, int scale) {
        this.context = contexts;
        this.radius = radius;
        this.bitmap = bitmaps;
        this.imageView = imageView;
        this.scale = scale;
        renderScript = RenderScript.create(context);
        options = new BitmapFactory.Options();
        stream = new ByteArrayOutputStream();
    }

    @Override
    protected Drawable doInBackground(Drawable... drawables) {
        options.inSampleSize = scale;
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        imageInByte = stream.toByteArray();
        bis = new ByteArrayInputStream(imageInByte);
        finalResult = BitmapFactory.decodeStream(bis, null, options);
        scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        allocationIn = Allocation.createFromBitmap(renderScript, finalResult, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        allocationOut = Allocation.createTyped(renderScript, allocationIn.getType());
        scriptIntrinsicBlur.setRadius(radius); //radius option from users
        scriptIntrinsicBlur.setInput(allocationIn);
        scriptIntrinsicBlur.forEach(allocationOut);
        allocationOut.copyTo(finalResult);
        bitmapDrawable = new BitmapDrawable(context.getResources(), finalResult);
        return bitmapDrawable;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (drawable != null){
            imageView.setImageDrawable(drawable);
            try {
                if (renderScript != null) {
                    renderScript.destroy();
                }
                if (allocationIn != null) {
                    allocationIn.destroy();
                }
                if (allocationOut != null) {
                    allocationOut.destroy();
                }
                if (scriptIntrinsicBlur != null) {
                    scriptIntrinsicBlur.destroy();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}


