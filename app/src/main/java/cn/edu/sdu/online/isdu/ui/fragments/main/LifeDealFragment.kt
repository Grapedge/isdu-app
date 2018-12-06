package cn.edu.sdu.online.isdu.ui.fragments.main

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import com.liaoinstan.springview.widget.SpringView
import org.json.JSONArray
import org.json.JSONObject

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/25
 *
 * 主页关注碎片
 * 内容为关注的用户的帖子
 ****************************************************
 */

class LifeDealFragment : LazyLoadFragment() {

    private var recyclerView: RecyclerView? = null
    private var adapter: PostItemAdapter? = null
    //    private var updateBar: TextView? = null
    private var pullRefreshLayout: SpringView? = null
    private var dataList: MutableList<Post> = ArrayList()
//    private var blankView: TextView? = null

    private var lastId = 0
    private var needOffset = false
    private var loadComplete = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_life_deal, container, false)
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
//        updateBar = view.findViewById(R.id.update_bar)
        pullRefreshLayout = view.findViewById(R.id.pull_refresh_layout)
//        blankView = view.findViewById(R.id.blank_view)

//        updateBar!!.translationY = -100f
//        blankView!!.visibility = View.GONE
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = PostItemAdapter(activity!!, dataList)

        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }

    /**
     * 初始化下拉刷新
     */
    private fun initPullRefreshLayout() {
        // 下拉刷新监听器
        pullRefreshLayout!!.setListener(object : SpringView.OnFreshListener {
            override fun onLoadmore() {
                // 上拉加载更多
                lastId = if (dataList.isEmpty()) 0 else dataList[dataList.size - 1].postId
                needOffset = true
                loadComplete = false
                loadData()
            }

            override fun onRefresh() {
                // 下拉刷新
                lastId = 0
                dataList.clear()
                needOffset = false
                loadComplete = false
                loadData()
            }
        })

    }

    override fun isLoadComplete(): Boolean = loadComplete

    override fun loadData() {
        if (loadComplete) return
//        if (User.staticUser == null) User.staticUser = User.load()
//        if (User.staticUser.studentNumber == null) return
//        if (!User.isLogin()) return
        NetworkAccess.cache(ServerInfo.getSyncPostTen(lastId)) { success, cachePath ->
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
                        post.tag = if (obj.has("tag")) obj.getString("tag") else ""

                        if (!dataList.contains(post))
                            dataList.add(post)

                        lastId = post.postId
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

}