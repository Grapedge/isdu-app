package cn.edu.sdu.online.isdu.ui.fragments.main

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.News
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.NewsActivity
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import cn.edu.sdu.online.isdu.util.download.IDownloadItem
import org.json.JSONArray

class NewsContentFragment : LazyLoadFragment() {

    private var recyclerView: RecyclerView? = null
    private val sectionName = listOf("学生在线", "本科生院", "青春山大", "山大视点")
    private var index = 0
    private var dataList: MutableList<News> = ArrayList()
    private var adapter: MyAdapter? = null
    private var blankView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_news_content, container, false)

        initView(view)

        return view
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        blankView = view.findViewById(R.id.blank_view)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter(dataList)
        recyclerView!!.adapter = adapter
    }

    override fun loadData() {
        NetworkAccess.cache(ServerInfo.getNewsUrl(index)) { success, cachePath ->
            if (success) {
                try {
                    val jsonArray = JSONArray(FileUtil.getStringFromFile(cachePath))

                    synchronized(dataList) {
                        dataList.clear()

                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)
                            val news = News()
                            news.title = jsonObj.getString("title")
                            news.date = jsonObj.getString("date")
                            news.source = jsonObj.getString("block")
                            news.originUrl = jsonObj.getString("url")
                            news.url = ServerInfo.getNewsUrl(index, i)
                            dataList.add(news)
                        }

                        activity?.runOnUiThread {
                            publishData()
                        }
                    }

                } catch (e: Exception) {
                    Logger.log(e)
                }
            } else {
                activity?.runOnUiThread {
                    publishData()
                }
            }
        }
    }

    override fun publishData() {
        if (dataList.isNotEmpty()) {
            blankView?.visibility = View.GONE
        } else {
            blankView?.visibility = View.VISIBLE
            blankView?.text = "加载失败"
        }

        adapter!!.notifyDataSetChanged()
    }

    override fun isLoadComplete(): Boolean = dataList.isNotEmpty()

    private fun setArguments(index: Int) {
        this.index = index
    }

    inner class MyAdapter(mDataList: List<News>) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private var mDataList = mDataList
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.news_item, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = mDataList.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val news = mDataList[position]

                if (news.source == "办公文件") {
                    holder.itemLayout.setOnClickListener {
                        IDownloadItem(news.originUrl).startDownload()
                    }
                } else {
                    holder.itemLayout.setOnClickListener {
                        activity!!.startActivity(Intent(activity, NewsActivity::class.java)
                                .putExtra("section", sectionName[index])
                                .putExtra("url", news.url))
                    }
                }

                holder.newsDate.text = news.date
                holder.newsSource.text = news.source
                holder.newsTitle.text = news.title
            } catch (e: Exception) {}

        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var itemLayout: View = view.findViewById(R.id.item_layout)
            var newsTitle: TextView = view.findViewById(R.id.news_item_title)
            var newsSource: TextView = view.findViewById(R.id.news_source)
            var newsDate: TextView = view.findViewById(R.id.news_date)
        }


    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("index", index)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null)
            index = savedInstanceState.getInt("index", 0)
    }

    companion object {
        fun newInstance(index: Int): NewsContentFragment {
            val fragment = NewsContentFragment()
            fragment.setArguments(index)
            return fragment
        }
    }
}