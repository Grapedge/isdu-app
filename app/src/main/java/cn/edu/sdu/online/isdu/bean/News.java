package cn.edu.sdu.online.isdu.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.edu.sdu.online.isdu.util.Logger;
import cn.edu.sdu.online.isdu.util.history.HistoryRecord;

/**
 ****************************************************
 * @author zsj
 * Last Modifier: ZSJ
 * Last Modify Time: 2018/7/15
 *
 * 新闻实体类
 ****************************************************
 */

public class News extends AbstractNews {

//    private String title; // 新闻标题
//    private String briefContent; // 新闻内容摘要
    private String newsContent; // 新闻完整内容
    private String date; // 新闻日期
    private String source; // 新闻来源或类别
    private String section; // 新闻所属板块
    private String url; // 新闻链接地址
    private List<String> extras; // 附件名称
    private List<String> extraUrl; // 附件下载地址
    private String originUrl; // 新闻原始链接

    public News() {
        super();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getBriefContent() {
        return mContent;
    }

    public void setBriefContent(String briefContent) {
        this.mContent = briefContent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
        if (date != null && !date.trim().equals("")) {
            String y = date.substring(0, 4);
            String m = date.substring(5, 7);
            String d = date.substring(8);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Integer.parseInt(y), Integer.parseInt(m), Integer.parseInt(d),
                    0, 0, 0);
            mTime = calendar.getTimeInMillis();
        }

    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getExtras() {
        return extras;
    }

    public void setExtras(List<String> extras) {
        this.extras = extras;
    }

    public List<String> getExtraUrl() {
        return extraUrl;
    }

    public void setExtraUrl(List<String> extraUrl) {
        this.extraUrl = extraUrl;
    }

    public String getNewsContent() {
        return newsContent;
    }

    public void setNewsContent(String newsContent) {
        this.newsContent = newsContent;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public static News loadFromString(String json) {
        News news = new News();
        try {
            JSONObject jsonObject = new JSONObject(json);
            news.section = jsonObject.getString("site");
            news.mTitle = jsonObject.getString("title");
            news.setDate(jsonObject.getString("date"));
            news.newsContent = jsonObject.getString("content");
            news.originUrl = jsonObject.getString("url");
            if (jsonObject.getJSONArray("attachment") != null &&
                    jsonObject.getJSONArray("attachment").length() > 0) {
                JSONArray jsonArray = jsonObject.getJSONArray("attachment");
                List<String> names = new ArrayList<>();
                List<String> urls = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = jsonArray.getJSONObject(i);
                    names.add(obj.getString("title"));
                    urls.add(obj.getString("url"));
                }
                news.extras = names;
                news.extraUrl = urls;
            }
        } catch (Exception e) {
            Logger.log(e);
        }
        return news;
    }

    @Override
    public void onCollect() {

    }

    @Override
    public void onScan() {
//        HistoryRecord.INSTANCE.newHistory(this);
    }
}
