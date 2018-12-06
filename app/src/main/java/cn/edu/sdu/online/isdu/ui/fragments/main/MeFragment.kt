package cn.edu.sdu.online.isdu.ui.fragments.main

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.bean.Message
import cn.edu.sdu.online.isdu.bean.Schedule
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.AccountOp
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.*
import cn.edu.sdu.online.isdu.ui.design.button.ImageButton
import cn.edu.sdu.online.isdu.ui.widget.ScheduleListService
import cn.edu.sdu.online.isdu.util.EnvVariables
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import com.alibaba.fastjson.JSON
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_me.*
import org.json.JSONException
import org.json.JSONObject
import q.rorbin.badgeview.QBadgeView
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/15
 *
 * 主页个人中心碎片
 ****************************************************
 */

class MeFragment : Fragment(), View.OnClickListener, Serializable {

    /* 八宫格按钮 */
    private var btnSchedule: ImageButton? = null
    private var btnLibrary: ImageButton? = null
    private var btnBus: ImageButton? = null
    private var btnCalender: ImageButton? = null
    private var btnExam: ImageButton? = null
    private var btnStudyroom: ImageButton? = null
    private var btnGrade: ImageButton? = null
    private var btnCloud: ImageButton? = null
    private var btnMsg: TextView? = null
    private var btnFavorite: TextView? = null
    private var btnHistory: TextView? = null
    private var btnFollow: TextView? = null
    private var todayScheduleLayout: View? = null
    private var imgArrowForward: ImageView? = null
    private var btnDownload: View? = null
    private var txtDownload: TextView? = null
    private var btnSettings: View? = null

    private var functionButtonLayout: LinearLayout? = null

    private var circleImageView: CircleImageView? = null
    private var userName: TextView? = null
    private var userId: TextView? = null

    private var emptySymbol: TextView? = null
    /* TodoList */
    private var recyclerView: RecyclerView? = null
    private var adapter: TodoAdapter? = null
    private var todoList: MutableList<Schedule> = LinkedList()

    private var hasNewMsg = false // 是否有新消息

    private var personalInformationLayout: ConstraintLayout? = null // 个人信息入口，进入个人主页

    private var broadcastReceiver: UserSyncBroadcastReceiver? = null

    private var onMessageListener = Message.OnMessageListener {
        hasNewMsg = true
        if (btnMsg != null) {
            activity?.runOnUiThread {
                QBadgeView(activity)
                        .bindTarget(btnMsg)
                        .hide(false)
                QBadgeView(activity)
                        .bindTarget(btnMsg)
                        .setBadgeGravity(Gravity.TOP or Gravity.END)
                        .setGravityOffset(16f, 4f, true)
                        .setShowShadow(false)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_me, container, false)

        initView(view)
        initRecyclerView()

        prepareBroadcastReceiver()

        loadUserInformation()

        // 添加消息监听接口
        Message.addOnMessageListener(onMessageListener)
        return view
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            btn_bus.id -> {
                startActivity(Intent(activity, SchoolBusActivity::class.java))
            }
            btn_calender.id -> {
                startActivity(Intent(activity, CalendarActivity::class.java))
            }
            btn_cloud.id -> {
                startActivity(Intent(activity, CloudActivity::class.java))
            }
            btn_exam.id -> {
                startActivity(Intent(activity, ExamActivity::class.java))
            }
            btn_grade.id -> {
                startActivity(Intent(activity, GradeActivity::class.java))
            }
            btn_library.id -> {
                startActivity(Intent(activity, LibraryActivity::class.java))
            }
            btn_schedule.id -> {
                startActivity(Intent(activity, ScheduleActivity::class.java))
            }
            btn_studyroom.id -> {
                startActivity(Intent(activity, StudyRoomActivity::class.java))
            }
            btn_msg.id -> {
                startActivity(Intent(activity, MessageActivity::class.java))
                hasNewMsg = false
                setMsgBadge(false)
            }
            btn_my_favorite.id -> {
                startActivity(Intent(activity, CollectActivity::class.java))
            }
            btn_history.id -> {
                startActivity(Intent(activity, HistoryActivity::class.java))
            }
            btn_follow.id -> {

            }
            personal_information_layout.id -> {
                try {
                    Logger.log("User login = ${User.isLogin()}")
                    if (User.isLogin()) {
                        startActivity(Intent(activity, MyHomePageActivity::class.java))
                    } else {
                        startActivity(Intent(activity, LoginActivity::class.java))
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }
            }
            today_schedule.id -> {
                startActivity(Intent(activity, ScheduleActivity::class.java))
            }
            btn_download.id -> {
                startActivity(Intent(activity, DownloadActivity::class.java))
            }
            btn_settings.id -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserInformation()
        loadSchedule()
        if (hasNewMsg) {
            QBadgeView(activity)
                    .bindTarget(btnMsg)
                    .hide(false)
            QBadgeView(activity)
                    .bindTarget(btnMsg)
                    .setBadgeGravity(Gravity.TOP or Gravity.END)
                    .setGravityOffset(16f, 4f, true)
                    .setShowShadow(false)
        } else {
            QBadgeView(activity)
                    .bindTarget(btnMsg)
                    .hide(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unRegBroadcastReceiver()
    }

    fun setMsgBadge(show: Boolean) {
//        if (show) {
//            QBadgeView(context)
//                    .setBadgeGravity(Gravity.TOP or Gravity.END)
//                    .bindTarget(btnMsg).isShowShadow = false
//        } else {
//            QBadgeView(context)
//                    .setBadgeGravity(Gravity.TOP or Gravity.END)
//                    .bindTarget(btnMsg)
//                    .hide(false)
//        }
    }

    /**
     * 初始化View
     */
    private fun initView(view: View) {
        circleImageView = view.findViewById(R.id.circle_image_view)
        userName = view.findViewById(R.id.user_name)
        userId = view.findViewById(R.id.user_id)
        recyclerView = view.findViewById(R.id.todo_recycler_view)
        btnBus = view.findViewById(R.id.btn_bus)
        btnCalender = view.findViewById(R.id.btn_calender)
        btnCloud = view.findViewById(R.id.btn_cloud)
        btnExam = view.findViewById(R.id.btn_exam)
        btnGrade = view.findViewById(R.id.btn_grade)
        btnLibrary = view.findViewById(R.id.btn_library)
        btnSchedule = view.findViewById(R.id.btn_schedule)
        btnStudyroom = view.findViewById(R.id.btn_studyroom)
        btnMsg = view.findViewById(R.id.btn_msg)
        btnFavorite = view.findViewById(R.id.btn_my_favorite)
        btnHistory = view.findViewById(R.id.btn_history)
        btnFollow = view.findViewById(R.id.btn_follow)
        personalInformationLayout = view.findViewById(R.id.personal_information_layout)
        todayScheduleLayout = view.findViewById(R.id.today_schedule)
        imgArrowForward = view.findViewById(R.id.img_arrow_forward)
        emptySymbol = view.findViewById(R.id.empty_symbol)
        functionButtonLayout = view.findViewById(R.id.function_button)
        btnDownload = view.findViewById(R.id.btn_download)
        txtDownload = view.findViewById(R.id.txt_download)
        btnSettings = view.findViewById(R.id.btn_settings)

        btnBus!!.setOnClickListener(this)
        btnCalender!!.setOnClickListener(this)
        btnCloud!!.setOnClickListener(this)
        btnExam!!.setOnClickListener(this)
        btnGrade!!.setOnClickListener(this)
        btnLibrary!!.setOnClickListener(this)
        btnStudyroom!!.setOnClickListener(this)
        btnSchedule!!.setOnClickListener(this)
        todayScheduleLayout!!.setOnClickListener(this)
        btnDownload!!.setOnClickListener(this)
        btnSettings!!.setOnClickListener(this)

        btnMsg!!.setOnClickListener(this)
        btnFavorite!!.setOnClickListener(this)
        btnHistory!!.setOnClickListener(this)
        btnFollow!!.setOnClickListener(this)
        personalInformationLayout!!.setOnClickListener(this)
    }

    private fun prepareBroadcastReceiver() {
        if (broadcastReceiver == null) {
            val intentFilter = IntentFilter(AccountOp.ACTION_SYNC_USER_INFO)
            broadcastReceiver = UserSyncBroadcastReceiver(this)
            AccountOp.localBroadcastManager.registerReceiver(broadcastReceiver!!,
                    intentFilter)
        }
    }

    private fun unRegBroadcastReceiver() {
        if (broadcastReceiver != null)
            AccountOp.localBroadcastManager.unregisterReceiver(broadcastReceiver!!)
    }

    /**
     * 加载用户信息
     */
    private fun loadUserInformation() {
        if (User.isLogin()) {
            // 加载登录后信息
            val user = User.staticUser
//            circleImageView?.setImageBitmap(ImageManager.convertStringToBitmap(user.avatarString))
            if (circleImageView != null)
                Glide.with(context!!)
                        .load(user.avatarUrl)
                        .apply(RequestOptions.skipMemoryCacheOf(true))
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                        .into(circleImageView!!)
            userName?.text = user.nickName
            userId?.text = "学号:${user.studentNumber}"
            userId?.visibility = View.VISIBLE
            imgArrowForward?.visibility = View.VISIBLE
            functionButtonLayout?.visibility = View.VISIBLE
        } else {
            // 加载未登录信息
            circleImageView?.setImageResource(R.mipmap.ic_launcher)
            userName?.text = "点击以登录"
            userId?.text = ""
            userId?.visibility = View.GONE
            imgArrowForward?.visibility = View.GONE
            functionButtonLayout?.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        adapter = TodoAdapter()
        recyclerView!!.adapter = adapter

        if (adapter!!.itemCount == 0) {
            recyclerView!!.visibility = View.GONE
            emptySymbol!!.visibility = View.VISIBLE
        } else {
            recyclerView!!.visibility = View.VISIBLE
            emptySymbol!!.visibility = View.GONE
        }
    }

    /**
     * 加载今日安排
     */
    private fun loadSchedule() {
        if (User.isLogin())
            NetworkAccess.cache(ServerInfo.getScheduleUrl(User.staticUser.uid)) { success, cachePath ->
            if (success) {
                val jsonString = FileUtil.getStringFromFile(cachePath)
                try {
                    Schedule.localScheduleList =
                            Schedule.loadCourse(JSONObject(jsonString).getJSONArray("obj"))

                    if (EnvVariables.currentWeek <= 0)
                        EnvVariables.currentWeek = EnvVariables.calculateWeekIndex(System.currentTimeMillis())
                    todoList.clear()

                    // 去重
                    for (schedule in Schedule.localScheduleList[EnvVariables.currentWeek - 1][EnvVariables.getCurrentDay() - 1]) {
                        if (!todoList.contains(schedule)) todoList.add(schedule)
                    }
//                    todoList.addAll(Schedule.localScheduleList[EnvVariables.currentWeek - 1][EnvVariables.getCurrentDay() - 1])

                    // 排序
                    for (i in 0 until todoList.size - 1) {
                        for (j in i + 1 until todoList.size) {
                            if (todoList[i].startTime.laterThan(todoList[j].startTime)) {
                                todoList[i].swap(todoList[j])
                            }
                        }
                    }

                    // 去掉本周不上的课
                    val copyTodoList: MutableList<Schedule> = ArrayList()
                    copyTodoList.addAll(todoList)
                    todoList.clear()
                    for (schedule in copyTodoList) {
                        if (schedule.repeatWeeks.contains(EnvVariables.currentWeek))
                            todoList.add(schedule)
                    }

                    activity!!.runOnUiThread {
                        if (todoList.isEmpty()) {
                            recyclerView!!.visibility = View.GONE
                            emptySymbol!!.visibility = View.VISIBLE
                        } else {
                            recyclerView!!.visibility = View.VISIBLE
                            emptySymbol!!.visibility = View.GONE
                        }
                        adapter!!.notifyDataSetChanged()
                    }

                    // 储存到本地并通知小部件更新
                    context!!.getSharedPreferences("schedule", Context.MODE_PRIVATE)
                            .edit()
                            .putString("schedule", JSON.toJSONString(todoList))
                            .apply()

                    context!!.sendBroadcast(Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE))
                } catch (e: JSONException) {
                    Logger.log(e)
                }
            }
        }

    }


    inner class TodoAdapter : RecyclerView.Adapter<TodoAdapter.ViewHolder>() {

        override fun getItemCount(): Int = todoList.size


            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = todoList[position]
                holder.todoIndex.text = (position + 1).toString()
                holder.todoName.text = item.scheduleName
                holder.todoTime.text = item.startTime.toString()
                holder.todoLocation.text = item.scheduleLocation
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false)

                return ViewHolder(view)
            }

            inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
                var todoIndex = view.findViewById<TextView>(R.id.todo_index)
                var todoName = view.findViewById<TextView>(R.id.todo_name)
                var todoTime = view.findViewById<TextView>(R.id.todo_time)
                var todoLocation = view.findViewById<TextView>(R.id.todo_location)
            }

    }

    class UserSyncBroadcastReceiver(meFragment: MeFragment) : BroadcastReceiver() {
        private val fragmentMe = meFragment
        override fun onReceive(context: Context?, intent: Intent?) {
            fragmentMe.loadUserInformation()
//            if (intent!!.action == AccountOp.ACTION_SYNC_USER_INFO) {
//                val data = intent.extras.getString("result")
//                Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
//            }
        }
    }
}