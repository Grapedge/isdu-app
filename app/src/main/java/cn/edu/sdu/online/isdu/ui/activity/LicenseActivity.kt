package cn.edu.sdu.online.isdu.ui.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebViewClient
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import kotlinx.android.synthetic.main.activity_license.*

class LicenseActivity : SlideActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license)

        btn_back.setOnClickListener { finish() }

        web_view.webViewClient = WebViewClient()
        web_view.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        val webSettings = web_view.settings
        webSettings.defaultTextEncodingName = "UTF-8"
        webSettings.setSupportZoom(false)
        webSettings.builtInZoomControls = false
        webSettings.useWideViewPort = false
        webSettings.loadWithOverviewMode = false
//        webSettings.textZoom = 200
//        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        web_view.setInitialScale(200)
        web_view.loadUrl("file:///android_asset/license.html")
    }
}
