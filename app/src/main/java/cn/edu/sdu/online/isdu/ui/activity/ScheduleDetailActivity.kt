package cn.edu.sdu.online.isdu.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.Schedule
import kotlinx.android.synthetic.main.activity_schedule_detail.*

class ScheduleDetailActivity : SlideActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_detail)
        
        btn_back.setOnClickListener {
        	finish()
        }

        txt_name.text = intent.getStringExtra("name")
        txt_location.text = intent.getStringExtra("location")
        txt_time_start.text = intent.getStringExtra("start_time")
        txt_time_end.text = intent.getStringExtra("end_time")

        color_bar.setBackgroundColor(intent.getIntExtra("color", 0xFF717DEB.toInt()))

        val s = intent.getStringExtra("repeat_weeks")

        txt_repeat_weeks.text = "第${s.substring(0, s.length - 1)}周"

    }
}
