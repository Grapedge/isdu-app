package cn.edu.sdu.online.isdu.ui.activity;

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.NormActivity
import cn.edu.sdu.online.isdu.bean.User
import cn.edu.sdu.online.isdu.ui.design.dialog.AlertDialog
import cn.edu.sdu.online.isdu.ui.design.dialog.ProgressDialog
import cn.edu.sdu.online.isdu.ui.design.xrichtext.RichTextEditor
import cn.edu.sdu.online.isdu.util.ImageManager
import cn.edu.sdu.online.isdu.util.Permissions
import com.alibaba.fastjson.JSON
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.android.synthetic.main.activity_create_notice.*
import kotlinx.android.synthetic.main.edit_operation_bar.*



class CreateNoticeActivity : NormActivity(), View.OnClickListener{

    private var btnBackLife:View? = null
    private var btnDoneNotice:View? = null
    private var btnAlbum: View? = null
    private var btnCamera: View? = null
    private var btnThingCho: View? =null
    private var textNoticeType: TextView? = null
    private var textCollege: TextView? = null
    private var textThingType: TextView? = null
    private var editThingName: EditText? = null
    private var richEditDis: RichTextEditor? = null
    private var editOwnerContact: EditText? = null
    private var dialog: ProgressDialog? = null
    private val imageManager = ImageManager()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_notice)



        initView()
        dialog = ProgressDialog(this, false)
        dialog!!.setMessage("正在上传")
        dialog!!.setButton(null, null)
        dialog!!.setCancelable(false)




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


        textNoticeType = text_notice_type
        textCollege = text_college
        textThingType = text_thing_type
        editThingName = edit_thing_name
        richEditDis = rich_edit_dis
        btnAlbum = operate_album
        btnCamera = operate_camera
        editOwnerContact = edit_owner_contect



        btnDoneNotice = btn_done_success
        btnBackLife = btn_back_life
        btnThingCho = btn_thing_cho


        btnAlbum!!.setOnClickListener(this)
        btnCamera!!.setOnClickListener(this)
        btnDoneNotice!!.setOnClickListener(this)
        btnBackLife!!.setOnClickListener(this)


        /*richEditDis!!.setOnRtImageClickListener { path: String ->

            startActivity(Intent(this, ViewImageActivity::class.java)

                    .putExtra("file_path", path))

        }*/

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

            btn_done_success.id -> {
                val list = richEditDis!!.buildEditData()
                if (textNoticeType!!.text.toString() == "" ||
                        textCollege!!.text.toString()==""||
                        textThingType!!.text.toString() ==""||
                        (list.isEmpty() || (list.size == 1 &&
                                list[0].imagePath == null &&
                                list[0].inputStr == ""))) {
                    Toast.makeText(this, "启事类型、所在校区、物品分类和详细描述不能为空", Toast.LENGTH_SHORT).show()
                } else {
                    //performUpload(list, editNoticeType!!.text.toString())
                }
            }

            btn_back_life.id -> {
                if (textNoticeType!!.text.toString() == ""&&
                        textCollege!!.text.toString() == ""&&
                        textThingType!!.text.toString() == ""&&
                        editThingName!!.text.toString() == ""&&
                        editOwnerContact!!.text.toString() == ""&&
                        richEditDis!!.buildEditData().size == 1 &&
                        richEditDis!!.buildEditData()[0].imagePath == null &&
                        richEditDis!!.buildEditData()[0].inputStr == "") {
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


    fun notice_type(view: View) {
        val items = arrayOf("寻物", "招领")
        val tv = findViewById<TextView>(R.id.text_notice_type)
        val builder = QMUIDialog.CheckableDialogBuilder(this)
        builder.addItems(items) { dialog, which -> }
        builder.addAction("取消") { dialog, index -> dialog.dismiss() }
        builder.addAction("确定") { dialog, index ->
            var result: String? = null
            for (i in 0..builder.checkedIndex)
                result = items[i]
            tv.text = result
            dialog.dismiss()
        }
        builder.show()
    }
    fun thing_type(view: View) {
        val items = arrayOf("卡", "证件","书籍","衣帽饰品","电子产品","其它")
        //val checkedIndex = 0
        val tv = findViewById<TextView>(R.id.text_thing_type)
        val builder = QMUIDialog.CheckableDialogBuilder(this)
        //builder.checkedIndex = checkedIndex
        builder.addItems(items) { dialog, which -> }
        builder.addAction("取消") { dialog, index -> dialog.dismiss() }
        builder.addAction("确定") { dialog, index ->
            var result: String? = null
            for (i in 0..builder.checkedIndex)
                result = items[i]
            tv.text = result
            dialog.dismiss()
        }
        builder.show()
    }



    fun college(view: View) {
        val items = arrayOf("中心校区", "洪家楼","软件园校区","千佛山校区","兴隆山校区","趵突泉校区")
        val tv = findViewById<TextView>(R.id.text_college)
        val builder = QMUIDialog.CheckableDialogBuilder(this)
        builder.addItems(items) { dialog, which -> }
        builder.addAction("取消") { dialog, index -> dialog.dismiss() }
        builder.addAction("确定") { dialog, index ->
            var result: String? = null
            for (i in 0..builder.checkedIndex)
                result = items[i]
            tv.text = result
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onBackPressed() {
        btnBackLife!!.callOnClick()
    }





    private fun saveDraft() {
        val string = JSON.toJSONString(richEditDis!!.buildEditData())
        val editor = getSharedPreferences("post_notice_draft", Context.MODE_PRIVATE).edit()
        editor.putString("thing_dis", string)
        editor.putString("notice_type", textNoticeType!!.text.toString())
        editor.putString("college",textCollege!!.text.toString())
        editor.putString("thing_type",textThingType!!.text.toString())
        editor.putString("thing_name",editThingName!!.text.toString())
        editor.putString("owner_contact",editOwnerContact!!.text.toString())
        editor.apply()
    }



    private fun clearDraft() {
        val editor = getSharedPreferences("post_notice_draft", Context.MODE_PRIVATE).edit()
        editor.remove("thing_dis")
        editor.remove("notice_type")
        editor.remove("college")
        editor.remove("thing_type")
        editor.remove("thing_name")
        editor.remove("owner_contact")
        editor.apply()

    }

    private fun loadDraft() {
        val sp = getSharedPreferences("post_notice_draft", Context.MODE_PRIVATE)
        val string = sp.getString("thing_dis", "")
        val notice = sp.getString("notice_type", "")
        val college = sp.getString("college","")
        val thing = sp.getString("thing_type","")
        val name = sp.getString("thing_name","")
        val contact = sp.getString("owner_contact","")



        if (string != "") {
            richEditDis!!.setEditData(JSON.parseArray(string, RichTextEditor.EditData::class.java))
        }
        textNoticeType!!.setText(notice)
        textCollege!!.setText(college)
        textThingType!!.setText(thing)
        editThingName!!.setText(name)
        editOwnerContact!!.setText(contact)

    }

}