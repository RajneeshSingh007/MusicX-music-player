package com.rks.musicx.misc.widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.widget.ImageView;

/**
 * Created by Coolalien on 10/21/2016.
 */

public class BlurArtwork extends AsyncTask<String,Void,String> {

    Context context;
    int radius;
    Bitmap bitmap;
    ImageView imageView;
    Bitmap finalResult;

    public BlurArtwork (Context context, int radius, Bitmap bitmap, ImageView imageView){
        this.context = context;
        this.radius = radius;
        this.bitmap = bitmap;
        this.imageView = imageView;
    }

    @Override
    protected String doInBackground(String... params) {
        finalResult = Bitmap.createBitmap (bitmap.getWidth (), bitmap.getHeight (), Bitmap.Config.ARGB_8888);
        RenderScript renderScript = RenderScript.create(context); //rs initialized
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript)); //start blur
        Allocation allocationIn = Allocation.createFromBitmap(renderScript, bitmap); //blurred bitmap
        Allocation allocationOut = Allocation.createFromBitmap(renderScript,finalResult); //output bitmap
        scriptIntrinsicBlur.setRadius(radius); //radius option from users
        scriptIntrinsicBlur.setInput(allocationIn);
        scriptIntrinsicBlur.forEach(allocationOut);
        allocationOut.copyTo(finalResult);
        return "BlurredArtwork";
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        imageView.setImageBitmap(finalResult);
    }

}
