package io.agora.chatdemo.group.model;

import java.io.Serializable;

public class GroupManageItemBean implements Serializable {
    private int icon;
    private String title;
    private boolean isAlert;

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
}
