package cn.edu.sdu.online.isdu.net;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import org.json.JSONObject;

import java.io.IOException;

import cn.edu.sdu.online.isdu.app.MyApplication;
import cn.edu.sdu.online.isdu.bean.User;
import cn.edu.sdu.online.isdu.net.pack.ServerInfo;
import cn.edu.sdu.online.isdu.util.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/10
 *
 * 用户信息访问
 ****************************************************
 */

public class AccountOp {

    public static final String ACTION_SYNC_USER_INFO =
            "cn.edu.sdu.online.isdu.SYNC_USER_INFO_SUCCESS";
    public static final String ACTION_USER_LOG_OUT =
            "cn.edu.sdu.online.isdu.USER_LOG_OUT";
    public static final String ACTION_UPDATE_USER_INFO =
            "cn.edu.sdu.online.isdu.UPDATE_USER_INFO";
    public static final String ACTION_SYNC_USER_AVATAR =
            "cn.edu.sdu.online.isdu.SYNC_USER_AVATAR_SUCCESS";

    public static LocalBroadcastManager localBroadcastManager =
            LocalBroadcastManager.getInstance(MyApplication.getContext());

    /**
     * 从服务器同步用户信息
     */
    public static void syncUserInformation() {
        /////////Network
        if (User.staticUser == null) User.staticUser = User.load();
        String stuNum = User.staticUser.getStudentNumber();
        String stuPwd = User.staticUser.getPasswordMD5();

        if (stuNum != null && stuPwd != null && !stuNum.trim().equals("") && !stuPwd.trim().equals(""))
            NetworkAccess.buildRequest(ServerInfo.getUrlLogin(stuNum, stuPwd), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Logger.log(e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.body() != null) {

                        String jsonString = response.body().string();

                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);

                            if (jsonObject.isNull("status") || !jsonObject.getString("status").equals("failed")) {
                                AccountOp.syncUserInformation(jsonObject); // 同步用户信息
                                final Intent intent = new Intent(ACTION_SYNC_USER_INFO);
                                intent.putExtra("result", "success");
                                localBroadcastManager.sendBroadcast(intent);
                            } else {
                                final Intent intent = new Intent(ACTION_SYNC_USER_INFO);
                                intent.putExtra("result", jsonObject.getString("failed"));
                                localBroadcastManager.sendBroadcast(intent);
                            }

                        } catch (Exception e) {
                            Logger.log(e);
                        }


                    }
                }
            });

    }

    /**
     * 从JSON同步用户信息
     *
     * @param jsonObject 包含用户信息的JSON对象
     */
    public static void syncUserInformation(JSONObject jsonObject) {
        try {
            if (jsonObject.isNull("status") || !jsonObject.getString("status").equals("failed")) {
                JSONObject userObj = jsonObject.getJSONObject("user");
                User.staticUser.setStudentNumber(userObj.getString("studentnumber"));
                User.staticUser.setPasswordMD5(userObj.getString("j_password"));
                User.staticUser.setNickName(userObj.getString("nickname"));
                User.staticUser.setName(userObj.getString("name"));
                User.staticUser.setAvatarUrl(userObj.getString("avatar"));

                String genderString = userObj.getString("gender");
                if (genderString.equals("男")) {
                    User.staticUser.setGender(User.GENDER_MALE);
                } else if (genderString.equals("女")) {
                    User.staticUser.setGender(User.GENDER_FEMALE);
                } else {
                    User.staticUser.setGender(User.GENDER_SECRET);
                }

                User.staticUser.setSelfIntroduce(userObj.getString("sign"));
                User.staticUser.setMajor(userObj.getString("major"));
                User.staticUser.setDepart(userObj.getString("depart"));
                User.staticUser.setUid(userObj.getInt("id"));
                User.staticUser.setUserVerification(userObj.getInt("verification"));

                User.staticUser.save(MyApplication.getContext());
                User.staticUser.loginCache(MyApplication.getContext());

                final Intent intent = new Intent(ACTION_SYNC_USER_INFO);
                intent.putExtra("result", "success");
                localBroadcastManager.sendBroadcast(intent);
            } else {
                final Intent intent = new Intent(ACTION_SYNC_USER_INFO);
                intent.putExtra("result", jsonObject.getString("status"));
                localBroadcastManager.sendBroadcast(intent);
            }

        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public static void logout(Context context) {
        SharedPreferences.Editor editor =
                context.getSharedPreferences("login_cache", Context.MODE_PRIVATE).edit();
        editor.remove("student_number");
        editor.apply();
        final Intent intent = new Intent(ACTION_USER_LOG_OUT);
        localBroadcastManager.sendBroadcast(intent);
    }

}
