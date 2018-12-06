//package cn.edu.sdu.online.isdu.ui.fragments.search
//
//import android.content.Intent
//import android.os.Bundle
//import android.support.v7.widget.LinearLayoutManager
//import android.support.v7.widget.RecyclerView
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.FrameLayout
//import android.widget.TextView
//import android.widget.Toast
//import cn.edu.sdu.online.isdu.R
//import cn.edu.sdu.online.isdu.app.LazyLoadFragment
//import cn.edu.sdu.online.isdu.bean.Post
//import cn.edu.sdu.online.isdu.net.pack.ServerInfo
//import cn.edu.sdu.online.isdu.net.NetworkAccess
//import cn.edu.sdu.online.isdu.ui.activity.PostDetailActivity
//import cn.edu.sdu.online.isdu.util.Logger
//import okhttp3.Call
//import okhttp3.Callback
//import okhttp3.Response
//import org.json.JSONArray
//import org.json.JSONObject
//import java.io.IOException
//import java.text.SimpleDateFormat
//
///**
// ****************************************************
// * @author zsj
// * Last Modifier: ZSJ
// * Last Modify Time: 2018/7/17
// *
// * 搜索帖子的Fragment
// ****************************************************
// */
//
//class SearchPostFragment : LazyLoadFragment() {
//
//    private var dataList: MutableList<Post> = ArrayList()
//    private var adapter: MyAdapter? = null
//    private var loadingLayout: View? = null
//    private var recyclerView: RecyclerView? = null
//    private var blankView: TextView? = null
//    private var search : String? = null
//    private var isLoadComplete = false
//    private var isLoading = false
//
//    private var lastSearchString = ""
//    private var searchCall: Call? = null
//    //private var isLoadComplete = false
//
//
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        val view = inflater.inflate(R.layout.fragment_search_post, container, false)
//        initView(view)
//        initRecyclerView()
//        return view
//    }
//
//    private fun initView(view: View) {
//        loadingLayout = view.findViewById(R.id.loading_layout)
//        recyclerView = view.findViewById(R.id.recycler_view)
//        blankView = view.findViewById(R.id.blank_view)
//    }
//
//    override fun isLoadComplete(): Boolean = isLoadComplete && lastSearchString == search
//
//    fun setSearch(search: String?){
//        this.search = search
//        if(userVisibleHint && (isLoadComplete || search != lastSearchString)){
//            if (searchCall != null && !searchCall!!.isCanceled) searchCall!!.cancel()
//            isLoadComplete = false
//
//            lastSearchString = search!!
//            loadData()
//        }
//    }
//    override fun loadData() {
//        super.loadData()
//        if(search != null){
//            isLoading = true
//            onLoading()
//            var url = ServerInfo.searchPost(search)
//            searchCall = NetworkAccess.buildRequest(url, object : Callback {
//                override fun onFailure(call: Call?, e: IOException?) {
//                    activity!!.runOnUiThread {
//                        Logger.log(e)
//                        Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                override fun onResponse(call: Call?, response: Response?) {
//                    val json = response?.body()?.string()
//                    try {
//                        dataList.clear()
//                        val jsonObject = JSONObject(json)
//                        if (jsonObject.getInt("code")==0) {
//                            val arr =JSONArray(jsonObject.getString("obj"))
//                            for (k in 0 until arr.length()) {
//                                val obj = arr.getJSONObject(k)
//                                val post = Post()
//
//                                post.postId = obj.getInt("id")
//                                post.commentsNumbers = obj.getInt("commentNumber")
//                                post.collectNumber = obj.getInt("collectNumber")
//                                post.likeNumber = obj.getInt("likeNumber")
//                                post.uid = obj.getString("uid")
//                                post.title = obj.getString("title")
//                                post.time = obj.getString("time").toLong()
//                                post.content = obj.getString("info")
//
//                                dataList.add(post)
//                            }
//                        }
//                    } catch (e: Exception) {
//                        Log.w("spf",e.toString())
//                        Logger.log(e)
//                        activity!!.runOnUiThread {
//                            Toast.makeText(context, "网络错误\n服务器无响应", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    activity!!.runOnUiThread {
//                        isLoadComplete = true
//                        publishData()
//                    }
//                }
//            })
//        }
//    }
//
//    fun onLoading(){
//        recyclerView!!.visibility = View.GONE
//        loadingLayout!!.visibility = View.VISIBLE
//        blankView!!.visibility = View.GONE
//    }
//
//    override fun publishData() {
//        super.publishData()
//        if(dataList.size != 0){
//            recyclerView!!.visibility = View.VISIBLE
//            loadingLayout!!.visibility = View.GONE
//            blankView!!.visibility = View.GONE
//        }else{
//            recyclerView!!.visibility = View.GONE
//            loadingLayout!!.visibility = View.GONE
//            blankView!!.visibility = View.VISIBLE
//        }
//        if(adapter != null){
//            adapter!!.notifyDataSetChanged()
//        }
//
//    }
//
//
//
//    private fun initRecyclerView() {
//        recyclerView!!.layoutManager = LinearLayoutManager(context)
//        adapter = MyAdapter()
//        recyclerView!!.adapter = adapter
//    }
//
//    inner class MyAdapter: RecyclerView.Adapter<MyAdapter.ViewHolder>() {
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//            val view = LayoutInflater.from(parent.context)
//                    .inflate(R.layout.post_item, parent, false)
//            val h = ViewHolder(view)
//            return h
//        }
//
//        override fun getItemCount(): Int = dataList.size
//
//        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//            val item = dataList[position]
//            holder.cardView.setOnClickListener {
//                context!!.startActivity(Intent(context, PostDetailActivity::class.java)
//                        .putExtra("id", item.postId)
//                        .putExtra("uid", item.uid)
//                        .putExtra("title", item.title)
//                        .putExtra("time", item.time))
//            }
//            holder.titleView.text = item.title
//            holder.commentNumber.text = item.commentsNumbers.toString()
//            holder.content.text = item.content
//            holder.txtLike.text = item.likeNumber.toString()
//            holder.releaseTime.text = if (System.currentTimeMillis() - item.time < 60 * 1000)
//                "刚刚" else (if (System.currentTimeMillis() - item.time < 24 * 60 * 60 * 1000)
//                "${(System.currentTimeMillis() - item.time) / (60 * 60 * 1000)} 小时前" else (
//                    if (System.currentTimeMillis() - item.time < 48 * 60 * 60 * 1000) "昨天 ${SimpleDateFormat("HH:mm").format(item.time)}"
//                    else SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.time)))
//        }
//
//        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(view) {
//            val cardView: FrameLayout = v.findViewById(R.id.card_view)
//            val titleView: TextView = v.findViewById(R.id.title_view) // 标题
//            val commentNumber: TextView = v.findViewById(R.id.comments_number) // 评论数
//            val releaseTime: TextView = v.findViewById(R.id.release_time) // 发布时间
//            val content: TextView = v.findViewById(R.id.content)
//            val txtLike: TextView = v.findViewById(R.id.like_count)
//        }
//
//    }
//
//}
//虽然不知道为什么，但是重构之后能用了
//庄大佬nb
//庄大佬nbbbb
//如果哪位好心人愿意告诉我为什么之前的不能用我会很感激的（
package cn.edu.sdu.online.isdu.ui.fragments.search

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.util.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SearchPostFragment : LazyLoadFragment(){
    private var recyclerView: RecyclerView? = null
    private var loadingLayout: View? = null
    private var blankView: View? = null
    private var dataList =ArrayList<Post>()
    private var search : String? =null
    private var isLoadComplete = false
    private var isLoading = false

    private var adapter: PostItemAdapter ?= null
    private var lastSearchString = ""
    private var searchCall: Call? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_post, container, false)
        initView(view)
        initRecyclerView()
        return view
    }

    fun initView(view : View){
        recyclerView = view.findViewById(R.id.recycler_view)
        loadingLayout = view.findViewById(R.id.loading_layout)
        blankView = view.findViewById(R.id.blank_view)
    }

    private fun initRecyclerView(){
        adapter = PostItemAdapter(activity!!, dataList)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }
    fun setSearch(search : String){
        this.search =search
        if(userVisibleHint&&(isLoadComplete || search != lastSearchString)){
            if(searchCall !=null&& !searchCall!!.isCanceled)
                searchCall!!.cancel()
            isLoadComplete = false
            lastSearchString = search
            loadData()
        }
    }

    override fun loadData() {
        super.loadData()
        isLoading = true
        if(search != null){
            onLoading()
            val url = ServerInfo.queryPost(search)
            searchCall = NetworkAccess.buildRequest(url,
                    object : Callback{
                        override fun onFailure(call: Call?, e: IOException?) {
                            activity!!.runOnUiThread{
                                Logger.log(e)
                                Toast.makeText(context,"网络错误",Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            val json = response?.body()?.string()
                            try {
                                dataList.clear()
                                if(json == "[]") {
                                }else{
                                    val jsonObject = JSONObject(json)
                                    if (jsonObject.getInt("code") == 0) {
                                        val arr = JSONArray(jsonObject.getString("obj"))
                                        for (k in 0 until arr.length()) {
                                            val obj = arr.getJSONObject(k)
                                            val post = Post()
                                            post.postId = obj.getInt("id")
                                            post.commentsNumbers = obj.getInt("commentNumber")
                                            post.collectNumber = obj.getInt("collectNumber")
                                            post.likeNumber = obj.getInt("likeNumber")
                                            post.uid = obj.getString("uid")
                                            post.title = obj.getString("title")
                                            post.time = obj.getString("time").toLong()
                                            post.content = obj.getString("info")
                                            post.tag = if (obj.has("tag")) obj.getString("tag") else ""
                                            dataList.add(post)
                                        }
                                    }
                                }
                            }catch (e : Exception){
                                Logger.log(e)
                                activity!!.runOnUiThread {
                                    Toast.makeText(context,"网络错误\n" +
                                            "服务器无响应",Toast.LENGTH_SHORT).show()
                                }
                            }
                            activity!!.runOnUiThread {
                                isLoadComplete = true
                                publishData()
                            }
                        }
                    }
            )
        }

    }

    override fun isLoadComplete(): Boolean {
        return isLoadComplete&&lastSearchString == search
    }

    override fun publishData() {
        super.publishData()
        if(dataList.size != 0){
            recyclerView!!.visibility = View.VISIBLE
            blankView!!.visibility = View.GONE
            loadingLayout!!.visibility = View.VISIBLE
        }else{
            recyclerView!!.visibility = View.GONE
            blankView!!.visibility = View.VISIBLE
            loadingLayout!!.visibility = View.GONE
        }
        adapter?.notifyDataSetChanged()
    }

    fun onLoading(){
        recyclerView!!.visibility = View.GONE
        blankView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE
    }

}