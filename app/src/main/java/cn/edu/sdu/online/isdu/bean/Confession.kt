package cn.edu.sdu.online.isdu.bean

import java.util.Objects

import cn.edu.sdu.online.isdu.interfaces.Collectible
import cn.edu.sdu.online.isdu.util.history.HistoryRecord

/***********************************************************************
 * @author Grapes
 * @date 2018-12-13
 * @Description 表白帖子信息
 ***********************************************************************/

class Confession : AbstractPost() {

    var confessionId: Int = 0 // tid
    var commentCount: Int = 0   // the number of comments
    var postType: Int = 0

    var likeCount: Int = 0      // the number of likes
    var value: Double = 0.0
    var tag: String = ""     // the tag of it, maybe it's useless

    var title: String
        get() = mTitle
        set(title) {
            this.mTitle = title
        }

    var uid: String
        get() = mUserId.toString()
        set(uid) {
            this.mUserId = Integer.parseInt(uid)
        }

    var postContent: String
        get() = mContent
        set(value) {
            mContent = value
        }

    var time: Long
        get() = mTime
        set(time) {
            this.mTime = time
        }

    // temporarily unavailable
    /*
    constructor(type: Int, content: String) : this() {
        postType = type
        mContent = content
    }
    */

    override fun getType(): Int {
        return postType
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val confession = o as Confession?
        return confessionId == confession!!.confessionId
    }

    override fun hashCode(): Int {
        return Objects.hash(confessionId)
    }

    override fun onCollect() {

    }

    override fun onScan() {
        HistoryRecord.newHistory(this)
    }
}
