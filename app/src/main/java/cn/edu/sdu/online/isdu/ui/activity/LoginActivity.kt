package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.AccountOp
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.Security
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class LoginActivity : SlideActivity(), View.OnClickListener {

    private var btnBack: ImageView? = null
    private var txtStudentNumber: EditText? = null
    private var txtPassword: EditText? = null
    private var btnLogin: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initView()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btn_back)
        txtStudentNumber = findViewById(R.id.student_number)
        txtPassword = findViewById(R.id.password)
        btnLogin = findViewById(R.id.btn_login)

        btnBack!!.setOnClickListener(this)
        btnLogin!!.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {finish()}
            R.id.btn_login -> {
                val stuNum = txtStudentNumber!!.text.toString()
                val stuPwd = txtPassword!!.text.toString()

                if (stuNum.trim() == "" || stuPwd.trim() == "") {
                    Toast.makeText(this, "学号和密码不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    performLogin(stuNum, Security.encodeByMD5(stuPwd))
                }
            }
        }
    }

    /**
     * 登录操作
     *
     * @param num  学号
     * @param pwd  MD5加密后密码
     */
    private fun performLogin(num: String, pwd: String) {
        setEnabled(false)

        val url = ServerInfo.getUrlLogin(num, pwd)

        NetworkAccess.buildRequest(url, object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Logger.log(e)
                    setEnabled(true)
                    Toast.makeText(this@LoginActivity, "网络错误", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                runOnUiThread {
                    setEnabled(true)
                }

                val json = response?.body()?.string()
                try {
                    val jsonObj = JSONObject(json)

                    if (!jsonObj.isNull("status") && jsonObj.getString("status") == "failed") {
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "学号或密码错误", Toast.LENGTH_SHORT).show()
                        }
                    } else if (!jsonObj.isNull("status") && jsonObj.getInt("status") == 500) {
                        // 教务系统崩了，后台返回500
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "服务器无响应", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // 初始化用户缓存
                        User.staticUser.studentNumber = num
                        User.staticUser.passwordMD5 = pwd
                        AccountOp.syncUserInformation(jsonObj) // 同步用户信息

                        finish()
                        runOnUiThread {
                            Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        }

                    }
                } catch (e: Exception) {
                    Logger.log(e)
                    runOnUiThread {
                        Toast.makeText(this@LoginActivity, "网络错误\n服务器无响应", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun setEnabled(b: Boolean) {
        txtStudentNumber!!.isEnabled = b
        txtPassword!!.isEnabled = b
        btnLogin!!.isEnabled = b
        if (b) {
            btnLogin!!.text = "登录"
        } else {
            btnLogin!!.text = "登录中..."
        }
    }
}
