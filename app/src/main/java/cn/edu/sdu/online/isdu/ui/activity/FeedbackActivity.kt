package cn.edu.sdu.online.isdu.ui.activity

import android.os.Build
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.View
import android.widget.*
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.util.Logger
import kotlinx.android.synthetic.main.activity_feedback.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/8
 *
 * 设置Activity
 *
 * #添加云同步
 ****************************************************
 */

class FeedbackActivity : SlideActivity(), View.OnClickListener{
    private var btnFeedback: TextView?=null
    private var btnBack: ImageView? = null
    private var textQQ: TextInputEditText? = null
    private var textPhone: TextInputEditText? = null
    private var textFeedback: EditText?= null
    private var txtDevice: TextView? = null
    private var txtSystem: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        initView()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.btn_feedback -> {
                val feedback = textFeedback!!.text.toString()
                val qq = textQQ!!.text.toString()
                val phone = textPhone!!.text.toString()
//                Toast.makeText(this, "反馈成功",Toast.LENGTH_SHORT).show()
                val keys = listOf("content", "device", "os", "qq", "phone")
                val values = listOf(feedback, txtDevice!!.text.toString(),
                        txtSystem!!.text.toString(), qq, phone)
                NetworkAccess.buildRequest(ServerInfo.getFeedbackUrl(), keys, values,
                        object : Callback {
                            override fun onFailure(call: Call?, e: IOException?) {
                                Logger.log(e)
                                runOnUiThread {
                                    Toast.makeText(this@FeedbackActivity, "网络错误",Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onResponse(call: Call?, response: Response?) {
                                runOnUiThread {
                                    Toast.makeText(this@FeedbackActivity, "反馈成功",Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }
                        })
            }
        }
    }

    private fun initView() {
        btnBack = findViewById(R.id.btn_back)
        btnFeedback = findViewById(R.id.btn_feedback)
        textQQ = findViewById(R.id.feedback_qq)
        textPhone = findViewById(R.id.feedback_phone)
        textFeedback = findViewById(R.id.et_feedback)

        txtDevice = device
        txtSystem = system

        textFeedback!!.setSingleLine(false)
        btnBack!!.setOnClickListener(this)
        btnFeedback!!.setOnClickListener(this)

        txtDevice!!.text = "机型：${Build.BRAND} ${Build.DEVICE}"
        txtSystem!!.text = "系统：安卓API${Build.VERSION.SDK_INT}"
    }
}
