package cn.edu.sdu.online.isdu.bean;

public class CommentUser {
    private int userId;
    private String nickName;
    private String avatar;

    public CommentUser() {

    }

    public CommentUser(int userId, String nickName, String avatar) {
        this.userId = userId;
        this.nickName = nickName;
        this.avatar = avatar;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
