package cn.edu.sdu.online.isdu.ui.design.snake.util;

import android.text.TextUtils;
import android.util.Log;

/**
 * Log utils.
 *
 * @author Scott Smith 2018-01-16 13:33
 */
public class Logger {
    public static boolean debug = true;
    public static final String TAG = "Snake";

    public static void d(String msg) {
        if(debug && !TextUtils.isEmpty(msg)) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if(debug && !TextUtils.isEmpty(msg)) {
            Log.e(TAG, msg);
        }
    }
}
