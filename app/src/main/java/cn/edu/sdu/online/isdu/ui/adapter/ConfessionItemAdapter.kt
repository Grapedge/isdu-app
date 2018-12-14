package cn.edu.sdu.online.isdu.ui.adapter

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.bean.Confession
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.ConfessionDetailActivity
import cn.edu.sdu.online.isdu.ui.activity.MyHomePageActivity
import cn.edu.sdu.online.isdu.util.DateCalculate
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.PixelUtil
import com.bumptech.glide.Glide
import com.qmuiteam.qmui.layout.QMUIFrameLayout
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

/***********************************************************
 * @author Grapes
 * @date 2018-12-13
 * @Description 表白信息适配器
 ***********************************************************/
class ConfessionItemAdapter (private val activity: Activity, private val dataList: List<Confession>) :
        RecyclerView.Adapter<ConfessionItemAdapter.ViewHolder>() {

    companion object {
        private const val EXTRA_DATA = "confession"
    }

    // bind view holder
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]

        // 设计：使用帖子标题被表白人姓名？暂时

        /*
        if (targetNameMap.containsKey(item.uid)) {
            holder.targetName.text = targetNameMap[item.uid]
        } else {
            // get user who post the confession info
            NetworkAccess.cache(ServerInfo.getUserInfo(item.uid, "nickname")) { success, cachePath ->
                if (success) {
                    try {
                        val obj = JSONObject(FileUtil.getStringFromFile(cachePath))
                        targetNameMap[item.uid] = obj.getString("nickname")
                        activity.runOnUiThread {
                            holder.targetName.text = targetNameMap[item.uid]
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, e.message)
                    }
                }
            }
        }*/

        holder.cardView.setOnClickListener {
            activity.startActivity(Intent(activity, ConfessionDetailActivity::class.java)
                    .putExtra(EXTRA_DATA, item))
        }

        holder.targetName.text = item.title
        holder.commentCount.text = item.commentCount.toString()
        holder.content.text = item.postContent
        holder.likeCount.text = item.likeCount.toString()
        holder.releaseTime.text = DateCalculate.getExpressionDate(item.time)
        holder.likeIcon.setImageResource(R.drawable.ic_like_normal)

    }

    // create view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.confession_item, parent, false)
        view.findViewById<QMUIFrameLayout>(R.id.card_view).setRadiusAndShadow(
                0,
                PixelUtil.dp2px(MyApplication.getContext(), 8), 0.2f)
        return ViewHolder(v = view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun getItemId(position: Int): Long = position.toLong()

    // view holder
    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // tag here
        val cardView: FrameLayout = v.findViewById(R.id.card_view)
        val targetName: TextView = v.findViewById(R.id.confession_name)
        val releaseTime: TextView = v.findViewById(R.id.release_time)
        val content:TextView = v.findViewById(R.id.content)
        val likeIcon:ImageView = v.findViewById(R.id.like_icon)
        val likeCount:TextView = v.findViewById(R.id.like_count)
        val commentCount:TextView = v.findViewById(R.id.comment_count)
    }
}
