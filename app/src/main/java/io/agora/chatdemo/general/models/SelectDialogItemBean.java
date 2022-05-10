package io.agora.chatdemo.general.models;

import java.io.Serializable;

public class SelectDialogItemBean implements Serializable {
    private int id;
    private int icon;
    private String title;
    private boolean isAlert;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
