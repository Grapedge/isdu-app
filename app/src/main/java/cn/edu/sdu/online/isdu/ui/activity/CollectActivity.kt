package cn.edu.sdu.online.isdu.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.Collect
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.interfaces.PostViewable
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.WeakReferences
import org.json.JSONArray
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

class CollectActivity : SlideActivity(), PostViewable {
    private var collectList: MutableList<Collect> = ArrayList()

    private var btnCollectBack: View? = null
    private var recyclerView: RecyclerView? = null
    private var adapter: CollectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_me_collect)

        initView()

        // 判断登录
        Thread(Runnable {
//            if (User.staticUser == null) User.staticUser = User.load()
//            if (User.staticUser.studentNumber == null) {
            if (!User.isLogin()) {
                val dialog = AlertDialog(this)
                dialog.setTitle("未登录")
                dialog.setMessage("请登录后重试")
                dialog.setCancelOnTouchOutside(false)
                dialog.setCancelable(false)
                dialog.setPositiveButton("登录") {
                    dialog.dismiss()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
                dialog.setNegativeButton("返回") {
                    finish()
                    dialog.dismiss()
                }
                dialog.show()
            } else {
                runOnUiThread {
                    initRecyclerView()
                }
                getData()
            }


        }).start()

    }

    private fun initView() {
        btnCollectBack = findViewById(R.id.btn_back)
        recyclerView = findViewById(R.id.recycler_view)

        btnCollectBack!!.setOnClickListener {
            finish()
        }
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = CollectAdapter()
        recyclerView!!.adapter = adapter
    }

    override fun removeItem(item: Any?) {

    }

    private fun getData() {
//        if (User.staticUser == null) User.staticUser = User.load()
//        if (User.staticUser.studentNumber == null) return
        if (!User.isLogin()) return
        NetworkAccess.cache(ServerInfo.getCollectList + "?userId=" + User.staticUser.uid) { success, cachePath ->
            if (success) {
                try {
                    val jsonStr = JSONObject(FileUtil.getStringFromFile(cachePath)).getString("obj")
                    val arr = JSONArray(jsonStr)
                    collectList.clear()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val collect = Collect()
                        collect.collectTitle = obj.getString("title")
                        collect.collectTime = obj.getString("time").toLong()
                        collect.collectUrl = ServerInfo.getPost(obj.getInt("id"))
                        collect.collectContent = obj.getString("info")
                        collect.id = obj.getInt("id")
                        collect.uid = obj.getString("uid").toInt()
                        collectList.add(collect)
                    }

                    runOnUiThread {
                        adapter?.notifyDataSetChanged()
                    }
                } catch (e: Exception) {
                    // 未获取到信息
                }
            } else {

            }
        }
    }

    inner class CollectAdapter :
            RecyclerView.Adapter<CollectAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var collectTitle: TextView = view.findViewById(R.id.textview_collect_title)
            var collectContent: TextView = view.findViewById(R.id.textview_collect_content)
            var collectTime: TextView = view.findViewById(R.id.collect_time)
            var itemLayout: View = view.findViewById(R.id.item_layout)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collect, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val collect = collectList[position]
            holder.collectTitle.text = collect.collectTitle
            holder.collectContent.text = collect.collectContent
            holder.collectTime.text = SimpleDateFormat("yyyy-MM-dd").format(collect.collectTime)
            holder.itemLayout.setOnClickListener {
                when (collect.collectType) {
                    Collect.TYPE_NEWS -> {
                        startActivity(Intent(this@CollectActivity, NewsActivity::class.java)
                                .putExtra("url", collect.collectUrl))
                    }
                    Collect.TYPE_POST -> {
                        WeakReferences.postViewableWeakReference = WeakReference(this@CollectActivity)
                        startActivity(Intent(this@CollectActivity, PostDetailActivity::class.java)
                                .putExtra("id", collect.id)
                                .putExtra("uid", collect.uid.toString())
                                .putExtra("title", collect.collectTitle)
                                .putExtra("time", collect.collectTime)
                                .putExtra("tag", TAG))
                    }
                }
            }
        }

        override fun getItemCount(): Int = collectList.size

    }

    companion object {
        const val TAG = "CollectActivity"
    }

}

