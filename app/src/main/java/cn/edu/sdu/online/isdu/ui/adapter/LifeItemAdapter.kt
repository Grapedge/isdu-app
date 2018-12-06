package cn.edu.sdu.online.isdu.ui.adapter

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.bean.Life
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.LifeDetailActivity
import cn.edu.sdu.online.isdu.ui.activity.MyHomePageActivity
import cn.edu.sdu.online.isdu.ui.activity.PostDetailActivity
import cn.edu.sdu.online.isdu.util.DateCalculate
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.PixelUtil
import com.bumptech.glide.Glide
import com.qmuiteam.qmui.layout.QMUIFrameLayout
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.lost_and_found_item.view.*
import org.json.JSONObject

class LifeItemAdapter(private val activity: Activity, private val dataList: List<Life>) :
        RecyclerView.Adapter<LifeItemAdapter.ViewHolder>() {

    private val uNicknameMap = HashMap<String, String>()
    private val uAvatarMap = HashMap<String, String>()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        if (uNicknameMap.containsKey(item.uid)) {
            holder.txtNickname.text = uNicknameMap[item.uid]
            Glide.with(MyApplication.getContext())
                    .load(uAvatarMap[item.uid])
                    .into(holder.circleImageView)
        } else {
            NetworkAccess.cache(ServerInfo.getUserInfo(item.uid, "nickname-avatar")) { success, cachePath ->
                if (success) {
                    try {
                        val obj = JSONObject(FileUtil.getStringFromFile(cachePath))
                        uNicknameMap[item.uid] = obj.getString("nickname")
                        uAvatarMap[item.uid] = obj.getString("avatar")

                        activity.runOnUiThread {
                            holder.txtNickname.text = uNicknameMap[item.uid]
                            Glide.with(MyApplication.getContext())
                                    .load(uAvatarMap[item.uid])
                                    .into(holder.circleImageView)
                        }
                    } catch (e: Exception) {}

                }
            }
        }

        holder.cardView.setOnClickListener {
            activity.startActivity(Intent(activity, LifeDetailActivity::class.java)
                    .putExtra("id", item.lifeId))
//                    .putExtra("uid", item.uid)
//                    .putExtra("title", item.title)
//                    .putExtra("time", item.time))
        }
        holder.titleView.text = item.title
        //holder.commentNumber.text = item.commentsNumbers.toString()
        holder.content.text = item.content
        holder.txtLike.text = item.likeNumber.toString()
        holder.releaseTime.text = DateCalculate.getExpressionDate(item.time)


        holder.circleImageView.setOnClickListener {
            activity.startActivity(Intent(activity, MyHomePageActivity::class.java)
                    .putExtra("id", item.uid.toInt()))


        }
        /*新加的*/
        holder.lostCampus.text = item.campus
        holder.lifePostClass.text = item.lifePostClass
        holder.lostClass.text = item.lostClass

        // 添加帖子标识
        if (item.tag != null && item.tag != "") {
            holder.tag.visibility = View.VISIBLE
            holder.tag.text = item.tag
        } else {
            holder.tag.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context).inflate(
                        R.layout.lost_and_found_item, parent, false)
        view.findViewById<QMUIFrameLayout>(R.id.card_view).setRadiusAndShadow(
                PixelUtil.dp2px(MyApplication.getContext(), 8),
                PixelUtil.dp2px(MyApplication.getContext(), 8), 0.1f)
        return ViewHolder(v = view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun getItemId(position: Int): Long = position.toLong()

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cardView: FrameLayout = v.findViewById(R.id.card_view)
        val titleView: TextView = v.findViewById(R.id.title_view) // 标题
        //            val contentLayout: LinearLayout = v.findViewById(R.id.content_layout) // 内容Layout
//            val userName: TextView = v.findViewById(R.id.user_name) // 用户名
        //val commentNumber: TextView = v.findViewById(R.id.comments_number) // 评论数
        val releaseTime: TextView = v.findViewById(R.id.release_time) // 发布时间
        val content: TextView = v.findViewById(R.id.content)
        val txtLike: TextView = v.findViewById(R.id.like_count)
        val lifePostClass: TextView = v.findViewById(R.id.life_class)
        val lostClass: TextView = v.findViewById(R.id.lost_class)
        val lostCampus: TextView = v.findViewById(R.id.lost_campus)
        // 新增：用户信息区域
        val circleImageView: CircleImageView = v.findViewById(R.id.circle_image_view)
        val txtNickname: TextView = v.findViewById(R.id.txt_nickname)
        val tag: TextView = v.findViewById(R.id.title_flag)
    }
}
