package cn.edu.sdu.online.isdu.bean;

public class History {
    private String title;
    private String subject;
    private Long time;
    private String url;

    public History(String title, String subject, Long time, String url) {
        this.title = title;
        this.subject = subject;
        this.time = time;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
