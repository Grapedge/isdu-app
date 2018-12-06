package cn.edu.sdu.online.isdu.ui.fragments.search

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.MyHomePageActivity
import cn.edu.sdu.online.isdu.util.Logger
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/18
 *
 * 搜索用户的Fragment
 ****************************************************
 */

class SearchUserFragment : LazyLoadFragment() {
    private var mAdapter: MyAdapter? = null
    private var loadingLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var blankView: TextView? = null
    private var dataList = ArrayList<LikeUser>()
    private var search : String? = null
    private var isLoadComplete = false
    private var isLoading = false

    private var lastSearchString = ""
    private var searchCall: Call? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_user, container, false)
        initView(view)
        initRecyclerView()

        return view
    }

    private fun initView(view: View) {
        loadingLayout = view.findViewById(R.id.loading_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
        blankView = view.findViewById(R.id.blank_view)
    }

    private fun initRecyclerView() {
        mAdapter = MyAdapter()
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = mAdapter
    }

    fun setSearch(search: String?){
        this.search = search
        if(userVisibleHint && (isLoadComplete || search != lastSearchString)){
            if (searchCall != null && !searchCall!!.isCanceled) searchCall!!.cancel()
            isLoadComplete = false

            lastSearchString = search!!
            loadData()
        }
    }

    override fun loadData() {
        super.loadData()
        if(search != null){
            isLoading = true
            onLoading()
            var url = ServerInfo.searchUserbyNickName(search)
            searchCall = NetworkAccess.buildRequest(url, object : Callback {
                override fun onFailure(call: Call?, e: IOException?) {
                    activity!!.runOnUiThread {
                        Logger.log(e)
                        Toast.makeText(context, "网络错误", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onResponse(call: Call?, response: Response?) {
                    val json = response?.body()?.string()
                    try {
                        Thread(Runnable {
                            synchronized(dataList) {
                                dataList.clear()
                                if (json == "[]") {
                                } else {
                                    // 获取我的关注列表
//                                    val client = OkHttpClient.Builder()
//                                            .connectTimeout(10, TimeUnit.SECONDS)
//                                            .writeTimeout(10, TimeUnit.SECONDS)
//                                            .readTimeout(10, TimeUnit.SECONDS)
//                                            .build()
//                                    val request = Request.Builder()
//                                            .url(ServerInfo.getMyLike(User.staticUser.uid.toString()))
//                                            .get()
//                                            .build()
//                                    val response = client.newCall(request).execute()
//                                    val myLikeStr = JSONObject(response?.body()?.string()).getString("obj")
//
//                                    val myLikeList = myLikeStr.split("-").subList(0,
//                                            if (myLikeStr != "") myLikeStr.split("-").size - 1 else 0)

                                    val jsonArray = JSONArray(json)
                                    for (k in 0 until jsonArray.length()) {
                                        val obj = jsonArray.getJSONObject(k)
                                        val item = LikeUser()
                                        item.nickName = obj.getString("nickname")
                                        item.avatarUrl = obj.getString("avatar")
                                        item.selfIntroduce = obj.getString("sign")
                                        item.uid = obj.getInt("id")
//                                        item.isLiked = myLikeList.contains(item.uid.toString())
                                        dataList.add(item)
                                    }
                                    activity!!.runOnUiThread {
                                        isLoadComplete = true
                                        publishData()
                                    }
                                }
                            }
                        }).start()

                    } catch (e: Exception) {
                        Logger.log(e)
                        activity!!.runOnUiThread {
                            Toast.makeText(context, "网络错误\n服务器无响应", Toast.LENGTH_SHORT).show()
                        }
                    }
                    activity!!.runOnUiThread {
                        isLoadComplete = true
                        publishData()
                    }
                }
            })
        }
    }

    override fun isLoadComplete(): Boolean = isLoadComplete && lastSearchString == search


    override fun publishData() {
        if(dataList.size != 0){
            recyclerView!!.visibility = View.VISIBLE
            loadingLayout!!.visibility = View.GONE
            blankView!!.visibility = View.GONE
        }else{
            recyclerView!!.visibility = View.GONE
            loadingLayout!!.visibility = View.GONE
            blankView!!.visibility = View.VISIBLE
        }
        if(mAdapter != null){
            mAdapter!!.notifyDataSetChanged()
        }
    }

    fun onLoading(){
        recyclerView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE
        blankView!!.visibility = View.GONE
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.search_user_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getItemCount(): Int = dataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = dataList[position]
            Glide.with(context!!)
                    .load(user.avatarUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(holder.circleImageView)
            holder.userName.text = user.nickName
            holder.userSign.text = user.selfIntroduce

            holder.btnFollow.visibility = View.INVISIBLE

            holder.btnFollow.setOnClickListener {
//                if (User.staticUser == null || User.staticUser.studentNumber == null) {

                if (!User.isLogin()) {
                } else {
                    Thread(Runnable {
                        try {
                            val client = OkHttpClient.Builder()
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(10, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.SECONDS)
                                    .build()
                            val request = Request.Builder()
                                    .url(ServerInfo.userLike(User.staticUser.uid.toString(), user.uid.toString()))
                                    .get()
                                    .build()
                            client.newCall(request).execute()

                            user.isLiked = !user.isLiked
                            activity!!.runOnUiThread { notifyDataSetChanged() }
                        } catch (e: Exception) {
                            Logger.log(e)
                        }
                    }).start()
                }

            }
            holder.itemLayout.setOnClickListener {
                //(activity as SearchActivity).editSearch!!.setText("")
                //clear()
                startActivity(Intent(context, MyHomePageActivity::class.java).putExtra("id", user.uid))
            }

            if (user.isLiked) {
                holder.btnFollow.text = "已关注"
                holder.btnFollow.setTextColor(0xFF717EDB.toInt())
                holder.btnFollow.setBackgroundResource(R.drawable.purple_stroke_rect_colorchanged)
            } else {
                holder.btnFollow.text = "关注"
                holder.btnFollow.setTextColor(0xFF808080.toInt())
                holder.btnFollow.setBackgroundResource(R.drawable.text_button_background)
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var circleImageView: CircleImageView = view.findViewById(R.id.circle_image_view)
            var userName: TextView = view.findViewById(R.id.user_name)
            var userSign: TextView = view.findViewById(R.id.user_sign)
            var btnFollow: TextView = view.findViewById(R.id.btn_follow)
            var itemLayout: View = view.findViewById(R.id.item_layout)
        }
    }


    inner class LikeUser : User() {
        var isLiked = false
    }
}