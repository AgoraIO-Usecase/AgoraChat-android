package io.agora.chatdemo.general.db.entity;

import android.text.TextUtils;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import java.io.Serializable;

import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.dao.InviteMessageDao;

@Entity(tableName = "em_msg_type", primaryKeys = {"id"},
        indices = {@Index(value = {"type"}, unique = true)})
public class MsgTypeManageEntity implements Serializable {
    private int id;
    private String type;
    private String extField;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExtField() {
        return extField;
    }

    public void setExtField(String extField) {
        this.extField = extField;
    }

    @Ignore
    public Object getLastMsg() {
        if(TextUtils.equals(type, msgType.NOTIFICATION.name())) {
            InviteMessageDao inviteMessageDao = DemoDbHelper.getInstance(DemoApplication.getInstance()).getInviteMessageDao();
            return inviteMessageDao == null ? null : inviteMessageDao.lastInviteMessage();
        }
        return null;
    }

    public int getUnReadCount() {
        if(TextUtils.equals(type, msgType.NOTIFICATION.name())) {
            InviteMessageDao inviteMessageDao = DemoDbHelper.getInstance(DemoApplication.getInstance()).getInviteMessageDao();
            return inviteMessageDao == null ? 0 : inviteMessageDao.queryUnreadCount();
        }
        return 0;
    }

    public enum msgType {

        /**
         * 通知
         */
        NOTIFICATION
    }
}
