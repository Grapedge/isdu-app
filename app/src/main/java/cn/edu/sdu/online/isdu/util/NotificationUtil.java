package cn.edu.sdu.online.isdu.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sdu.online.isdu.R;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/15
 *
 * 整合消息工具包
 *
 * #添加线程锁，保证线程安全
 ****************************************************
 */

public class NotificationUtil {

    static List<Integer> notifyIdList = new ArrayList<>();

    public static String[] channelIds = new String[] {"MIN", "LOW", "DEFAULT", "HIGH", "MAX"};

    static NotificationManager manager;
    static NotificationCompat.Builder builder; // API26以下
    static Notification.Builder nBuilder; // API26及以上
    static NotificationChannel notificationChannel;

    public static final int PRIORITY_MIN = 0;
    public static final int PRIORITY_LOW = 1;
    public static final int PRIORITY_DEFAULT = 2;
    public static final int PRIORITY_HIGH = 3;
    public static final int PRIORITY_MAX = 4;

    static Context mContext;

    static final Object lock = new Object();

    public static class Builder {
        private int smallIconRes = R.mipmap.ic_alpha_not;
        private int largeIconRes = 0;
        private String title;
        private String message;
        private PendingIntent pendingIntent;
        private String channelId = channelIds[0];
        private int notifyId = 0;
        private long when = 0;
        private boolean autoCancel = false;
        private boolean vibrate = false;
        private boolean lights = false;
        private int lightColor = Color.GREEN;
        private boolean useDefault = false;
        private boolean longText = false;
        private boolean bigPicture = false;
        private Bitmap bigBitmap;
        private boolean onGoing = false; // 常驻通知栏
        private String ticker;

        private boolean needProgress = false;
        private int progress; // 进度
        private int maxProgress;
        private boolean indeterminateProgress;
        private int priority = NotificationCompat.PRIORITY_MAX;
        List<NotificationCompat.Action> actionList = new ArrayList<>();
        List<Notification.Action> nActionList = new ArrayList<>();

        public Builder(Context context) {
            mContext = context;
            manager = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
        }

        public Builder addAction(int icon, String title, PendingIntent intent) {
            if (Build.VERSION.SDK_INT < 26) {
                actionList.add(new NotificationCompat.Action(icon, title, intent));
            } else {
                nActionList.add(new Notification.Action.Builder(
                        Icon.createWithResource(mContext, icon), title, intent).build());
            }
            return this;
        }

        public Builder setOnGoing(boolean onGoing) {
            this.onGoing = onGoing;
            return this;
        }

        public Builder setTicker(String ticker) {
            this.ticker = ticker;
            return this;
        }

        public Builder setSmallIcon(int res) {
            this.smallIconRes = res;
            return this;
        }

        public Builder setLargeIcon(int res) {
            this.largeIconRes = res;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setIntent(PendingIntent pi) {
            this.pendingIntent = pi;
            return this;
        }

        /**
         * @deprecated Use {@link NotificationUtil.Builder#setPriority(int)} instead
         */
        @Deprecated
        public Builder setChannelId(String id) {
            this.channelId = id;
            return this;
        }

        public Builder setNotifyId(int notifyId) {
            this.notifyId = notifyId;
            return this;
        }

        public Builder setWhen(long when) {
            this.when = when;
            return this;
        }

        public Builder setAutoCancel(boolean autoCancel) {
            this.autoCancel = autoCancel;
            return this;
        }

        public Builder setVibrate(boolean vibrate) {
            this.vibrate = vibrate;
            return this;
        }

        public Builder setLights(boolean lights) {
            this.lights = lights;
            return this;
        }

        public Builder setLightColor(int lightColor) {
            this.lightColor = lightColor;
            return this;
        }

        public Builder setUseDefault(boolean useDefault) {
            this.useDefault = useDefault;
            return this;
        }

        public Builder setLongText(boolean longText) {
            this.longText = longText;
            return this;
        }

        public Builder setBigPicture(boolean bigPicture) {
            this.bigPicture = bigPicture;
            return this;
        }

        /**
         * Set priority between 0 and 4
         *
         * @param priority Int number in the arrange of [0, 4]
         * @return Builder
         */
        public Builder setPriority(int priority) {
            this.priority = priority;
            if (this.priority < 0) this.priority = 0;
            if (this.priority > 4) this.priority = 4;
            return this;
        }

        public Builder setBigBitmap(Bitmap bigBitmap) {
            this.bigBitmap = bigBitmap;
            return this;
        }

        public Builder setProgress(int max, int progress, boolean indeterminate) {
            needProgress = true;
            this.maxProgress = max;
            this.progress = progress;
            this.indeterminateProgress = indeterminate;
            return this;
        }

        public Notification build() {
            // 必须提供smallIcon，不然会报错
            // 这里进行return处理
            if (smallIconRes == 0) {
                return null;
            }
            synchronized (lock) {
                if (Build.VERSION.SDK_INT < 26) {
                    builder = new NotificationCompat.Builder(mContext, channelIds[priority]);
                    if (smallIconRes != 0) builder.setSmallIcon(smallIconRes);
                    if (largeIconRes != 0) builder.setLargeIcon(
                            BitmapFactory.decodeResource(mContext.getResources(),
                                    largeIconRes));
                    if (title != null) builder.setContentTitle(title);
                    if (message != null) builder.setContentText(message);
                    if (pendingIntent != null) builder.setContentIntent(pendingIntent);
                    if (when != 0) builder.setWhen(when);
                    builder.setAutoCancel(autoCancel);
                    if (vibrate) builder.setVibrate(new long[] {100, 100, 100, 100});
                    if (lights) builder.setLights(lightColor, 1000, 1000);
                    if (longText) builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));
                    if (bigPicture) builder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bigBitmap));
                    if (useDefault) builder.setDefaults(NotificationCompat.DEFAULT_ALL);
                    if (onGoing) builder.setOngoing(true);
                    if (ticker != null) builder.setTicker(ticker);
                    if (needProgress) builder.setProgress(maxProgress, progress, indeterminateProgress);
                    builder.setPriority(priority - 2);

                    if (!actionList.isEmpty()) {
                        for (NotificationCompat.Action action : actionList)
                            builder.addAction(action);
                    }

                    return builder.build();
                } else {
                    nBuilder = new Notification.Builder(mContext, channelIds[priority]);
                    if (smallIconRes != 0) nBuilder.setSmallIcon(smallIconRes);
                    if (largeIconRes != 0) nBuilder.setLargeIcon(
                            BitmapFactory.decodeResource(mContext.getResources(),
                                    largeIconRes));
                    if (title != null) nBuilder.setContentTitle(title);
                    if (message != null) nBuilder.setContentText(message);
                    if (pendingIntent != null) nBuilder.setContentIntent(pendingIntent);
                    if (when != 0) nBuilder.setWhen(when);
                    nBuilder.setAutoCancel(autoCancel);
                    if (longText) nBuilder.setStyle(new Notification.BigTextStyle().bigText(message));
                    if (bigPicture) nBuilder.setStyle(new Notification.BigPictureStyle().bigPicture(bigBitmap));
                    if (onGoing) nBuilder.setOngoing(true);
                    if (ticker != null) nBuilder.setTicker(ticker);
                    if (needProgress) nBuilder.setProgress(maxProgress, progress, indeterminateProgress);

                    if (!nActionList.isEmpty()) {
                        for (Notification.Action action : nActionList)
                            nBuilder.addAction(action);
                    }

                    return nBuilder.build();
                }
            }

        }

        public void show() {
            Notification notification = build();
            if (notification != null) {
                if (lights) notification.flags = Notification.FLAG_SHOW_LIGHTS;
                manager.notify(notifyId, notification);
            }
        }
    }

    public static void init(Context mContext) {
        manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26)
            for (int i = 0; i < channelIds.length; i++) {
                String s = channelIds[i];
                if (manager.getNotificationChannel(s) == null) {
                    manager.createNotificationChannel(new NotificationChannel(
                            s, s, i + 1
                    ));
                }
            }
    }

    public static void send(int notifyId, Notification notification) {
        if (manager != null)
            manager.notify(notifyId, notification);
    }

    public static void updateNotification(int notifyId, Notification notification) {
        manager.notify(notifyId, notification);
    }

    public static void cancel(int id) {
        if (manager != null) {
            try {
                manager.cancel(id);
                if (notifyIdList.indexOf(id) != -1)
                    notifyIdList.remove(notifyIdList.indexOf(id));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getNextId() {
        Integer i = 0;
        while (notifyIdList.contains(i)) i++;
        notifyIdList.add(i);
        return i;
    }

    public interface OnClickListener {
        void onClick(int notifyId, String channelId, NotificationManager manager);
    }

    public interface OnCancelListener {
        void onCancel(int notifyId, String channelId, NotificationManager manager);
    }

}
