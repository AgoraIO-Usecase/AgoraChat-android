package io.agora.chatdemo.chatthread.bean;

import java.io.Serializable;

public class MenuItemBean implements Serializable {
    private int icon;
    private String title;
    private boolean isAlert;
    private int id;

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public void setAlert(boolean alert) {
        isAlert = alert;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
