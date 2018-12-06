package cn.edu.sdu.online.isdu.ui.fragments.message

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.LazyLoadFragment

class ChatFragment : Fragment(){

    private var loadingLayout: View? = null
    private var recyclerView: RecyclerView? = null
    private var blankView: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        initView(view)
        //loadData()
        //initRecyclerView()
        recyclerView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.GONE
        blankView!!.visibility = View.VISIBLE
        return view
    }

    private fun initRecyclerView() {
    }

    private fun initView(view: View) {
        loadingLayout = view.findViewById(R.id.loading_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
        blankView = view.findViewById(R.id.blank_view)
    }

    fun isLoadComplete(): Boolean {
        return false
        //TODO isLoadComplete
    }

    fun loadData() {
        //TODO loadData
        publishData()
    }

    fun publishData() {
        //TODO publishData
    }

    fun onLoading(){
        recyclerView!!.visibility = View.GONE
        loadingLayout!!.visibility = View.VISIBLE
        blankView!!.visibility = View.GONE
    }
}