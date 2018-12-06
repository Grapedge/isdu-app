package cn.edu.sdu.online.isdu.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.ui.design.button.RadioImageButton
import cn.edu.sdu.online.isdu.ui.design.dialog.WeekDialog
import cn.edu.sdu.online.isdu.util.DateCalculate
import cn.edu.sdu.online.isdu.util.EnvVariables

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/5
 *
 * 自习室活动
 ****************************************************
 */

class StudyRoomActivity : SlideActivity(), View.OnClickListener {

    private var dayNames = listOf("一", "二", "三", "四", "五", "六", "日")

    private var campuses = listOf("中心校区", "洪家楼校区", "趵突泉校区", "软件园校区", "兴隆山校区", "千佛山校区")
    private val mDataList = ArrayList<String>()

    private var adapter: MyAdapter? = null

    private var radioButtons = ArrayList<RadioImageButton>()
    private var selectLayout: LinearLayout? = null
    private var selectWeek: TextView? = null
    private var selectDay: TextView? = null
    private var weeks = ArrayList<Int>()
    private var days = ArrayList<Int>()
    private var btnBack: ImageView? = null
    private var recyclerView: RecyclerView? = null

    private var currentCampus = -1
    private var date = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_room)

        initView()
        initSelection()
        updateData()

        updateRecyclerView()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
        }
    }

    private fun initView() {
        radioButtons.add(findViewById(R.id.radio_zhongxin))
        radioButtons.add(findViewById(R.id.radio_honglou))
        radioButtons.add(findViewById(R.id.radio_baotuq))
        radioButtons.add(findViewById(R.id.radio_ruanjian))
        radioButtons.add(findViewById(R.id.radio_xinglong))
        radioButtons.add(findViewById(R.id.radio_qianfo))
        selectLayout = findViewById(R.id.select_layout)
        selectWeek = findViewById(R.id.select_week)
        selectDay = findViewById(R.id.select_day)
        recyclerView = findViewById(R.id.recycler_view)
        btnBack = findViewById(R.id.btn_back)

        btnBack!!.setOnClickListener(this)
        for (i in 0 until radioButtons.size) {
            radioButtons[i].setOnClickListener {
                if (radioButtons[i].isSelected) {
                    cancelAllClick()
                    mDataList.clear()
                    adapter!!.notifyDataSetChanged()
                } else {
                    cancelAllClick()
                    radioButtons[i].isSelected = true

                    currentCampus = i
                    getData(campuses[i])
                }
            }
        }

        selectLayout!!.setOnClickListener {
            val dialog = WeekDialog(this, 1, 20)
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

            dialog.setSingleOption(true)
            dialog.setPositiveButton("确定") {
                weeks = dialog.weeks as ArrayList<Int>
                days = dialog.days as ArrayList<Int>
                updateData()
                dialog.dismiss()
            }
            dialog.setNegativeButton("取消") {dialog.dismiss()}
            dialog.show()
            dialog.show()
        }

        /************************************
         * 初始化RecyclerView的Adapter
         ************************************/
        adapter = MyAdapter(mDataList)
    }

    private fun initSelection() {
        weeks.clear()
        if (EnvVariables.currentWeek == -1)
            EnvVariables.currentWeek = EnvVariables.calculateWeekIndex(System.currentTimeMillis())
        weeks.add(EnvVariables.currentWeek)

        days.clear()
        days.add(EnvVariables.getCurrentDay())

        date = DateCalculate.Cal(EnvVariables.currentWeek.toLong(), EnvVariables.getCurrentDay().toLong())
    }

    private fun cancelAllClick() {
        for (i in 0 until 6)
            radioButtons[i].isSelected = false
        currentCampus = -1
    }

    private fun updateData() {
        selectWeek!!.text = weeks[0].toString()
        selectDay!!.text = dayNames[days[0] - 1]

        date = DateCalculate.Cal(weeks[0].toLong(), days[0].toLong())
        Log.d("AAA", date)
    }

    /**
     * 从服务器获取数据
     */
    private fun getData(campus: String) {
        mDataList.clear()
        when (campus) {
            campuses[0] -> {
                mDataList.add("中心公教楼")
                mDataList.add("中心文史楼")
                mDataList.add("中心理综楼")
                mDataList.add("中心电教北楼")
                mDataList.add("中心知新楼B座")
                mDataList.add("中心董明珠楼")
            }
            campuses[1] -> {
                mDataList.add("洪楼法学新楼")
                mDataList.add("洪楼3号楼")
                mDataList.add("洪楼6号楼")
                mDataList.add("洪楼公教楼")
                mDataList.add("洪楼物理楼")
            }
            campuses[2] -> {
                mDataList.add("趵突泉2号楼")
                mDataList.add("趵突泉8号楼")
                mDataList.add("趵突泉9号楼")
                mDataList.add("趵突泉图东")
                mDataList.add("趵突泉图西")
            }
            campuses[3] -> {
                mDataList.add("软件园1区")
                mDataList.add("软件园4区")
                mDataList.add("软件园5区")
            }
            campuses[4] -> {
                mDataList.add("兴隆山群楼A座")
                mDataList.add("兴隆山群楼B座")
                mDataList.add("兴隆山群楼C座")
                mDataList.add("兴隆山群楼D座")
                mDataList.add("兴隆山群楼E座")
                mDataList.add("兴隆山讲学堂")
            }
            campuses[5] -> {
                mDataList.add("千佛山1号楼")
                mDataList.add("千佛山5号楼")
                mDataList.add("千佛山9号楼")
                mDataList.add("千佛山10号楼")
            }
        }
        adapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        EnvVariables.init(this)
    }


    private fun updateRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter
    }

    inner class MyAdapter(list: List<String>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private var list = list

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.study_room_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = list[position]
            holder.layout.setOnClickListener {
                startActivity(Intent(this@StudyRoomActivity, StudyRoomTableActivity::class.java)
                        .putExtra("building", mDataList[position])
                        .putExtra("campus", campuses[currentCampus])
                        .putExtra("date", date))
            }
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var textView: TextView = view.findViewById(R.id.study_room_name)
            var layout: View = view.findViewById(R.id.layout)
        }
    }
}
