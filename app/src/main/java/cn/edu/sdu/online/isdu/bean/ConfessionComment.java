package cn.edu.sdu.online.isdu.bean;

public class ConfessionComment {

    private int id;
    private int fatherId;
    private long time;
    private String uid;
    private String content;
    private int confessionId;
    private CommentUser theUser;
    private CommentUser fatheruser;

    public ConfessionComment() {
        super();
    }

    public ConfessionComment(int id, int fatherId, long time, String uid, String content, int confessionId) {
        this.id = id;
        this.fatherId = fatherId;
        this.time = time;
        this.uid = uid;
        this.content = content;
        this.confessionId = confessionId;
    }

    public int getConfessiond() {
        return confessionId;
    }

    public void setConfessionId(int confessionId) {
        this.confessionId = confessionId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFatherId() {
        return fatherId;
    }

    public void setFatherId(int fatherId) {
        this.fatherId = fatherId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentUser getTheUser() {
        return theUser;
    }

    public void setTheUser(CommentUser theUser) {
        this.theUser = theUser;
    }

    public CommentUser getFatheruser() {
        return fatheruser;
    }

    public void setFatheruser(CommentUser fatheruser) {
        this.fatheruser = fatheruser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfessionComment that = (ConfessionComment) o;
        return id == that.id &&
                fatherId == that.fatherId;
    }

}
