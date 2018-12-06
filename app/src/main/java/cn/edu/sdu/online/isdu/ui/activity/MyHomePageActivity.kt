package cn.edu.sdu.online.isdu.ui.activity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CollapsingToolbarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.BaseActivity
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.AccountOp
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.viewpager.NoScrollViewPager
import cn.edu.sdu.online.isdu.ui.fragments.me.MeCommentFragment
import cn.edu.sdu.online.isdu.ui.fragments.me.MePostsFragment
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.UserVerification
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import de.hdodenhof.circleimageview.CircleImageView
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_my_home_page.*
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/13
 *
 * 主页个人主页碎片
 ****************************************************
 */

class MyHomePageActivity : SlideActivity(), View.OnClickListener {

    private var magicIndicator: MagicIndicator? = null
    private var viewPager: NoScrollViewPager? = null
    private val mDataList = listOf("帖子", "评论"/*, "关注"*/) // Indicator 数据
    private val mFragments: List<Fragment> = listOf(MePostsFragment(),
            MeCommentFragment()/*, MePostsFragment()*/) // AppFragment 数组
    private var mViewPagerAdapter: FragAdapter? = null // ViewPager适配器

    private var collapsingToolbar: CollapsingToolbarLayout? = null
    private var toolBar: Toolbar? = null
    private var appBarLayout: AppBarLayout? = null // AppBarLayout实例
    private var myFollower: TextView? = null
    private var followMe: TextView? = null
    private var btnEditProfile: ImageView? = null // 编辑个人资料
    private var txtMyFollower: TextView? = null // 我关注的人
    private var txtFollowMe: TextView? = null // 关注我的人
    private var userName: TextView? = null
    private var btnBack: ImageView? = null
    private var miniCircleImageView: CircleImageView? = null
    private var backgroundImage: ImageView? = null
    private var circleImageView: CircleImageView? = null
    private var txtSign: TextView? = null // 个人签名
    private var btnFollow: TextView? = null // 关注按钮

    private var prevAvatarUrl = ""

    private var user: User? = null
    private var id: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        QMUIStatusBarHelper.translucent(this)

        setContentView(R.layout.activity_my_home_page)

        id = intent.getIntExtra("id", User.load().uid)

        initView()
        initFragments()
        initIndicator()

        loadUserInfo()

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            btn_edit_profile.id -> {
                startActivity(Intent(this, EditProfileActivity::class.java))
            }
            btn_back.id -> {
                finish()
            }
            background_image.id -> {
                startActivity(Intent(this, ViewImageActivity::class.java)
                        .putExtra("url", user?.avatarUrl))
//                        .putExtra("url", ServerInfo.getUserInfo(user?.uid.toString(), "avatar"))
//                        .putExtra("key", "avatar")
//                        .putExtra("isString", false))
            }
            btn_follow.id -> {
                if (User.staticUser.uid.toString() != "")
                    Thread(Runnable {
                        try {
                            val client = OkHttpClient.Builder()
                                    .connectTimeout(10, TimeUnit.SECONDS)
                                    .writeTimeout(10, TimeUnit.SECONDS)
                                    .readTimeout(10, TimeUnit.SECONDS)
                                    .build()
                            val request = Request.Builder()
                                    .url(ServerInfo.userLike(User.staticUser.uid.toString(), id.toString()))
                                    .get()
                                    .build()
                            client.newCall(request).execute()

                            getLiked()
                            getUserLikes()
                        } catch (e: Exception) {
                            Logger.log(e)
                        }
                    }).start()
            }
        }
    }

    /**
     * 获取是否关注
     */
    private fun getLiked() {
        if (!User.isLogin()) {
            btnFollow!!.visibility = View.GONE
        } else {
            if (id != User.staticUser.uid) {
                btnFollow!!.visibility = View.VISIBLE
                NetworkAccess.buildRequest(ServerInfo.getMyLike(User.staticUser.uid.toString()), object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Logger.log(e)
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        try {
                            val str = JSONObject(response?.body()?.string()).getString("obj")
                            val list = str.split("-")
                            if (list.contains(id.toString())) {
                                runOnUiThread {
                                    btnFollow!!.text = "已关注"
                                    btnFollow!!.setTextColor(0xFF717EDB.toInt())
                                    btnFollow!!.setBackgroundResource(R.drawable.purple_stroke_rect_colorchanged)
                                }
                            } else {
                                runOnUiThread {
                                    btnFollow!!.text = "关注"
                                    btnFollow!!.setTextColor(0xFF808080.toInt())
                                    btnFollow!!.setBackgroundResource(R.drawable.text_button_background)
                                }
                            }
                        } catch (e: Exception) {
                            Logger.log(e)
                        }
                    }
                })
            } else {
                btnFollow!!.visibility = View.GONE
            }
        }
    }

    private fun initView() {
        magicIndicator = findViewById(R.id.magic_indicator)
        viewPager = findViewById(R.id.view_pager)
        appBarLayout = findViewById(R.id.appbar_layout)
        btnEditProfile = findViewById(R.id.btn_edit_profile)
        txtMyFollower = findViewById(R.id.my_follower_count)
        txtFollowMe = findViewById(R.id.following_me_count)
        myFollower = findViewById(R.id.my_follower)
        followMe = findViewById(R.id.who_follow_me)
        userName = findViewById(R.id.user_name)
        collapsingToolbar = findViewById(R.id.collapsing_toolbar)
        btnBack = findViewById(R.id.btn_back)
        toolBar = findViewById(R.id.tool_bar)
        miniCircleImageView = findViewById(R.id.mini_circle_image_view)
        backgroundImage = findViewById(R.id.background_image)
        circleImageView = findViewById(R.id.circle_image_view)
        txtSign = findViewById(R.id.txt_sign)
        btnFollow = findViewById(R.id.btn_follow)

        viewPager!!.setAppBarLayout(appBarLayout)

        appBarLayout!!.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            if ((verticalOffset == 0) or (Math.abs(verticalOffset) == Math.abs(appBarLayout!!.totalScrollRange))) {
                viewPager!!.setScroll(true)
            } else {
                viewPager!!.setScroll(false)
            }
            if (Math.abs(verticalOffset) == Math.abs(appBarLayout.totalScrollRange))
                miniCircleImageView!!.visibility = View.VISIBLE
            else
                miniCircleImageView!!.visibility = View.GONE
        })

        toolBar!!.setOnClickListener { appBarLayout!!.setExpanded(true, true) }
        toolBar!!.setOnLongClickListener {
            appBarLayout!!.setExpanded(true, true)
            true
        }


        btnBack!!.setOnClickListener(this)
        btnEditProfile!!.setOnClickListener(this)
//        circleImageView!!.setOnClickListener(this)
        backgroundImage!!.setOnClickListener(this)
        btnFollow!!.setOnClickListener(this)

        layout_my_like.setOnClickListener {
            startActivity(Intent(this, MyLikeActivity::class.java)
                    .putExtra("uid", id.toString()))
        }

        layout_like_me.setOnClickListener {
            startActivity(Intent(this, LikeMeActivity::class.java)
                    .putExtra("uid", id.toString()))
        }

    }

    /**
     * 初始化MagicIndicator导航栏
     */
    private fun initIndicator() {
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getTitleView(p0: Context?, p1: Int): IPagerTitleView {
                val simplePagerTitleView = ColorTransitionPagerTitleView(p0!!)
                simplePagerTitleView.normalColor = 0xFF808080.toInt()
                simplePagerTitleView.selectedColor = 0xFF131313.toInt()
                simplePagerTitleView.text = mDataList[p1]
                simplePagerTitleView.textSize = 18f
                simplePagerTitleView.setOnClickListener { viewPager?.currentItem = p1 }
                return simplePagerTitleView
            }

            override fun getCount(): Int = mDataList.size

            override fun getIndicator(p0: Context?): IPagerIndicator {
                val linePagerIndicator = LinePagerIndicator(p0)
                linePagerIndicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
                linePagerIndicator.lineWidth = UIUtil.dip2px(this@MyHomePageActivity, 10.0).toFloat()
                linePagerIndicator.setColors(0xFF717DEB.toInt())
                return linePagerIndicator
            }
        }

        commonNavigator.isAdjustMode = true
        magicIndicator!!.navigator = commonNavigator

        ViewPagerHelper.bind(magicIndicator, viewPager)
    }

    /**
     * 初始化推荐、关注、热榜和校内相关碎片
     */
    private fun initFragments() {
//        for (fragment in mFragments) {
        (mFragments[0] as MePostsFragment).setUid(id)
        (mFragments[1] as MeCommentFragment).setUid(id)
//        }
        mViewPagerAdapter = FragAdapter(supportFragmentManager)
        viewPager!!.adapter = mViewPagerAdapter
    }

    private fun loadUserInfo() {
        user = User.load(id)
        if (id != User.load().uid) {
            setGuestView()
        } else {
            setNotGuestView()
        }
        NetworkAccess.cache(ServerInfo.getUserInfo(id.toString() + "", "id-nickname-sign-studentnumber-gender-avatar")) {
            success, cachePath ->
            if (success) {
                try {
                    val json = FileUtil.getStringFromFile(cachePath)

                    if (user == null) user = User()

                    if (json != null && json.trim() != "") {
                        try {

                            val jsonObject = JSONObject(json)
                            user!!.nickName = jsonObject.getString("nickname")
                            user!!.selfIntroduce = jsonObject.getString("sign")
                            user!!.studentNumber = jsonObject.getString("studentnumber")
                            user!!.avatarUrl = jsonObject.getString("avatar")
                            user!!.uid = jsonObject.getInt("id")
                            user!!.gender = if (jsonObject.getString("gender") == "男") User.GENDER_MALE
                            else (if (jsonObject.getString("gender") == "女") User.GENDER_FEMALE
                            else User.GENDER_SECRET)
                            user!!.save(this@MyHomePageActivity)

                            runOnUiThread { publishUserInfo() }


                        } catch (e: Exception) {
                            Logger.log(e)
//                        runOnUiThread {
                            val dialog = AlertDialog(this@MyHomePageActivity)
                            dialog.setTitle("错误")
                            dialog.setMessage("未获取到数据")
                            dialog.setCancelable(false)
                            dialog.setCancelOnTouchOutside(false)
                            dialog.setPositiveButton("返回") {
                                dialog.dismiss()
                                finish()
                            }
                            dialog.show()
//                        }
                        }
                    }

                } catch (e: Exception) {
                    Logger.log(e)
                }

            }
        }

        getUserLikes()

        getUserVerification()
    }

    /**
     * 获取关注和被关注的数量
     */
    private fun getUserLikes() {
        NetworkAccess.cache(ServerInfo.getLikeMe(id.toString())) { success, cachePath ->
            if (success) {
                try {
                    val str = JSONObject(FileUtil.getStringFromFile(cachePath)).getString("obj")
                    var count = 0
                    for (i in 0 until str.length) {
                        if (str[i] == '-') count++
                    }
                    runOnUiThread {
                        txtFollowMe!!.text = count.toString()
                    }
                } catch (e: Exception) {}
            }
        }
        NetworkAccess.cache(ServerInfo.getMyLike(id.toString())) { success, cachePath ->
            if (success) {
                try {
                    val str = JSONObject(FileUtil.getStringFromFile(cachePath)).getString("obj")
                    var count = 0
                    for (i in 0 until str.length) {
                        if (str[i] == '-') count++
                    }
                    runOnUiThread {
                        txtMyFollower!!.text = count.toString()
                    }
                } catch (e: Exception) {}
            }
        }

        getLiked()
    }

    private fun publishUserInfo() {
        if (user != null) {
            userName!!.text = user!!.nickName
            txtSign!!.text = "个人签名：${user!!.selfIntroduce}"
            val avatarUrl = user!!.avatarUrl
            if (avatarUrl != null && avatarUrl != "" && avatarUrl != prevAvatarUrl) {
                fillBackgroundImage(avatarUrl)
                fillAvatarImage(avatarUrl)
                prevAvatarUrl = avatarUrl
            }
        }
    }

    /**
     * 隐藏设置、修改信息的按钮
     */
    private fun setGuestView() {
        circleImageView!!.setOnClickListener {
            startActivity(Intent(this, ViewImageActivity::class.java)
                    .putExtra("url", user?.avatarUrl))
        }
        btnEditProfile!!.visibility = View.GONE
//        if (User.staticUser == null ||
//                User.staticUser.studentNumber == null) {
        btnFollow!!.visibility = View.GONE
        if (!User.isLogin()) {
            btnFollow!!.visibility = View.GONE
        } else {
            btnFollow!!.visibility = View.VISIBLE
        }
        followMe!!.text="关注TA的人"
        myFollower!!.text="TA关注的人"
    }

    private fun setNotGuestView() {
        circleImageView!!.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        btnEditProfile!!.visibility = View.VISIBLE
        btnFollow!!.visibility = View.GONE
        followMe!!.text="关注我的人"
        myFollower!!.text="我关注的人"
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        window.decorView.invalidate()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (id != intent!!.getIntExtra("id", User.load().uid)) {
            id = intent.getIntExtra("id", User.load().uid)
            loadUserInfo()

            // 更新帖子和评论
            (mFragments[0] as MePostsFragment).setUid(id)
            (mFragments[1] as MeCommentFragment).setUid(id)
            (mFragments[0] as MePostsFragment).removeAllItems()
            (mFragments[1] as MeCommentFragment).removeAllItems()
            (mFragments[0] as LazyLoadFragment).loadData()
            (mFragments[1] as LazyLoadFragment).loadData()
        }
    }

    private fun fillBackgroundImage(bmp: String?) {
        backgroundImage!!.tag = null
        if (bmp != null) {
            Glide.with(this)
                    .load(bmp)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(40)))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .into(backgroundImage!!)
        } else {
            backgroundImage!!.setImageBitmap(null)
        }

    }

    private fun fillAvatarImage(bmp: String?) {
        if (bmp == null || bmp == "") {
            circleImageView!!.setImageBitmap(null)
            miniCircleImageView!!.setImageBitmap(null)
        } else {
            Glide.with(this).load(bmp)
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(circleImageView!!)
            Glide.with(this)
                    .load(bmp).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .into(miniCircleImageView!!)
        }
    }

    fun getAppBar(): AppBarLayout = appBarLayout!!

    private fun getUserVerification() {
        NetworkAccess.buildRequest(ServerInfo.getUserVerification(id.toString()),
                object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Logger.log(e)
                        Toast.makeText(this@MyHomePageActivity, "网络错误", Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        val resp = response!!.body()?.string()
                        var v = 0
                        try {
                            v = resp!!.toInt()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // 加载用户标示
                        runOnUiThread {
                            UserVerification.setCards(verification_container, v)
                        }
                    }
                })
    }

    /**
     * 用户权限标识
     */
//    private fun getUserVerification() {
//        NetworkAccess.buildRequest(ServerInfo.getUserVerification(id.toString()), object : Callback {
//            override fun onFailure(call: Call, e: IOException) {
//                Logger.log(e)
//                Toast.makeText(this@MyHomePageActivity, "网络错误", Toast.LENGTH_SHORT).show()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val resp = response.body()!!.string()
//                var v = 0
//                try {
//                    v = resp.toInt()
//                } catch (e: Exception) {
//
//                }
//
//            }
//        })
//    }

    /**
     * 自定义ViewPager适配器类
     */
    inner class FragAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return mDataList[position]
        }
    }

    private inner class UserSyncBroadcastReceiver(activity: MyHomePageActivity) :
            BaseActivity.MyBroadcastReceiver(activity) {
        private val activity = activity
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent!!.action == AccountOp.ACTION_USER_LOG_OUT) {
                activity.finish()
            } else if (intent.action == AccountOp.ACTION_SYNC_USER_INFO) {

                if (user == null) user = User()

                if (intent.getStringExtra("json") != null && intent.getStringExtra("json").trim() != "") {
                    try {
                        Thread(Runnable {
                            val jsonObject = JSONObject(intent.getStringExtra("json"))
                            user!!.nickName = jsonObject.getString("nickname")
                            user!!.selfIntroduce = jsonObject.getString("sign")
                            user!!.studentNumber = jsonObject.getString("studentnumber")
                            user!!.avatarUrl = jsonObject.getString("avatar")
                            user!!.uid = jsonObject.getInt("id")
                            user!!.gender = if (jsonObject.getString("gender") == "男") User.GENDER_MALE
                            else (if (jsonObject.getString("gender") == "女") User.GENDER_FEMALE
                            else User.GENDER_SECRET)
                            user!!.save(this@MyHomePageActivity)

//                        AccountOp.getUserAvatar(id)

                            runOnUiThread { publishUserInfo() }

                        }).start()
                    } catch (e: Exception) {
                        Logger.log(e)
//                        runOnUiThread {
                            val dialog = AlertDialog(this@MyHomePageActivity)
                            dialog.setTitle("错误")
                            dialog.setMessage("未获取到数据")
                            dialog.setCancelable(false)
                            dialog.setCancelOnTouchOutside(false)
                            dialog.setPositiveButton("返回") {
                                dialog.dismiss()
                                finish()
                            }
                            dialog.show()
//                        }
                    }
                }


            }
        }
    }

    override fun prepareBroadcastReceiver() {
        if (myBroadcastReceiver == null) {
            val intentFilter1 = IntentFilter(AccountOp.ACTION_USER_LOG_OUT)
            val intentFilter2 = IntentFilter(AccountOp.ACTION_SYNC_USER_INFO)
//            val intentFilter3 = IntentFilter(AccountOp.ACTION_SYNC_USER_AVATAR)
            myBroadcastReceiver = UserSyncBroadcastReceiver(this)
            AccountOp.localBroadcastManager.registerReceiver(myBroadcastReceiver!!,
                    intentFilter1)
            AccountOp.localBroadcastManager.registerReceiver(myBroadcastReceiver!!,
                    intentFilter2)
//            AccountOp.localBroadcastManager.registerReceiver(myBroadcastReceiver!!,
//                    intentFilter3)
        }
    }

    override fun unRegBroadcastReceiver() {
        if (myBroadcastReceiver != null)
            AccountOp.localBroadcastManager.unregisterReceiver(myBroadcastReceiver!!)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState?.getInt("id") != null) {
            id = savedInstanceState.getInt("id")
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt("id", id)
    }
}
