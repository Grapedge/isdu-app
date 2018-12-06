package cn.edu.sdu.online.isdu.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.Spanned
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.app.ThreadPool
import cn.edu.sdu.online.isdu.bean.*
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.OptionDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.ui.design.popupwindow.BasePopupWindow
import cn.edu.sdu.online.isdu.ui.design.xrichtext.RichTextEditor
import cn.edu.sdu.online.isdu.ui.design.xrichtext.RichTextView
import cn.edu.sdu.online.isdu.util.*
import com.bumptech.glide.Glide
import cn.edu.sdu.online.isdu.util.history.HistoryRecord
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qmuiteam.qmui.span.QMUITouchableSpan
import com.qmuiteam.qmui.widget.textview.QMUISpanTouchFixTextView
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_post_detail.*
import kotlinx.android.synthetic.main.activity_view_image.view.*
import kotlinx.android.synthetic.main.edit_area.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LifeDetailActivity : SlideActivity(), View.OnClickListener {

    private var url: String = "" // URL

    private var txtTitle: TextView? = null
    private var txtContent: RichTextView? = null
    private var circleImageView: CircleImageView? = null // 发帖者头像
    private var txtNickname: TextView? = null
    private var txtDate: TextView? = null
    private var posterLayout: View? = null
    private var btnOptions: View? = null
    private var txtLike: TextView? = null
    private var txtFlag: TextView? = null

    //private var btnComment: View? = null
    //private var btnLike: ImageView? = null
    private var btnCollect: ImageView? = null

    //private var commentLine: TextView? = null

    private var editArea: View? = null // 隐藏的编辑区域
    private var editText: EditText? = null // 编辑区域的文本框
    private var btnSend: View? = null


////    private var postCommentList = ArrayList<PostComment>()
//    private var userIdMap: HashMap<String, String> = HashMap()
//    private var userNicknameMap: HashMap<String, String> = HashMap()

    private var isLike = false // 是否点赞
    private var showCollectToast = false  // 是否显示已经收藏

    private var lifeId = 0
    private var fatherCommentId = -1 // 父评论ID
    private var tag = ""
    //private var commentList = LinkedList<PostComment>()
    private var toUserId = "0"

    private var window: BasePopupWindow? = null // 右上角点击弹出窗口

    private val life = Life() // 该浏览页面的帖子实例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_post_detail)

        lifeId = intent.getIntExtra("id", 0)
        url = ServerInfo.getLife(lifeId)
//        uid = intent.getStringExtra("uid") ?: ""
//        title = intent.getStringExtra("title") ?: ""
//        time = intent.getLongExtra("time", 0L)
        tag = intent.getStringExtra("tag") ?: ""

        if (User.staticUser == null) User.staticUser = User.load()

        initView()

        //写数据库纪录浏览
        life.lifeId = lifeId

        getLifeData()



    }

    private fun initView() {
        txtTitle = txt_title
        txtContent = rich_text_content
        circleImageView = circle_image_view
        txtNickname = txt_nickname
        posterLayout = poster_layout
        txtDate = post_date
        editText = edit_text

        btnCollect = btn_collect
        btnOptions = btn_options


        txtLike = like_count
        txtFlag = title_flag

        btn_back.setOnClickListener { finish() }

        title_text_view.setOnClickListener {
            scroll_view.scrollTo(0, 0)
        }

        btnSend!!.setOnClickListener(this)
        posterLayout!!.setOnClickListener(this)
        btnCollect!!.setOnClickListener(this)

        editText!!.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editArea!!.visibility = View.GONE
                operate_bar.visibility = View.VISIBLE
                hideSoftKeyboard()
            }
        }

        comment_blank_view.setOnClickListener {
            editText!!.clearFocus()

        }


    }

    override fun onBackPressed() {
        if (editArea!!.visibility == View.VISIBLE) {
            editArea!!.clearFocus()
        } else super.onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {


            btn_options.id -> {
                editArea!!.clearFocus()
                // 弹出弹窗
                window = object : BasePopupWindow(this, R.layout.popup_post_detail,
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT) {

                    override fun initEvent() {

                        getContentView().findViewById<View>(R.id.btn_delete).setOnClickListener { _ ->
                            popupWindow.dismiss()
                            editArea!!.clearFocus()
                            val dialog = AlertDialog(this@LifeDetailActivity)
                            dialog.setTitle("删除帖子")
                            dialog.setMessage("确定要删除帖子吗？该操作不可逆")
                            dialog.setPositiveButton("删除") {_ ->
                                val dialog1 = ProgressDialog(this@LifeDetailActivity, false)
                                dialog1.setMessage("正在删除")
                                dialog1.setButton(null, null)
                                dialog1.setCancelable(false)
                                dialog1.show()
                                dialog.dismiss()
                                if (User.isLogin()) {
                                    NetworkAccess.buildRequest(ServerInfo.deleteLife, listOf("lifeId", "userId"), listOf(lifeId.toString(), User.staticUser.uid.toString()),
                                            object : Callback {
                                                override fun onFailure(call: Call?, e: IOException?) {
                                                    Logger.log(e)
                                                    runOnUiThread {
                                                        dialog1.dismiss()
                                                        Toast.makeText(this@LifeDetailActivity, "删除失败", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onResponse(call: Call?, response: Response?) {
                                                    runOnUiThread {
                                                        dialog1.dismiss()
                                                    }
                                                    try {
                                                        val str = response?.body()?.string()
                                                        if (str != null && str.contains("success")) {
                                                            runOnUiThread {
                                                                Toast.makeText(this@LifeDetailActivity,
                                                                        "删除成功", Toast.LENGTH_SHORT).show()

                                                                WeakReferences.postViewableWeakReference?.get()?.removeItem(lifeId)

                                                                finish()
                                                            }
                                                        } else {
                                                            runOnUiThread {
                                                                Toast.makeText(this@LifeDetailActivity,
                                                                        "删除失败", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        Logger.log(e)
                                                        runOnUiThread {
                                                            Toast.makeText(this@LifeDetailActivity,
                                                                    "网络错误，删除失败", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            })
                                }

                            }



                            dialog.setNegativeButton("取消") {
                                dialog.dismiss()
                            }

                            dialog.show()
                        }

                        getContentView().findViewById<View>(R.id.btn_cancel).setOnClickListener {
                            popupWindow.dismiss()
                        }
                    }

                    override fun initWindow() {
                        super.initWindow()
                        val instance = popupWindow
                        instance.setOnDismissListener {
                            val lp = getWindow().attributes
                            lp.alpha = 1f
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                            getWindow().attributes = lp
                        }
                    }
                }
                window!!.popupWindow.animationStyle = R.style.popupAnimTranslate
                window!!.showAtLocation(base_view, Gravity.BOTTOM, 0, 0)
                val lp = getWindow().attributes
                lp.alpha = 0.5f
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                getWindow().attributes = lp
            }
            poster_layout.id -> {
                if (life.uid != "") startActivity(Intent(this, MyHomePageActivity::class.java)
                        .putExtra("id", life.uid.toInt()))
            }
            /*btn_like.id -> {
//                if (User.staticUser != null &&
//                        User.staticUser.studentNumber != null)
                if (User.isLogin())
                    NetworkAccess.buildRequest(ServerInfo.likePost + "?postId=$lifeId&userId=${User.staticUser.uid}",
                            object : Callback {
                                override fun onFailure(call: Call?, e: IOException?) {
                                    Logger.log(e)
                                    runOnUiThread {
                                        Toast.makeText(this@LifeDetailActivity, "点赞失败", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onResponse(call: Call?, response: Response?) {
                                    try {
                                        runOnUiThread {
                                            isLike = !isLike
                                            getLikeNumber()
                                        }
                                    } catch (e: Exception) {
                                        Logger.log(e)
                                    }
                                }
                            })
            }
            btn_collect.id -> {
                showCollectToast = true
                NetworkAccess.buildRequest(ServerInfo.collectPost + "?postId=$lifeId&userId=${User.staticUser.uid}",
                        object : Callback {
                            override fun onFailure(call: Call?, e: IOException?) {
                                Logger.log(e)
                                runOnUiThread {
                                    Toast.makeText(this@LifeDetailActivity, "收藏失败", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onResponse(call: Call?, response: Response?) {
                                try {
                                    runOnUiThread {
                                        getIsCollect()
                                    }
                                } catch (e: Exception) {
                                    Logger.log(e)
                                }
                            }
                        })
            }*/
        }
    }

    private fun showSoftKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(editText, 0)
    }

    private fun hideSoftKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(editText!!.windowToken, 0)
    }

    /*private fun getLikeNumber() {
        NetworkAccess.buildRequest(ServerInfo.getIsLike(lifeId, User.staticUser.uid.toString()), object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Logger.log(e)
                isLike = false
            }

            override fun onResponse(call: Call?, response: Response?) {
                try {
                    var str = response?.body()?.string()
                    isLike = str == "true"
                    runOnUiThread {
                        btnLike!!.setImageResource(if (isLike) R.drawable.ic_like_yes else R.drawable.ic_like_no)
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }
            }
        })
        NetworkAccess.buildRequest(ServerInfo.getLikeNumber(lifeId), object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Logger.log(e)
                runOnUiThread {
                    txtLike!!.text = "点赞 0 次"
                    life.likeNumber = 0
                    HistoryRecord.newHistory(life)
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                try {
                    val str = response?.body()?.string()
                    runOnUiThread {
                        val strInt = str!!.toInt()
                        var des = ""
                        if (strInt < 1000)
                            des = strInt.toString()
                        else if (strInt < 10000) {
                            des = "${(Math.floor((strInt / 1000).toDouble()))} 千"
                        } else {
                            des = "${(Math.floor((strInt / 10000).toDouble()))} 万"
                        }
                        txtLike!!.text = "点赞 $des 次"
//                        btnLike!!.setImageResource(if (isLike) R.drawable.ic_like_yes else R.drawable.ic_like_no)

                        life.likeNumber = strInt
                        HistoryRecord.newHistory(life)
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }
            }
        })
    }*/

    /**
     * 获取帖子内容
     */
    private fun getLifeData() {
        NetworkAccess
                .cache(url) { success, cachePath ->
                    try {
                        if (success) {
                            if (FileUtil.getStringFromFile(cachePath) != "") {
                                val json = JSONObject(FileUtil.getStringFromFile(cachePath))

                                val data = JSONObject(json.getString("obj"))
                                val editDataList = ArrayList<RichTextEditor.EditData>()
                                val content = JSONArray(data.getString("content"))


                                life.title = data.getString("title")
                                life.time = data.getString("time").toLong()
                                life.uid = data.getString("userId")
                                life.tag = if (data.has("tag")) data.getString("tag") else ""

                                getUserData(life.uid)

                                // 获取是否为管理员
                                val isAdmin = (User.staticUser.userVerification and 0x01) == 0x01

                                for (i in 0 until content.length()) {
                                    val obj = content.getJSONObject(i)
                                    val data = RichTextEditor.EditData()
                                    if (obj.getInt("type") == 0) {
                                        data.imageName = obj.getString("content")
                                        if (data.inputStr != "" && (life.content == null || life.content == ""))
                                            life.content = "[图片]"
                                    } else {
                                        data.inputStr = obj.getString("content")
                                        if (data.inputStr != "" && (life.content == null || life.content == ""))
                                            life.content = data.inputStr
                                    }
                                    editDataList.add(data)
                                }

                                // 写入浏览记录
                                life.onScan()

                                runOnUiThread {

                                    //                                    if (User.staticUser.studentNumber == null) {
                                    if (!User.isLogin()) {
                                        // 未登录
                                        operate_bar!!.visibility = View.GONE
                                        btnOptions!!.visibility = View.INVISIBLE
                                    } else if (User.staticUser.uid.toString() == life.uid) {
                                        // 本用户的帖子
                                        operate_bar!!.visibility = View.VISIBLE
                                        btnOptions!!.setOnClickListener(this)
                                        btnOptions!!.visibility = View.VISIBLE
                                    } else if (isAdmin) {
                                        // 管理员身份浏览
                                        operate_bar!!.visibility = View.VISIBLE
                                        btnOptions!!.setOnClickListener(this)
                                        btnOptions!!.visibility = View.VISIBLE
                                    } else {
                                        btnOptions!!.visibility = View.INVISIBLE
                                        operate_bar!!.visibility = View.VISIBLE
                                    }

                                    txtTitle!!.text = life.title
                                    txtDate!!.text = "发表于 ${DateCalculate.getExpressionDate(life.time)}"

                                    if (life.tag != null && life.tag.isNotEmpty()) {
                                        txtFlag!!.visibility = View.VISIBLE
                                        txtFlag!!.text = life.tag
                                    }

                                    txtContent!!.setData(editDataList)

                                    txtContent!!.setOnRtImageClickListener {imagePath ->
                                        if (imagePath.startsWith("http")) {
                                            // 网络图片
                                            startActivity(Intent(this@LifeDetailActivity, ViewImageActivity::class.java)
                                                    .putExtra("url", imagePath))
                                        } else {
                                            // 本地图片
                                        }
                                    }



                                }
                            }

                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "获取内容出错", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
    }

    /**
     * 获取发帖用户的信息
     */
    private fun getUserData(id: String) {
        NetworkAccess.cache(ServerInfo.getUserInfo(id, "nickname"), "nickname") { success, cachePath ->
            if (success) {
                val obj = FileUtil.getStringFromFile(cachePath)
                runOnUiThread { txtNickname!!.text = obj }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show()
                }

            }
        }

        NetworkAccess.cache(ServerInfo.getUserInfo(id, "avatar"), "avatar") { success, cachePath ->
            if (success) {
                val obj = FileUtil.getStringFromFile(cachePath)
//                val bmp = ImageManager.convertStringToBitmap(obj)
                runOnUiThread {
                    Glide.with(MyApplication.getContext())
                            .load(obj)
                            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .into(circleImageView!!)
//                    circleImageView!!.setImageBitmap(bmp)
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "获取用户信息失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 获取是否收藏
     */
    /*private fun getIsCollect() {
        NetworkAccess.buildRequest(ServerInfo.getIsCollect(lifeId, User.staticUser.uid.toString()), object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Logger.log(e)
            }

            override fun onResponse(call: Call?, response: Response?) {
                try {
                    val str = response?.body()?.string()
                    runOnUiThread {
                        btnCollect!!.setImageResource(if (str == "true") R.drawable.ic_collect_yes else R.drawable.ic_collect_no)
                        if ((str == "true") && showCollectToast)
                            Toast.makeText(this@LifeDetailActivity, "收藏成功", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }
            }
        })
    }*/


    /**
     * 获取帖子内容
     */
    private fun getCommentContent(id: String): JSONObject {
        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build()
        val formBody = FormBody.Builder().add("id", id).build()
        val request = Request.Builder()
                .url(ServerInfo.getComments())
                .post(formBody)
                .build()
        val response = client.newCall(request).execute()

        return JSONObject(response?.body()?.string()).getJSONObject("obj")
    }

    /**
     * 获取评论
     * 一次性从服务器获取全部评论
     *
     * @param index 评论在list中的序号
     * @param list 评论ID列表
     */



    private fun setLifeTag(tag: String) {
        if (User.isLogin()) {
            NetworkAccess.buildRequest(ServerInfo.setPostTag(lifeId, tag, User.staticUser.uid.toString()),
                    object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Logger.log(e)
                            runOnUiThread {
                                Toast.makeText(this@LifeDetailActivity, "操作失败", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call, response: Response) {
                            try {
                                val obj = JSONObject(response.body()?.string())
                                if (obj.getString("status") == "success") {
                                    runOnUiThread {
                                        Toast.makeText(this@LifeDetailActivity, "操作成功", Toast.LENGTH_SHORT).show()
                                        getLifeData()
                                    }
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@LifeDetailActivity, "操作失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                Logger.log(e)
                                runOnUiThread {
                                    Toast.makeText(this@LifeDetailActivity, "操作失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
        }
    }





         fun getItemId(position: Int): Long = position.toLong()



        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemLayout = view.findViewById<View>(R.id.item_layout)
            val circleImageView = view.findViewById<CircleImageView>(R.id.circle_image_view)
            val txtNickname = view.findViewById<TextView>(R.id.txt_nickname)
            val txtTime = view.findViewById<TextView>(R.id.txt_time)
            val txtContent = view.findViewById<TextView>(R.id.txt_content)
            val txtReply = view.findViewById<QMUISpanTouchFixTextView>(R.id.reply_comment)
            val txtPostOwner = view.findViewById<TextView>(R.id.txt_post_owner)
        }
    }


