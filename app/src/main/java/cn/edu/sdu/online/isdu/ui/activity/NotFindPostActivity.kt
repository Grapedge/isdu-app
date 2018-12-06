package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import kotlinx.android.synthetic.main.activity_not_find_post.*

class NotFindPostActivity : SlideActivity(), View.OnClickListener{

    private var backBtn : ImageView?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_not_find_post)
        backBtn = btn_back
        backBtn!!.setOnClickListener (this)
    }
    override fun onClick(v: View?) {
        when(v!!.id){
            btn_back.id->{
                finish()
            }
        }
    }

}