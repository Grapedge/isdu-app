package cn.edu.sdu.online.isdu.ui.fragments.main;

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Confession
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.ConfessionItemAdapter
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import com.liaoinstan.springview.widget.SpringView
import org.json.JSONArray
import org.json.JSONObject

class HomeConfessionFragment  : LazyLoadFragment() {


    private var recyclerView: RecyclerView? = null
    private var adapter: ConfessionItemAdapter? = null
    //    private var updateBar: TextView? = null
    private var pullRefreshLayout: SpringView? = null
    private var dataList: MutableList<Confession> = ArrayList()
//    private var blankView: TextView? = null

    private var lastValue = 0
    private var needOffset = false
    private var loadComplete = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_confession, container, false)
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
//        blankView!!.visibility  = View.GONE
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = ConfessionItemAdapter(activity!!, dataList)

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
                lastValue = if (dataList.isEmpty()) 0 else dataList[dataList.size - 1].confessionId
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

    override fun loadData() {
        if (loadComplete) return
        /*这个要改*/
                NetworkAccess.cache(ServerInfo.getTagedPostTen(lastValue)) { success, cachePath ->
            if (success) {
                try {
                    val arr = JSONArray(JSONObject(FileUtil.getStringFromFile(cachePath)).getString("obj"))
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val confession = Confession()
                        confession.uid = obj.getString("uid")
                        confession.commentsNumbers = obj.getInt("commentNumber")
                        confession.confessionId = obj.getInt("id")
                        confession.time = obj.getString("time").toLong()
                        confession.title = obj.getString("title")
                        confession.likeNumber = obj.getInt("likeNumber")
                        confession.content = obj.getString("info")
                        confession.tag = if (obj.has("tag")) obj.getString("tag") else ""

                        if (!dataList.contains(confession))
                            dataList.add(confession)

                        lastValue = confession.confessionId
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
