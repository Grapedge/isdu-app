package cn.edu.sdu.online.isdu.bean;

import cn.edu.sdu.online.isdu.interfaces.Collectible;

public abstract class AbstractCollectible implements Collectible {
    public static final int TYPE_POST = 0;
    public static final int TYPE_NEWS = 1;
    public static final int TYPE_LIFE = 2;

    String mTitle;
    String mContent;
    String mCampus;
    String mLifePostClass;
    String mLostClass;
    long mTime;
    int mUserId;

    public abstract int getType();

    @Override
    public void onCollect() {
    }

    @Override
    public void onScan() {
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmCampus(){ return mCampus;}

    public void setmCampus(String mCampus) {
        this.mCampus = mCampus;
    }

    public long getmTime() {
        return mTime;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    public int getmUserId() {
        return mUserId;
    }

    public void setmUserId(int mUserId) {
        this.mUserId = mUserId;
    }
}
