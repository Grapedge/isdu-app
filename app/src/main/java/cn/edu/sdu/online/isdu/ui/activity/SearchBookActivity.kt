package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.AlphaActivity
import cn.edu.sdu.online.isdu.bean.Book
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.util.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class SearchBookActivity : AlphaActivity(), View.OnClickListener {

    //private var noBook: TextView? = null
    //private var netWorkError: TextView? = null
    private var loading_layout:TextView? = null
    private var blank_view:TextView? = null
    private var btnBack: View? = null
    private var editSearch: EditText? = null
    private var btnSearch: TextView? = null
    private var bookName: String? =null
    private var error:TextView? = null
    private val dataList: MutableList<Book> = java.util.ArrayList()

    private var recyclerView: RecyclerView? = null
    private var adapter: MyAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_book)

        initView()

        initRecyclerView()
    }




    private fun initView() {
        //noBook = findViewById(R.id.nobook)
        //netWorkError = findViewById(R.id.network_error)
        btnBack = findViewById(R.id.btn_back)
        editSearch = findViewById(R.id.edit_search)
        btnSearch = findViewById(R.id.btn_search)
        loading_layout = findViewById(R.id.loading_layout)
        blank_view = findViewById(R.id.blank_view)
        recyclerView = findViewById(R.id.recycler_view)
        error= findViewById(R.id.error)

        btnBack!!.setOnClickListener(this)
        btnSearch!!.setOnClickListener(this)

        editSearch!!.setOnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                btnSearch!!.callOnClick()
                true
            }
            false
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_search -> {
                if(editSearch!!.text.toString()!=""){
                    bookName=editSearch!!.text.toString()
                    Log.w("sba",bookName)
                    dataList.clear()
                    recyclerView!!.visibility = View.VISIBLE
                    loading_layout?.visibility = View.GONE
                    blank_view?.visibility = View.GONE
                    error!!.visibility = View.GONE
                    if(adapter!=null){
                        Log.w("sba","notifyDataSetChanged")
                        adapter!!.notifyDataSetChanged()
                    }
                    initData()
                }
            }
        }
    }
    private fun initData() {
        Log.w("sba","initData")
        recyclerView!!.visibility = View.GONE
        loading_layout?.visibility = View.VISIBLE
        blank_view?.visibility = View.GONE
        error!!.visibility = View.GONE
        var url = ServerInfo.searchBookUrl(bookName)
        NetworkAccess.buildRequest(url,object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Logger.log(e)
                runOnUiThread{
                    Toast.makeText(this@SearchBookActivity, "网络错误", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                val json=JSONObject(response?.body()?.string())
                Log.w("sba",json.toString())
                try{
                    if(json.getInt("code")==0){
                        val jsonArray = json.getJSONArray("obj")
                        for(i in 0 until jsonArray.length()){
                            val obj = jsonArray.getJSONObject(i)
                            val book = Book()
                            book.bookName = obj.getString("name")
                            book.idNumber = obj.getString("code")
                            book.author = obj.getString("author")
                            book.press = obj.getString("press")
                            book.all = obj.getInt("all")
                            book.canBor = obj.getInt("canBor")
                            val arr=obj.getJSONArray("books")
                            val books = ArrayList<String>()
                            for(j in 0 until arr.length()){
                                books.add(arr.get(j).toString())
                            }
                            book.setBorPlace(books)
                            dataList.add(book)
                            runOnUiThread {
                                recyclerView!!.visibility = View.VISIBLE
                                loading_layout?.visibility = View.GONE
                                blank_view?.visibility = View.GONE
                                error!!.visibility = View.GONE
                                if(adapter!=null){
                                    Log.w("sba","notifyDataSetChanged")
                                    adapter!!.notifyDataSetChanged()
                                }
                            }
                            Log.w("sba",dataList.size.toString())
                        }
                    }else if(json.getInt("code")==1){
                        runOnUiThread {
                            recyclerView!!.visibility = View.GONE
                            loading_layout!!.visibility = View.GONE
                            blank_view!!.visibility = View.VISIBLE
                            error!!.visibility = View.GONE
                        }
                    }else{
                        runOnUiThread {
                            error!!.text = json.getString("status")
                            recyclerView!!.visibility = View.GONE
                            loading_layout!!.visibility = View.GONE
                            blank_view!!.visibility = View.GONE
                            error!!.visibility = View.VISIBLE
                        }
                    }
                }catch (e: Exception){
                    Logger.log(e)
                }
            }
        })
    }

    private fun initRecyclerView() {
        adapter = MyAdapter(dataList)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = adapter
    }
    internal inner class MyAdapter(dataList: List<Book>?) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private val mdataList = dataList

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val book = mdataList!![position]
            holder.bookName.text = book.bookName
            holder.idNumber.text = book.idNumber
            if(book.getBorPlace().isEmpty()){
                holder.bookPlace.text="书刊没有复本\n" +
                        "可能正在订购中或者处理中"
            }else{
                holder.bookPlace.text = book.getBorPlace()
            }
            holder.press.text = book.press
            holder.all.text = book.all.toString()
            holder.canbor.text = book.canBor.toString()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.item_search_book, parent, false)
            return ViewHolder(v = view)
        }

        override fun getItemCount(): Int = dataList.size

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val bookName : TextView = v.findViewById(R.id.book_name) // 借阅书名
            val idNumber : TextView = v.findViewById(R.id.id_number) // 借阅号
            val bookPlace : TextView = v.findViewById(R.id.book_place) // 借阅地点
            val press : TextView = v.findViewById(R.id.press)
            val all: TextView = v.findViewById(R.id.all)
            val canbor: TextView = v.findViewById(R.id.canbor)
            val line : View = v.findViewById(R.id.separate_line) // 分割线
        }
    }
}