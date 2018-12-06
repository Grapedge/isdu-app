package cn.edu.sdu.online.isdu.bean;

import android.content.Intent;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import cn.edu.sdu.online.isdu.util.EnvVariables;
import cn.edu.sdu.online.isdu.util.ScheduleTime;

/**
 ****************************************************
 * @author Zsj
 * Last Modifier: Zsj
 * Last Modify Time: 2018/7/7
 *
 * 考试安排信息
 ****************************************************
 */

public class Exam {
    private String date;
    private String time;
    private String location;
    private String gradeRate;
    private String type;
    private String name;

    public Exam() {

    }

    public Exam(String date, String time, String location, String gradeRate, String type, String name) {
        this.date = date;
        this.time = time;
        this.location = location;
        this.gradeRate = gradeRate;
        this.type = type;
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getGradeRate() {
        return gradeRate;
    }

    public void setGradeRate(String gradeRate) {
        this.gradeRate = gradeRate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Schedule toSchedule() {
        Schedule schedule = new Schedule(
                "考试：" + name,
                location,
                new ScheduleTime(Integer.parseInt(time.substring(0, 2)),
                        Integer.parseInt(time.substring(3, 5))),
                new ScheduleTime(Integer.parseInt(time.substring(6, 8)),
                        Integer.parseInt(time.substring(9, 11))),
                Schedule.RepeatType.ONCE
        );
        return schedule;
    }

    public int getWeek() {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return EnvVariables.calculateWeekIndex(calendar.getTimeInMillis());
    }

    public int getDay() {
        int year = Integer.parseInt(date.substring(0, 4));
        int month = Integer.parseInt(date.substring(5, 7));
        int day = Integer.parseInt(date.substring(8, 10));
        Calendar calendar;
        calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        return calendar.get(Calendar.DAY_OF_WEEK) - 1;
    }

}
