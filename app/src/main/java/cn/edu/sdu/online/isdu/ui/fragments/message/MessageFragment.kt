package cn.edu.sdu.online.isdu.ui.fragments.message

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.interfaces.PostViewable
import cn.edu.sdu.online.isdu.ui.activity.PostDetailActivity
import cn.edu.sdu.online.isdu.util.WeakReferences
import java.lang.ref.WeakReference


class MessageFragment : Fragment(), PostViewable {

    private var dataList: MutableList<Post> = arrayListOf()
    private var adapter: MyAdapter? = null
    private var loadingLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var blankView: TextView? = null
    private var isLoadComplete = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        initView(view)
        loadData()
        initRecyclerView()
        return view
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter(dataList)
        recyclerView!!.adapter = adapter
    }

    private fun initView(view: View) {
        loadingLayout = view.findViewById(R.id.loading_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
        blankView = view.findViewById(R.id.blank_view)
    }

     fun isLoadComplete(): Boolean {
         return isLoadComplete
    }

     fun loadData() {
         onLoading()
        isLoadComplete = true
        publishData()
    }

    override fun removeItem(item: Any?) {
//        dataList.remove(item as Post)
//        adapter?.notifyDataSetChanged()
    }

    fun publishData() {
        if(dataList.size!= 0){
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
    inner class MyAdapter(mDataList: List<Post>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        private var mDataList = mDataList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = mDataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = mDataList[position]
            holder.itemLayout.setOnClickListener {
                WeakReferences.postViewableWeakReference = WeakReference(this@MessageFragment)
                activity!!.startActivity(Intent(activity, PostDetailActivity::class.java)
                        .putExtra("tag", TAG))

            }
//            holder.title_flag.text = post.title_flag
//            holder.titleView.text = post.title
//            holder.user_name.text = post.userName
//            holder.comments_number.text = post.comments_numbers.toString()

            val timeDelta:Long = System.currentTimeMillis()-post.time
            if(((timeDelta/1000/60).toInt())<1){
                holder.release_time.text ="刚刚"
            }else if((timeDelta/1000/60)<60){
                holder.release_time.text =(((timeDelta/1000/60).toInt()).toString()+"分钟前")
            }else if((timeDelta/1000/60/60)<24){
                holder.release_time.text =(((timeDelta/1000/60/60).toInt()).toString() + "小时前")
            }else if((timeDelta/1000/60/60/24)<30){
                holder.release_time.text =(((timeDelta/1000/60/60/24).toInt()).toString() + "天前")
            }else if((timeDelta/1000/60/60/24/30)<12){
                holder.release_time.text =(((timeDelta/1000/60/60/24/30).toInt()).toString() + "月前")
            }else {
                holder.release_time.text =(((timeDelta/1000/60/60/24/365).toInt()).toString() + "年前")
            }
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val itemLayout: FrameLayout = view.findViewById(R.id.card_view)
            var title_flag: TextView = view.findViewById(R.id.title_flag)
            val titleView: TextView = view.findViewById(R.id.title_view)
            val contentLayout: LinearLayout = view.findViewById(R.id.content_layout)
            var user_name: TextView = view.findViewById(R.id.user_name)
            var comments_number: TextView = view.findViewById(R.id.comments_number)
            var release_time: TextView = view.findViewById(R.id.release_time)
        }
    }

    companion object {
        const val TAG = "MessageFragment"
    }
}