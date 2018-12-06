package cn.edu.sdu.online.isdu.util.history

import android.content.Context
import android.util.Log
import cn.edu.sdu.online.isdu.app.MyApplication
import cn.edu.sdu.online.isdu.app.ThreadPool
import cn.edu.sdu.online.isdu.bean.AbstractCollectible
import cn.edu.sdu.online.isdu.bean.Post
import cn.edu.sdu.online.isdu.interfaces.Collectible
import com.alibaba.fastjson.JSON
import java.util.*
import kotlin.collections.HashSet

object HistoryRecord {
    var historyList: LinkedList<AbstractCollectible> = LinkedList() // 采用链表，方便进行插入移动操作
    private var historySet: MutableSet<AbstractCollectible> = HashSet()

    fun newHistory(collectible: AbstractCollectible) {
//        load()
        if (!collectible.getmTitle().isNullOrEmpty()) {
            if (!historySet.contains(collectible)) {
                historyList.addFirst(collectible)
                historySet.add(collectible)
            } else {
                historyList.remove(collectible)
                historyList.addFirst(collectible)
            }
            save()
        }
    }

    fun removeHistory(collectible: AbstractCollectible) {
        load()
        if (historySet.contains(collectible)) {
            historySet.remove(collectible)
            historyList.remove(collectible)
        }
        save()
    }

    fun removeAllHistory() {
        load()
        historySet.clear()
        historyList.clear()
        save()
    }

    private fun save() {
        ThreadPool.execute {
            synchronized(historyList) {
                val editor = MyApplication.getContext().getSharedPreferences("history", Context.MODE_PRIVATE).edit()
                editor.putString("json", JSON.toJSONString(historyList))
                editor.apply()
            }
        }
    }

    fun load() {
        val sp = MyApplication.getContext().getSharedPreferences("history", Context.MODE_PRIVATE)
        val str = sp.getString("json", "")
        if (str != "") {
            for (item in JSON.parseArray(str, Post::class.java)) {
                if (item.postId != 0 && item.title != null) {
                    newHistory(item)
                }
            }
            historySet.addAll(historyList)
        }
    }

}