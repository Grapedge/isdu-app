package cn.edu.sdu.online.isdu.ui.activity

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.ImageView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.ui.fragments.download.DownloadedFragment
import cn.edu.sdu.online.isdu.ui.fragments.download.DownloadingFragment
import kotlinx.android.synthetic.main.activity_download.*
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.ViewPagerHelper
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/15
 *
 * 下载内容展示Activity
 ****************************************************
 */


class DownloadActivity : SlideActivity(), View.OnClickListener {

    private var btnBack: ImageView? = null
    private val mDataList = listOf("正在下载", "下载完成")
    private var magicIndicator: MagicIndicator? = null
    private var mViewPager: ViewPager? = null

    private var mViewPagerAdapter: FragAdapter? = null
    val mFragments = listOf(DownloadingFragment(), DownloadedFragment())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download)

        initView()
        initFragments()
        initMagicIndicator()
    }

    private fun initView() {
        btnBack = btn_back
        magicIndicator = magic_indicator
        mViewPager = view_pager

        btnBack!!.setOnClickListener(this)
    }


    private fun initMagicIndicator() {
        val commonNavigator = CommonNavigator(this)
        commonNavigator.adapter = object : CommonNavigatorAdapter() {
            override fun getTitleView(p0: Context?, p1: Int): IPagerTitleView {
                val simplePagerTitleView = ColorTransitionPagerTitleView(p0!!)
                simplePagerTitleView.normalColor = 0xFF808080.toInt()
                simplePagerTitleView.selectedColor = 0xFF131313.toInt()
                simplePagerTitleView.text = mDataList[p1]
                simplePagerTitleView.textSize = 16f
                simplePagerTitleView.setOnClickListener { mViewPager?.currentItem = p1 }
                return simplePagerTitleView
            }

            override fun getCount(): Int = mDataList.size

            override fun getIndicator(p0: Context?): IPagerIndicator {
                val linePagerIndicator = LinePagerIndicator(p0)
                linePagerIndicator.mode = LinePagerIndicator.MODE_WRAP_CONTENT
                linePagerIndicator.lineWidth = UIUtil.dip2px(this@DownloadActivity,
                        10.0).toFloat()
                linePagerIndicator.setColors(0xFF717DEB.toInt())
                return linePagerIndicator
            }
        }

        commonNavigator.isAdjustMode = true
        magicIndicator!!.navigator = commonNavigator

        ViewPagerHelper.bind(magicIndicator, mViewPager)
    }

    /**
     * 初始化推荐、关注、热榜和校内相关碎片
     */
    private fun initFragments() {
        mViewPagerAdapter = FragAdapter(supportFragmentManager, mFragments)
        mViewPager!!.adapter = mViewPagerAdapter
    }

    /**
     * 自定义ViewPager适配器类
     */
    class FragAdapter(fm: FragmentManager, fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
        private val mFragments = fragments
        private val mDataList = listOf("正在下载", "下载完成") // Indicator 数据

        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return mDataList[position]
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            btn_back.id -> {
                finish()
            }

        }
    }

    override fun prepareBroadcastReceiver() {

    }

    override fun unRegBroadcastReceiver() {

    }
}
