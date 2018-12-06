
/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/17
 *
 * 搜索资讯的Fragment
 ****************************************************
 */

package cn.edu.sdu.online.isdu.ui.fragments.search

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment
import cn.edu.sdu.online.isdu.bean.Life
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.net.pack.ServerInfo
import cn.edu.sdu.online.isdu.net.NetworkAccess
import cn.edu.sdu.online.isdu.ui.adapter.LifeItemAdapter
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.util.Logger
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SearchLifeFragment : LazyLoadFragment(){
    private var recyclerView: RecyclerView? = null
    private var loadingLayout: View? = null
    private var blankView: View? = null
    private var dataList =ArrayList<Life>()
    private var search : String? =null
    private var isLoadComplete = false
    private var isLoading = false

    private var adapter: LifeItemAdapter?= null
    private var lastSearchString = ""
    private var searchCall: Call? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search_post, container, false)
        initView(view)
        initRecyclerView()
        return view
    }

    fun initView(view : View){
        recyclerView = view.findViewById(R.id.recycler_view)
        loadingLayout = view.findViewById(R.id.loading_layout)
        blankView = view.findViewById(R.id.blank_view)
    }

    private fun initRecyclerView(){
        adapter = LifeItemAdapter(activity!!, dataList)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }
    fun setSearch(search : String){
        this.search =search
        if(userVisibleHint&&(isLoadComplete || search != lastSearchString)){
            if(searchCall !=null&& !searchCall!!.isCanceled)
                searchCall!!.cancel()
            isLoadComplete = false
            lastSearchString = search
            loadData()
        }
    }

    override fun loadData() {
        super.loadData()
        isLoading = true
        if(search != null){
            onLoading()
            val url = ServerInfo.queryPost(search)
            searchCall = NetworkAccess.buildRequest(url,
                    object : Callback {
                        override fun onFailure(call: Call?, e: IOException?) {
                            activity!!.runOnUiThread{
                                Logger.log(e)
                                Toast.makeText(context,"网络错误", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onResponse(call: Call?, response: Response?) {
                            val json = response?.body()?.string()
                            try {
                                dataList.clear()
                                if(json == "[]") {
                                }else{
                                    val jsonObject = JSONObject(json)
                                    if (jsonObject.getInt("code") == 0) {
                                        val arr = JSONArray(jsonObject.getString("obj"))
                                        for (k in 0 until arr.length()) {
                                            val obj = arr.getJSONObject(k)
                                            val life = Life()
                                            life.lifeId = obj.getInt("id")
                                            //life.commentsNumbers = obj.getInt("commentNumber")//这个也不用
                                            //life.collectNumber = obj.getInt("collectNumber")//这个应该不用
                                            //life.likeNumber = obj.getInt("likeNumber")//这个应该不用
                                            life.uid = obj.getString("uid")
                                            life.title = obj.getString("title")
                                            life.time = obj.getString("time").toLong()
                                            life.content = obj.getString("info")
                                            life.tag = if (obj.has("tag")) obj.getString("tag") else ""
                                            dataList.add(life)
                                        }
                                    }
                                }
                            }catch (e : Exception){
                                Logger.log(e)
                                activity!!.runOnUiThread {
                                    Toast.makeText(context,"网络错误\n" +
                                            "服务器无响应", Toast.LENGTH_SHORT).show()
                                }
                            }
                            activity!!.runOnUiThread {
                                isLoadComplete = true
                                publishData()
                            }
                        }
                    }
            )
        }

    }

    override fun isLoadComplete(): Boolean {
        return isLoadComplete&&lastSearchString == search
    }

    override fun publishData() {
        super.publishData()
        if(dataList.size != 0){
            recyclerView!!.visibility = View.VISIBLE
            blankView!!.visibility = View.GONE
            loadingLayout!!.visibility = View.VISIBLE
        }else{
            recyclerView!!.visibility = View.GONE
            blankView!!.visibility = View.VISIBLE
            loadingLayout!!.visibility = View.GONE
        }
        adapter?.notifyDataSetChanged()
    }

    fun onLoading(){
        recyclerView!!.visibility = View.GONE
        blankView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE
    }

}