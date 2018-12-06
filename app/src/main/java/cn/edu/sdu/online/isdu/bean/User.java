package cn.edu.sdu.online.isdu.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.litepal.LitePal;
import org.litepal.LitePalDB;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.util.List;
import java.util.Objects;

import cn.edu.sdu.online.isdu.app.MyApplication;
import cn.edu.sdu.online.isdu.net.AccountOp;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/10
 *
 * 用户信息类
 *
 * 加入学院和专业信息
 ****************************************************
 */

public class User extends LitePalSupport {
    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;
    public static final int GENDER_SECRET = 2;

    private String nickName; // 昵称
    private int gender; // 性别
    private String studentNumber; // 学号
    private String name; // 姓名
//    private String avatarString; // 头像字符串
    private String avatarUrl; // 头像所在URL
    private String selfIntroduce; // 个人介绍
    private String passwordMD5; // MD5加密的教务密码
    private String major; // 专业
    private String depart; // 学院
    private int uid; // ID号，非学号
    private Boolean bind; //是否绑定校园卡
    private transient int userVerification = 0;

    public static User staticUser; // 全局用户实例

    public User() {}

    public User(String nickName, String studentNumber, String avatarUrl, String selfIntroduce, int uid) {
        this.nickName = nickName;
        this.studentNumber = studentNumber;
//        this.avatarString = avatarString;
        this.selfIntroduce = selfIntroduce;
        this.uid = uid;
    }

    /**
     * 加载本地用户信息
     * @return Local user information
     */
    public static User load() {
        Context context = MyApplication.getContext();
        // 获取登录学号
        SharedPreferences sp = context.getSharedPreferences("login_cache", Context.MODE_PRIVATE);
        String studentNumber = sp.getString("student_number", "");

        return load(studentNumber);
    }

    public static User load(String studentNumber) {
        User user;

        if (!studentNumber.equals("")) {
            List<User> users = LitePal
                    .where("studentNumber = ?", studentNumber)
                    .find(User.class);

            if (!users.isEmpty())
                user = users.get(0);
            else
                user = new User();
        } else
            user = new User();

        return user;
    }

    public static User load(int uid) {
        User user;

        if (uid != -1) {
            List<User> users = LitePal
                    .where("uid = ?", uid + "")
                    .find(User.class);

            if (!users.isEmpty())
                user = users.get(0);
            else
                user = new User();
        } else
            user = new User();

        user.uid = uid;
        return user;
    }

    public static void logout(Context context) {
        AccountOp.logout(context);
        staticUser.studentNumber = null;
        staticUser.passwordMD5 = null;
    }

    /**
     * 保存信息至SharedPreference
     *
     * 首先在数据库中查找对应ID的用户，如果存在则进行更新操作
     * 如果不存在则新建
     */
    public void save(Context context) {
        // Find user in database
        List<User> users = LitePal.findAll(User.class);
        User user = null;
        for (User u : users) {
            if (u.studentNumber.equals(this.studentNumber)) {
                user = u;
                break;
            }
        }

        if (user == null) user = new User();
        user.setUid(uid);
        user.setDepart(depart);
        user.setMajor(major);
        user.setSelfIntroduce(selfIntroduce);
        user.setGender(gender);
        user.setName(name);
        user.setNickName(nickName);
        user.setPasswordMD5(passwordMD5);
        user.setStudentNumber(studentNumber);
        user.setAvatarUrl(avatarUrl);
        user.save(); // LitePal Save
    }

    @Override
    public boolean equals(Object o) {
        User user = (User) o;
        return Objects.equals(studentNumber, user.studentNumber) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentNumber);
    }

    public void loginCache(Context context) {
        SharedPreferences sp = context.getSharedPreferences("login_cache", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("student_number", studentNumber);
        editor.apply();
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSelfIntroduce() {
        return selfIntroduce;
    }

    public void setSelfIntroduce(String selfIntroduce) {
        this.selfIntroduce = selfIntroduce;
    }

    public String getPasswordMD5() {
        return passwordMD5;
    }

    public void setPasswordMD5(String passwordMD5) {
        this.passwordMD5 = passwordMD5;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public Boolean getBind() {
        return bind;
    }

    public void setBind(Boolean bind) {
        this.bind = bind;
    }

    public int getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(int userVerification) {
        this.userVerification = userVerification;
    }

    /**
     * 是否登录
     *
     * @return 已经登录为true
     */
    public static boolean isLogin() {
        if (staticUser == null) {
            staticUser = load();
        }
        return staticUser.studentNumber != null;
    }

}
