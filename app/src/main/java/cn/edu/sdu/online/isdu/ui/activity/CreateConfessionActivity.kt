package cn.edu.sdu.online.isdu.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.NormActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.ui.design.xrichtext.RichTextEditor
import cn.edu.sdu.online.isdu.util.ImageManager
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.Permissions
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition

import kotlinx.android.synthetic.main.activity_create_post.*
import kotlinx.android.synthetic.main.edit_operation_bar.*
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/29
 *
 * 新建帖子页面
 *
 * 新增对GIF动画的判定
 ****************************************************
 */

class CreateConfessionActivity : NormActivity(), View.OnClickListener {

    private var btnAlbum: View? = null
    private var btnCamera: View? = null
    private var editTitle: EditText? = null
    private var richEditText: RichTextEditor? = null
    private var btnDone: TextView? = null
    private var btnBack: View? = null

    private var dialog: ProgressDialog? = null

    private val imageManager = ImageManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_confession)

        initView()

        dialog = ProgressDialog(this, false)
        dialog!!.setMessage("正在上传")
        dialog!!.setButton(null, null)
        dialog!!.setCancelable(false)


//        if (User.staticUser == null) User.staticUser = User.load()
//        if (User.staticUser.studentNumber == null) {
        if (!User.isLogin()) {
            val dialog = AlertDialog(this)
            dialog.setTitle("未登录")
            dialog.setMessage("请登录后重试")
            dialog.setCancelOnTouchOutside(false)
            dialog.setCancelable(false)
            dialog.setPositiveButton("登录") {
                dialog.dismiss()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            dialog.setNegativeButton("返回") {
                finish()
                dialog.dismiss()
            }
            dialog.show()
        } else {

        }

    }

    private fun initView() {
        btnAlbum = operate_album
        btnCamera = operate_camera
        editTitle = edit_title
        richEditText = rich_edit_content
        btnDone = btn_done
        btnBack = btn_back

        btnAlbum!!.setOnClickListener(this)
        btnCamera!!.setOnClickListener(this)
        btnDone!!.setOnClickListener(this)
        btnBack!!.setOnClickListener(this)

        richEditText!!.setOnRtImageClickListener { path: String ->
            startActivity(Intent(this, ViewImageActivity::class.java)
                    .putExtra("file_path", path))
        }
        loadDraft()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            operate_album.id -> {
                imageManager.selectFromGallery(this)
            }
            operate_camera.id -> {
                if (ContextCompat.checkSelfPermission(this, Permissions.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Permissions.CAMERA), 2)
                } else {
                    imageManager.captureByCamera(this)
                }

            }
            btn_done.id -> {
                val list = richEditText!!.buildEditData()
                if (editTitle!!.text.toString() == "" ||
                        (list.isEmpty() || (list.size == 1 &&
                                list[0].imagePath == null &&
                                list[0].inputStr == ""))) {
                    Toast.makeText(this, "标题和内容不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    performUpload(list, editTitle!!.text.toString())
                }
            }
            btn_back.id -> {
                if (editTitle!!.text.toString() == "" &&
                        richEditText!!.buildEditData().size == 1 &&
                        richEditText!!.buildEditData()[0].imagePath == null &&
                        richEditText!!.buildEditData()[0].inputStr == "") {
                    finish()
                } else {
                    val dialog = AlertDialog(this)
                    dialog.setTitle("退出")
                    dialog.setMessage("保存草稿？")
                    dialog.setPositiveButton("保存") {
                        saveDraft()
                        dialog.dismiss()
                        finish()
                    }
                    dialog.setNegativeButton("放弃") {
                        clearDraft()
                        dialog.dismiss()
                        finish()
                    }
                    dialog.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        btnBack!!.callOnClick()
//        super.onBackPressed()
    }

    /**
     *
     * @param list 富文本编辑器生成的数据列表
     */
    private fun performUpload(list: List<RichTextEditor.EditData>, title: String) {
        dialog!!.show()
        // 预处理图片列表
        val hashMap = handleImages(list) // 获取优化后的图片散列表

        val hashMapList = ArrayList<String>()
        for (entry in hashMap.entries) {
            hashMapList.add(entry.value)
        }

        val params = HashMap<String, String>()

        val jsonArray = JSONArray()

        for (i in 0 until list.size) {
            val data = list[i] // Get each data
//            val obj = org.json.JSONObject()
            val obj = JSONObject()
            if (data.imagePath != null && data.imagePath != "") {
                // Image
                obj.put("type", 0)
                obj.put("content", "http://202.194.15.133:8380/isdu/forum/img/" + data.imageName)
            } else {
                // Text
                obj.put("type", 1)
                obj.put("content", data.inputStr)
            }

            jsonArray.add(obj)
        }
        // Create Complete!

        val jsonObj = JSONObject()
        jsonObj.put("uid", User.staticUser.uid)
        jsonObj.put("obj", jsonArray.toJSONString())
        jsonObj.put("title", title)
        jsonObj.put("time", System.currentTimeMillis())

        params.put("data", jsonObj.toString())
        post(ServerInfo.uploadPostUrl, params, hashMap)
    }


    /**
     * 预处理图片
     * 给List中的图片编号
     * 存入Map中，可以优化上传
     */
    private fun handleImages(list: List<RichTextEditor.EditData>): HashMap<String, String> {
        val hashMap = HashMap<String, String>()

        for (data in list) {
            if (data.imagePath != null && data.imagePath != "") {
                // 是图片
                if (hashMap.containsKey(data.imagePath)) {
                    data.imageName = hashMap[data.imagePath]
                } else {
                    if (ImageManager.isGif(File(data.imagePath))) {
                        // GIF特判
                        data.imageName = User.staticUser.uid.toString() + "_" + System.nanoTime().toString() + ".gif"
                    } else {
                        data.imageName = User.staticUser.uid.toString() + "_" + System.nanoTime().toString() + ".jpg"
                    }
                    hashMap[data.imagePath] = data.imageName
                }
            }
        }

        return hashMap
    }

    /**
     * 保存草稿
     */
    private fun saveDraft() {
        val string = JSON.toJSONString(richEditText!!.buildEditData())
        val editor = getSharedPreferences("confession_draft", Context.MODE_PRIVATE).edit()
        editor.putString("content", string)
        editor.putString("title", editTitle!!.text.toString())
        editor.apply()
    }

    private fun clearDraft() {
        val editor = getSharedPreferences("confession_draft", Context.MODE_PRIVATE).edit()
        editor.remove("content")
        editor.remove("title")
        editor.apply()
    }

    /**
     * 加载草稿
     */
    private fun loadDraft() {
        val sp = getSharedPreferences("confession_draft", Context.MODE_PRIVATE)
        val string = sp.getString("content", "")
        val title = sp.getString("title", "")


        if (string != "") {
            richEditText!!.setEditData(JSON.parseArray(string, RichTextEditor.EditData::class.java))
        }

        editTitle!!.setText(title)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ImageManager.TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    Luban.with(this)
                            .load(File(imageManager.imagePath))
                            .ignoreBy(100)
                            .setTargetDir(File(imageManager.imagePath).parentFile.absolutePath)
                            .setCompressListener(object : OnCompressListener {
                                private var dialog: ProgressDialog? = null

                                override fun onSuccess(file: File?) {
                                    runOnUiThread {
                                        richEditText!!.insertImage(BitmapFactory.decodeFile(file!!.absolutePath),
                                                file.absolutePath)
                                        dialog?.dismiss()
                                    }
                                }

                                override fun onError(e: Throwable?) {
                                    Logger.log(e)
                                    runOnUiThread {
                                        Toast.makeText(this@CreateConfessionActivity, "插入图片失败",
                                                Toast.LENGTH_SHORT).show()
                                        dialog?.dismiss()
                                    }
                                }

                                override fun onStart() {
                                    runOnUiThread {
                                        dialog = ProgressDialog(this@CreateConfessionActivity)
                                        dialog?.setMessage("正在插入图片")
                                        dialog?.setButton(null, null)
                                        dialog?.setCancelable(false)
                                        dialog?.show()
                                    }
                                }
                            }).launch()

                }
            }
            ImageManager.OPEN_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageManager.handleImage(data, this)

                    if (ImageManager.isGif(File(imageManager.imagePath))) {
                        val target = object : ViewTarget<RichTextEditor, GifDrawable>(richEditText!!) {
                            override fun onResourceReady(resource: GifDrawable, transition: Transition<in GifDrawable>?) {
                                richEditText!!.insertGif(resource, imageManager.imagePath)
                            }
                        }
                        Glide.with(MyApplication.getContext()).asGif().load(imageManager.imagePath)
                                .into(target)
                    } else Luban.with(this)
                            .load(File(imageManager.imagePath))
                            .ignoreBy(100)
                            .setTargetDir(File(imageManager.imagePath).parentFile.absolutePath)
                            .setCompressListener(object : OnCompressListener {
                                private var dialog: ProgressDialog? = null

                                override fun onSuccess(file: File?) {
                                    runOnUiThread {
                                        richEditText!!.insertImage(BitmapFactory.decodeFile(file!!.absolutePath),
                                                file.absolutePath)
                                        dialog?.dismiss()
                                    }
                                }

                                override fun onError(e: Throwable?) {
                                    Logger.log(e)
                                    runOnUiThread {
                                        Toast.makeText(this@CreateConfessionActivity, "插入图片失败",
                                                Toast.LENGTH_SHORT).show()
                                        dialog?.dismiss()
                                    }
                                }

                                override fun onStart() {
                                    runOnUiThread {
                                        dialog = ProgressDialog(this@CreateConfessionActivity)
                                        dialog?.setMessage("正在插入图片")
                                        dialog?.setButton(null, null)
                                        dialog?.setCancelable(false)
                                        dialog?.show()
                                    }
                                }
                            }).launch()
                }
            }
            2 -> {

            }
        }
    }


    /**
     *
     */
    private fun post(actionUrl: String, params: HashMap<String, String>, hashMap: HashMap<String, String>) {
        Thread(Runnable {
            try {
                val BOUNDARY = "--------------et567z"
                //数据分隔线
                val MULTIPART_FORM_DATA = "Multipart/form-data"
                val url = URL(actionUrl)

                val conn = url.openConnection() as HttpURLConnection
                conn.doInput = true
                //允许输入
                conn.doOutput = true
                //允许输出
                conn.useCaches = false
                //不使用Cache
                conn.requestMethod = "POST"
                conn.setRequestProperty("Connection", "Keep-Alive")
                conn.setRequestProperty("Charset", "UTF-8")
                conn.setRequestProperty("Content-Type", "$MULTIPART_FORM_DATA;boundary=$BOUNDARY")

                //获取map对象里面的数据，并转化为string
                val sb = StringBuilder()
                //上传的表单参数部分，不需要更改
//            for (entry in params.entries) {
                //构建表单字段内容
                sb.append("--")
                sb.append(BOUNDARY)
                sb.append("\r\n")
                sb.append("Content-Disposition: form-data; name=\"" + "data" + "\"\r\n\r\n")
                sb.append(params.get("data"))
                sb.append("\r\n")

                //上传图片部分
                val outStream = DataOutputStream(conn.outputStream)
                outStream.write(sb.toString().toByteArray())
                //发送表单字段数据

                for (entry in hashMap.entries) {
                    //调用自定义方法获取图片文件的byte数组
                    val content = readFileImage(entry.key)
                    //再次设置报头信息
                    val split = StringBuilder()
                    split.append("--")
                    split.append(BOUNDARY)
                    split.append("\r\n")

                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!非常重要
                    //此处将图片的name设置为file ,filename不做限制，不需要管
                    split.append("Content-Disposition: form-data;name=\"file\";filename=\"${entry.value}\"\r\n")
                    //这里的Content-Type非常重要，一定要是图片的格式，例如image/jpeg或者image/jpg
                    //服务器端对根据图片结尾进行判断图片格式到底是什么,因此务必保证这里类型正确
                    if (entry.value.toLowerCase().endsWith(".gif")) {
                        // GIF特判
                        split.append("Content-Type: image/gif\r\n\r\n")
                    } else {
                        split.append("Content-Type: image/jpg\r\n\r\n")
                    }

                    outStream.write(split.toString().toByteArray())
                    outStream.write(content, 0, content.size)
                    outStream.write("\r\n".toByteArray())

                }
                val endData = ("--$BOUNDARY--\r\n").toByteArray()
                //数据结束标志
                outStream.write(endData)

                outStream.flush()


                //返回状态判断
                val cah = conn.responseCode

                runOnUiThread {
                    if (cah == 200) {
                        Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show()
                        clearDraft()
                        finish()
                    } else if (cah == 400) {
                        Toast.makeText(this, "发布失败(400)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "发布失败($cah)", Toast.LENGTH_SHORT).show()
                    }
                }

                outStream.close()

                conn.disconnect()

            } catch (e: Exception) {
                Logger.log(e)
            } finally {
                dialog!!.dismiss()
            }
        }).start()

    }


    @Throws(IOException::class)
    private fun readFileImage(filePath: String): ByteArray {
        if (ImageManager.isGif(File(filePath))) {
            return ImageManager.convertGifToByteArray(filePath)
        } else return ImageManager.convertBitmapToByteArray(BitmapFactory.decodeFile(filePath))
    }
}
