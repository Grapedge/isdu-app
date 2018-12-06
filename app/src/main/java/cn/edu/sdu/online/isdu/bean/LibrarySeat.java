package cn.edu.sdu.online.isdu.bean;

/**
 * Created by TYZ on 2018/7/16.
 */

public class LibrarySeat {
    private String room;
    private int used;
    private int free;

    public LibrarySeat() {
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int free) {
        this.free = free;
    }
}
