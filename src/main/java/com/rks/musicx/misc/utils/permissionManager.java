package com.rks.musicx.misc.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.rks.musicx.R;

import java.util.ArrayList;
import java.util.List;

import static com.rks.musicx.misc.utils.Constants.OVERLAY_REQ;
import static com.rks.musicx.misc.utils.Constants.PERMISSIONS_REQ;

/**
 * Created by Coolalien on 12/26/2016.
 */

public class permissionManager {

    /**
     * String array of permissions
     */
    private static String[] permissions= new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SYSTEM_ALERT_WINDOW};

    /**
     * Check Permission
     * @param activity
     * @return
     */
    public static boolean checkPermissions(Activity activity) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(activity,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),PERMISSIONS_REQ );
            return false;
        }
        return true;
    }

    public static void widgetPermission(Activity activity){
        // check if we can draw overlays
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !Settings.canDrawOverlays(activity)) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                @TargetApi(Build.VERSION_CODES.N)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName()));
                        activity.startActivityForResult(intent, OVERLAY_REQ);
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        Toast.makeText(activity, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                }
            };
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.permissions_title)
                    .setMessage(R.string.draw_over_permissions_message)
                    .setPositiveButton(R.string.btn_continue, listener)
                    .setNegativeButton(R.string.btn_cancel, listener)
                    .setCancelable(false)
                    .show();
        }
    }


}
