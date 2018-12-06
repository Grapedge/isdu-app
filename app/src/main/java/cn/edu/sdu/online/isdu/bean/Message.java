package cn.edu.sdu.online.isdu.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import cn.edu.sdu.online.isdu.app.MyApplication;
import cn.edu.sdu.online.isdu.util.Logger;

public class Message {
    private String type;
    private String senderId;
    private String senderNickname;
    private String senderAvatar;
    private String time;
    private String content;
    private int postId;
    private boolean isRead = false;
    private static List<OnMessageListener> listeners = new ArrayList<>();

    public static LinkedList<Message> msgList = new LinkedList<>();

    public Message() {}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setRead(boolean read, Context context) {
        isRead = read;
        msgList.remove(this);
        int i;
        for (i = 0; i < msgList.size(); i++) {
            if (msgList.get(i).isRead != isRead) {
                msgList.addLast(this);
                break;
            }
        }
        if (i == msgList.size()) msgList.addLast(this);
        saveMsgList(context);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public void setSenderNickname(String senderNickname) {
        this.senderNickname = senderNickname;
    }

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public static void loadMsgList(Context context) {
        msgList.clear();
        SharedPreferences sp = MyApplication.getContext().getSharedPreferences("msg", Context.MODE_PRIVATE);
        String str = sp.getString("json", "[]");
        try {
            msgList.addAll(JSON.parseArray(str, Message.class));
        } catch (Exception e) {
            Logger.log(e);
        }
    }

    public static void addOnMessageListener(OnMessageListener listener) {
        listeners.add(listener);
    }

    public static void saveMsgList(Context context) {
        SharedPreferences.Editor editor =
                MyApplication.getContext().getSharedPreferences("msg", Context.MODE_PRIVATE).edit();
        editor.putString("json", JSON.toJSONString(msgList));
        editor.apply();
        for (OnMessageListener listener : listeners) {
            if (listener != null)
                listener.onMessage();
        }
    }

    public static void newMsg(List<Message> msgList, Context context) {
        if (Message.msgList == null || Message.msgList.size() == 0)
            loadMsgList(context);
        for (Message msg : msgList) {
            if (Message.msgList.contains(msg)) {
                Message.msgList.remove(msg);
            }
        }
        for (int i = 0; i < msgList.size(); i++) {
            Message.msgList.addFirst(msgList.get(i));
        }
        saveMsgList(context);
    }

    public static int getUnreadCount() {
        int count = 0;
        if (msgList == null || msgList.size() == 0)
            loadMsgList(MyApplication.getContext());
        for (Message msg : msgList) {
            if (!msg.isRead) count++;
        }
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return type.equals(message.type) &&
                Objects.equals(senderId, message.senderId) &&
                Objects.equals(content, message.content);
    }

    public interface OnMessageListener {
        void onMessage();
    }
}
