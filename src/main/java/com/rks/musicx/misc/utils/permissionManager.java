package com.rks.musicx.misc.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import com.rks.musicx.R;

import java.util.ArrayList;
import java.util.List;

import static com.rks.musicx.misc.utils.Constants.OVERLAY_REQ;
import static com.rks.musicx.misc.utils.Constants.PERMISSIONS_REQ;
import static com.rks.musicx.misc.utils.Constants.WRITESETTINGS;
import static com.rks.musicx.misc.utils.Constants.permissions;

/*
 * Created by Coolalien on 6/28/2016.
 */

public class permissionManager {


    public static boolean checkPermissions(Activity activity) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(activity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PERMISSIONS_REQ);
            return false;
        }
        return true;
    }

    public static void widgetPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Intent intent;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", activity.getPackageName(), null));
                            activity.startActivityForResult(intent, OVERLAY_REQ);
                        }
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        Toast.makeText(activity, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
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

    public static void settingPermission(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(activity)) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Intent intent;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null));
                            activity.startActivityForResult(intent, WRITESETTINGS);
                        }
                    } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                        Toast.makeText(activity, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            };
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.permissions_title)
                    .setMessage(R.string.writesetting)
                    .setPositiveButton(R.string.btn_continue, listener)
                    .setNegativeButton(R.string.btn_cancel, listener)
                    .setCancelable(false)
                    .show();
        }
    }

    /**
     * Check if alert windows granted
     *
     * @param context
     * @return
     */
    public static boolean isSystemAlertGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED || Settings.canDrawOverlays(context)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isAudioRecordGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isWriteSettingsGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PermissionChecker.PERMISSION_GRANTED || !Settings.System.canWrite(context)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean isExternalReadStorageGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean writeExternalStorageGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }
}
