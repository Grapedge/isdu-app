package cn.edu.sdu.online.isdu.ui.activity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.BuildConfig
import cn.edu.sdu.online.isdu.app.SlideActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : SlideActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        version_name.text = BuildConfig.VERSION_NAME
        btn_claim.setOnClickListener {
            startActivity(Intent(this, ClaimActivity::class.java))
        }
        btn_back.setOnClickListener { finish() }
    }
}
