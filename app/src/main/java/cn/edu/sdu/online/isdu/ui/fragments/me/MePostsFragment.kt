package cn.edu.sdu.online.isdu.ui.fragments.me

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.interfaces.PostViewable
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.util.Logger
import com.liaoinstan.springview.widget.SpringView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
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

class MePostsFragment : LazyLoadFragment(), PostViewable {

    private var recyclerView: RecyclerView? = null
    private var adapter: PostItemAdapter? = null
    private var dataList: MutableList<Post> = ArrayList()

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

        adapter = PostItemAdapter(activity!!, dataList)
        recyclerView!!.adapter = adapter
    }

    override fun loadData() {
        NetworkAccess.buildRequest(ServerInfo.getPostList(uid,
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
                            val list = ArrayList<Post>()
                            val str = response?.body()?.string()
                            val jsonObj = JSONObject(str)

                            val jsonArr = JSONArray(jsonObj.getString("obj"))

                            for (i in 0 until jsonArr.length()) {
                                val obj = jsonArr.getJSONObject(i)
                                val post = Post()

                                post.postId = obj.getInt("id")
                                post.commentsNumbers = obj.getInt("commentNumber")
                                post.collectNumber = obj.getInt("collectNumber")
                                post.likeNumber = obj.getInt("likeNumber")
                                post.uid = obj.getString("uid")
                                post.title = obj.getString("title")
                                post.time = obj.getString("time").toLong()
                                post.content = obj.getString("info")
                                post.value = obj.getDouble("value")
                                post.tag = if (obj.has("tag")) obj.getString("tag") else ""

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

    fun removeAllItems() {
        dataList.clear()
        adapter?.notifyDataSetChanged()
    }

    private fun publishLoadData(list: List<Post>) {
        if (list.isNotEmpty()) {
            dataList.addAll(list)
            adapter!!.notifyDataSetChanged()
            if (needOffset)
                recyclerView!!.smoothScrollBy(0, 100)
        }
    }

    /**
     * 初始化下拉刷新
     */
    private fun initPullRefreshLayout() {
        // 下拉刷新监听器
        pullRefreshLayout!!.setListener(object : SpringView.OnFreshListener {
            override fun onLoadmore() {
                needOffset = true
                loadData()
            }

            override fun onRefresh() {
                dataList.clear()
                needOffset = false
                loadData()
            }
        })

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
        const val TAG = "MeArticleFragment"
    }
}