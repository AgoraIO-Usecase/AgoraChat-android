package io.agora.chatdemo.chat.models;

public class UrlPreViewBean {
    private String title = ""; // 标题
    private String describe = ""; // 内容
    private String primaryImg = ""; // 主图

    public String getPrimaryImg() {
        return primaryImg;
    }

    public void setPrimaryImg(String primaryImg) {
        this.primaryImg = primaryImg;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String content) {
        this.describe = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "UrlPreViewBean{" +
                "title='" + title + '\'' +
                ", content='" + describe + '\'' +
                ", primaryImg='" + primaryImg + '\'' +
                '}';
    }
}
