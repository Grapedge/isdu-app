package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.StudyRoom
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.Logger
import kotlinx.android.synthetic.main.activity_study_room_table.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

class StudyRoomTableActivity : SlideActivity() {

    private val mDataList: MutableList<StudyRoom> = ArrayList()
    private var recyclerView: RecyclerView? = null
    private var adapter: MyAdapter? = null
    private var dialog: ProgressDialog? = null
    private var txtTitle: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_room_table)

        dialog = ProgressDialog(this, false)
        dialog!!.setMessage("正在加载")
        dialog!!.setButton(null, null)
        dialog!!.setCancelable(false)
        dialog!!.show()

        val campus = intent.getStringExtra("campus")
        val building = intent.getStringExtra("building")
        val date = intent.getStringExtra("date")

        recyclerView = recycler_view
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(mDataList)
        recyclerView!!.adapter = adapter

        txtTitle = txt_title
        txtTitle!!.text = building

        btn_back.setOnClickListener {
            if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
            finish()
        }

        getData(campus, building, date)
    }

    private fun getData(campus: String, building: String, date: String) {
        NetworkAccess.buildRequest(ServerInfo.getStudyRooms(campus, building, date),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        mDataList.clear()
                        Logger.log(e)
                        runOnUiThread {
                            dialog?.dismiss()
                            adapter?.notifyDataSetChanged()
                            val dialog = AlertDialog(this@StudyRoomTableActivity)
                            dialog.setTitle("错误")
                            dialog.setMessage("未获取到数据")
                            dialog.setPositiveButton("取消") {
                                dialog.dismiss()
                            }
                            dialog.show()
                        }
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        try {
                            val jsonString = response?.body()?.string()
                            val jsonArray = JSONArray(jsonString)

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val studyRoom = StudyRoom()
                                studyRoom.building = obj.getString("building")
                                studyRoom.classroom = obj.getString("classroom")

                                val statusArray = obj.getJSONObject("status")
                                for (j in 0 until 6) {
                                    val status = statusArray.getString((j + 1).toString())
                                    studyRoom.status[j] = if (status == "空闲")
                                        0 else 1
                                }
                                mDataList.add(studyRoom)
                            }

                            runOnUiThread {
                                dialog?.dismiss()
                                adapter?.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            mDataList.clear()
                            Logger.log(e)
                            runOnUiThread {
                                dialog?.dismiss()
                                adapter?.notifyDataSetChanged()
                                val dialog = AlertDialog(this@StudyRoomTableActivity)
                                dialog.setTitle("错误")
                                dialog.setMessage("未获取到数据")
                                dialog.setPositiveButton("取消") {
                                    dialog.dismiss()
                                }
                                dialog.show()
                            }
                        }
                    }
                })
    }

    inner class MyAdapter(dataList: List<StudyRoom>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private var mDataList = dataList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.study_room_result_item,
                    parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = mDataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = mDataList[position]

            holder.txtClassroom.text = item.classroom
            setTextView(holder.s1, item.status[0])
            setTextView(holder.s2, item.status[1])
            setTextView(holder.s3, item.status[2])
            setTextView(holder.s4, item.status[3])
            setTextView(holder.s5, item.status[4])
            setTextView(holder.s6, item.status[5])

            if (position % 2 == 1) {
                holder.txtClassroom.setBackgroundColor(0xFFECECEC.toInt())
                holder.s1.setBackgroundColor(0xFFECECEC.toInt())
                holder.s2.setBackgroundColor(0xFFECECEC.toInt())
                holder.s3.setBackgroundColor(0xFFECECEC.toInt())
                holder.s4.setBackgroundColor(0xFFECECEC.toInt())
                holder.s5.setBackgroundColor(0xFFECECEC.toInt())
                holder.s6.setBackgroundColor(0xFFECECEC.toInt())
            } else {
                holder.txtClassroom.setBackgroundColor(0xFFFFFFFF.toInt())
                holder.s1.setBackgroundColor(0xFFFFFFFF.toInt())
                holder.s2.setBackgroundColor(0xFFFFFFFF.toInt())
                holder.s3.setBackgroundColor(0xFFFFFFFF.toInt())
                holder.s4.setBackgroundColor(0xFFFFFFFF.toInt())
                holder.s5.setBackgroundColor(0xFFFFFFFF.toInt())
                holder.s6.setBackgroundColor(0xFFFFFFFF.toInt())
            }
        }

        private fun setTextView(tv: TextView, status: Int) {
            if (status == 0) {
                tv.setTextColor(0xFF717DEB.toInt())
                tv.text = "闲"
            } else {
                tv.setTextColor(0xFFE83747.toInt())
                tv.text = "占"
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var txtClassroom: TextView = view.findViewById(R.id.classroom)
            var s1: TextView = view.findViewById(R.id.status_1)
            var s2: TextView = view.findViewById(R.id.status_2)
            var s3: TextView = view.findViewById(R.id.status_3)
            var s4: TextView = view.findViewById(R.id.status_4)
            var s5: TextView = view.findViewById(R.id.status_5)
            var s6: TextView = view.findViewById(R.id.status_6)
        }
    }
}
