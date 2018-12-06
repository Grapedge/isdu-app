package cn.edu.sdu.online.isdu.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/5/25
 *
 * 用户设置类
 ****************************************************
 */

public class Settings {

    public static int STARTUP_PAGE = 0; // 最先启动页

    public static boolean ALARM_MESSAGE = false; // 新消息提醒
    public static boolean ALARM_NEWS = false; // 新资讯提醒
    public static int ALARM_SCHEDULE = 0; // 日程提醒方式
    public static boolean CLOUD_SYNC = true; // 云同步
    public static String DEFAULT_DOWNLOAD_LOCATION =
            Environment.getExternalStorageDirectory() + "/iSDU/download/"; // 默认下载位置

    public static void load(Context context) {
        SharedPreferences sp = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        ///////Begin Load/////////
        STARTUP_PAGE = sp.getInt("startup_page", 0);
        ALARM_MESSAGE = sp.getBoolean("alarm_message", false);
        ALARM_NEWS = sp.getBoolean("alarm_news", false);
        ALARM_SCHEDULE = sp.getInt("alarm_schedule", 0);
        CLOUD_SYNC = sp.getBoolean("cloud_sync", true);
        DEFAULT_DOWNLOAD_LOCATION = sp.getString("default_download_location",
                Environment.getExternalStorageDirectory() + "/iSDU/download/");
    }

    public static void store(Context context) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();
        ///////Begin Storage////////
        editor.putInt("startup_page", STARTUP_PAGE);
        editor.putBoolean("alarm_message", ALARM_MESSAGE);
        editor.putBoolean("alarm_news", ALARM_NEWS);
        editor.putInt("alarm_schedule", ALARM_SCHEDULE);
        editor.putBoolean("cloud_sync", CLOUD_SYNC);
        editor.putString("default_download_location", DEFAULT_DOWNLOAD_LOCATION);

        editor.apply();
    }

    /**
     * Modify one item of Settings
     */
    public static void mod(Context context, String key, int value) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();

        editor.putInt(key, value);

        editor.apply();
    }
    public static void mod(Context context, String key, boolean value) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();

        editor.putBoolean(key, value);

        editor.apply();
    }
    public static void mod(Context context, String key, long value) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();

        editor.putLong(key, value);

        editor.apply();
    }
    public static void mod(Context context, String key, String value) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();

        editor.putString(key, value);

        editor.apply();
    }
    public static void mod(Context context, String key, float value) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit();

        editor.putFloat(key, value);

        editor.apply();
    }

}
