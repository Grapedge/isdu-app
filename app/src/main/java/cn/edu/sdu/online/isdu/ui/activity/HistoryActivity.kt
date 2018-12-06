package cn.edu.sdu.online.isdu.ui.activity

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.app.SlideActivity
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.interfaces.PostViewable
import cn.edu.sdu.online.isdu.ui.adapter.PostItemAdapter
import cn.edu.sdu.online.isdu.util.history.HistoryRecord


import kotlinx.android.synthetic.main.activity_history.*


class HistoryActivity : SlideActivity(), View.OnClickListener, PostViewable{

    private var mAdapter: PostItemAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var btnBack: ImageView? = null
    private var btnClear:TextView? = null
    private var blankView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        initView()
        blankView!!.visibility = if (HistoryRecord.historyList.isEmpty()) View.VISIBLE else View.GONE
        initRecyclerView()
    }

    private fun initView() {
        recyclerView = recycler_view
        btnBack = btn_back
        btnClear = clear
        blankView = findViewById(R.id.blank_view)
        btnBack!!.setOnClickListener(this)
        btnClear!!.setOnClickListener(this)
    }

    override fun removeItem(item: Any?) {
        HistoryRecord.historyList.remove(item)
        mAdapter?.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        mAdapter?.notifyDataSetChanged()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_back -> {
                finish()
            }
            R.id.clear -> {
                HistoryRecord.removeAllHistory()
                mAdapter!!.notifyDataSetChanged()
                blankView!!.visibility = View.VISIBLE
            }
        }
    }

    private fun initRecyclerView() {
        HistoryRecord.load()
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        mAdapter = PostItemAdapter(this, HistoryRecord.historyList as List<Post>)
        recyclerView!!.adapter = mAdapter

        if (mAdapter!!.itemCount != 0) {
            blankView!!.visibility = View.GONE
        } else {
            blankView!!.visibility = View.VISIBLE
        }
    }

}
