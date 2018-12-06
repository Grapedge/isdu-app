package cn.edu.sdu.online.isdu.ui.fragments.message

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.bean.Message
import cn.edu.sdu.online.isdu.bean.News
import cn.edu.sdu.online.isdu.ui.activity.MyHomePageActivity
import cn.edu.sdu.online.isdu.ui.activity.NewsActivity
import cn.edu.sdu.online.isdu.ui.activity.PostDetailActivity
import cn.edu.sdu.online.isdu.util.DateCalculate
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat


class NotificationFragment : Fragment(){

    private var adapter: MyAdapter? = null
    private var loadingLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var blankView: TextView? = null
    private var isLoadComplete = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        initView(view)
        loadData()
        initRecyclerView()
        return view
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter()
        recyclerView!!.adapter = adapter
    }

    private fun initView(view: View) {
        loadingLayout = view.findViewById(R.id.loading_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
        blankView = view.findViewById(R.id.blank_view)
    }



     fun loadData() {
         onLoading()
         isLoadComplete = true
         publishData()
    }

     fun publishData() {
         if(Message.msgList.size!= 0){
             recyclerView!!.visibility = View.VISIBLE
             loadingLayout!!.visibility = View.GONE
             blankView!!.visibility = View.GONE
         }else{
             recyclerView!!.visibility = View.GONE
             loadingLayout!!.visibility = View.GONE
             blankView!!.visibility = View.VISIBLE
         }
         if(adapter!=null){
             adapter!!.notifyDataSetChanged()
         }
    }

    fun onLoading(){
        recyclerView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE
        blankView!!.visibility = View.GONE
    }

    inner class MyAdapter : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_comment_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = Message.msgList.size

        override fun getItemId(position: Int): Long = position.toLong()

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val msg = Message.msgList[position]
            if (msg.type == "beLikeUser") {
                holder.itemLayout.setOnClickListener {
//                    msg.setRead(true, context)
//                    notifyItemChanged(position)
                    activity!!.startActivity(Intent(activity, MyHomePageActivity::class.java)
                            .putExtra("id", msg.senderId.toInt()))
                }
            } else {
                holder.itemLayout.setOnClickListener {
//                    msg.setRead(true, context)
//                    notifyItemChanged(position)
                    activity!!.startActivity(Intent(activity, PostDetailActivity::class.java)
                            .putExtra("id", msg.postId))
                }
            }

//            if (msg.isRead) {
//                holder.content.setTextColor(context!!.resources.getColor(R.color.colorSecondaryText))
//            } else {
//                holder.content.setTextColor(context!!.resources.getColor(R.color.colorPrimaryText))
//            }

            holder.content.setTextColor(context!!.resources.getColor(R.color.colorPrimaryText))

            holder.nickname.text = msg.senderNickname
            Glide.with(context!!).load(msg.senderAvatar)
                    .apply(RequestOptions.skipMemoryCacheOf(false))
                    .into(holder.circleImageView)
            holder.time.text = DateCalculate.getExpressionDate(msg.time.toLong())
            holder.content.text = msg.content
//            when (msg.type) {
//                "beLikeUser" -> {
//                    holder.content.text = msg.content
//                }
//                "beLikePost" -> {
//
//                }
//                "beCommentPost" -> {
//
//                }
//            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemLayout: View = view.findViewById(R.id.item_layout)
            var circleImageView: CircleImageView = view.findViewById(R.id.circle_image_view)
            var nickname: TextView = view.findViewById(R.id.txt_nickname)
            val content: TextView = view.findViewById(R.id.txt_content)
            val time: TextView = view.findViewById(R.id.txt_time)
        }
    }
}