package cn.edu.sdu.online.isdu.ui.activity

import android.content.Context
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
import cn.edu.sdu.online.isdu.bean.Bus
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.Logger
import kotlinx.android.synthetic.main.activity_school_bus_table.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException
import java.util.*

/**
 ****************************************************
 * @author Cola_Mentos
 * Last Modifier: Cola_Mentos
 * Last Modify Time: 2018/7/9
 *
 * 校车时间表活动
 ****************************************************
 */

class SchoolBusTableActivity : SlideActivity() ,View.OnClickListener{

    private var searchNum : Int ?= null
    private var fromP : Int ?= null
    private var toP : Int ?= null
    private var backBtn : ImageView ?= null
    private var adapter: SchoolBusTableActivity.MyAdapter? = null
    private var recyclerView: RecyclerView ?= null
    private val dataList: MutableList<Bus> = ArrayList()
    private var textView : TextView ?= null

    private var dialog: ProgressDialog? = null

    private val xqName = arrayOf("","中心校区","洪家楼校区","趵突泉校区","软件园校区","兴隆山校区","千佛山校区")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_bus_table)

        initView()

        getData()
        initRecyclerView()

    }

    /**
     * 初始化view
     */
    private fun initView(){
        val intent = intent
        searchNum = intent.getIntExtra("searchNum",0)
        fromP = intent.getIntExtra("fromP",0)
        toP = intent.getIntExtra("toP",0)

        backBtn = btn_back
        recyclerView = recycler_view
        textView = tips

        backBtn!!.setOnClickListener(this)
    }

    private fun getData() {
        dialog = ProgressDialog(this, false)
        dialog!!.setMessage("正在加载")
        dialog!!.setButton(null, null)
        dialog!!.setCancelable(false)
        dialog!!.show()
        NetworkAccess.buildRequest(ServerInfo.getSchoolBusUrl(xqName[fromP!!], xqName[toP!!], searchNum!!),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        dialog?.dismiss()
                        Logger.log(e)
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        dialog?.dismiss()
                        try {
                            val jsonString = response?.body()?.string()
                            if (jsonString == null || jsonString == "[]") {
                                runOnUiThread {

                                    textView!!.visibility = View.VISIBLE

                                    dataList.clear()
                                    adapter?.notifyDataSetChanged()
                                }
                                return
                            }
                            val jsonArray = JSONArray(jsonString)

                            dataList.clear()
                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)

                                val bus = Bus()
                                bus.busFrom = obj.getString("s")
                                bus.busTo = obj.getString("e")
                                bus.busTime = obj.getString("t")
                                bus.busPass = obj.getString("p")
                                dataList.add(bus)
                            }

                            runOnUiThread {
                                adapter?.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            Logger.log(e)
                        }
                    }
                })
    }

    override fun onClick(v: View?) {
        when (v!!.id){
            btn_back.id->{
                finish()
            }
        }
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = MyAdapter(dataList, this)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter
    }

    /**
     * recyclerView容器类
     */
    internal inner class MyAdapter(dataList: List<Bus>?, context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private val context = context
        private val dataList = dataList

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (dataList?.size != 0){
                textView!!.visibility = View.GONE
            } else {
                textView!!.visibility = View.VISIBLE
            }

            val item = dataList!![position]

            holder.busTime.text = item.busTime
            holder.busFrom.text = item.busFrom
            holder.busTo.text = item.busTo
            holder.busPass.text = item.busPass

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val view =
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.bus_item, parent, false)
            return ViewHolder(v = view)
        }

        override fun getItemCount(): Int = dataList?.size ?: 0

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val busTime : TextView = v.findViewById(R.id.bus_time) // 发车时间
            val busFrom : TextView = v.findViewById(R.id.bus_from) // 起点
            val busPass : TextView = v.findViewById(R.id.bus_pass) // 途径
            val busTo : TextView = v.findViewById(R.id.bus_to) // 终点
        }
    }
}
