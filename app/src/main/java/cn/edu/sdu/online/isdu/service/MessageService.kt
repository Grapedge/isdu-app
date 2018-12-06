package cn.edu.sdu.online.isdu.service

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import cn.edu.sdu.online.isdu.bean.Message
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.ui.activity.MessageActivity
import cn.edu.sdu.online.isdu.util.NotificationUtil
import com.alibaba.fastjson.JSON
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MessageService : Service() {
    private var userId: String = ""

    private var noticeRunnable = Runnable {
        val client = OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build()
        val request = Request.Builder()
                .get()
                .url(ServerInfo.getNotice(userId))
                .build()
        while (true) {
            try {
                val response = client.newCall(request).execute()
                try {
                    val str = response?.body()?.string()
                    if (JSONObject(str).getString("status") == "success") {
                        val jsonString = JSONObject(str).getString("obj")
                        val msgList = JSON.parseArray(jsonString, Message::class.java)
                        Message.newMsg(msgList, this)
                        Message.saveMsgList(this)

                        // 发送消息通知
                        if (msgList.size == 1) {
                            NotificationUtil.Builder(this)
                                    .setTitle(msgList[0].content)
                                    .setMessage("点击前往消息页面查看")
                                    .setIntent(PendingIntent.getActivity(
                                            this, 0,
                                            Intent(this, MessageActivity::class.java),
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    ))
                                    .setAutoCancel(true)
                                    .setPriority(NotificationUtil.PRIORITY_MAX)
                                    .show()
                        } else {
                            NotificationUtil.Builder(this)
                                    .setTitle("有 ${msgList.size} 条新消息")
                                    .setMessage("点击前往消息页面查看")
                                    .setIntent(PendingIntent.getActivity(
                                            this, 0,
                                            Intent(this, MessageActivity::class.java),
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    ))
                                    .setAutoCancel(true)
                                    .setPriority(NotificationUtil.PRIORITY_MAX)
                                    .show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                Thread.sleep(10000)
            } catch (e: Exception) {

            }
        }
    }

    override fun onBind(intent: Intent): IBinder {
        throw Exception("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        userId = intent?.getStringExtra("uid") ?: ""
        if (userId != "") {
            Thread(noticeRunnable).start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
    }

}
