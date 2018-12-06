package cn.edu.sdu.online.isdu.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.BuildConfig
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.button.WideButton
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.InputDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.OptionDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.Settings
import cn.edu.sdu.online.isdu.util.download.IDownloadItem
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/8
 *
 * 设置Activity
 *
 * #添加云同步
 ****************************************************
 */

class SettingsActivity : SlideActivity(), View.OnClickListener, WideButton.OnItemClickListener,
                    WideButton.OnItemSwitchListener {

    private var btnBack: ImageView? = null

    private var btnStartupPage: WideButton? = null
    private var btnAlarmMessage: WideButton? = null
    private var btnAlarmNews: WideButton? = null
    private var btnAlarmSchedule: WideButton? = null
    private var btnCloudSync: WideButton? = null
    private var btnFeedBack: WideButton? = null
    private var btnLogout: TextView? = null
    private var btnDownloadLocation: WideButton? = null
    private var btnCheckUpdate: WideButton? = null
    private var btnAbout: WideButton? = null
    private var btnLicense: WideButton? = null

    private var btnBugReport: WideButton? = null

    private val startupPages = arrayListOf("论坛", "资讯", "个人中心")
    private val alarmScheduleList = listOf("不提醒", "震动", "铃声（需要开启媒体音量）")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        Settings.load(this)

        initView()

        initSettings()
    }

    /**
     * 加载设置项
     */
    private fun initSettings() {
        btnStartupPage!!.setTxtComment(startupPages[Settings.STARTUP_PAGE])

        btnAlarmMessage!!.setSwitch(Settings.ALARM_MESSAGE)
        btnAlarmNews!!.setSwitch(Settings.ALARM_NEWS)
        btnAlarmSchedule!!.setTxtComment(alarmScheduleList[Settings.ALARM_SCHEDULE])
        btnDownloadLocation!!.setTxtComment(Settings.DEFAULT_DOWNLOAD_LOCATION)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_logout -> {
                User.logout(this)
                Toast.makeText(this, "注销成功", Toast.LENGTH_SHORT).show()
                finish()
            }

        }
    }

    override fun onClick(itemId: String?) {
        when (itemId) {
            "startup_page" -> {
                val dialog = OptionDialog(this, startupPages)
                dialog.setMessage("选择启动页")
                dialog.setOnItemSelectListener { itemName ->
                    Settings.STARTUP_PAGE = startupPages.indexOf(itemName)
                    Settings.store(this)
                    btnStartupPage!!.setTxtComment(itemName)
                }
                dialog.show()
            }
            "alarm_schedule" -> {
                val dialog = OptionDialog(this, alarmScheduleList)
                dialog.setMessage("日程提醒")
                dialog.setOnItemSelectListener { itemName ->
                    Settings.ALARM_SCHEDULE = alarmScheduleList.indexOf(itemName)
                    Settings.store(this)
                    btnAlarmSchedule!!.setTxtComment(itemName)
                }
                dialog.show()
            }
            "feedback" -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
            "download_location" -> {
                val dialog = InputDialog(this)
                dialog.setTitle("输入下载位置")
                dialog.text = Settings.DEFAULT_DOWNLOAD_LOCATION
                dialog.setPositiveButton("确定") {

                    Settings.DEFAULT_DOWNLOAD_LOCATION = dialog.text
                    Settings.store(this)
                    dialog.dismiss()


                }

                dialog.setNegativeButton("取消") {
                    dialog.dismiss()
                }

                dialog.show()
            }
            "bug_report" -> {
                val dialog = ProgressDialog(this, false)
                dialog.setMessage("正在处理日志文件")
                dialog.setButton(null, null)
                dialog.show()

                val dir = File(Environment.getExternalStorageDirectory().toString() + "/iSDU/log/")
                val files = dir.listFiles()

                val sb = StringBuilder()

                for (i in files.size - 1 until files.size - 6) {
                    // 获取最近5次的Log文件
                    if (i < 0) break
                    if (files[i].name.endsWith(".log")) {
                        sb.append(FileUtil.getStringFromFile(files[i].absolutePath))
                    }
                }

                NetworkAccess.buildRequest(ServerInfo.url, "bug", sb.toString(), object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        runOnUiThread {
                            dialog.dismiss()
                            Toast.makeText(this@SettingsActivity, "提交失败", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        runOnUiThread {
                            dialog.dismiss()
                            Toast.makeText(this@SettingsActivity, "提交成功", Toast.LENGTH_SHORT).show()
                        }

                    }
                })
            }
            "check_update" -> {
                val progDialog = ProgressDialog(this, false)
                progDialog.setMessage("正在检查更新")
                progDialog.setButton(null, null)
                progDialog.show()
                NetworkAccess.buildRequest(ServerInfo.buildInfo, object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        runOnUiThread {
                            progDialog.dismiss()
                            Toast.makeText(this@SettingsActivity, "网络错误", Toast.LENGTH_SHORT).show()
                        }

                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        try {
                            val obj = JSONObject(response?.body()?.string())
                            if (BuildConfig.VERSION_CODE < obj.getInt("versionCode")) {
                                runOnUiThread {
                                    val dialog = AlertDialog(this@SettingsActivity)
                                    dialog.setTitle("检测到新版本")
                                    dialog.setMessage("新版本 ${obj.getString("versionName")}\n${obj.getString("versionDescription")}")
                                    dialog.setPositiveButton("更新") {
                                        IDownloadItem(obj.getString("downloadUrl")).startDownload()
                                        dialog.dismiss()
                                    }
                                    dialog.setNegativeButton("取消") {
                                        dialog.dismiss()
                                    }
                                    dialog.show()
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@SettingsActivity, "已经是最新版本",
                                            Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (e: Exception) {
                            Logger.log(e)
                            runOnUiThread {
                                Toast.makeText(this@SettingsActivity, "未获取到信息",
                                        Toast.LENGTH_SHORT).show()
                            }

                        } finally {
                            runOnUiThread { progDialog.dismiss() }
                        }
                    }
                })
            }
            "about" -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            "license" -> {
                startActivity(Intent(this, LicenseActivity::class.java))
            }
        }
    }

    override fun onSwitch(itemId: String?, b: Boolean) {
        when (itemId) {
            "alarm_message" -> {
                Settings.ALARM_MESSAGE = b
                Settings.store(this)
            }
            "alarm_news" -> {
                Settings.ALARM_NEWS = b
                Settings.store(this)
            }
            "cloud_sync" -> {
                Settings.CLOUD_SYNC = b
                Settings.store(this)
            }
        }
    }

    private fun initView() {
        btnBack = findViewById(R.id.btn_back)
        btnStartupPage = findViewById(R.id.btn_startup_page)
        btnAlarmMessage = findViewById(R.id.btn_alarm_message)
        btnAlarmNews = findViewById(R.id.btn_alarm_news)
        btnAlarmSchedule = findViewById(R.id.btn_alarm_schedule)
        btnCloudSync = findViewById(R.id.btn_cloud_sync)
        btnFeedBack = findViewById(R.id.btn_feedback)
        btnDownloadLocation = findViewById(R.id.btn_download_location)
        btnBugReport = findViewById(R.id.btn_bug_report)
        btnCheckUpdate = findViewById(R.id.btn_check_update)
        btnAbout = findViewById(R.id.btn_about)
        btnLicense = findViewById(R.id.btn_license)

        btnCheckUpdate!!.setTxtComment("版本号 ${BuildConfig.VERSION_NAME}")

        if (!BuildConfig.IS_TEST_VERSION) btnBugReport!!.visibility = View.GONE

        btnLogout = findViewById(R.id.btn_logout)

        btnBack!!.setOnClickListener(this)
        btnLogout!!.setOnClickListener(this)
        btnFeedBack!!.setOnItemClickListener(this)
        btnStartupPage!!.setOnItemClickListener(this)
        btnAlarmSchedule!!.setOnItemClickListener(this)
        btnAlarmNews!!.setOnItemSwitchListener(this)
        btnAlarmMessage!!.setOnItemSwitchListener(this)
        btnCloudSync!!.setOnItemSwitchListener(this)
        btnDownloadLocation!!.setOnItemClickListener(this)
        btnBugReport!!.setOnItemClickListener(this)
        btnCheckUpdate!!.setOnItemClickListener(this)
        btnAbout!!.setOnItemClickListener(this)
        btnLicense!!.setOnItemClickListener(this)

//        if (User.staticUser == null) User.staticUser = User.load()
//        if (User.staticUser.studentNumber == null ||
//                User.staticUser.studentNumber == "")
        if (!User.isLogin())
            btnLogout!!.visibility = View.GONE
    }

    override fun prepareBroadcastReceiver() {

    }

    override fun unRegBroadcastReceiver() {

    }
}
