package cn.edu.sdu.online.isdu.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.edu.sdu.online.isdu.interfaces.IWrapper;
import cn.edu.sdu.online.isdu.net.pack.ServerInfo;
import cn.edu.sdu.online.isdu.net.NetworkAccess;
import cn.edu.sdu.online.isdu.util.EnvVariables;
import cn.edu.sdu.online.isdu.util.FileUtil;
import cn.edu.sdu.online.isdu.util.Logger;
import cn.edu.sdu.online.isdu.util.ScheduleTime;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/18
 *
 * 日程的Java Bean
 ****************************************************
 */

public class Schedule implements Parcelable, IWrapper {

    public static final int[] defaultScheduleColor = new int[] {
        0xFFFF7F66, 0xFFFFCC66, 0xFF66E6FF, 0xFF7F66FF, 0xFFFF2970, 0xFF00EB9C
    }; // 默认提供的日程背景颜色

    public static final int defaultScheduleTextColor = 0xFFFFFFFF;

    public static List<List<List<Schedule>>> localScheduleList;

    private String scheduleName; // 事件名称
    private String scheduleLocation; // 事件地点
    private ScheduleTime startTime = new ScheduleTime(); // 事件开始时间
    private ScheduleTime endTime = new ScheduleTime(); // 事件结束时间
    private RepeatType repeatType; // 重复类型
    private int scheduleColor = 0xFF717DEB; // 日程背景颜色
    private int scheduleTextColor = 0xFFFFFFFF; // 日程文字颜色
    private List<Integer> repeatWeeks = new ArrayList<>();
    private int courseOrder;

    public Schedule() {}

    public Schedule(String scheduleName, String scheduleLocation, ScheduleTime startTime, ScheduleTime endTime, RepeatType repeatType) {
        this.scheduleName = scheduleName;
        this.scheduleLocation = scheduleLocation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatType = repeatType;
    }

    public Schedule(String scheduleName, String scheduleLocation, ScheduleTime startTime, ScheduleTime endTime, RepeatType repeatType, int courseOrder) {
        this.scheduleName = scheduleName;
        this.scheduleLocation = scheduleLocation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.repeatType = repeatType;
        this.courseOrder = courseOrder;
    }

    protected Schedule(Parcel in) {
        scheduleName = in.readString();
        scheduleLocation = in.readString();
        startTime = in.readParcelable(ScheduleTime.class.getClassLoader());
        endTime = in.readParcelable(ScheduleTime.class.getClassLoader());
        scheduleColor = in.readInt();
        scheduleTextColor = in.readInt();
        courseOrder = in.readInt();
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }

        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }
    };

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleLocation() {
        return scheduleLocation;
    }

    public void setScheduleLocation(String scheduleLocation) {
        this.scheduleLocation = scheduleLocation;
    }

    public ScheduleTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ScheduleTime startTime) {
        this.startTime = startTime;
    }

    public ScheduleTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ScheduleTime endTime) {
        this.endTime = endTime;
    }

    public RepeatType getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(RepeatType repeatType) {
        this.repeatType = repeatType;
    }

    public int getScheduleColor() {
        return scheduleColor;
    }

    public void setScheduleColor(int scheduleColor) {
        this.scheduleColor = scheduleColor;
    }

    public int getScheduleTextColor() {
        return scheduleTextColor;
    }

    public void setScheduleTextColor(int scheduleTextColor) {
        this.scheduleTextColor = scheduleTextColor;
    }

    public List<Integer> getRepeatWeeks() {
        return repeatWeeks;
    }

    public void setRepeatWeeks(List<Integer> repeatWeeks) {
        this.repeatWeeks = repeatWeeks;
    }

    public int getCourseOrder() {
        return courseOrder;
    }

    public void setCourseOrder(int courseOrder) {
        this.courseOrder = courseOrder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(scheduleName);
        dest.writeString(scheduleLocation);
        dest.writeInt(scheduleColor);
        dest.writeInt(scheduleTextColor);
        dest.writeParcelable(startTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelable(endTime, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeInt(courseOrder);
    }

    /**
     * 日程重复类型
     */
    public enum RepeatType implements Serializable {
        ONCE, DAILY, WEEKLY
    }

    /**
     * 本地读取日程
     */
    public static List<List<List<Schedule>>> load(Context context) {
        SharedPreferences sp = context.getSharedPreferences("schedule_list", Context.MODE_PRIVATE);
        String jsonString = sp.getString("schedules", "");
        if (jsonString.equals("")) {
            loadFromNet(context);
            return new ArrayList<>();
        }
        return load(jsonString);
    }

    public static List<List<List<Schedule>>> load(String jsonString) {
        List<List<List<Schedule>>> sList = new ArrayList<>();
        try {
            JSONArray weeksArray = new JSONArray(jsonString); // startWeek到endWeek
            for (int i = 0; i < weeksArray.length(); i++) {
                List<List<Schedule>> iList = new ArrayList<>();

                JSONArray daysArray = weeksArray.getJSONArray(i); // 周一到周日

                for (int j = 0; j < daysArray.length(); j++) {
                    List<Schedule> jList = new ArrayList<>();


                    JSONArray dayArray = daysArray.getJSONArray(j); // 某一天
                    for (int k = 0; k < dayArray.length(); k++) {
                        Schedule schedule = new Schedule();
                        JSONObject jsonObject = dayArray.getJSONObject(k);

                        schedule.setScheduleName(jsonObject.getString("schedule_name"));
                        schedule.setScheduleColor(jsonObject.getInt("schedule_color"));
                        schedule.setScheduleLocation(jsonObject.getString("schedule_location"));
                        schedule.setScheduleTextColor(jsonObject.getInt("schedule_text_color"));
                        schedule.setStartTime(new ScheduleTime(
                                jsonObject.getInt("start_time_hour"),
                                jsonObject.getInt("start_time_minute")
                        ));
                        schedule.setEndTime(new ScheduleTime(
                                jsonObject.getInt("end_time_hour"),
                                jsonObject.getInt("end_time_minute")
                        ));
                        int rtype = jsonObject.getInt("repeat_type");
                        schedule.setRepeatType(rtype == 0 ? RepeatType.ONCE :
                                (rtype == 1 ? RepeatType.DAILY : RepeatType.WEEKLY));

                        String repeatWeeks = jsonObject.getString("repeat_weeks");
                        String[] weeks = repeatWeeks.split(",");
                        List<Integer> wl = new ArrayList<>();
                        for (String num : weeks) {
                            wl.add(Integer.parseInt(num));
                        }
                        schedule.setRepeatWeeks(wl);

                        jList.add(schedule);
                    }


                    iList.add(jList);
                }

                sList.add(iList);
            }

            return sList;
        } catch (Exception e) {
            Logger.log(e);
            return sList;
        }
    }

    public static void loadFromNet(final Context context) {
//        if (User.staticUser == null) User.staticUser = User.load();
//        if (User.staticUser.getStudentNumber() != null)
        if (User.isLogin())
            NetworkAccess.cache(ServerInfo.getScheduleUrl(User.staticUser.getUid()),
                    (success, cachePath) -> {
                        if (success) {
                            String jsonString = FileUtil.getStringFromFile(cachePath);
                            try {
                                localScheduleList = loadCourse(new JSONObject(jsonString).getJSONArray("obj"));
                            } catch (JSONException e) {
                                Logger.log(e);
                            }
                            save(context);
                        }
                    });
    }

    public static String parse() {
        try {
            JSONArray iArray = new JSONArray();
            for (int i = 0; i < localScheduleList.size(); i++) {
                JSONArray jArray = new JSONArray();
                for (int j = 0; j < localScheduleList.get(i).size(); j++) {
                    JSONArray kArray = new JSONArray();
                    for (int k = 0; k < localScheduleList.get(i).get(j).size(); k++) {
                        Schedule schedule = localScheduleList.get(i).get(j).get(k);
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("schedule_name", schedule.scheduleName);
                        jsonObject.put("schedule_color", schedule.scheduleColor);
                        jsonObject.put("schedule_location", schedule.scheduleLocation);
                        jsonObject.put("schedule_text_color", schedule.scheduleTextColor);
                        jsonObject.put("start_time_hour", schedule.startTime.getHour());
                        jsonObject.put("start_time_minute", schedule.startTime.getMinute());
                        jsonObject.put("end_time_hour", schedule.endTime.getHour());
                        jsonObject.put("end_time_minute", schedule.endTime.getMinute());
                        jsonObject.put("repeat_type",
                                schedule.repeatType == RepeatType.ONCE ? 0 :
                                        (schedule.repeatType == RepeatType.DAILY ? 1 : 2));

                        StringBuilder sb = new StringBuilder("");
                        for (Integer week : schedule.repeatWeeks) sb.append(week + ",");
                        String s = sb.toString();
                        jsonObject.put("repeat_weeks", s.substring(0, s.length() - 1));

                        kArray.put(jsonObject);
                    }
                    jArray.put(kArray);
                }
                iArray.put(jArray);
            }

            return iArray.toString();

        } catch (Exception e) {
            Logger.log(e);
        }
        return "";
    }

    public static void save(Context context) {
        if (localScheduleList == null) return;
        SharedPreferences.Editor editor =
                context.getSharedPreferences("schedule_list", Context.MODE_PRIVATE).edit();
        editor.putString("schedules", parse());
        editor.apply();
    }


    /*************************************
     * 加载日程表
     ************************************/
    public static List<List<List<Schedule>>> loadCourse(JSONArray jsonArray) {
        List<List<List<Schedule>>> list =
                new ArrayList<>(); // 20周的总表

        // 初始化
        for (int i = EnvVariables.startWeek; i <= EnvVariables.endWeek; i++) {
            // 第i周
            List<List<Schedule>> listI = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                List<Schedule> listJ = new ArrayList<>();
                listI.add(listJ);
            }
            list.add(listI);
        }

        List<Schedule> schedules = new ArrayList<>();
        int colorId = 0;
        for (int i = 0; i < jsonArray.length(); i++) { // 遍历每个课程
            try {
                JSONObject obj = jsonArray.getJSONObject(i);
                String courseName = obj.getString("courseName") + "(" + obj.getString("teacher") + ")";
                String week = obj.getString("week");
                int weekDay = obj.getInt("weekday");
                int courseOrder = obj.getInt("courseOrder");
                String location = obj.getString("room");

                Schedule schedule = new Schedule(courseName, location, getCourseStartTime(courseOrder),
                        getCourseEndTime(courseOrder), RepeatType.WEEKLY, courseOrder);

                // 确定日程颜色
                boolean flag = false;
                for (Schedule s : schedules) {
                    if (s.getScheduleName().equals(courseName) && s.getScheduleLocation().equals(location)) {
                        flag = true;
                        schedule.setScheduleColor(s.scheduleColor);
                        schedule.setScheduleTextColor(s.scheduleTextColor);
                    }
                }

                if (!flag) {
                    schedule.setScheduleColor(defaultScheduleColor[colorId]);
                    schedule.setScheduleTextColor(defaultScheduleTextColor);
                    colorId = (colorId + 1) % defaultScheduleColor.length;
                }
                schedules.add(schedule);

                int startWeek = EnvVariables.startWeek;
                int endWeek = week.lastIndexOf('1') + 1;
                for (int j = 0; j < week.length(); j++) {
                    if (week.charAt(j) == '1') {
                        schedule.repeatWeeks.add(j + 1);
                    }
                }

                for (int j = startWeek; j <= endWeek; j++) {
//                    if (schedule.repeatWeeks.contains(j))
                        list.get(j - 1).get(weekDay - 1).add(schedule);
                }

            } catch (JSONException e) {
                Logger.log(e);
            }
        }



        return list;
    }

    private static ScheduleTime getCourseStartTime(int order) {
        switch (order) {
            case 1:
                return new ScheduleTime(8, 0);
            case 2:
                return new ScheduleTime(10, 10);
            case 3:
                if (EnvVariables.lessonDelay)
                    return new ScheduleTime(14, 0);
                else
                    return new ScheduleTime(13, 30);
            case 4:
                if (EnvVariables.lessonDelay)
                    return new ScheduleTime(16, 00);
                else
                    return new ScheduleTime(15, 30);
            case 5:
            default:
                if (EnvVariables.lessonDelay)
                    return new ScheduleTime(19, 0);
                else
                    return new ScheduleTime(18, 30);
        }
    }

    private static ScheduleTime getCourseEndTime(int order) {
        switch (order) {
            case 1:
                return new ScheduleTime(9, 50);
            case 2:
                return new ScheduleTime(12, 0);
            case 3:
                if (EnvVariables.lessonDelay)
                    return new ScheduleTime(15, 50);
                else
                    return new ScheduleTime(15, 20);
            case 4:
                if (EnvVariables.lessonDelay)
                    return new ScheduleTime(17, 50);
                else
                    return new ScheduleTime(17, 20);
            case 5:
            default:
                if (EnvVariables.lessonDelay)
                    return new ScheduleTime(20, 50);
                else
                    return new ScheduleTime(20, 20);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(scheduleName, schedule.scheduleName) &&
                Objects.equals(scheduleLocation, schedule.scheduleLocation) &&
                Objects.equals(startTime, schedule.startTime) &&
                Objects.equals(endTime, schedule.endTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(scheduleName, scheduleLocation);
    }

    @Override
    public void set(IWrapper a) {
        if (a instanceof Schedule) {
            setScheduleTextColor(((Schedule) a).scheduleTextColor);
            setScheduleColor(((Schedule) a).scheduleColor);

            getRepeatWeeks().clear();
            getRepeatWeeks().addAll(((Schedule) a).repeatWeeks);
            setRepeatType(((Schedule) a).repeatType);
            setScheduleLocation(((Schedule) a).scheduleLocation);
            setScheduleName(((Schedule) a).scheduleName);
            getStartTime().set(((Schedule) a).startTime);
            getEndTime().set(((Schedule) a).endTime);
            setCourseOrder(((Schedule) a).courseOrder);
        }
    }

    @Override
    public void swap(IWrapper a) {
        Schedule temp = new Schedule();
        temp.set(a);
        a.set(this);
        this.set(temp);
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "scheduleName='" + scheduleName + '\'' +
                ", scheduleLocation='" + scheduleLocation + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", courseOrder=" + courseOrder +
                '}';
    }
}
