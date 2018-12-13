package cn.edu.sdu.online.isdu.ui.fragments.main;

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
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
import java.util.*

class HomeConfessionFragment : LazyLoadFragment() {

    // View
    private var recyclerView: RecyclerView? = null
    private var adapter: ConfessionItemAdapter? = null
    private var pullRefreshLayout: SpringView? = null
    private var dataList: MutableList<Confession> = ArrayList()

    private var lastValue = 0
    private var needOffset = false
    private var loadComplete = false

    // create view
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home_confession, container, false)
        initView(view)
        initRecyclerView()
        initPullRefreshLayout()
        return view
    }

    // init view
    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        pullRefreshLayout = view.findViewById(R.id.pull_refresh_layout)
    }

    // init recycler view
    private fun initRecyclerView() {

        adapter = ConfessionItemAdapter(activity!!, dataList)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }

    // init refresh layout
    private fun initPullRefreshLayout() {
        pullRefreshLayout!!.setListener(object : SpringView.OnFreshListener {
            override fun onLoadmore() {
                // load more
                lastValue = if (dataList.isEmpty()) 0 else dataList[dataList.size - 1].confessionId
                needOffset = true
                loadComplete = false
                loadData()
            }

            override fun onRefresh() {
                // refresh
                lastValue = 0
                dataList.clear()
                needOffset = false
                loadComplete = false
                loadData()
            }
        })

    }

    override fun isLoadComplete(): Boolean = loadComplete

    val nameList = listOf("庄总", "耿大佬", "高老板", "洛七", "七七", "冬冬", "董叔", "齐站")
    override fun loadData() {
        // test load here
        for (i in nameList) {
            val confession = Confession()
            confession.uid = "2"
            confession.commentCount = (Math.random() * 300).toInt()
            confession.confessionId = (Math.random() * 300).toInt()
            confession.time = Date().time
            confession.title = "表白$i"
            confession.likeCount = (Math.random() * 300).toInt()
            confession.postContent =
                    if (i != "董叔")
                        "$i 好${if (Math.random() <= 0.5) "帅" else "可爱"}鸭，不知道有没有${if (Math.random() <= 0.5) "男朋友" else "女朋友"}，我真的真的好喜欢他啊！"
                    else "董叔都大四了还没有女朋友，替董叔征个婚"

            confession.tag = "confession"
            dataList.add(confession)
        }
        loadComplete = true
        adapter?.notifyDataSetChanged()
        if (needOffset) recyclerView?.smoothScrollBy(0, 50)
        pullRefreshLayout!!.onFinishFreshAndLoad()
        return
        /*
        if (loadComplete) return
        NetworkAccess.cache(ServerInfo.getTagedPostTen(lastValue)) { success, cachePath ->
            if (success) {
                try {
                    val arr = JSONArray(JSONObject(FileUtil.getStringFromFile(cachePath)).getString("obj"))
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val confession = Confession()
                        confession.uid = obj.getString("uid")
                        confession.commentCount = obj.getInt("commentNumber")
                        confession.confessionId = obj.getInt("id")
                        confession.time = obj.getString("time").toLong()
                        confession.title = obj.getString("title")
                        confession.likeCount = obj.getInt("likeNumber")
                        confession.postContent = obj.getString("info")
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
        */
    }
}
