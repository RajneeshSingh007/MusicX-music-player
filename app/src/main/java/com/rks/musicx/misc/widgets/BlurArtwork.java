package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

import com.rks.musicx.R;
import com.rks.musicx.misc.utils.ArtworkUtils;


/*
 * Created by Coolalien on 6/28/2016.
 */

public class BlurArtwork extends AsyncTask<Drawable, Void, Drawable> {

    private Context context;
    private int radius;
    private Bitmap bitmap;
    private ImageView imageView;
    private Bitmap finalResult;
    private BitmapDrawable bitmapDrawable;
    private RenderScript renderScript = null;
    private Allocation allocationIn  = null;
    private Allocation allocationOut = null;
    private ScriptIntrinsicBlur scriptIntrinsicBlur = null;

    public BlurArtwork(Context contexts, int radius, Bitmap bitmaps, ImageView imageView, int scale) {
        this.context = contexts;
        this.radius = radius;
        this.bitmap = bitmaps;
        this.imageView = imageView;
        renderScript = RenderScript.create(contexts);
    }

    @Override
    protected Drawable doInBackground(Drawable... drawables) {
        finalResult = ArtworkUtils.optimizeBitmap(bitmap, bitmap.getWidth());
        if (finalResult != null && finalResult.getConfig() != null) {
            scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
            allocationIn = Allocation.createFromBitmap(renderScript, finalResult, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
            allocationOut = Allocation.createTyped(renderScript, allocationIn.getType());
            scriptIntrinsicBlur.setRadius(radius); //radius option from users
            scriptIntrinsicBlur.setInput(allocationIn);
            scriptIntrinsicBlur.forEach(allocationOut);
            allocationOut.copyTo(finalResult);
            bitmapDrawable = new BitmapDrawable(context.getResources(), finalResult);
            return bitmapDrawable;
        } else {
            Drawable defaultDrawable = ContextCompat.getDrawable(context, R.mipmap.ic_launcher);
            return defaultDrawable;
        }
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


