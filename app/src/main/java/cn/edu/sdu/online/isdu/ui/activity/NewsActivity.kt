package cn.edu.sdu.online.isdu.ui.activity

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.News
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.PixelUtil
import cn.edu.sdu.online.isdu.util.download.IDownloadItem

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/15
 *
 * 新闻具体内容展示活动
 ****************************************************
 */

class NewsActivity : SlideActivity() {

    private var extraSection: LinearLayout? = null
    private var btnBack: ImageView? = null
    private var newsTitle: TextView? = null
    private var newsSection: TextView? = null
    private var newsDate: TextView? = null
    private var newsContent: TextView? = null

    private var newsUrl: String? = null

    private var news: News? = null

    private var extraAdded: MutableList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        newsUrl = intent.getStringExtra("url")

        initView()

        fetchNews()
    }

    private fun initView() {
        extraSection = findViewById(R.id.extra_section)
        btnBack = findViewById(R.id.btn_back)
        newsTitle = findViewById(R.id.news_title)
        newsSection = findViewById(R.id.news_section)
        newsDate = findViewById(R.id.news_date)
        newsContent = findViewById(R.id.news_content)

        btnBack!!.setOnClickListener { finish() }
    }


    private fun publish() {
        newsTitle!!.text = news!!.title
        newsSection!!.text = news!!.section
        newsDate!!.text = news!!.date
        newsContent!!.text = news!!.newsContent
        if (news!!.extras == null || news!!.extras.isEmpty()) {
            extraSection!!.visibility = View.GONE
        } else {
            for (i in 0 until news!!.extras.size) {
                addExtra(news!!.extras[i], news!!.extraUrl[i])
            }
        }
    }

    /**
     * 获取新闻完整内容
     */
    private fun fetchNews() {
        NetworkAccess.cache(newsUrl) { success, cachePath ->
                if (success) {
                    news = News.loadFromString(FileUtil.getStringFromFile(cachePath))
                    runOnUiThread { publish() }
                } else {
                    // 本地没有该缓存，则退出
                    if (news == null) {
                        val dialog = AlertDialog(this)
                        dialog.setCancelable(false)
                        dialog.setCancelOnTouchOutside(false)
                        dialog.setTitle("错误")
                        dialog.setMessage("未获取到内容")
                        dialog.setPositiveButton("返回") {
                            dialog.dismiss()
                            finish()
                        }
                        dialog.show()
                    }
                }
        }
    }

    /**
     * 添加附件
     */
    private fun addExtra(name: String, url: String) {
        if (extraAdded.contains(url)) return
        extraAdded.add(url)
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        params.setMargins(0, PixelUtil.dp2px(this, 8), 0, PixelUtil.dp2px(this, 8))
        val textView = TextView(this)
        textView.text = name
        textView.gravity = Gravity.CENTER
        textView.setBackgroundResource(R.drawable.selector_grey_text_background)
        textView.isFocusable = true
        textView.isClickable = true
        textView.setPadding(PixelUtil.dp2px(this, 28), PixelUtil.dp2px(this, 4),
                PixelUtil.dp2px(this, 28), PixelUtil.dp2px(this, 4))
        textView.minHeight = PixelUtil.dp2px(this, 40)
        textView.typeface = Typeface.DEFAULT_BOLD
        textView.setTextColor((0xFF131313).toInt())
        textView.setOnClickListener {
            Toast.makeText(this, "开始下载 $name", Toast.LENGTH_SHORT).show()
            val item = IDownloadItem(url)
            item.fileName = name
            item.startDownload()
        }

        textView.layoutParams = params
        extraSection!!.addView(textView)
    }
}
