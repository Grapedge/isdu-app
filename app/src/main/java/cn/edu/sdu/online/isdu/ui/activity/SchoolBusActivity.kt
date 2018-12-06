package cn.edu.sdu.online.isdu.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.ui.design.button.SchoolImageButton
import kotlinx.android.synthetic.main.activity_school_bus.*

/**
 ****************************************************
 * @author zsj
 * Last Modifier: Cola_Mentos
 * Last Modify Time: 2018/7/12
 *
 * 校车活动
 ****************************************************
 */

class SchoolBusActivity : SlideActivity() , View.OnClickListener{

    private var clearBtn : Button ?= null
    private var searchBtn : Button ?= null
    private var workdayBtn : Button ?= null
    private var non_workdayBtn : Button ?= null
    private var backBtn : ImageView ?= null
    private var searchNum : Int = 0
    private var fromP : Int = 0
    private var toP : Int = 0
    private val xqBtn : Array<SchoolImageButton?> = arrayOfNulls(10)
    private val xqName = arrayOf("","中心校区","洪家楼校区","趵突泉校区","软件园校区","兴隆山校区","千佛山校区")
    private var tipText : TextView ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_school_bus)
        initView()
    }

    /**
     * 初始化view
     */
    private fun initView(){
        clearBtn = btn_clear
        searchBtn = btn_search
        workdayBtn = workday
        non_workdayBtn = non_workday
        tipText = tips
        backBtn = btn_back
        xqBtn[1] = btn_zhongxin
        xqBtn[2] = btn_hongjialou
        xqBtn[3] = btn_baotuquan
        xqBtn[4] = btn_ruanjianyuan
        xqBtn[5] = btn_xinglongshan
        xqBtn[6] = btn_qianfoshan
        clearBtn!!.setOnClickListener(this)
        searchBtn!!.setOnClickListener(this)
        workdayBtn!!.setOnClickListener(this)
        non_workdayBtn!!.setOnClickListener(this)
        backBtn!!.setOnClickListener(this)
        for (i in 1..6){
            xqBtn[i]!!.setOnClickListener(this)
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
        /**
         * 重置按钮
         */
            btn_clear.id->{
                fromP=0
                toP=0
                for (i in 1..6){
                    xqBtn[i]!!.setBacColor(resources.getColor(R.color.colorWhite))
                    xqBtn[i]!!.setText(xqName[i])
                    xqBtn[i]!!.setColor(resources.getColor(R.color.colorPrimaryText))
                }
                tipText!!.text=""
            }
        /**
         * 查询按钮
         */
            btn_search.id->{
                if (fromP == 0 && toP == 0){
                    tipText!!.text = "请选择起点和终点"
                }
                else if (toP == 0){
                    tipText!!.text = "请选择终点"
                }
                else if (fromP == 0){
                    tipText!!.text = "请选择起点"
                }
                else {
                    tipText!!.text = ""
                    val intent = Intent(this,SchoolBusTableActivity::class.java)
                    intent.putExtra("searchNum",searchNum)
                    intent.putExtra("fromP",fromP)
                    intent.putExtra("toP",toP)
                    startActivity(intent)
                }
            }
            workday.id->{
                searchNum = 0
            }
            non_workday.id->{
                searchNum = 1
            }
            btn_back.id->{
                finish()
            }
        /**
         * 校区选择按钮
         */
            else ->{
                for (i in 1..6){
                    if (v.id == xqBtn[i]!!.id){
                        //将当前选择的校区背景变成灰色
                        xqBtn[i]!!.setBacColor(resources.getColor(R.color.colorThemeGrey))
                        //如果没有选择出发地，并且当前点击的校区之前没有被选中，那么久作为出发地，否则取消选中
                        if (fromP == 0) {
                            if (toP != i) {
                                fromP = i
                                xqBtn[i]!!.setText("从  " + xqName[i])
                                xqBtn[i]!!.setColor(resources.getColor(R.color.colorPurpleDark))
                            }
                            else {
                                changeAppearance(i)
                                toP = 0
                            }
                        }
                        //如果当前校区之前被选为出发地了，那么就取消选择
                        else if (i == fromP){
                            changeAppearance(i)
                            fromP=0
                        }
                        //如果当前校区之前被选为目的地了，那么就取消选择
                        else if(i == toP){
                            changeAppearance(i)
                            toP=0
                        }
                        else {
                            if (toP != 0){
                                changeAppearance(fromP)
                                fromP = toP
                                xqBtn[fromP]!!.setColor(resources.getColor(R.color.colorPurpleDark))
                                xqBtn[fromP]!!.setText("从  "+xqName[fromP])
                            }
                            toP = i
                            xqBtn[i]!!.setText("到  "+xqName[i])
                            xqBtn[i]!!.setColor(resources.getColor(R.color.colorPurpleDark))
                        }
                    }
                }
            }

        }

    }

    /**
     * 将校区的按钮恢复原样
     *
     * @param i 校区编号
     */
    private fun changeAppearance(i : Int){
        xqBtn[i]!!.setBacColor(resources.getColor(R.color.colorWhite))
        xqBtn[i]!!.setText(xqName[i])
        xqBtn[i]!!.setColor(resources.getColor(R.color.colorPrimaryText))
    }
}

