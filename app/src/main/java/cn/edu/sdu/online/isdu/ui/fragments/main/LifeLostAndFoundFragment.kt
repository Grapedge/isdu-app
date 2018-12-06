package cn.edu.sdu.online.isdu.ui.fragments.main

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Life
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.LifeItemAdapter
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.ui.fragments.search.SearchLifeFragment
import cn.edu.sdu.online.isdu.ui.fragments.search.SearchNewsFragment
import cn.edu.sdu.online.isdu.ui.fragments.search.SearchPostFragment
import cn.edu.sdu.online.isdu.ui.fragments.search.SearchUserFragment
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import com.liaoinstan.springview.widget.SpringView
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.dialog.QMUIDialogBlockBuilder
import kotlinx.android.synthetic.main.fragment_life_lost_and_found.*
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
 *
 * 适配器还没写啊
 * 还有serverInfo没写
 * 不过应该问题不大，增加几条语句就好了，大概
 ****************************************************
 */

class LifeLostAndFoundFragment : LazyLoadFragment() {

    private var recyclerView: RecyclerView? = null
    private var imageCard: ImageView? = null
    private var imageCredentials: ImageView? = null
    private var imageBook: ImageView? = null
    private var imageClothing: ImageView? = null
    private var imageElectronics: ImageView? = null
    private var imageOther: ImageView? = null
    private var imageZhongxin: ImageView? = null
    private var imageRunjianyuan: ImageView? = null
    private var imageHongjialou: ImageView? = null
    private var imageQianfoshan: ImageView? = null
    private var imageBaotuquan: ImageView? = null
    private var imageXinglongshan: ImageView? = null

    private var adapter: LifeItemAdapter? = null//这里适配器
    //    private var updateBar: TextView? = null
    private var pullRefreshLayout: SpringView? = null
    private var dataList: MutableList<Life> = ArrayList()//这里也要改啊
//    private var blankView: TextView? = null

    private var lastId = 0
    private var needOffset = false
    private var loadComplete = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_life_lost_and_found, container, false)

        initView(view)
        initRecyclerView()
        initPullRefreshLayout()

        return view
    }


    /**
     * 初始化View
     */
    private fun initView(view: View) {
        imageCard = view.findViewById(R.id.image_card)
        imageCredentials = view.findViewById(R.id.image_credentials)
        imageBook = view.findViewById(R.id.image_book)
        imageClothing = view.findViewById(R.id.image_clothing)
        imageElectronics = view.findViewById(R.id.image_electronics)
        imageOther = view.findViewById(R.id.image_other)
        imageZhongxin = view.findViewById(R.id.image_zhongxin)
        imageRunjianyuan = view.findViewById(R.id.image_ruanjianyuan)
        imageHongjialou = view.findViewById(R.id.image_hongjialou)
        imageQianfoshan = view.findViewById(R.id.image_qianfoshan)
        imageBaotuquan = view.findViewById(R.id.image_baotuquan)
        imageXinglongshan = view.findViewById(R.id.image_xinglnogshan)





        recyclerView = view.findViewById(R.id.recycler_view_1)
//        updateBar = view.findViewById(R.id.update_bar)
        pullRefreshLayout = view.findViewById(R.id.pull_refresh_layout_1)
//        blankView = view.findViewById(R.id.blank_view)

//        updateBar!!.translationY = -100f
//        blankView!!.visibility = View.GONE
    }

    /*
    * 我想要初始化QMUI
    * */



    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        adapter = LifeItemAdapter(activity!!, dataList)//这里适配器

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
                lastId = if (dataList.isEmpty()) 0 else dataList[dataList.size - 1].lifeId
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
     fun onClick(v: View?) {
        when (v!!.id) {
            R.id.image_qianfoshan -> {loadData()//这里还要等后端接口出来吧
            }
            R.id.image_hongjialou -> {

            }
            R.id.spinner -> {

            }
        }
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
                        val life = Life()
                        life.uid = obj.getString("uid")
                        //life.commentsNumbers = obj.getInt("commentNumber")//不要
                        life.campus = obj.getString("campus")//新加的校区

                        life.lifeId = obj.getInt("id")
                        life.time = obj.getString("time").toLong()
                        life.title = obj.getString("title")
                        //life.likeNumber = obj.getInt("likeNumber")//不要
                        life.content = obj.getString("info")
                        life.tag = if (obj.has("tag")) obj.getString("tag") else ""

                        if (!dataList.contains(life))
                            dataList.add(life)

                        lastId = life.lifeId
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