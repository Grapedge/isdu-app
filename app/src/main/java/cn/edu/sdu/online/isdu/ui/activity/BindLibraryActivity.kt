package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.util.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class BindLibraryActivity : SlideActivity(), View.OnClickListener {

    private var btnBack: ImageView? = null
    private var txtCardNumber: EditText? = null
    private var txtPassword: EditText? = null
    private var btnBind: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bind_library)

        initView()
    }

    private fun initView() {
        btnBack = findViewById(R.id.btn_back)
        txtCardNumber = findViewById(R.id.card_number)
        txtPassword = findViewById(R.id.card_password)
        btnBind = findViewById(R.id.btn_bind)

        btnBack!!.setOnClickListener(this)
        btnBind!!.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {finish()}
            R.id.btn_bind -> {
                val stuNum = txtCardNumber!!.text.toString()
                val stuPwd = txtPassword!!.text.toString()

                if (stuNum.trim() == "" || stuPwd.trim() == "") {
                    Toast.makeText(this, "校园卡号和密码不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    performLogin(stuNum, stuPwd)
                }
            }
        }
    }


    private fun performLogin(num: String, pwd: String) {
        setEnabled(false)

        val url = ServerInfo.getBindUrl(User.staticUser.uid.toString(),num, pwd)

        NetworkAccess.buildRequest(url, object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    Logger.log(e)
                    setEnabled(true)
                    Toast.makeText(this@BindLibraryActivity, "网络错误", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                runOnUiThread {
                    setEnabled(true)
                }

                val json = response?.body()?.string()
                try {
                    val jsonObj = JSONObject(json)
                    Log.d("onResponse", json)
                    if (!jsonObj.isNull("code") && jsonObj.getString("code") == "2") {
                        runOnUiThread {
                            Toast.makeText(this@BindLibraryActivity, "卡号或密码错误", Toast.LENGTH_SHORT).show()
                        }

                    } else {
                        finish()
                        runOnUiThread {
                            Toast.makeText(this@BindLibraryActivity, "绑定成功", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                    runOnUiThread {
                        Toast.makeText(this@BindLibraryActivity, "网络错误\n服务器无响应", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun setEnabled(b: Boolean) {
        txtCardNumber!!.isEnabled = b
        txtPassword!!.isEnabled = b
        btnBind!!.isEnabled = b
        if (b) {
            btnBind!!.text = "绑定"
        } else {
            btnBind!!.text = "绑定中..."
        }
    }
}
