package cn.edu.sdu.online.isdu.ui.fragments.search

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.algorithm.Search
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.News
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.activity.NewsActivity
import cn.edu.sdu.online.isdu.util.FileUtil
import cn.edu.sdu.online.isdu.util.Logger
import okhttp3.Call
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/17
 *
 * 搜索资讯的Fragment
 ****************************************************
 */

class SearchNewsFragment : LazyLoadFragment() {

    private var dataList: MutableList<MutableList<News>> = ArrayList()

    val list: MutableList<News> = LinkedList()

    private var adapter: MyAdapter? = null
    private var loadingLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var blankView: TextView? = null
    private var search : String? = null
    private var isLoadComplete = false
    private val section = listOf("sduOnline","underGraduate","sduYouth","sduView")
    private val sectionName = listOf("学生在线", "本科生院", "青春山大", "山大视点")

    private var lastSearchString = ""

    private var searchCall: Call? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_news, container, false)

        dataList.clear()
        dataList.add(LinkedList())
        dataList.add(LinkedList())
        dataList.add(LinkedList())
        dataList.add(LinkedList())

        initView(view)
        initRecyclerView()
        return view
    }


    fun setSearch(search: String?){
        this.search = search
        if (userVisibleHint && (isLoadComplete || lastSearchString != search)){
            if (searchCall != null && !searchCall!!.isCanceled) searchCall!!.cancel()
            isLoadComplete = false
            lastSearchString = search!!
            loadData()
        }
    }

    private fun initView(view: View) {
        loadingLayout = view.findViewById(R.id.loading_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
        blankView = view.findViewById(R.id.blank_view)
    }

    override fun isLoadComplete(): Boolean = isLoadComplete && lastSearchString == search

    override fun loadData() {
        dataList.clear()
        dataList.add(LinkedList())
        dataList.add(LinkedList())
        dataList.add(LinkedList())
        dataList.add(LinkedList())
        pullData(0)
    }

    private fun pullData(index: Int) {
        if (index >= section.size) {
            publishData()
            return
        }
        if (search == null) return
//        val pattern = Pattern.compile(search, Pattern.CASE_INSENSITIVE)
        searchCall = NetworkAccess.cache(ServerInfo.getNewsUrl(index)) { success, cachePath ->
            if (success) {
                try {
                    val str = FileUtil.getStringFromFile(cachePath)
                    dataList[index].clear()
                    if (str != "") {
                        val jsonArray = JSONArray(str)

                        for (i in 0 until jsonArray.length()) {
                            val jsonObj = jsonArray.getJSONObject(i)

                            val title = jsonObj.getString("title")
//                            val matcher1 = pattern.matcher(title)

                            val proximity = Search.calculateProximity(title, search)

                            if (proximity > 0) {
                                val news = News()
                                news.title = title
                                news.date = jsonObj.getString("date")
                                news.source = jsonObj.getString("block")
                                news.section = sectionName[index]
                                news.url = ServerInfo.getNewsUrl(index, i)
                                dataList[index].add(news)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Logger.log(e)
                }


            }

            activity!!.runOnUiThread {
                pullData(index + 1)
            }
        }

    }

    override fun publishData() {
        isLoadComplete = true

        list.clear()
        list.addAll(dataList[0])
        list.addAll(dataList[1])
        list.addAll(dataList[2])
        list.addAll(dataList[3])

        if(list.size != 0){
            recyclerView!!.visibility = View.VISIBLE
            loadingLayout!!.visibility = View.GONE
            blankView!!.visibility = View.GONE
        }else{
            recyclerView!!.visibility = View.GONE
            loadingLayout!!.visibility = View.GONE
            blankView!!.visibility = View.VISIBLE
        }
        if(adapter != null){
            adapter!!.notifyDataSetChanged()
        }
    }

    fun onLoading(){
        recyclerView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE
        blankView!!.visibility = View.GONE
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        adapter = MyAdapter(list)
        recyclerView!!.adapter = adapter
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
            val news = mDataList[position]
            holder.itemLayout.setOnClickListener {
                //clear()
                //(activity as SearchActivity).editSearch!!.setText("")
                activity!!.startActivity(Intent(activity, NewsActivity::class.java)
                        .putExtra("section", news.section)
                        .putExtra("url", news.url))
            }

            holder.newsDate.text = news.date
            holder.newsSource.text = news.source + "-" + news.section
            holder.newsTitle.text = news.title
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var itemLayout: View = view.findViewById(R.id.item_layout)
            var newsTitle: TextView = view.findViewById(R.id.news_item_title)
            var newsSource: TextView = view.findViewById(R.id.news_source)
            var newsDate: TextView = view.findViewById(R.id.news_date)
        }
    }

}