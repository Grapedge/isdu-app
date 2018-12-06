package cn.edu.sdu.online.isdu.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/5
 *
 * 云盘活动
 ****************************************************
 */

class CloudActivity : SlideActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cloud)

        val dialog = AlertDialog(this)
        dialog.setTitle("功能未开放")
        dialog.setMessage("对不起，“云盘”功能尚未开放")
        dialog.setPositiveButton("返回") {
            dialog.dismiss()
            finish()
        }
        dialog.setCancelable(false)
        dialog.setCancelOnTouchOutside(false)
        dialog.show()
    }
}
