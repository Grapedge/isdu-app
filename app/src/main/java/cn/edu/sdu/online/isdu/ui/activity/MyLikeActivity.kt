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
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.Logger
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_my_like.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class MyLikeActivity : SlideActivity() {
    private var recyclerView: RecyclerView? = null
    private var dataList: MutableList<LikeUser> = ArrayList()
    private var idList: MutableList<String> = ArrayList()
    private var adapter: MyAdapter? = null

    private var uid = ""

    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_like)

        uid = intent.getStringExtra("uid") ?: ""

        if (User.isLogin() && User.staticUser.uid.toString() == uid) {
            title_text.text = "我关注的人"
        } else {
            title_text.text = "TA关注的人"
        }

        dialog = ProgressDialog(this, false)
        dialog!!.setMessage("正在加载")
        dialog!!.setButton(null, null)
        dialog!!.setCancelable(false)

        btn_back.setOnClickListener { finish() }

        initView()
        initRecyclerView()

        getUserList()
    }

    private fun initView() {
        recyclerView = recycler_view
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter()
        recyclerView!!.adapter = adapter
    }

    private fun getUserList() {
        dialog!!.show()
        NetworkAccess.buildRequest(ServerInfo.getMyLike(uid), object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Logger.log(e)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    Thread(Runnable {
                        synchronized(idList) {
                            val str = JSONObject(response.body()?.string()).getString("obj")
                            val ids = str.split("-")
                            idList.clear()
                            for (id in ids) {
                                if (id != "") idList.add(id)
                            }

                            // 获取关注我的列表
//                            val client = OkHttpClient.Builder()
//                                    .connectTimeout(10, TimeUnit.SECONDS)
//                                    .writeTimeout(10, TimeUnit.SECONDS)
//                                    .readTimeout(10, TimeUnit.SECONDS)
//                                    .build()
//                            val request = Request.Builder()
//                                    .url(ServerInfo.getLikeMe(User.staticUser.uid.toString()))
//                                    .get()
//                                    .build()
//                            val response = client.newCall(request).execute()
//                            val myLikeStr = JSONObject(response?.body()?.string()).getString("obj")
//
//                            val myLikeList = myLikeStr.split("-").subList(0,
//                                    if (myLikeStr != "") myLikeStr.split("-").size - 1 else 0)

                            // 同线程依次获取用户信息
                            dataList.clear()
                            for (id in idList) {
                                val client = OkHttpClient.Builder()
                                        .connectTimeout(10, TimeUnit.SECONDS)
                                        .readTimeout(10, TimeUnit.SECONDS)
                                        .writeTimeout(10, TimeUnit.SECONDS)
                                        .build()
                                val request = Request.Builder()
                                        .url(ServerInfo.getUserInfo(id, "avatar-nickname-sign"))
                                        .get()
                                        .build()
                                val response = client.newCall(request).execute()

                                val obj = JSONObject(response?.body()?.string())

                                val user = LikeUser()
                                user.uid = id.toInt()
                                user.nickName = obj.getString("nickname")
                                user.selfIntroduce = obj.getString("sign")
                                user.avatarUrl = obj.getString("avatar")
//                                user.isLiked = myLikeList.contains(user.uid.toString())
                                user.isLiked = true

                                dataList.add(user)
                            }

//                            runOnUiThread {
//                                adapter?.notifyDataSetChanged()
//                                dialog?.dismiss()
//                            }
                            if (recyclerView!!.isComputingLayout) {
                                while (recyclerView!!.isComputingLayout) {
                                    Thread.sleep(100)
                                }
                                runOnUiThread {
                                    adapter?.notifyDataSetChanged()
                                    dialog?.dismiss()
                                }
                            } else {
                                runOnUiThread {
                                    adapter?.notifyDataSetChanged()
                                    dialog?.dismiss()
                                }
                            }
                        }

                    }).start()

                } catch (e: Exception) {
                    Logger.log(e)
                    runOnUiThread {
                        dialog?.dismiss()
                    }
                }
            }
        })
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.search_user_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = dataList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataList[position]
            holder.txtUserName.text = item.nickName
            holder.txtSign.text = item.selfIntroduce
//            Thread(Runnable {
//                val bmp = ImageManager.convertStringToBitmap(item.avatarString)
//                runOnUiThread {
//                    holder.circleImageView.setImageBitmap(bmp)
//                }
//            }).start()

            Glide.with(this@MyLikeActivity)
                    .load(item.avatarUrl)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(holder.circleImageView)

            holder.itemLayout.setOnClickListener {
                startActivity(Intent(this@MyLikeActivity, MyHomePageActivity::class.java)
                        .putExtra("id", item.uid))
            }

//            if (User.staticUser == null || User.staticUser.studentNumber == null) {
            if (!User.isLogin()) {
                holder.btnFollow.visibility = View.INVISIBLE
            } else {
                if (item.uid.toString() == User.staticUser.uid.toString()) {
                    holder.btnFollow.visibility = View.INVISIBLE
                } else {
                    holder.btnFollow.visibility = View.VISIBLE
                }
            }


            holder.btnFollow.setOnClickListener {
                // 加关注/取消关注
                if (User.staticUser.uid.toString() != "")
                    Thread(Runnable {
                        try {
                            val client = OkHttpClient.Builder()
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(10, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.SECONDS)
                                    .build()
                            val request = Request.Builder()
                                    .url(ServerInfo.userLike(User.staticUser.uid.toString(), item.uid.toString()))
                                    .get()
                                    .build()
                            client.newCall(request).execute()

                            item.isLiked = !item.isLiked
                            runOnUiThread { adapter?.notifyDataSetChanged() }
                        } catch (e: Exception) {
                            Logger.log(e)
                        }

                    }).start()

            }

            if (item.isLiked) {
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
            val circleImageView: CircleImageView = view.findViewById(R.id.circle_image_view)
            val txtUserName: TextView = view.findViewById(R.id.user_name)
            val txtSign: TextView = view.findViewById(R.id.user_sign)
            val btnFollow: TextView = view.findViewById(R.id.btn_follow)
            val itemLayout: View = view.findViewById(R.id.item_layout)
        }
    }

    inner class LikeUser : User() {
        var isLiked = false
    }
}
