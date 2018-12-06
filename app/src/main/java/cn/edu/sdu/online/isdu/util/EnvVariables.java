package cn.edu.sdu.online.isdu.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import cn.edu.sdu.online.isdu.net.pack.ServerInfo;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.Calendar.DAY_OF_WEEK;

public class EnvVariables {
    public static final long DEFAULT_FIRST_WEEK_TIME_MILLIS = 1536508800000L;
    public static int startWeek = 1;
    public static int endWeek = 20;
    public static int currentWeek = -1;

    public static boolean lessonDelay = false; // 是否按照夏季作息推迟上课

    public static long firstWeekTimeMillis = DEFAULT_FIRST_WEEK_TIME_MILLIS;


    public static void init(final Context context) {
        SharedPreferences sp = context.getSharedPreferences("env_variables", Context.MODE_PRIVATE);
//        if (sp.getInt("start_week", 0) == 0) {
            // 未进行信息同步
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
            Request request = new Request.Builder()
                    .url(ServerInfo.envVarUrl)
                    .get()
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.log(e);
                    startWeek = 1;
                    endWeek = 20;
                    firstWeekTimeMillis = DEFAULT_FIRST_WEEK_TIME_MILLIS; // 加载默认值
                    lessonDelay = false;
                    save(context);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        startWeek = jsonObject.getInt("start_week");
                        endWeek = jsonObject.getInt("end_week");
                        firstWeekTimeMillis = jsonObject.getLong("first_week_time_millis");
                        currentWeek = calculateWeekIndex(System.currentTimeMillis());
                        lessonDelay = jsonObject.getInt("lessonDelay") == 1;
                        save(context);
                    } catch (Exception e) {
                        Logger.log(e);
                    }
                }
            });
//        } else {
//            startWeek = sp.getInt("start_week", 1);
//            endWeek = sp.getInt("end_week", 20);
//            firstWeekTimeMillis = sp.getLong("first_week_time_millis", DEFAULT_FIRST_WEEK_TIME_MILLIS);
//
//            currentWeek = calculateWeekIndex(System.currentTimeMillis());
//        }
    }

    private static void save(Context context) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("env_variables", Context.MODE_PRIVATE).edit();
        if (startWeek != 0 && endWeek != 0 && firstWeekTimeMillis != 0) {
            editor.putInt("start_week", startWeek);
            editor.putInt("end_week", endWeek);
            editor.putLong("first_week_time_millis", firstWeekTimeMillis);
            editor.putBoolean("lessonDelay", lessonDelay);
        } else {
            editor.putInt("start_week", 1);
            editor.putInt("end_week", 20);
            editor.putLong("first_week_time_millis", DEFAULT_FIRST_WEEK_TIME_MILLIS);
            editor.putBoolean("lessonDelay", false);
        }
        editor.apply();
    }

    public static int calculateWeekIndex(long timeMillis) {
        long delta = (timeMillis - firstWeekTimeMillis) / 1000L;

        if (delta < 0) return 1;

        int daily = 24 * 60 * 60;
        int days = (int) Math.ceil(((double) delta) / ((double) daily));
        if ((days / 7) + 1 > endWeek)
            return endWeek;
        else if ((days / 7) + 1 < startWeek)
            return startWeek;
        else return (days / 7) + 1;
    }

    /**
     * 获取当前是星期几
     *
     * @return 星期，范围是[1, 7]
     */
    public static int getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int res = calendar.get(DAY_OF_WEEK) - 1;
        if (res == 0) res = 7;
        return res;
    }

}
