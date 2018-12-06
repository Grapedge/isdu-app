package cn.edu.sdu.online.isdu.ui.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.*
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.NormActivity
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.design.dialog.OptionDialog
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.ImageManager
import cn.edu.sdu.online.isdu.util.Phone
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_view_image.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import java.io.*

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/14
 *
 * 图片浏览活动
 ****************************************************
 */

class ViewImageActivity : NormActivity() {

//    private var draggableImageView: DraggableImageView? = null
    private var progressBar: ProgressBar? = null
    private var textView: TextView? = null
    private var loadingLayout: LinearLayout? = null
    private var draggableImageView: PhotoView? = null

    var resId: Int = 0
    var url: String = ""
    var bmpStr: String = ""
    var cacheKey: String = ""
    var isString = false
    var filePath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_image)

        decorateWindow()

        draggableImageView = image_view
//        photoView = image_view
        progressBar = progress_bar
        textView = text
        loadingLayout = loading_layout

        loadingLayout!!.visibility = View.GONE

        draggableImageView!!.minimumScale = 0.8f
        draggableImageView!!.setOnClickListener {
            finish()
        }

        draggableImageView!!.setOnLongClickListener {
            Phone.vibrate(this, Phone.VibrateType.Once)
            return@setOnLongClickListener true
        }

        draggableImageView!!.setOnLongClickListener {
            Phone.vibrate(this, Phone.VibrateType.Once)

            val dialog = OptionDialog(this, listOf("保存图片", "取消"))
            dialog.setCancelOnTouchOutside(true)
            dialog.setMessage("图片")
            dialog.setOnItemSelectListener { itemName ->
                when (itemName) {
                    "保存图片" -> {
                        dialog.dismiss()

                        if (resId != 0) {
                            val file = File(Environment.getExternalStorageDirectory().toString() +
                                    "/iSDU/Image/" + System.currentTimeMillis() + ".jpg")

                            if (!file.exists()) {
                                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                file.createNewFile()
                            }
                            val fos = FileOutputStream(file)
                            val bmp = BitmapFactory.decodeResource(resources, resId)
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            fos.flush()
                            fos.close()
                            runOnUiThread {
                                Toast.makeText(this, "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                            }
                        } else if (url != "") {
                            if (cacheKey == "") {
                                NetworkAccess.buildRequest(url, object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        runOnUiThread {
                                            Toast.makeText(this@ViewImageActivity, "网络错误", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val byteArray = response.body()?.bytes()
                                        /********************************************
                                         *
                                         ********************************************/
                                        if (ImageManager.isGif(byteArray)) {
                                            val file = File(Environment.getExternalStorageDirectory().toString() +
                                                    "/iSDU/Image/" + System.currentTimeMillis() + ".gif")

                                            if (!file.exists()) {
                                                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                                file.createNewFile()
                                            }
                                            val fos = FileOutputStream(file)

//                                            val fis = FileInputStream(byteArray)
                                            val bis = BufferedInputStream(byteArray!!.inputStream())
                                            var byte = ByteArray(1024)
                                            var len = bis.read(byte)
                                            while (len > 0) {
                                                fos.write(byte, 0, len)
                                                len = bis.read(byte)
                                            }
//                                            fis.close()
                                            bis.close()

                                            runOnUiThread {
                                                Toast.makeText(this@ViewImageActivity,
                                                        "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            val file = File(Environment.getExternalStorageDirectory().toString() +
                                                    "/iSDU/Image/" + System.currentTimeMillis() + ".jpg")

                                            if (!file.exists()) {
                                                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                                file.createNewFile()
                                            }
                                            val fos = FileOutputStream(file)
                                            val bmp = if (isString) ImageManager.convertStringToBitmap(byteArray!!.toString()) else
                                                BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
                                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                            fos.flush()
                                            fos.close()

                                            runOnUiThread {
                                                Toast.makeText(this@ViewImageActivity,
                                                        "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                })
                                /*
                                NetworkAccess.cache(url) { success, cachePath ->
                                    if (success) {
                                        /********************************************
                                         *
                                         ********************************************/
                                        if (ImageManager.isGif(cachePath)) {
                                            val file = File(Environment.getExternalStorageDirectory().toString() +
                                                    "/iSDU/Image/" + System.currentTimeMillis() + ".gif")

                                            if (!file.exists()) {
                                                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                                file.createNewFile()
                                            }
                                            val fos = FileOutputStream(file)

                                            val fis = FileInputStream(File(cachePath))
                                            val bis = BufferedInputStream(fis)
                                            var byte = ByteArray(1024)
                                            var len = bis.read(byte)
                                            while (len > 0) {
                                                fos.write(byte, 0, len)
                                                len = bis.read(byte)
                                            }
                                            fis.close()
                                            bis.close()

                                            runOnUiThread {
                                                Toast.makeText(this, "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            val file = File(Environment.getExternalStorageDirectory().toString() +
                                                    "/iSDU/Image/" + System.currentTimeMillis() + ".jpg")

                                            if (!file.exists()) {
                                                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                                file.createNewFile()
                                            }
                                            val fos = FileOutputStream(file)
                                            val bmp = if (isString) ImageManager.convertStringToBitmap(FileUtil.getStringFromFile(cachePath)) else
                                                BitmapFactory.decodeFile(cachePath)
                                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                            fos.flush()
                                            fos.close()

                                            runOnUiThread {
                                                Toast.makeText(this, "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                */
                            } else {
                                NetworkAccess.buildRequest(url, object : Callback {
                                    override fun onFailure(call: Call, e: IOException) {
                                        runOnUiThread {
                                            Toast.makeText(this@ViewImageActivity, "网络错误", Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                    override fun onResponse(call: Call, response: Response) {
                                        val jsonObject = JSONObject(response.body()!!.string())
                                        val str = jsonObject.getString(cacheKey)
                                        /********************************************
                                        dd                *
                                         ********************************************/
                                        val file = File(Environment.getExternalStorageDirectory().toString() +
                                                "/iSDU/Image/" + System.currentTimeMillis() + ".jpg")

                                        if (!file.exists()) {
                                            if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                            file.createNewFile()
                                        }
                                        val fos = FileOutputStream(file)
                                        val bmp = if (isString) ImageManager.convertStringToBitmap(str) else
                                            BitmapFactory.decodeByteArray(str.toByteArray(), 0, str.toByteArray().size)
                                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                        fos.flush()
                                        fos.close()
                                        runOnUiThread {
                                            Toast.makeText(this@ViewImageActivity,
                                                    "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                })
                                /*
                                NetworkAccess.cache(url, cacheKey) { success, cachePath ->
                                    if (success) {
                                        /********************************************
                                        dd                *
                                         ********************************************/
                                        val file = File(Environment.getExternalStorageDirectory().toString() +
                                                "/iSDU/Image/" + System.currentTimeMillis() + ".jpg")

                                        if (!file.exists()) {
                                            if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                            file.createNewFile()
                                        }
                                        val fos = FileOutputStream(file)
                                        val bmp = if (isString) ImageManager.convertStringToBitmap(FileUtil.getStringFromFile(cachePath)) else
                                            BitmapFactory.decodeFile(cachePath)
                                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                                        fos.flush()
                                        fos.close()
                                        runOnUiThread {
                                            Toast.makeText(this, "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        runOnUiThread {
                                            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                                */
                            }

                        } else if (bmpStr != "") {
                            val file = File(Environment.getExternalStorageDirectory().toString() +
                                    "/iSDU/Image/" + System.currentTimeMillis() + ".jpg")

                            if (!file.exists()) {
                                if (!file.parentFile.exists()) file.parentFile.mkdirs()
                                file.createNewFile()
                            }
                            val fos = FileOutputStream(file)
                            val bmp = ImageManager.convertStringToBitmap(bmpStr)
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                            fos.flush()
                            fos.close()
                            runOnUiThread {
                                Toast.makeText(this, "成功保存至 /sdcard/iSDU/Image/" + file.name, Toast.LENGTH_SHORT).show()
                            }
                        }

                        runOnUiThread {
                            decorateWindow()
                        }

                    }
                    "取消" -> {
                        dialog.dismiss()
                        runOnUiThread {
                            decorateWindow()
                        }

                    }
                }
            }
            dialog.show()

            return@setOnLongClickListener true
        }

        resId = intent.getIntExtra("res_id", 0)
        url = if (intent.getStringExtra("url") == null) "" else intent.getStringExtra("url")
        bmpStr = if (intent.getStringExtra("bmp_str") == null) "" else intent.getStringExtra("bmp_str")
        cacheKey = if (intent.getStringExtra("key") == null) "" else intent.getStringExtra("key")
        isString = intent.getBooleanExtra("isString", false)
        filePath = if (intent.getStringExtra("file_path") == null) "" else intent.getStringExtra("file_path")

        if (resId != 0) {
            draggableImageView!!.setImageResource(resId)
        } else if (url != "") {
            loadingLayout!!.visibility = View.VISIBLE
            textView!!.text = "正在加载..."
            NetworkAccess.cache(url, cacheKey) { success, cachePath ->
                if (success) {
                    if (isString) {
                        val bmp = ImageManager.loadStringFromFile(cachePath)
                        runOnUiThread {
                            draggableImageView!!.setImageBitmap(bmp)
                            loadingLayout!!.visibility = View.GONE
                        }
                    } else {
                        if (ImageManager.isGif(cachePath)) {
                            runOnUiThread {
                                Glide.with(MyApplication.getContext())
                                        .asGif()
                                        .load(cachePath)
                                        .into(draggableImageView!!)

                                loadingLayout!!.visibility = View.GONE
                            }
                        } else {
                            val target = object : ViewTarget<PhotoView, Drawable>(draggableImageView!!) {
                                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                                    this.view.setImageBitmap((resource as BitmapDrawable).bitmap)
                                }
                            }
                            runOnUiThread {
                                Glide.with(MyApplication.getContext())
                                        .load(cachePath)
                                        .into(target)

                                loadingLayout!!.visibility = View.GONE
                            }
                        }

                    }

                } else {
                    runOnUiThread {
                        loadingLayout!!.visibility = View.VISIBLE
                        progressBar!!.visibility = View.GONE
                        textView!!.text = "加载失败"
                    }
                }
            }
        } else if (bmpStr != "") {
            val bmp = ImageManager.convertStringToBitmap(bmpStr)
            draggableImageView!!.setImageBitmap(bmp)
        } else if (filePath != "") {
            if (ImageManager.isGif(filePath)) {
                Glide.with(MyApplication.getContext()).asGif().load(filePath)
                        .into(draggableImageView!!)
            } else {
                Glide.with(MyApplication.getContext()).load(filePath)
                        .into(draggableImageView!!)
            }
//            val bmp = BitmapFactory.decodeFile(filePath)
//            draggableImageView!!.setImageBitmap(bmp)
        }
    }

    override fun onResume() {
        super.onResume()
        decorateWindow()
    }

//    private fun decorateWindow() {
//        val decorView = window.decorView
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
//        } else {
//            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
//        }
//    }

}
