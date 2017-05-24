package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class BlurArtwork extends AsyncTask<String, Void, String> {

    private Context context;
    private int radius;
    private Bitmap bitmap;
    private ImageView imageView;
    private Bitmap finalResult;
    private final int height;
    private final int width;
    private RenderScript renderScript = null;
    private Allocation allocationIn  = null;
    private Allocation allocationOut = null;
    private ScriptIntrinsicBlur scriptIntrinsicBlur = null;

    public BlurArtwork(Context contexts, int radius, Bitmap bitmaps, ImageView imageView, float scale) {
        this.context = contexts;
        this.radius = radius;
        this.bitmap = bitmaps;
        this.imageView = imageView;
        height = Math.round(bitmap.getHeight() * scale);
        width = Math.round(bitmap.getWidth() * scale);
    }

    @Override
    protected String doInBackground(String... params) {
        finalResult = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        renderScript = RenderScript.create(context); //rs initialized
        scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        allocationIn = Allocation.createFromBitmap(renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        allocationOut = Allocation.createFromBitmap(renderScript, finalResult);
        scriptIntrinsicBlur.setRadius(radius); //radius option from users
        scriptIntrinsicBlur.setInput(allocationIn);
        scriptIntrinsicBlur.forEach(allocationOut);
        allocationOut.copyTo(finalResult);
        return "Executed";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        imageView.setImageBitmap(finalResult);
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


