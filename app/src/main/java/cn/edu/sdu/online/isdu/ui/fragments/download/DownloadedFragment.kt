package cn.edu.sdu.online.isdu.ui.fragments.download

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView

import cn.edu.sdu.online.isdu.R
import cn.edu.sdu.online.isdu.ui.design.dialog.OptionDialog
import cn.edu.sdu.online.isdu.util.Settings
import cn.edu.sdu.online.isdu.util.download.Download
import java.io.File

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/17
 *
 * 下载完成任务的Fragment
 ****************************************************
 */

class DownloadedFragment : Fragment() {
    private var btnClearAll: View? = null
    private var recyclerView: RecyclerView? = null

    var adapter = ItemAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_downloaded, container, false)
        initView(view)
        initRecyclerView()
        return view
    }

    private fun initView(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view)
        btnClearAll = view.findViewById(R.id.btn_clear_all)

        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter

        btnClearAll!!.setOnClickListener {
            val list = listOf("仅删除任务", "删除任务和文件")
            val dialog = OptionDialog(context!!, list)
            dialog.setMessage("清空所有下载任务")
            dialog.setOnItemSelectListener {itemName ->  
                when (itemName) {
                    "仅删除任务" -> {
                        for (item in Download.getDownloadedIdList()) {
                            Download.remove(item)
                        }
                        adapter.notifyDataSetChanged()
                    }
                    "删除任务和文件" -> {
                        for (item in Download.getDownloadedIdList()) {
                            val file = File(Settings.DEFAULT_DOWNLOAD_LOCATION + Download.get(item).fileName)
                            if (file.exists()) file.delete()
                            Download.remove(item)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
            dialog.show()
        }
    }

    private fun initRecyclerView() {
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        recyclerView!!.adapter = adapter
    }

    /**
     * 直接从Download获取下载列表
     */
    inner class ItemAdapter : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_download, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount(): Int = Download.getDownloadedIdList().size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = Download.get(Download.getDownloadedIdList()[position])
            holder.fileName.text = item.fileName

            if (!File(Settings.DEFAULT_DOWNLOAD_LOCATION + item.fileName).exists()) {
                holder.fileName.setTextColor(0xFF808080.toInt())
                holder.txtStatus.text = "本地文件已删除"
                holder.itemLayout.setOnClickListener {
                    item.startDownload()
                    notifyDataSetChanged()
                }
                holder.btnStartPause.visibility = View.VISIBLE
                holder.btnStartPause.text = "重新下载"
                holder.btnStartPause.setOnClickListener {
                    item.startDownload()
                    notifyDataSetChanged()
                }
            } else {
                holder.fileName.setTextColor(0xFF131313.toInt())
                holder.btnStartPause.visibility = View.GONE
                holder.btnCancel.visibility = View.GONE

                holder.txtStatus.text = "下载成功，点击打开"
                holder.txtProgress.visibility = View.GONE
                holder.progressBar.visibility = View.GONE
                holder.itemLayout.setOnClickListener {
                    item.open()
                }
            }

            holder.progressBar.visibility = View.GONE
            holder.txtProgress.visibility = View.GONE
            holder.btnCancel.visibility = View.GONE

            holder.btnClear.setOnClickListener {
                val dialog = OptionDialog(context!!, listOf("仅删除任务", "删除任务和文件"))
                dialog.setMessage(item.fileName)
                dialog.setCancelOnTouchOutside(true)
                dialog.setOnItemSelectListener { itemName ->
                    when (itemName) {
                        "仅删除任务" -> {

                            Download.remove(item.notifyId)

                            adapter.notifyDataSetChanged()
                        }
                        "删除任务和文件" -> {

                            val file = File(Settings.DEFAULT_DOWNLOAD_LOCATION +
                                    Download.get(item.notifyId).fileName)
                            if (file.exists()) file.delete()
                            Download.remove(item.notifyId)

                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                dialog.show()
            }

        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var fileName: TextView = view.findViewById(R.id.file_name)
            var itemLayout: View = view.findViewById(R.id.item_layout)
            var progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
            var txtProgress: TextView = view.findViewById(R.id.txt_progress)
            var txtStatus: TextView = view.findViewById(R.id.item_status)
            var btnStartPause: TextView = view.findViewById(R.id.btn_start_pause)
            var btnCancel: TextView = view.findViewById(R.id.btn_cancel)
            var btnClear: TextView = view.findViewById(R.id.btn_clear)
        }
    }
}
