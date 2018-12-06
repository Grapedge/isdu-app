package cn.edu.sdu.online.isdu.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.bean.Book
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.BindLibraryActivity
import cn.edu.sdu.online.isdu.ui.activity.LoginActivity
import cn.edu.sdu.online.isdu.ui.activity.SearchBookActivity
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.Logger
import kotlinx.android.synthetic.main.fragment_my_book.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 ****************************************************
 * @author Cola_Mentos
 * Last Modifier: Cola_Mentos
 * Last Modify Time: 2018/7/16
 *
 * 图书馆馆藏查询碎片
 ****************************************************
 */

class MyBookFragment : Fragment(), View.OnClickListener {

    private var noBook: TextView? = null
    private var netWorkError: TextView? = null
    private var recyclerView: RecyclerView?= null
    private var adapter: MyAdapter? = null
    private val dataList: MutableList<Book> = ArrayList()
    private var searchBar : LinearLayout?= null
    private var progressDialog : ProgressDialog ?=  null
    private var rebAll: TextView? = null
    private var unBind: TextView? = null
    private var loadingLayout: TextView? = null
    private var yf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private var firstLoad = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_book, container, false)
        initView(view)
        recyclerView!!.visibility = View.GONE
        noBook!!.visibility = View.GONE
        netWorkError!!.visibility = View.GONE
        unBind!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE

        initRecyclerView()
        return (view)
    }

    private fun initView(view : View){
        noBook = view.findViewById(R.id.nobook)
        netWorkError = view.findViewById(R.id.network_error)
        recyclerView = view.findViewById(R.id.recycler_view)
        searchBar = view.findViewById(R.id.search_bar)
        rebAll = view.findViewById(R.id.reb_all)
        unBind = view.findViewById(R.id.unbind)
        loadingLayout = view.findViewById(R.id.loading_layout)

        searchBar!!.setOnClickListener(this)
        rebAll!!.setOnClickListener(this)
        unBind!!.setOnClickListener(this)
    }

    private fun checkState(){
        if (!User.isLogin()) {
            login()
        } else {
            checkIsBind()
        }
    }

    private fun login(){
        val dialog = AlertDialog(activity!!)
        dialog.setTitle("无数据")
        dialog.setMessage("请登录后重试")
        dialog.setCancelOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setPositiveButton("登录") {
            dialog.dismiss()
            startActivity(Intent(activity, LoginActivity::class.java))
        }
        dialog.setNegativeButton("返回") {
            dialog.dismiss()
            recyclerView!!.visibility = View.GONE
            noBook!!.visibility = View.GONE
            netWorkError!!.visibility = View.GONE
            unBind!!.visibility = View.VISIBLE
            loadingLayout!!.visibility = View.GONE
        }
        dialog.show()
    }

    private fun checkIsBind(){
        NetworkAccess.buildRequest(ServerInfo.getLibraryInfoUrl(User.staticUser.uid.toString()),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?){
                        Logger.log(e)
                        activity!!.runOnUiThread {
                            recyclerView!!.visibility = View.GONE
                            noBook!!.visibility = View.GONE
                            netWorkError!!.visibility = View.VISIBLE
                            unBind!!.visibility = View.GONE
                            loadingLayout!!.visibility = View.GONE
                        }
                    }
                    override fun onResponse(call: Call?, response: Response?){
                        try{
                            val json =JSONObject(response?.body()?.string())
                            if (json.getInt("code") == -1) {
                                //未绑定
                                User.staticUser.bind = false
                                activity!!.runOnUiThread {
                                    bind()
                                }
                                activity!!.runOnUiThread {
                                    recyclerView!!.visibility = View.GONE
                                    noBook!!.visibility = View.GONE
                                    netWorkError!!.visibility = View.GONE
                                    unBind!!.visibility = View.VISIBLE
                                    loadingLayout!!.visibility = View.GONE
                                }
                            }else{
                                User.staticUser.bind = true
                                initData()
                            }
                        }catch (e: Exception){
                            Logger.log(e)
                        }
                    }
                })
    }

    private fun bind(){
        val dialog = AlertDialog(activity!!)
        dialog.setTitle("无数据")
        dialog.setMessage("绑定用户图书馆系统后重试")
        dialog.setCancelOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.setPositiveButton("绑定") {
            startActivity(Intent(activity, BindLibraryActivity::class.java))
            dialog.dismiss()
        }
        dialog.setNegativeButton("返回") {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        if(firstLoad){
            firstLoad = false
            checkState()
        }else{
            if (User.isLogin()) {
                if (User.staticUser.bind == true){
                    initData()
                } else {
                    recyclerView!!.visibility = View.GONE
                    noBook!!.visibility = View.GONE
                    netWorkError!!.visibility = View.GONE
                    unBind!!.visibility = View.VISIBLE
                    rebAll!!.visibility = View.VISIBLE
                    loadingLayout!!.visibility = View.GONE
                }
            } else {
                recyclerView!!.visibility = View.GONE
                noBook!!.visibility = View.GONE
                netWorkError!!.visibility = View.GONE
                unBind!!.visibility = View.VISIBLE
                rebAll!!.visibility = View.GONE
                loadingLayout!!.visibility = View.GONE
            }
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            search_bar.id->{
                startActivity(Intent(activity!!,SearchBookActivity::class.java))
            }
            reb_all.id -> {
                if (noBook!!.visibility == View.VISIBLE) {
                    Toast.makeText(context, "您还没有借阅任何图书", Toast.LENGTH_SHORT).show()
                    return
                }

                NetworkAccess.buildRequest(ServerInfo.renewBookUrl(User.staticUser.uid),
                        object : Callback{
                            override fun onFailure(call: Call?, e: IOException?) {
                                Logger.log(e)
                                activity!!.runOnUiThread{
                                    Toast.makeText(activity,"网络连接失败",Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onResponse(call: Call?, response: Response?) {
                                try{
                                    val json = JSONObject(response?.body()?.string())
                                    if(json.getInt("code")==0){
                                        activity!!.runOnUiThread {
                                            Toast.makeText(activity,"续借成功",Toast.LENGTH_LONG).show()
                                        }
                                    }else{
                                            activity!!.runOnUiThread {
                                                Toast.makeText(activity, "服务器发生错误", Toast.LENGTH_LONG).show()
                                            }
                                    }

                                }catch (e : Exception){
                                    Logger.log(e)
                                }
                            }
                        })
            }
            unbind.id -> {
                checkState()
            }

        }
    }

    private fun initData(){
        dataList.clear()
        activity!!.runOnUiThread {
            adapter!!.notifyDataSetChanged()
            recyclerView!!.visibility = View.GONE
            netWorkError!!.visibility = View.GONE
            noBook!!.visibility = View.GONE
            unBind!!.visibility = View.GONE
            loadingLayout!!.visibility = View.VISIBLE
        }
        Log.w("mbf", ServerInfo.getBookListUrl(User.staticUser.uid.toString()))
        NetworkAccess.buildRequest(ServerInfo.getBookListUrl(User.staticUser.uid.toString()),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?){
                        Logger.log(e)
                        activity!!.runOnUiThread {
                            recyclerView!!.visibility = View.GONE
                            netWorkError!!.visibility = View.VISIBLE
                            noBook!!.visibility = View.GONE
                            unBind!!.visibility = View.GONE
                            loadingLayout!!.visibility = View.GONE
                        }
                    }
                    override fun onResponse(call: Call?, response: Response?){
                        try{
                            dataList.clear()
                            val json =JSONObject(response?.body()?.string())
                            if(json.getInt("code")==1){
                                //没借书
                                activity!!.runOnUiThread{
                                    recyclerView!!.visibility = View.GONE
                                    netWorkError!!.visibility = View.GONE
                                    unBind!!.visibility = View.GONE
                                    noBook!!.visibility = View.VISIBLE
                                    loadingLayout!!.visibility = View.GONE
                                }
                            }else if(json.getInt("code")==2){
                                activity!!.runOnUiThread{
                                    val dialog = AlertDialog(activity as Context)
                                    dialog.setTitle("无数据")
                                    dialog.setMessage("绑定用户图书馆系统后重试")
                                    dialog.setCancelOnTouchOutside(false)
                                    dialog.setCancelable(false)
                                    dialog.setPositiveButton("绑定") {
                                        dialog.dismiss()
                                        startActivity(Intent(activity, BindLibraryActivity::class.java))

                                    }
                                    dialog.setNegativeButton("返回") {
                                        dialog.dismiss()
                                    }
                                    dialog.show()
                                }
                            }else{
                                val jsonArray = json.getJSONArray("obj")
                                for (i in 0 until jsonArray.length()){
                                    val obj = jsonArray.getJSONObject(i)
                                    val book = Book()
                                    book.bookName = obj.getString("name")
                                    Log.w("borDate",obj.getString("borDate"))
                                    Log.w("retDate",obj.getString("retDate"))
                                    book.borrowDate = yf.format(obj.getString("borDate").toLong())
                                    book.backDate = yf.format(obj.getString("retDate").toLong())
                                    book.remainDays = obj.getString("restDay").toInt()
                                    book.borrowTimes = obj.getInt("rebNumber")
                                    book.bookPlace = obj.getString("borPlace")
                                    book.id = obj.getString("id")
                                    book.checkCode = obj.getString("checkCode")
                                    dataList.add(book)
                                }
                                activity!!.runOnUiThread{
                                    recyclerView!!.visibility = View.VISIBLE
                                    netWorkError!!.visibility = View.GONE
                                    noBook!!.visibility = View.GONE
                                    unBind!!.visibility = View.GONE
                                    loadingLayout!!.visibility = View.GONE
                                    adapter!!.notifyDataSetChanged()
                                }
                            }
                        }catch (e: Exception){
                            Logger.log(e)
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
        progressDialog = ProgressDialog(context,false)
        //progressDialog!!.setMessage("正在加载中...")
        //progressDialog!!.setButton(null,null)
        //progressDialog!!.show()
    }

    /**
     * recyclerView容器类
     */
    internal inner class MyAdapter(dataList: List<Book>, private val context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private val dataList = dataList

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if(position==0){
                holder.line.visibility = View.GONE
            }
            val book = dataList[position]
            holder.bookName.text = book.bookName
            holder.idNumber.text = book.id
            holder.bookPlace.text = book.bookPlace
            holder.borrowDate.text = book.borrowDate
            holder.backDate.text = book.backDate
            holder.remainDays.text = book.remainDays.toString()
            holder.borrowTimes.text = book.borrowTimes.toString()
//            if(book.borrowTimes == 0){
//                holder.renew.visibility = View.GONE
//            } else {
//                holder.renew.visibility = View.VISIBLE
//            }
            holder.renew.setOnClickListener {
                NetworkAccess.buildRequest(ServerInfo.renewOneBookUrl(User.staticUser.uid , book.id , book.checkCode),
                        object : Callback{
                            override fun onFailure(call: Call?, e: IOException?) {
                                Logger.log(e)
                                activity!!.runOnUiThread{
                                    Toast.makeText(activity,"网络连接失败",Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onResponse(call: Call?, response: Response?) {
                                try {
                                    val json = JSONObject(response?.body()?.string())
                                    if (json.getInt("code") == -1) {
                                        activity!!.runOnUiThread {
                                            Toast.makeText(activity, "服务器发生错误\n" +
                                                    "续借失败", Toast.LENGTH_LONG).show()
                                        }
                                    } else if(json.getInt("code") == 0) {
                                        activity!!.runOnUiThread {
                                            Toast.makeText(activity, "续借成功", Toast.LENGTH_LONG).show()
                                        }
                                        initData()
                                    }
                                    else{
                                        activity!!.runOnUiThread{
                                            Toast.makeText(activity,json.getString("status"),Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }catch (e : Exception){
                                    Logger.log(e)
                                }
                            }
                        })
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.book_item, parent, false)
            return ViewHolder(v = view)
        }

        override fun getItemCount(): Int=dataList.size

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val bookName : TextView = v.findViewById(R.id.book_name) // 借阅书名
            val idNumber : TextView = v.findViewById(R.id.id_number) // 借阅号
            val bookPlace : TextView = v.findViewById(R.id.book_place) // 借阅地点
            val borrowDate : TextView = v.findViewById(R.id.borrow_date) //借阅日期
            val backDate : TextView = v.findViewById(R.id.back_date) // 应还日期
            val remainDays : TextView = v.findViewById(R.id.remain_days) // 剩余天数
            val borrowTimes : TextView = v.findViewById(R.id.borrow_times) // 续借次数
            val line : View = v.findViewById(R.id.separate_line) // 分割线
            val renew : TextView = v.findViewById(R.id.renew) //续借
        }
    }
}
