package cn.edu.sdu.online.isdu.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import cn.edu.sdu.online.isdu.ui.activity.MainActivity;

public class HuaweiCompat {
    public static void goHuaWeiSetting(Activity activity) {
        try {
            Intent intent = new Intent("cn.edu.sdu.online.isdu");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, "跳转失败，请手动进入设置给予权限", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
