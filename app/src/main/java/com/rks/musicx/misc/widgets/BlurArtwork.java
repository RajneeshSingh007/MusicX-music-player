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

public class BlurArtwork extends AsyncTask<String, Void, String> {

    private Context context;
    private int radius;
    private Bitmap bitmap;
    private ImageView imageView;
    private Bitmap finalResult;
    private final int height;
    private final int width;

    public BlurArtwork(Context context, int radius, Bitmap bitmap, ImageView imageView, float scale) {
        this.context = context;
        this.radius = radius;
        this.bitmap = bitmap;
        this.imageView = imageView;
        height = Math.round(bitmap.getHeight() * scale);
        width = Math.round(bitmap.getWidth() * scale);
    }

    @Override
    protected String doInBackground(String... params) {
        finalResult = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        RenderScript renderScript = RenderScript.create(context); //rs initialized
        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        Allocation allocationIn = Allocation.createFromBitmap(renderScript, bitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT | Allocation.USAGE_SHARED);
        Allocation allocationOut = Allocation.createTyped(renderScript, allocationIn.getType());
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
    }

}


