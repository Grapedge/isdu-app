package cn.edu.sdu.online.isdu.ui.fragments.me

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.ThreadPool
import cn.edu.sdu.online.isdu.bean.PostComment
import cn.edu.sdu.online.isdu.interfaces.PostViewable
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.MyHomePageActivity
import cn.edu.sdu.online.isdu.ui.activity.PostDetailActivity
import cn.edu.sdu.online.isdu.util.DateCalculate
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.WeakReferences
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.liaoinstan.springview.widget.SpringView
import com.qmuiteam.qmui.span.QMUITouchableSpan
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView
import de.hdodenhof.circleimageview.CircleImageView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/25
 *
 * 个人主页我的帖子碎片
 ****************************************************
 */

class MeCommentFragment : LazyLoadFragment(), PostViewable {

    private var recyclerView: RecyclerView? = null
    private var adapter: MyAdapter? = null
    private var dataList: MutableList<MyComment> = ArrayList()

    private var pullRefreshLayout: SpringView? = null

    private var uid = -1

    private var lastId = 0
    private var needOffset = false // 是否需要列表位移


    fun setUid(uid: Int) {
        this.uid = uid
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_me_articles, container, false)

        initView(view)
        initRecyclerView()
        initPullRefreshLayout()

        return view
    }

    override fun onResume() {
        super.onResume()
        adapter?.notifyDataSetChanged()
    }

    override fun removeItem(item: Any?) {
        var j = 0
        for (i in 0 until  dataList.size) {
            if (dataList[i].postId == item as Int) j = i
        }
        dataList.removeAt(j)
        adapter?.notifyDataSetChanged()
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        pullRefreshLayout = view.findViewById(R.id.pull_refresh_layout)

    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)

        adapter = MyAdapter()
        recyclerView!!.adapter = adapter
    }

    fun removeAllItems() {
        dataList.clear()
        adapter?.notifyDataSetChanged()
    }

    override fun loadData() {
        NetworkAccess.buildRequest(ServerInfo.getMyComment10(uid.toString(),
                if (dataList.size > 0) dataList[dataList.size - 1].postId - 1 else 0),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Logger.log(e)
                        activity!!.runOnUiThread {
                            pullRefreshLayout!!.onFinishFreshAndLoad()
                        }
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        activity!!.runOnUiThread {
                            pullRefreshLayout!!.onFinishFreshAndLoad()
                        }
                        try {
                            val list = ArrayList<MyComment>()
                            val str = response?.body()?.string()
                            val jsonObj = JSONObject(str)

                            val jsonArr = JSONArray(jsonObj.getString("obj"))

                            for (i in 0 until jsonArr.length()) {
                                val obj = jsonArr.getJSONObject(i)
                                val post = MyComment()

                                post.postId = obj.getInt("postId")
                                post.fatherId = obj.getInt("fatherCommenrId")
                                post.id = obj.getInt("id")
                                post.uid = obj.getString("userId")
                                post.time = obj.getString("time").toLong()
                                post.content = obj.getString("content")

                                if (!dataList.contains(post))
                                    list.add(post)
                            }

                            activity!!.runOnUiThread {
                                publishLoadData(list)
                            }
                        } catch (e: Exception) {
                            Logger.log(e)
                        }
                    }
                })
    }

    /**
     * 下拉刷新发布最新帖子信息
     */
    private fun publishNewData(list: List<MyComment>) {
        if (list.isEmpty()) {

        } else {
            dataList.clear()
            dataList.addAll(list)
            adapter!!.notifyDataSetChanged()
        }
    }

    private fun publishLoadData(list: List<MyComment>) {
        if (list.isNotEmpty()) {
            dataList.addAll(list)
            adapter!!.notifyDataSetChanged()
            if (needOffset)
                recyclerView!!.smoothScrollBy(0, 100)
        }
    }

    override fun publishData() {
        super.publishData()
    }

    /**
     * 初始化下拉刷新
     */
    private fun initPullRefreshLayout() {
        // 下拉刷新监听器
        pullRefreshLayout!!.setListener(object : SpringView.OnFreshListener {
            override fun onLoadmore() {
                loadData()
                needOffset = true
            }

            override fun onRefresh() {
                dataList.clear()
                needOffset = false
                loadData()
            }
        })

    }


    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        val userNicknameMap = HashMap<String, String>()
        val userAvatarMap = HashMap<String, String>()
        val postTitleMap = HashMap<Int, String>()
//        val postTimeMap = HashMap<Int, Long>()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            // 开启新线程获取信息
            ThreadPool.execute {
                // 获取帖子信息
                if (!postTitleMap.containsKey(item.postId)) {
                    val client = OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build()
                    val request = Request.Builder()
                            .url(ServerInfo.getPostIntroduction(item.postId))
                            .get()
                            .build()
                    val response = client.newCall(request).execute()

                    val obj = JSONObject(JSONObject(response?.body()?.string()).getString("obj"))

                    item.postUserNickname = obj.getString("nickName")
                    item.postTitle = obj.getString("title")
                    item.postUserId = obj.getString("userId").toInt()

                }

                // 获取用户信息
                if (!userNicknameMap.containsKey(item.uid)) {
                    val client = OkHttpClient.Builder()
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .readTimeout(10, TimeUnit.SECONDS)
                            .build()
                    val request = Request.Builder()
                            .url(ServerInfo.getUserInfo(item.uid, "avatar-nickname"))
                            .get()
                            .build()
                    val response = client.newCall(request).execute()

                    val obj = JSONObject(response?.body()?.string())
                    userNicknameMap[item.uid] = obj.getString("nickname")
                    userAvatarMap[item.uid] = obj.getString("avatar")
                }

                val sp = SpannableString("${item.postUserNickname}: ${item.postTitle}")
                sp.setSpan(object : QMUITouchableSpan(
                        0xFF717EDB.toInt(), 0xFF717EDB.toInt(),
                        0x00000000, 0x11000000
                ) {
                    override fun onSpanClick(widget: View?) {
                        startActivity(Intent(context, MyHomePageActivity::class.java)
                                .putExtra("id", item.postUserId))
                    }
                }, 0, item.postUserNickname.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

                activity!!.runOnUiThread {
                    holder.txtReply.setText(sp)
                    holder.itemLayout.setOnClickListener {
                        WeakReferences.postViewableWeakReference = WeakReference(this@MeCommentFragment)
                        context!!.startActivity(Intent(context, PostDetailActivity::class.java)
                                .putExtra("id", item.postId)
                                .putExtra("uid", item.uid)
//                            .putExtra("title", item.title)
                                .putExtra("time", item.time))
                    }
                    holder.content.text = item.content
//            holder.commentNumber.text = item.commentsNumbers.toString()
                    holder.nickName.text = "${userNicknameMap[item.uid]}"
                    Glide.with(MyApplication.getContext()).load(userAvatarMap[item.uid])
                            .apply(RequestOptions.skipMemoryCacheOf(true))
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .into(holder.circleImageView)

                    holder.releaseTime.text = DateCalculate.getExpressionDate(item.time)
//                    holder.releaseTime.text = if (System.currentTimeMillis() - item.time < 60 * 1000)
//                        "刚刚" else (if (System.currentTimeMillis() - item.time < 24 * 60 * 60 * 1000)
//                        "${(System.currentTimeMillis() - item.time) / (60 * 60 * 1000)} 小时前" else (
//                            if (System.currentTimeMillis() - item.time < 48 * 60 * 60 * 1000) "昨天 ${SimpleDateFormat("HH:mm").format(item.time)}"
//                            else SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.time)))
                }

            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.post_comment_item, parent, false)
            view.findViewById<QMUISpanTouchFixTextView>(R.id.reply_comment)
                    .setMovementMethodDefault()
            view.findViewById<QMUISpanTouchFixTextView>(R.id.reply_comment)
                    .setNeedForceEventToParent(true)
            return ViewHolder(v = view)
        }

        override fun getItemCount(): Int = dataList.size

        override fun getItemId(position: Int): Long = position.toLong()

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val itemLayout: View = v.findViewById(R.id.item_layout)
            val nickName: TextView = v.findViewById(R.id.txt_nickname) // 标题
            //            val contentLayout: LinearLayout = v.findViewById(R.id.content_layout) // 内容Layout
//            val userName: TextView = v.findViewById(R.id.user_name) // 用户名
            val releaseTime: TextView = v.findViewById(R.id.txt_time) // 发布时间
            val content: TextView = v.findViewById(R.id.txt_content)
            val circleImageView: CircleImageView = v.findViewById(R.id.circle_image_view)
            val txtReply: QMUISpanTouchFixTextView = v.findViewById(R.id.reply_comment)
        }
    }

    inner class MyComment : PostComment() {
        var postUserId = 0
        var postUserNickname = ""
        var postTitle = ""
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("uid", uid)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState?.getInt("uid") != null)
            uid = savedInstanceState.getInt("uid")
    }

    companion object {
        const val TAG = "MeCommentFragment"
    }
}