package cn.edu.sdu.online.isdu.bean;

import java.util.Objects;

import cn.edu.sdu.online.isdu.interfaces.Collectible;
import cn.edu.sdu.online.isdu.util.history.HistoryRecord;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/5/26
 *
 * 论坛帖子的Java Bean
 ****************************************************
 */

public class Confession extends AbstractPost {

    private int confessionId; // 帖子ID
    private int type; // 论坛内容类型
    private String titleFlag;
    //    private String title;
//    private String uid;
    private int commentsNumbers;
    //    private Long time;
//    private String content; // 具体内容
    private int likeNumber;
    private int collectNumber;
    private double value;
    private String tag;

    public Confession() {}

    public Confession(int type, String content) {
        this.type = type;
//        this.content = content;
        this.mContent = content;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getConfessionId() {
        return confessionId;
    }

    public void setConfessionId(int confessionId) {
        this.confessionId = confessionId;
    }

    public int getLikeNumber() {
        return likeNumber;
    }

    public void setLikeNumber(int likeNumber) {
        this.likeNumber = likeNumber;
    }

    public int getCollectNumber() {
        return collectNumber;
    }

    public void setCollectNumber(int collectNumber) {
        this.collectNumber = collectNumber;
    }

    public String getTitleFlag() {
        return titleFlag;
    }

    public void setTitleFlag(String titleFlag) {
        this.titleFlag = titleFlag;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getUid() {
        return mUserId + "";
    }

    public void setUid(String uid) {
        this.mUserId = Integer.parseInt(uid);
    }

    public int getCommentsNumbers() {
        return commentsNumbers;
    }

    public void setCommentsNumbers(int commentsNumbers) {
        this.commentsNumbers = commentsNumbers;
    }

    public Long getTime() {
        return mTime;
    }

    public void setTime(Long time) {
        this.mTime = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Confession confession = (Confession) o;
        return confessionId == confession.confessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(confessionId);
    }

    @Override
    public void onCollect() {

    }

    @Override
    public void onScan() {
        HistoryRecord.INSTANCE.newHistory(this);
    }
}
