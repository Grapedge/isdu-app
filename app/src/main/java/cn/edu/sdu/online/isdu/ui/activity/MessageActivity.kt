package cn.edu.sdu.online.isdu.ui.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Message
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.View
import android.widget.ImageView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.ui.fragments.message.ChatFragment
import cn.edu.sdu.online.isdu.ui.fragments.message.MessageFragment
import cn.edu.sdu.online.isdu.ui.fragments.message.NotificationFragment
import kotlinx.android.synthetic.main.activity_message.*
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView
import q.rorbin.badgeview.Badge
import q.rorbin.badgeview.QBadgeView
import java.io.Serializable
import java.nio.file.attribute.BasicFileAttributeView

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/15
 *
 * 我的消息页面
 ****************************************************
 */

class MessageActivity : SlideActivity(), View.OnClickListener , Serializable {

    private var backBtn : ImageView?= null
    private val mFragments = listOf(NotificationFragment(), ChatFragment())
    private var mViewPagerAdapter: FragAdapter? = null // ViewPager适配器
    private val mDataList = listOf("通知", "聊天")
    private var viewPager: ViewPager? = null // ViewPager
    private var magicIndicator: MagicIndicator? = null // Magic Indicator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        initView()
        initFragments()
        initIndicator()

        cn.edu.sdu.online.isdu.bean.Message.loadMsgList(this)
    }

    private fun initView() {

        backBtn = findViewById(R.id.btn_back)
        magicIndicator = findViewById(R.id.magic_indicator)
        viewPager = findViewById(R.id.view_pager)

        backBtn!!.setOnClickListener(this)
        viewPager!!.offscreenPageLimit = mDataList.size
    }

    override fun onClick(v : View){
        when (v.id){
            btn_back.id -> {
                finish()
            }
        }
    }

    private fun initFragments() {
        mViewPagerAdapter = FragAdapter(supportFragmentManager, mFragments)
        viewPager!!.adapter = mViewPagerAdapter
    }

    private fun initIndicator() {
        val commonNavigator = CommonNavigator(this@MessageActivity)
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
                linePagerIndicator.lineWidth = UIUtil.dip2px(this@MessageActivity, 10.0).toFloat()
                linePagerIndicator.setColors(0xFF717DEB.toInt())
                return linePagerIndicator
            }
        }

        commonNavigator.isAdjustMode = true
        magicIndicator!!.navigator = commonNavigator

        ViewPagerHelper.bind(magicIndicator, viewPager)
    }

    inner class FragAdapter(fm: FragmentManager, fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
        private val mFragments = fragments

        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return mDataList[position]
        }
    }
}
