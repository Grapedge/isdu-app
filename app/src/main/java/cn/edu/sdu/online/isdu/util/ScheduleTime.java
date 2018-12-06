package cn.edu.sdu.online.isdu.util;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.SimpleDateFormat;

import cn.edu.sdu.online.isdu.interfaces.IWrapper;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/9
 *
 * 日程时间类
 *
 * #添加获取当前时间功能
 ****************************************************
 */

public class ScheduleTime implements Parcelable, IWrapper {

    private int hour;
    private int minute;

    public ScheduleTime() {}

    public ScheduleTime(int hour, int minute) {
        setHour(hour);
        setMinute(minute);
    }

    protected ScheduleTime(Parcel in) {
        hour = in.readInt();
        minute = in.readInt();
    }

    public static ScheduleTime currentTime() {
        long time = System.currentTimeMillis();
        String sHour = new SimpleDateFormat("HH").format(time);
        String sMinute = new SimpleDateFormat("mm").format(time);
        return new ScheduleTime(Integer.parseInt(sHour), Integer.parseInt(sMinute));
    }

    public static final Creator<ScheduleTime> CREATOR = new Creator<ScheduleTime>() {
        @Override
        public ScheduleTime createFromParcel(Parcel in) {
            return new ScheduleTime(in);
        }

        @Override
        public ScheduleTime[] newArray(int size) {
            return new ScheduleTime[size];
        }
    };

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        if (hour < 0 || hour >= 24) {
            this.hour = 0;
        } else {
            this.hour = hour;
        }
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        if (minute < 0 || minute >= 60) {
            this.minute = 0;
        } else
            this.minute = minute;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ScheduleTime) &&
                (((ScheduleTime) obj).minute == this.minute) &&
                (((ScheduleTime) obj).hour == this.hour);
    }

    @Override
    public String toString() {
        return hour + ":" + ((minute >= 10) ? minute : ("0" + minute));
    }

    public boolean laterThan(ScheduleTime time) {
        return (this.hour > time.hour || (this.hour == time.hour && this.minute > time.minute));
    }

    public boolean earlierThan(ScheduleTime time) {
        return (this.hour < time.hour || (this.hour == time.hour && this.minute < time.minute));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hour);
        dest.writeInt(minute);
    }

    @Override
    public void set(IWrapper a) {
        if (a instanceof ScheduleTime) {
            setMinute(((ScheduleTime) a).minute);
            setHour(((ScheduleTime) a).hour);
        }
    }

    @Override
    public void swap(IWrapper a) {
        ScheduleTime temp = new ScheduleTime();
        temp.set(a);
        a.set(this);
        this.set(temp);
    }
}
