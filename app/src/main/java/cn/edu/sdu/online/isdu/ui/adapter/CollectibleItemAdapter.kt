package cn.edu.sdu.online.isdu.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.bean.AbstractCollectible
import cn.edu.sdu.online.isdu.util.DateCalculate
import de.hdodenhof.circleimageview.CircleImageView

class CollectibleItemAdapter(private var dataList: List<AbstractCollectible>) :
        RecyclerView.Adapter<CollectibleItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item, parent, false)
        view.findViewById<View>(R.id.post_addition_information).visibility = View.GONE
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val item = dataList[position]
        when (item.type) {
            AbstractCollectible.TYPE_POST -> {
                setAs("post", vh)
            }
            AbstractCollectible.TYPE_NEWS -> {
                setAs("news", vh)
            }
            AbstractCollectible.TYPE_LIFE -> {
                setAs("life", vh)
            }
        }

        vh.content.text = item.getmContent()
        vh.releaseTime.text = DateCalculate.getExpressionDate(item.getmTime())
        vh.titleView.text = item.getmTitle()
    }

    private fun setAs(type: String, vh: ViewHolder) {
        when (type) {
            "post" -> {
                vh.userArea.visibility = View.VISIBLE
                vh.content.maxLines = 1
            }
            "news" -> {
                vh.userArea.visibility = View.GONE
                vh.content.maxLines = 2
            }
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val cardView: FrameLayout = v.findViewById(R.id.card_view)
        val titleView: TextView = v.findViewById(R.id.title_view) // 标题
        val releaseTime: TextView = v.findViewById(R.id.release_time) // 发布时间
        val content: TextView = v.findViewById(R.id.content)
        // 新增：用户信息区域
        val circleImageView: CircleImageView = v.findViewById(R.id.circle_image_view)
        val txtNickname: TextView = v.findViewById(R.id.txt_nickname)
        val userArea: View = v.findViewById(R.id.post_user_area)
    }
}
