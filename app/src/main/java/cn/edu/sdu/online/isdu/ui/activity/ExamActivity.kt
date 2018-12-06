package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.Exam
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import kotlinx.android.synthetic.main.activity_exam.*
import org.json.JSONObject

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/8
 *
 * 考试安排活动
 ****************************************************
 */

class ExamActivity : SlideActivity(), View.OnClickListener {

    private var dataList = arrayListOf<Exam>()
    private var mAdapter: MyAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var btnBack: ImageView? = null

    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)

        initView()
        initRecyclerView()

        getData()
    }

    private fun initView() {
        recyclerView = recycler_view
        btnBack = btn_back

        btnBack!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
        }
    }

    private fun initRecyclerView() {
        mAdapter = MyAdapter(dataList)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = mAdapter
    }

    private fun getData() {
        dialog = ProgressDialog(this, false)
        dialog!!.setMessage("正在加载")
        dialog!!.setButton(null, null)
        dialog!!.setCancelable(false)
        dialog!!.show()

        if (User.staticUser == null) User.staticUser = User.load()
        NetworkAccess.cache(ServerInfo.getExamUrl(User.staticUser.uid)) { success, cachePath ->
            dialog?.dismiss()
            if (success) {
                val jsonString = FileUtil.getStringFromFile(cachePath)
                if (jsonString != null && jsonString.trim() != "") {
                    try {
                        val jsonObject = JSONObject(jsonString)

                        if (jsonObject.getString("status") == "success") {

                            dataList.clear()

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
                                    dataList.add(Exam(
                                            exam.getString("examDate"),
                                            exam.getString("examTime"),
                                            exam.getString("examRoom"),
                                            exam.getString("resultComposition"),
                                            exam.getString("examMethod"),
                                            exam.getString("courseName")
                                    ))
                                }

                            }
//                            }

                            runOnUiThread {
                                mAdapter!!.notifyDataSetChanged()
                            }

                        } else {
                            val dialog = AlertDialog(this@ExamActivity)
                            dialog.setTitle("无信息")
                            dialog.setMessage("没有查询到考试安排\n╮(╯▽╰)╭")
                            dialog.setPositiveButton("返回") {
                                dialog.dismiss()
                                finish()
                            }
                            dialog.setNegativeButton(null, null)
                            dialog.setCancelable(false)
                            dialog.setCancelOnTouchOutside(false)
                            dialog.show()
                            throw Exception("No valid data is received in ExamActivity!")
                        }

                    } catch (e: Exception) {
                        Logger.log(e)
                    }
                } else {

                }
            }
        }
    }

    class MyAdapter(private val dataList: List<Exam>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.exam_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = dataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val exam = dataList[position]
            holder.examName.text = exam.name
            holder.examType.text = exam.type
            holder.examDate.text = exam.date
            holder.examTime.text = exam.time
            holder.examLocation.text = exam.location
            holder.examRate.text = exam.gradeRate
            holder.blankView.visibility = if (position == 0) View.VISIBLE else View.GONE
        }

        class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var examName: TextView = v.findViewById(R.id.exam_name)
            var examDate: TextView = v.findViewById(R.id.exam_date)
            var examTime: TextView = v.findViewById(R.id.exam_time)
            var examType: TextView = v.findViewById(R.id.exam_type)
            var examLocation: TextView = v.findViewById(R.id.exam_location)
            var examRate: TextView = v.findViewById(R.id.exam_rate)
            var blankView: View = v.findViewById(R.id.blank_view)
        }
    }
}
