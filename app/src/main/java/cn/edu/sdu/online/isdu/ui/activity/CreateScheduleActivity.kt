package cn.edu.sdu.online.isdu.ui.activity

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import kotlinx.android.synthetic.main.activity_create_schedule.*
import kotlin.coroutines.experimental.coroutineContext
import android.widget.TextView
import android.widget.BaseAdapter
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.TimeDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.WeekDialog
import cn.edu.sdu.online.isdu.util.EnvVariables
import cn.edu.sdu.online.isdu.util.ScheduleTime


/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/8
 *
 * 新建日程活动
 ****************************************************
 */

class CreateScheduleActivity : SlideActivity(), View.OnClickListener {
    private var dayNames = listOf("一", "二", "三", "四", "五", "六", "日")
    private var btnBack: ImageView? = null
    private var scheduleName: EditText? = null
    private var scheduleLocation: EditText? = null
    private var scheduleWeeks: TextView? = null
    private var scheduleTimeStart: TextView? = null
    private var scheduleTimeEnd: TextView? = null
    private var repeatType: Spinner? = null
    private var btnAdd: TextView? = null

    private var weeks = ArrayList<Int>()
    private var days = ArrayList<Int>()
    private var startTime = ScheduleTime.currentTime()
    private var endTime = ScheduleTime.currentTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_schedule)
        initView()
        updateData()

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.schedule_weeks -> {
                val dialog = WeekDialog(this, EnvVariables.startWeek,
                        EnvVariables.endWeek)
                dialog.setTitle("选择周/星期")
                val listWeek = ArrayList<WeekDialog.Week>()
                val listDay = ArrayList<WeekDialog.Day>()

                for (i in EnvVariables.startWeek until EnvVariables.endWeek + 1) {
                    listWeek.add(WeekDialog.Week(i))
                }
                for (i in 0 until EnvVariables.endWeek - EnvVariables.startWeek + 1) {
                    if (weeks.contains(i + EnvVariables.startWeek)) listWeek[i].chosen = true
                }

                for (i in 0 until 7) {
                    listDay.add(WeekDialog.Day(dayNames[i]))
                }
                for (i in 0 until 7) {
                    if (days.contains(i + 1)) listDay[i].chosen = true
                }

                dialog.setWeeks(listWeek)
                dialog.setDays(listDay)

                dialog.setPositiveButton("确定") {
                    weeks = dialog.weeks as ArrayList<Int>
                    days = dialog.days as ArrayList<Int>
                    updateData()
                    dialog.dismiss()
                }
                dialog.setNegativeButton("取消") {dialog.dismiss()}
                dialog.show()
            }
            R.id.schedule_time_start -> {
                val dialog = TimeDialog(this, startTime)
                dialog.setPositiveButton("确定") {
                    startTime = dialog.time
                    updateData()
                    dialog.dismiss()
                }
                dialog.setNegativeButton("取消") {dialog.dismiss()}
                dialog.show()
            }
            R.id.schedule_time_end -> {
                val dialog = TimeDialog(this, endTime)
                dialog.setPositiveButton("确定") {
                    endTime = dialog.time
                    updateData()
                    dialog.dismiss()
                }
                dialog.setNegativeButton("取消") {dialog.dismiss()}
                dialog.show()
            }
            R.id.btn_add -> {

            }
        }
    }

    private fun initView() {
        btnBack = btn_back
        scheduleName = schedule_name
        scheduleLocation = schedule_location
        scheduleWeeks = schedule_weeks
        scheduleTimeStart = schedule_time_start
        scheduleTimeEnd = schedule_time_end
        repeatType = repeat_type
        btnAdd = btn_add

        btnBack!!.setOnClickListener(this)
        scheduleWeeks!!.setOnClickListener(this)
        scheduleTimeStart!!.setOnClickListener(this)
        scheduleTimeEnd!!.setOnClickListener(this)
        btnAdd!!.setOnClickListener(this)

        ///Spinner
        val adapter = MySpinnerAdapter(this)
        repeatType!!.adapter = adapter
        repeatType!!.setSelection(0)
    }

    // 更新选择数据
    private fun updateData() {
        val sbWeek = StringBuilder()
        if (weeks.isEmpty() ||
                weeks.size == EnvVariables.endWeek - EnvVariables.startWeek + 1) {
            sbWeek.append("每周 ")
        } else {
            sbWeek.append("第")
            for (i in 0 until weeks.size - 1) sbWeek.append(weeks[i].toString() + "，")
            sbWeek.append(weeks[weeks.size - 1].toString() + "周 ")
        }

        if (days.isEmpty() ||
                days.size == 7) {
            sbWeek.append("每天")
        } else {
            for (i in 0 until days.size) sbWeek.append("周${dayNames[days[i] - 1]}")
        }
        scheduleWeeks!!.text = sbWeek.toString()

        if (endTime.earlierThan(startTime)) {
            val tmp = startTime
            startTime = endTime
            endTime = tmp
        }

        scheduleTimeStart!!.text = "从 $startTime"
        scheduleTimeEnd!!.text = "到 $endTime"
    }

    class MySpinnerAdapter(context: Context) : BaseAdapter() {
        private val repeatTypes = listOf("仅一次", "每天", "每周")
        private val context = context

        override fun isEmpty(): Boolean = repeatTypes.isEmpty()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            var holder: ViewHolder
            if (convertView == null) {
                holder = ViewHolder()
                convertView = LayoutInflater.from(context).inflate(R.layout.create_schedule_repeat_item, null)
                holder.text = convertView as TextView
                convertView.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
            }

            holder.text!!.text = repeatTypes[position]

            return convertView
        }

        override fun getItem(position: Int): Any = repeatTypes[position]

        override fun getCount(): Int = repeatTypes.size

        override fun getItemId(position: Int): Long = position.toLong()

        class ViewHolder {
            var text: TextView? = null
        }
    }


}
