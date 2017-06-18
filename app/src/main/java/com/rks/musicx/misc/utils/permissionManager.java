package com.rks.musicx.misc.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
             new MaterialDialog.Builder(activity)
                    .title(R.string.permissions_title)
                    .content(R.string.draw_over_permissions_message)
                    .positiveText(R.string.btn_continue)
                    .negativeText(R.string.btn_cancel)
                    .autoDismiss(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                         @Override
                         public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                             if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                 Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.fromParts("package", activity.getPackageName(), null));
                                 activity.startActivityForResult(intent, OVERLAY_REQ);
                             }
                         }
                     })
                     .onNegative(new MaterialDialog.SingleButtonCallback() {
                         @Override
                         public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                             Toast.makeText(activity, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
                             dialog.dismiss();
                         }
                     })
                     .neutralText("Never show again")
                     .onNeutral(new MaterialDialog.SingleButtonCallback() {
                         @Override
                         public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                             Extras.getInstance().setWidgetTrack(true);
                             dialog.dismiss();
                         }
                     })
                    .show();
        }
    }

    public static void settingPermission(Activity activity) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(activity)) {
            new MaterialDialog.Builder(activity)
                    .title(R.string.permissions_title)
                    .content(R.string.writesetting)
                    .positiveText(R.string.btn_continue)
                    .negativeText(R.string.btn_cancel)
                    .autoDismiss(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                               Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.fromParts("package", activity.getPackageName(), null));
                                activity.startActivityForResult(intent, WRITESETTINGS);
                            }
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Toast.makeText(activity, R.string.toast_permissions_not_granted, Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    })
                    .neutralText("Never show again")
                    .onNeutral(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Extras.getInstance().setSettings(true);
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
    }

    public static boolean isSystemAlertGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final boolean result = PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.SYSTEM_ALERT_WINDOW) == PermissionChecker.PERMISSION_GRANTED  || Settings.canDrawOverlays(context);
            return result;
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
            final boolean result = PermissionChecker.checkCallingOrSelfPermission(context, Manifest.permission.WRITE_SETTINGS) == PermissionChecker.PERMISSION_GRANTED || Settings.System.canWrite(context);
            return result;
        }else {
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
