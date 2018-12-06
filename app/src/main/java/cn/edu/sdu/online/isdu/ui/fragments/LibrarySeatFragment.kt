package cn.edu.sdu.online.isdu.ui.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.bean.LibrarySeat
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.button.LibraryRadioImageButton
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

/**
 ****************************************************
 * @author Cola_Mentos
 * Last Modifier: Cola_Mentos
 * Last Modify Time: 2018/7/16
 *
 * 图书馆余座查询碎片
 ****************************************************
 */

class LibrarySeatFragment : Fragment() {

    private var radioButtons = ArrayList<LibraryRadioImageButton>()
    private var recyclerView: RecyclerView ?= null
    private var adapter: LibrarySeatFragment.MyAdapter? = null
    private val dataList: MutableList<LibrarySeat> = java.util.ArrayList()
    private val libraries = listOf("zxwl", "zxjz", "hjl", "btq", "qfsgx", "xls")
    private var alertDialog : AlertDialog ?= null


    private var dialog: ProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_library_seat, container, false)
        initView(view)
        initRecyclerView()
        return view
    }

    private fun initView(view : View){

        recyclerView = view.findViewById(R.id.recycler_view)
        radioButtons.add(view.findViewById(R.id.radio_zhongxin))
        radioButtons.add(view.findViewById(R.id.radio_jiangzhen))
        radioButtons.add(view.findViewById(R.id.radio_honglou))
        radioButtons.add(view.findViewById(R.id.radio_baotuq))
        radioButtons.add(view.findViewById(R.id.radio_gongxue))
        radioButtons.add(view.findViewById(R.id.radio_xinglong))

        for (i in 0..5) {
            radioButtons[i].setOnClickListener {
                if (radioButtons[i].isSelected) {
                    cancelAllClick()
                    dataList.clear()
                    adapter?.notifyDataSetChanged()
                } else {
                    cancelAllClick()
                    radioButtons[i].isSelected = true

                    getData(i)
                }
            }
        }

    }

    private fun cancelAllClick() {
        for (i in 0..5)
            radioButtons[i].isSelected = false
    }

    private fun getData(index: Int) {
        dialog = ProgressDialog(context, false)
        dialog!!.setCancelable(false)
        dialog!!.setMessage("正在查询")
        dialog!!.show()

        NetworkAccess.buildRequest(ServerInfo.getLibrarySearUrl(libraries[index]),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Logger.log(e)

                        alertDialog = AlertDialog(context!!)
                        alertDialog!!.setTitle("抱歉")
                        alertDialog!!.setMessage("教务出现问题，该图书馆余座无法查询")
                        alertDialog!!.setPositiveButton("确定") {
                            activity?.runOnUiThread {
                                alertDialog!!.dismiss()
                            }
                        }
                        alertDialog!!.setCancelable(false)
                        alertDialog!!.setCancelOnTouchOutside(false)

                        activity?.runOnUiThread {
                            dialog?.dismiss()
                            alertDialog!!.show()
                            dataList.clear()
                            adapter?.notifyDataSetChanged()
                        }
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        activity?.runOnUiThread {

                            dialog?.dismiss()
                        }
                        try {


                            val jsonArray = JSONArray(response?.body()?.string())

                            if (response == null || response?.body()?.toString() == "" || jsonArray.toString() == "") {
                                alertDialog = AlertDialog(context!!)
                                alertDialog!!.setTitle("抱歉")
                                alertDialog!!.setMessage("教务出现问题，该图书馆余座无法查询")
                                alertDialog!!.setPositiveButton("确定") {
                                    activity?.runOnUiThread {
                                        alertDialog!!.dismiss()
                                    }
                                }
                                alertDialog!!.setCancelable(false)
                                alertDialog!!.setCancelOnTouchOutside(false)
                                activity?.runOnUiThread {
                                    alertDialog!!.show()
                                }

                            }

                            dataList.clear()

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)
                                val seat = LibrarySeat()
                                seat.free = obj.getInt("free")
                                seat.used = obj.getInt("used")
                                seat.room = obj.getString("room")

                                dataList.add(seat)
                            }

                            activity?.runOnUiThread {
                                adapter?.notifyDataSetChanged()
                            }
                        } catch (e: Exception) {
                            Logger.log(e)
                            activity?.runOnUiThread {
                                dataList.clear()
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                })
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = MyAdapter(dataList, context!!)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }

    /**
     * recyclerView容器类
     */
    internal inner class MyAdapter(dataList: List<LibrarySeat>?, context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private val context = context
        private val dataList = dataList

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if (position == 0){
                holder.line.visibility = View.GONE
            } else holder.line.visibility = View.VISIBLE

            val item = dataList!![position]

            holder.room.text = item.room
            holder.used.text = item.used.toString()
            holder.free.text = item.free.toString()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.seat_item, parent, false)
            return ViewHolder(v = view)
        }

        override fun getItemCount(): Int = dataList?.size ?: 0

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var line: View = v.findViewById(R.id.line)
            var room: TextView = v.findViewById(R.id.place)
            var used: TextView = v.findViewById(R.id.occupied_seats)
            var free: TextView = v.findViewById(R.id.remain_seats)
        }
    }

}
