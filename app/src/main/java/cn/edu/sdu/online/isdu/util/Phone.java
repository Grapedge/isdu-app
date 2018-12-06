package cn.edu.sdu.online.isdu.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/10
 *
 *
 * 调用手机相关操作，例如震动
 ****************************************************
 */

public class Phone {

    /*------------------------手机振动---------------------------*/
    private static Vibrator vibrator;

    public static void vibrate(Context context, VibrateType type) {
        vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        long pattern[];
        if (vibrator != null && vibrator.hasVibrator()) {
            switch (type) {
                case Notification:
                    pattern = new long[] {100, 100, 100, 100};
                    vibrator.vibrate(pattern, -1);
                    break;
                case Alarm:
                    pattern = new long[] {1000, 500};
                    vibrator.vibrate(pattern, 0);
                    break;
                case Once:
                    pattern = new long[] {25, 25};
                    vibrator.vibrate(pattern, -1);
                    break;
            }
        }

    }

    public static void stopVibrate() {
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.cancel();
        }
    }

    /**
     * Notification: 通知震动，短促的两次
     * Alarm: 响铃震动，长震动
     */
    public enum VibrateType {
        Notification, Alarm, Once
    }
    /*------------------------手机振动---------------------------*/

}
