package cn.edu.sdu.online.isdu.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/10
 *
 * 记录和管理APP权限
 ****************************************************
 */
public class Permissions {

    public static final String VIBRATE = Manifest.permission.VIBRATE;
    public static final String INTERNET = Manifest.permission.INTERNET;
    public static final String CAMERA = Manifest.permission.CAMERA;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String REQUEST_INSTALL_PACKAGES = Manifest.permission.REQUEST_INSTALL_PACKAGES;
//    public static final String REQUEST_SYSTEM_ALERT_WINDOW = Manifest.permission.SYSTEM_ALERT_WINDOW;

    private static final String[] permissions = new String[] {VIBRATE, INTERNET,
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, REQUEST_INSTALL_PACKAGES,
             CAMERA};

    public static void requestPermission(Activity activity, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission}, 2);
        }
    }

    public static void requestAllPermissions(final Activity activity) {
        if (!checkAllPermissions(activity)) {
            if (Build.BRAND.toLowerCase().contains("huawei")) {
                SharedPreferences sharedPreferences =
                        activity.getSharedPreferences("hw", Context.MODE_PRIVATE);
                if (!sharedPreferences.getBoolean("asked", false)) {
                    sharedPreferences.edit().putBoolean("asked", true).apply();
                    new android.app.AlertDialog.Builder(activity)
                            .setTitle("权限请求")
                            .setMessage("i山大APP需要手机部分权限，华为手机用户需要手动进入设置给予权限。")
                            .setPositiveButton("进入设置", (dialogInterface, i) -> {
                                HuaweiCompat.goHuaWeiSetting(activity);
                                dialogInterface.dismiss();
                            })
                            .setNegativeButton("取消", (dialogInterface, i) -> {
                                dialogInterface.dismiss();
                            })
                            .show();
                }
            } else {
                ActivityCompat.requestPermissions(activity, permissions, 3);
            }
        }
    }

    public static boolean checkAllPermissions(Activity activity) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

}
