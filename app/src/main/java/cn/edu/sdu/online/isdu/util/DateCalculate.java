package cn.edu.sdu.online.isdu.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author TYZ
 * @date 2018/7/19
 */

public class DateCalculate {

    private static long  nowMill = EnvVariables.firstWeekTimeMillis;

    public static String Cal(long week, long day){
        String date = "";
        nowMill = EnvVariables.firstWeekTimeMillis;

        nowMill += (week - 1) * 7 * 24 * 60 * 60 * 1000;
        nowMill += (day - 1) * 24 * 60 * 60 * 1000;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(nowMill);
        date += calendar.get(Calendar.YEAR);
        date += "-" + (calendar.get(Calendar.MONTH) + 1);
        date += "-" + calendar.get(Calendar.DAY_OF_MONTH);
        return date;
    }

    /**
     * 仅支持y年m月d日的格式
     * @param date
     * @return
     */
    public static long getTimeMillis(String date) {
        if (!date.matches("[0-9]+[^0-9][0-9]{1,2}[^0-9][0-9]{1,2}[^0-9]")) {
            return 0L;
        } else {
            String[] elements = date.split("[^0-9]");
            int[] e = new int[3];
            int i = 0, j = 0;
            while (i < elements.length) {
                if (!"".equals(elements[i]))
                    e[j++] = Integer.parseInt(elements[i]);
                i++;
            }
            return getTimeMillis(e[0], e[1], e[2]);
        }
    }

    public static long getTimeMillis(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(year, month - 1, day, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime().getTime();
    }

    public static String getExpressionDate(long timeMillis) {
        return (System.currentTimeMillis() - timeMillis < 60 * 1000) ?
            "刚刚" : ((System.currentTimeMillis() - timeMillis < 60 * 60 * 1000) ?
            (System.currentTimeMillis() - timeMillis) / (60 * 1000) + " 分钟前" : (
                (System.currentTimeMillis() - timeMillis < 24 * 60 * 60 * 1000) ?
                        (System.currentTimeMillis() - timeMillis) / (60 * 60 * 1000) + " 小时前" :
         (System.currentTimeMillis() - timeMillis < 48 * 60 * 60 * 1000) ?
                 "昨天 " + new SimpleDateFormat("HH:mm").format(timeMillis)
                    : new SimpleDateFormat("yyyy-MM-dd HH:mm").format(timeMillis)));
    }

}
