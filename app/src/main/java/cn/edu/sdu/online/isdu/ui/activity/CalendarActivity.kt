package cn.edu.sdu.online.isdu.ui.activity


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/6/5
 *
 * 校历活动
 ****************************************************
 */

class CalendarActivity : SlideActivity(), View.OnClickListener {

    private var btnBack: ImageView? = null
    private var imageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
        initView()

//        NetworkAccess.cache(ServerInfo.calanderUrl) {success, cachePath ->
//            if (success) {
//                val bmp = BitmapFactory.decodeFile(cachePath)
//
//                runOnUiThread {
//                    imageView!!.setImageBitmap(bmp)
//                }
//            }
//        }
        Glide.with(MyApplication.getContext()).load(ServerInfo.calanderUrl)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.skipMemoryCacheOf(false))
                .into(imageView!!)

        imageView!!.setOnClickListener {
            startActivity(Intent(this@CalendarActivity, ViewImageActivity::class.java)
                    .putExtra("url", ServerInfo.calanderUrl))
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
        }
    }
    private fun initView() {
        btnBack = findViewById(R.id.btn_back)
        imageView = findViewById(R.id.image_view)

        btnBack!!.setOnClickListener(this)
    }

}
