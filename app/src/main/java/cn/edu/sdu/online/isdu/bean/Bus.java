package cn.edu.sdu.online.isdu.bean;

/**
 * Created by TYZ on 2018/7/9.
 */

public class Bus {
    private String busTime,busFrom,busTo,busPass;

    public void setBusTime(String busTime){
        this.busTime=busTime;
    }

    public void setBusFrom(String busFrom){
        this.busFrom = busFrom;
    }

    public void setBusTo(String busTo){
        this.busTo = busTo;
    }

    public void setBusPass(String busPass){
        this.busPass = busPass;
    }

    public String getBusFrom() {
        return busFrom;
    }

    public String getBusPass() {
        return busPass;
    }

    public String getBusTime() {
        return busTime;
    }

    public String getBusTo() {
        return busTo;
    }
}
