package cn.edu.sdu.online.isdu.ui.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.Exam
import cn.edu.sdu.online.isdu.bean.Schedule
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.CourseTable
import cn.edu.sdu.online.isdu.ui.design.ScheduleTable
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.util.*
import kotlinx.android.synthetic.main.activity_schedule.*
import org.json.JSONObject

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/5/29
 *
 * 日程表活动
 ****************************************************
 */

class ScheduleActivity : SlideActivity(), View.OnClickListener {

//    private var scheduleTable: ScheduleTable? = null
    private var scheduleTable: CourseTable? = null
    private var totalWeeks = 20
    private var currentWeek = 1

    private var txtCurrentWeek: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var icIndicator: ImageView? = null
    private var btnBack: ImageView? = null
    private var btnAdd: ImageView? = null

    private var onWeekSelectListener: MyAdapter.OnWeekSelectListener? = null

    private var selectLayoutVisible = false

    private var totalList: MutableList<MutableList<MutableList<Schedule>>>? = ArrayList()

    private var adapter: MyAdapter? = null

    private var examList = ArrayList<Exam>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

    }

    private fun init() {
        if (!User.isLogin()) {
            val dialog = AlertDialog(this)
            dialog.setTitle("无数据")
            dialog.setMessage("请登录后重试")
            dialog.setCancelOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.setPositiveButton("登录") {
                dialog.dismiss()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            dialog.setNegativeButton("返回") {
                finish()
                dialog.dismiss()
            }
            dialog.show()
        }


        if (EnvVariables.currentWeek == -1) {

        } else {
            initView()
            initSchedule()
        }
    }

    override fun onResume() {
        super.onResume()
        EnvVariables.init(this)
        init()
    }

    private fun initView() {
        scheduleTable = findViewById(R.id.schedule_table)
        recyclerView = findViewById(R.id.recycler_view)
        txtCurrentWeek = findViewById(R.id.txt_current_week)
        icIndicator = findViewById(R.id.ic_indicator)
        btnBack = findViewById(R.id.btn_back)
        btnAdd = findViewById(R.id.btn_add)

        ic_indicator.setOnClickListener(this)

        txtCurrentWeek!!.setOnClickListener(this)
        btnBack!!.setOnClickListener(this)
        btnAdd!!.setOnClickListener(this)

        getTotalWeeks()
        getCurrentWeek()

        setCurrentWeek(currentWeek)

        initRecyclerView()
    }

    override fun onBackPressed() {
        if (scheduleTable != null && scheduleTable!!.onBackPressed()) {

        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.txt_current_week -> {
                if (selectLayoutVisible)
                    hideWeekSelect()
                else
                    showWeekSelect()

                rotateIndicator(selectLayoutVisible)
                selectLayoutVisible = !selectLayoutVisible
            }
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_add -> {
                startActivity(Intent(this, CreateScheduleActivity::class.java))
            }
            R.id.ic_indicator -> {
                txt_current_week.callOnClick()
            }
        }
    }

    /**
     * 旋转上下标识
     *
     * @param orientation 和 selectLayoutVisible 同真值
     */
    private fun rotateIndicator(orientation: Boolean) {
        if (orientation) {
            val animator = ObjectAnimator.ofFloat(icIndicator!!, "rotation", -180f, 0f)
            animator.duration = 100
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        } else {
            val animator = ObjectAnimator.ofFloat(icIndicator!!, "rotation", 0f, 180f)
            animator.duration = 100
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        }
    }

    /**
     * 在初始化日程表时
     * 指定currentWeek
     * 从startWeek到endWeek中选取第currentWeek - 1个列表进行绘制
     */
    private fun initSchedule() {
        initScheduleData()

//        scheduleTable!!.setOnItemClickListener {
//            schedule ->
//            val sb = StringBuilder()
//            for (i in schedule!!.repeatWeeks)
//                sb.append("$i,")
//
//            val intent = Intent(this@ScheduleActivity, ScheduleDetailActivity::class.java)
//            intent.putExtra("name", schedule.scheduleName)
//                    .putExtra("location", schedule.scheduleLocation)
//                    .putExtra("color", schedule.scheduleColor)
//                    .putExtra("start_time", schedule.startTime.toString())
//                    .putExtra("end_time", schedule.endTime.toString())
//                    .putExtra("repeat_weeks", sb.toString())
//            startActivity(intent)
//        }
        if (totalList!!.isNotEmpty())
            scheduleTable!!.setScheduleList(totalList!![currentWeek - 1])
    }

    private fun initRecyclerView() {
        val dataList = ArrayList<SelectableWeekIndex>()
        for (i in EnvVariables.startWeek until EnvVariables.endWeek + 1) {
            dataList.add(SelectableWeekIndex(i + 1))
        }
        dataList[currentWeek - 1].selected = true
        adapter = MyAdapter(dataList)

        onWeekSelectListener = object : MyAdapter.OnWeekSelectListener {
            override fun onWeekSelect(index: Int) {
                setCurrentWeek(index)
            }
        }

        adapter!!.setOnWeekSelectListener(onWeekSelectListener!!)

        recyclerView!!.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView!!.adapter = adapter

        recyclerView!!.scrollToPosition(currentWeek - 1)
    }

    private fun initScheduleData() {
        NetworkAccess.cache(ServerInfo.getScheduleUrl(User.staticUser.uid)) { success, cachePath ->
            if (success) {
                val jsonString = FileUtil.getStringFromFile(cachePath)
                try {
//                    Log.d("Jzz", "Url=${ServerInfo.getScheduleUrl(User.staticUser.uid)}\nContent=$jsonString")
                    val courseArray = JSONObject(jsonString).getJSONArray("obj")
                    totalList = Schedule.loadCourse(courseArray)
                    // 加载考试信息
                    getExamData()

                    runOnUiThread {
                        setCurrentWeek(currentWeek)
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }
            }
        }
    }


    private fun getExamData() {
        if (User.staticUser == null) User.staticUser = User.load()
        NetworkAccess.cache(ServerInfo.getExamUrl(User.staticUser.uid)) { success, cachePath ->
            if (success) {
                val jsonString = FileUtil.getStringFromFile(cachePath)
                if (jsonString != null && jsonString.trim() != "") {
                    try {
                        val jsonObject = JSONObject(jsonString)

                        if (jsonObject.getString("status") == "success") {

                            val exams = jsonObject.getJSONObject("obj")
//                            for (i in 0 until exams.length()) {
                            // 期末考试、形势政策考试……
                            val examNames = exams.names()
                            for (j in 0 until examNames.length()) {
                                // 获取期末考试、形势政策考试的JSONArray

                                val obj = exams.getJSONArray(examNames.getString(j))

                                for (k in 0 until obj.length()) {
                                    val exam = obj.getJSONObject(k)
                                    // 获取每场考试内容
                                    val item = Exam(
                                            exam.getString("examDate"),
                                            exam.getString("examTime"),
                                            exam.getString("examRoom"),
                                            exam.getString("resultComposition"),
                                            exam.getString("examMethod"),
                                            exam.getString("courseName")
                                    )

                                    if (EnvVariables.firstWeekTimeMillis >
                                            DateCalculate.getTimeMillis(item.date)) {
                                        // 考试在起始周之前，不添加
                                    } else {
                                        val schedule = item.toSchedule()
                                        schedule.repeatWeeks.add(item.week)

                                        totalList!![item.week - 1][item.day - 1].add(schedule)
                                    }

                                }

                            }
//                            }

                            runOnUiThread {
                                adapter!!.notifyDataSetChanged()
                            }

                        }

                    } catch (e: Exception) {
                        Logger.log(e)
                    }
                } else {

                }
            }
        }
    }

    private fun getTotalWeeks() {
        totalWeeks = EnvVariables.endWeek - EnvVariables.startWeek + 1
    }

    private fun getCurrentWeek() {
        currentWeek = EnvVariables.currentWeek
    }

    private fun setCurrentWeek(index: Int) {
        currentWeek = index

        if (totalList!!.isNotEmpty()) {
            scheduleTable!!.setScheduleList(totalList!![currentWeek - 1])
        }

        scheduleTable!!.setCurrentWeekIndex(currentWeek)
        txtCurrentWeek!!.text = "第 $index 周"

        adapter?.notifyDataSetChanged()

    }

    private fun hideWeekSelect() {
//        val px = PixelUtil.dp2px(this, 60)
        val animator = ObjectAnimator.ofFloat(scheduleTable!!, "translationY",
                scheduleTable!!.translationY, 0f)
        animator.duration = 100
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    private fun showWeekSelect() {
        val px = PixelUtil.dp2px(this, 60)
        val animator = ObjectAnimator.ofFloat(scheduleTable!!, "translationY",
                scheduleTable!!.translationY, px.toFloat())
        animator.duration = 100
        animator.interpolator = DecelerateInterpolator()
        animator.start()
    }

    class MyAdapter(private val dataList: List<SelectableWeekIndex>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private var onWeekSelectListener: OnWeekSelectListener? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.week_select_item, parent, false)

            return ViewHolder(view)
        }

        override fun getItemCount(): Int = dataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemLayout.text = (position + 1).toString()
            holder.itemLayout.setBackgroundResource(
                    if (dataList[position].selected) R.drawable.purple_circle
                    else R.drawable.white_circle)
            holder.itemLayout.setOnClickListener {
                onWeekSelectListener?.onWeekSelect(position + 1)

                for (item in dataList) {
                    item.selected = false
                }
                dataList[position].selected = true
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var itemLayout: TextView = view.findViewById(R.id.item_layout)
        }

        fun setOnWeekSelectListener(onWeekSelectListener: OnWeekSelectListener) {
            this.onWeekSelectListener = onWeekSelectListener
        }

        interface OnWeekSelectListener {
            fun onWeekSelect(index: Int)
        }
    }

    class SelectableWeekIndex(index: Int) {
        var weekIndex: Int = 0
        var selected: Boolean = false

        init {
            weekIndex = index
        }
    }
}
