package cn.edu.sdu.online.isdu.bean;

public class Collect {

    public static final int TYPE_POST = 0;
    public static final int TYPE_NEWS = 1;

    private String collectTitle;
    private String collectContent;
    private String collectUrl;
    private int collectType;
    private long collectTime;
    private int id;
    private int uid;


    public Collect() {}

    public String getCollectTitle() {
        return collectTitle;
    }

    public void setCollectTitle(String collectTitle) {
        this.collectTitle = collectTitle;
    }

    public String getCollectContent() {

        return collectContent;
    }

    public void setCollectContent(String collectContent) {
        this.collectContent = collectContent;
    }

    public String getCollectUrl() {
        return collectUrl;
    }

    public void setCollectUrl(String collectUrl) {
        this.collectUrl = collectUrl;
    }

    public int getCollectType() {
        return collectType;
    }

    public void setCollectType(int collectType) {
        this.collectType = collectType;
    }

    public long getCollectTime() {
        return collectTime;
    }

    public void setCollectTime(long collectTime) {
        this.collectTime = collectTime;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
