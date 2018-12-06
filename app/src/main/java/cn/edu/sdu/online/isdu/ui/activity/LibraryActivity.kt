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
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.ui.fragments.LibrarySeatFragment
import cn.edu.sdu.online.isdu.ui.fragments.MyBookFragment
import kotlinx.android.synthetic.main.activity_library.*
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
 * Last Modifier: Cola_Mentos
 * Last Modify Time: 2018/7/16
 *
 * 图书馆活动
 ****************************************************
 */

class LibraryActivity : SlideActivity(), View.OnClickListener{

    private var backBtn : ImageView ?= null
    private val mFragments = listOf(MyBookFragment(),LibrarySeatFragment())
    private var mViewPagerAdapter: LibraryActivity.FragAdapter? = null // ViewPager适配器
    private val mDataList = listOf("馆藏查询", "余座查询")
    private var viewPager: ViewPager? = null // ViewPager
    private var magicIndicator: MagicIndicator? = null // Magic Indicator
    private var progressDialog : ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        initView()
        initFragments()
        initIndicator()

    }

    /**
     * 初始化view
     */
    private fun initView(){


        backBtn = btn_back
        magicIndicator = magic_indicator
        viewPager = view_pager

        backBtn!!.setOnClickListener(this)
        viewPager!!.offscreenPageLimit = mDataList.size // 设置ViewPager的预加载页面数量，防止销毁


    }

    override fun onClick(v : View){
       when (v.id){
           btn_back.id -> {
               finish()
           }
       }
    }

    /**
     * 初始化馆藏查询和余座查询碎片
     */
    private fun initFragments() {
        mViewPagerAdapter = LibraryActivity.FragAdapter(supportFragmentManager, mFragments)
        viewPager!!.adapter = mViewPagerAdapter
    }

    /**
     * 初始化MagicIndicator导航栏
     */
    private fun initIndicator() {
        val commonNavigator = CommonNavigator(this@LibraryActivity)
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
                linePagerIndicator.lineWidth = UIUtil.dip2px(this@LibraryActivity, 10.0).toFloat()
                linePagerIndicator.setColors(0xFF717DEB.toInt())
                return linePagerIndicator
            }
        }

        commonNavigator.isAdjustMode = true
        magicIndicator!!.navigator = commonNavigator

        ViewPagerHelper.bind(magicIndicator, viewPager)
    }


    /**
     * 自定义ViewPager适配器类
     */
    class FragAdapter(fm: FragmentManager, fragments: List<Fragment>) : FragmentPagerAdapter(fm) {
        private val mFragments = fragments
        private val mDataList = listOf("馆藏查询", "余座查询") // Indicator 数据

        override fun getItem(position: Int): Fragment = mFragments[position]

        override fun getCount(): Int = mFragments.size

        override fun getPageTitle(position: Int): CharSequence? {
            return mDataList[position]
        }
    }






}
