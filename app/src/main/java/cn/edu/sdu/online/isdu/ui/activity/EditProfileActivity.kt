package cn.edu.sdu.online.isdu.ui.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.ContextCompat
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.NormActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.net.AccountOp
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.OptionDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.util.ImageManager
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.Permissions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.ViewTarget
import com.bumptech.glide.request.transition.Transition
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.DataOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/12
 *
 * 编辑个人资料页面
 *
 * #增加返回键确认
 ****************************************************
 */

class EditProfileActivity : NormActivity() {

    private var editUserName: EditText? = null
    private var editGender: TextView? = null
    private var editMajor: TextView? = null
    private var editDepart: TextView? = null
    private var editName: TextView? = null
    private var editIntroduction: EditText? = null
    private var avatar: CircleImageView? = null
    private var btnBack: ImageView? = null
    private var btnDone: ImageView? = null
    private var btnEditAvatar: TextView? = null
    private var editStudentNumber: TextView? = null

    private var finalBitmap: Bitmap? = null

    private var imageManager: ImageManager? = ImageManager()

    private var progressDialog: ProgressDialog? = null

    private var isAvatarChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initView()
        loadUserInformation()
    }

    private fun initView() {
        editUserName = findViewById(R.id.edit_user_name)
        editGender = findViewById(R.id.edit_gender)
        editMajor = findViewById(R.id.edit_profession)
        editDepart = findViewById(R.id.edit_campus)
        editName = findViewById(R.id.edit_name)
        editIntroduction = findViewById(R.id.edit_introduction)
        avatar = findViewById(R.id.circle_image_view)
        btnBack = findViewById(R.id.btn_back)
        btnDone = findViewById(R.id.btn_done)
        btnEditAvatar = findViewById(R.id.btn_edit_avatar)
        editStudentNumber = findViewById(R.id.edit_student_number)


        btnEditAvatar!!.setOnClickListener {
            val list = listOf("相机拍摄", "从相册选择")
            val dialog = OptionDialog(this, list)
            dialog.setMessage("设置头像")
            dialog.setOnItemSelectListener {
                itemName ->
                if (itemName == "相机拍摄") {
                    // 重要：首先确认权限
                    if (ContextCompat.checkSelfPermission(this, Permissions.CAMERA) !=
                            PackageManager.PERMISSION_GRANTED) {
                        Permissions.requestPermission(this, Permissions.CAMERA)
                    } else {
                        imageManager!!.captureByCamera(this)
                    }

                    dialog.dismiss()
                } else {
                    imageManager!!.selectFromGallery(this)
                    dialog.dismiss()
                }
            }
            dialog.show()
        }

        btnBack!!.setOnClickListener {
            val dialog = AlertDialog(this)
            dialog.setTitle("退出")
            dialog.setMessage("确定要退出吗？所做的更改将不会保存。")
            dialog.setPositiveButton("是") {_ ->
                dialog.dismiss()
                finish()
            }
            dialog.setNegativeButton("否") {_ ->
                dialog.dismiss()
            }
            dialog.show()
        }

        btnDone!!.setOnClickListener {
            submitChange()
        }
    }

    private fun loadUserInformation() {
        var user = User.staticUser
        if (user == null) {
            User.staticUser = User.load()
            user = User.staticUser
        }
        Glide.with(this)
                .load(user.avatarUrl)
                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                .apply(RequestOptions.skipMemoryCacheOf(true))
                .into(object : ViewTarget<CircleImageView, Drawable>(avatar!!) {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        finalBitmap = (resource as BitmapDrawable).bitmap
                        this.view.setImageBitmap(finalBitmap)
                    }
                })
        editUserName!!.setText(user.nickName)

        when (user.gender) {
            User.GENDER_MALE -> editGender!!.text = "男"
            User.GENDER_FEMALE -> editGender!!.text = "女"
            User.GENDER_SECRET -> editGender!!.text = "保密"
        }

        editMajor!!.text = user.major
        editDepart!!.text = user.depart
        editName!!.text = user.name
        editIntroduction!!.setText(user.selfIntroduce)
        editStudentNumber!!.text = user.studentNumber
    }

    override fun onDestroy() {
        super.onDestroy()
        // 递归删除缓存文件夹
        val folder = File(Environment.getExternalStorageDirectory().toString() + "/iSDU/thumb")
        if (folder.exists() && folder.isDirectory) {
            folder.deleteOnExit()
        }
    }

    /**
     * 提交更新
     */
    private fun submitChange() {
        if (editUserName!!.text.toString() == "") {
            val d = AlertDialog(this)
            d.setCancelOnTouchOutside(true)
            d.setTitle("缺少信息")
            d.setMessage("用户名不能为空")
            d.setPositiveButton("取消") {d.dismiss()}
            d.setNegativeButton(null, null)
            d.show()
        } else if (finalBitmap == null) {
            val d = AlertDialog(this)
            d.setCancelOnTouchOutside(true)
            d.setTitle("缺少信息")
            d.setMessage("头像不能为空")
            d.setPositiveButton("取消") {d.dismiss()}
            d.setNegativeButton(null, null)
            d.show()
        } else {
            if (editUserName!!.text.toString().length > 20) {
                val d = AlertDialog(this)
                d.setCancelOnTouchOutside(true)
                d.setTitle("字数超限")
                d.setMessage("昵称不能超过20字")
                d.setPositiveButton("取消") {d.dismiss()}
                d.setNegativeButton(null, null)
                d.show()
            } else if (editIntroduction!!.text.toString().length > 50) {
                val d = AlertDialog(this)
                d.setCancelOnTouchOutside(true)
                d.setTitle("字数超限")
                d.setMessage("个人签名不能超过50字")
                d.setPositiveButton("取消") {d.dismiss()}
                d.setNegativeButton(null, null)
                d.show()
            } else {
                progressDialog = ProgressDialog(this, false)
                progressDialog!!.setMessage("正在更新信息...")
                progressDialog!!.setButton(null, null)
                progressDialog!!.show()

                // 构建用户信息上传内容
                val hashMap = HashMap<String, String>()
                val paramsMap = HashMap<String, String>()
                val userObj = JSONObject()
                userObj.put("studentnumber", User.staticUser.studentNumber)
                userObj.put("j_password", User.staticUser.passwordMD5)
                userObj.put("nickname", editUserName!!.text.toString())

                if (isAvatarChanged)
                    paramsMap["avatar"] = "${ServerInfo.avatarUrl}/head_${User.staticUser.uid}_${System.currentTimeMillis()}.jpg"
                else
                    paramsMap["avatar"] = "nil"
                userObj.put("avatar", paramsMap["avatar"])

                userObj.put("sign", editIntroduction!!.text.toString())

                hashMap["userInfo"] = userObj.toString()

                post(ServerInfo.urlUpdate, hashMap, paramsMap)
            }

        }
    }

    private fun post(actionUrl: String, hashMap: HashMap<String, String>, paramsMap: HashMap<String, String>) {
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
                sb.append("Content-Disposition: form-data; name=\"" + "userInfo" + "\"\r\n\r\n")
                sb.append(hashMap["userInfo"])
                sb.append("\r\n")

                //上传图片部分
                val outStream = DataOutputStream(conn.outputStream)
                outStream.write(sb.toString().toByteArray())
                //发送表单字段数据

                //调用自定义方法获取图片文件的byte数组
                val content = if (isAvatarChanged)
                    ImageManager.convertBitmapToByteArray(finalBitmap) else ByteArray(1)
                //再次设置报头信息
                val split = StringBuilder()
                split.append("--")
                split.append(BOUNDARY)
                split.append("\r\n")

                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!非常重要
                //此处将图片的name设置为file ,filename不做限制，不需要管
                split.append("Content-Disposition: form-data;name=\"file\";filename=\"${paramsMap["avatar"]}\"\r\n")
                //这里的Content-Type非常重要，一定要是图片的格式，例如image/jpeg或者image/jpg
                //服务器端对根据图片结尾进行判断图片格式到底是什么,因此务必保证这里类型正确
                split.append("Content-Type: image/jpg\r\n\r\n")
                outStream.write(split.toString().toByteArray())
                outStream.write(content, 0, content.size)
                outStream.write("\r\n".toByteArray())

                val endData = ("--$BOUNDARY--\r\n").toByteArray()
                //数据结束标志
                outStream.write(endData)

                outStream.flush()


                //返回状态判断
                val cah = conn.responseCode

                // 为了不使用原先缓存，用Glide清除一下图片缓存
//                Glide.get(MyApplication.getContext()).clearDiskCache()
                runOnUiThread {
                    if (cah == 200) {
                        // 更新本地用户信息
                        AccountOp.syncUserInformation()

                        Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show()
                        finish()
                    } else if (cah == 400) {
                        Toast.makeText(this, "更新失败(400)", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "更新失败($cah)", Toast.LENGTH_SHORT).show()
                    }
                }

                outStream.close()

                conn.disconnect()

            } catch (e: Exception) {
                Logger.log(e)
                runOnUiThread {
                    Toast.makeText(this, "更新信息", Toast.LENGTH_SHORT).show()
                }
            } finally {
                runOnUiThread {
                    progressDialog?.dismiss()
                }
            }
        }).start()
    }

    override fun onBackPressed() {
        btnBack!!.callOnClick()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imageManager?.selectFromGallery(this)
            } else {
                Toast.makeText(this, "权限拒绝，无法打开相册", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                // 权限确认
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    imageManager?.captureByCamera(this)
                } else {
                    Toast.makeText(this, "权限拒绝，无法打开相机", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            ImageManager.TAKE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageManager?.openCrop(this)
                }
            }
            ImageManager.OPEN_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    imageManager?.handleImage(this, data)
                }
            }
            UCrop.REQUEST_CROP -> {
                if (data != null) {
                    finalBitmap = BitmapFactory.decodeStream(
                            contentResolver.openInputStream(imageManager!!.destUri))
                    Glide.with(MyApplication.getContext())
                            .load(finalBitmap)
                            .into(avatar!!)
                    isAvatarChanged = true
//                    avatar!!.setImageBitmap(finalBitmap)
                }
            }
            UCrop.RESULT_ERROR -> {
                Toast.makeText(this, "裁剪图片失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
