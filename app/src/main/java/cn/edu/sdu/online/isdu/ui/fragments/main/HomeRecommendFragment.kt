package cn.edu.sdu.online.isdu.ui.fragments.main

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import com.liaoinstan.springview.widget.SpringView
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigInteger
import kotlin.collections.ArrayList

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/5/25
 *
 * 主页论坛推荐碎片
 ****************************************************
 */

class HomeRecommendFragment : LazyLoadFragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: PostItemAdapter? = null
//    private var updateBar: TextView? = null
    private var pullRefreshLayout: SpringView? = null
    private var dataList: MutableList<Post> = ArrayList()
//    private var blankView: TextView? = null

    private var lastValue = 0
    private var needOffset = false
    private var loadComplete = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_recommend, container, false)
        initView(view)
        initRecyclerView()
        initPullRefreshLayout()

        return view
    }

    /**
     * 初始化View
     */
    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        pullRefreshLayout = view.findViewById(R.id.pull_refresh_layout)
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = PostItemAdapter(activity!!, dataList)

        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }

    override fun isLoadComplete(): Boolean = loadComplete

    /**
     * 初始化下拉刷新
     */
    private fun initPullRefreshLayout() {
        // 下拉刷新监听器
        pullRefreshLayout!!.setListener(object : SpringView.OnFreshListener {
            override fun onLoadmore() {
                // 上拉加载更多
//                lastValue = if (dataList.isEmpty()) 0.0 else calculateValue(dataList[dataList.size - 1])
                needOffset = true
                loadComplete = false
                loadData()
            }

            override fun onRefresh() {
                // 下拉刷新
                lastValue = 0
                dataList.clear()
                needOffset = false
                loadComplete = false
                loadData()
            }
        })

    }

//    private fun calculateValue(post: Post) : Double {
////        return post.likeNumber.toDouble() + 2.0 * post.commentsNumbers.toDouble() +
////                (post.time.toDouble() % 100000000000.0 / 1000000.0) +
////                (post.time.toDouble() % 100000.0) / 100000.0
//        return getValueHot(post.likeNumber, post.commentsNumbers, post.time.toString())
//    }
//
//    private fun getValueHot(likeNUmber: Int, commentNumber: Int, time: String): Double {
//        val times = BigInteger(time)
//
//        var timePart = times.mod(BigInteger.valueOf(100000000000L))
//
//        timePart = timePart.divide(BigInteger.valueOf(1000000L))
//
//        val intPart = 100000 * (likeNUmber + 2 * commentNumber) + Integer.valueOf(timePart.toString())
//
//        timePart = times.remainder(BigInteger.valueOf(100000L))
//
//        val miniPart = java.lang.Double.valueOf(timePart.toString()) / 100000
//
//        println(intPart + miniPart)
//        return intPart + miniPart
//    }

    override fun loadData() {
        if (isLoadComplete) return
        NetworkAccess.cache(ServerInfo.getRecommend10(lastValue)) { success, cachePath ->
            if (success) {
                try {
                    val arr = JSONArray(JSONObject(FileUtil.getStringFromFile(cachePath)).getString("obj"))
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val post = Post()
                        post.uid = obj.getString("uid")
                        post.commentsNumbers = obj.getInt("commentNumber")
                        post.postId = obj.getInt("id")
                        post.time = obj.getString("time").toLong()
                        post.title = obj.getString("title")
                        post.likeNumber = obj.getInt("likeNumber")
                        post.content = obj.getString("info")
                        post.value = obj.getDouble("value")
                        post.tag = if (obj.has("tag")) obj.getString("tag") else ""

                        if (!dataList.contains(post))
                            dataList.add(post)

//                        if (lastValue - 0.0001 < 0) {
//                            lastValue = calculateValue(post)
//                        } else {
//                            lastValue = Math.min(lastValue, calculateValue(post))
//                        }

//                        lastValue = post.value
                        lastValue = post.postId
//                        Log.d("Jzz", "lastValue = $lastValue")
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }
            } else {

            }

            loadComplete = true

            activity?.runOnUiThread {
                adapter?.notifyDataSetChanged()
                if (needOffset) recyclerView?.smoothScrollBy(0, 50)
                pullRefreshLayout!!.onFinishFreshAndLoad()
            }
        }
    }

/*
    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private val uNicknameMap = HashMap<String, String>()
        private val uAvatarMap = HashMap<String, String>()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]

            if (uNicknameMap.containsKey(item.uid)) {
                holder.txtNickname.text = uNicknameMap[item.uid]
                Glide.with(MyApplication.getContext())
                        .load(uAvatarMap[item.uid])
                        .into(holder.circleImageView)
            } else {
                NetworkAccess.cache(ServerInfo.getUserInfo(item.uid, "nickname-avatar")) { success, cachePath ->
                    if (success) {
                        try {
                            val obj = JSONObject(FileUtil.getStringFromFile(cachePath))
                            uNicknameMap[item.uid] = obj.getString("nickname")
                            uAvatarMap[item.uid] = obj.getString("avatar")

                            activity!!.runOnUiThread {
                                holder.txtNickname.text = uNicknameMap[item.uid]
                                Glide.with(MyApplication.getContext())
                                        .load(uAvatarMap[item.uid])
                                        .into(holder.circleImageView)
                            }
                        } catch (e: Exception) {}

                    }
                }
            }

            holder.cardView.setOnClickListener {
                context!!.startActivity(Intent(context, PostDetailActivity::class.java)
                        .putExtra("id", item.postId)
                        .putExtra("uid", item.uid)
                        .putExtra("title", item.title)
                        .putExtra("time", item.time))
            }
            holder.titleView.text = item.title
            holder.commentNumber.text = item.commentsNumbers.toString()
            holder.content.text = item.content
            holder.txtLike.text = item.likeNumber.toString()
            holder.releaseTime.text = DateCalculate.getExpressionDate(item.time)

            holder.circleImageView.setOnClickListener {
                startActivity(Intent(context, MyHomePageActivity::class.java)
                        .putExtra("id", item.uid.toInt()))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                    LayoutInflater.from(parent.context).inflate(
                            R.layout.post_item, parent, false)
            return ViewHolder(v = view)
        }

        override fun getItemCount(): Int = dataList.size

        override fun getItemId(position: Int): Long = position.toLong()

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val cardView: FrameLayout = v.findViewById(R.id.card_view)
            val titleView: TextView = v.findViewById(R.id.title_view) // 标题
            //            val contentLayout: LinearLayout = v.findViewById(R.id.content_layout) // 内容Layout
//            val userName: TextView = v.findViewById(R.id.user_name) // 用户名
            val commentNumber: TextView = v.findViewById(R.id.comments_number) // 评论数
            val releaseTime: TextView = v.findViewById(R.id.release_time) // 发布时间
            val content: TextView = v.findViewById(R.id.content)
            val txtLike: TextView = v.findViewById(R.id.like_count)
            // 新增：用户信息区域
            val circleImageView: CircleImageView = v.findViewById(R.id.circle_image_view)
            val txtNickname: TextView = v.findViewById(R.id.txt_nickname)
        }
    }
*/
}