package io.agora.chatdemo.general.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatManager;
import io.agora.chat.ChatRoomManager;
import io.agora.chat.ContactManager;
import io.agora.chat.GroupManager;
import io.agora.chat.PushManager;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.dao.EmUserDao;

public class BaseEMRepository {

    /**
     * return a new liveData
     * @param item
     * @param <T>
     * @return
     */
    public <T> LiveData<T> createLiveData(T item) {
        return new MutableLiveData<>(item);
    }

    /**
     * login before
     * @return
     */
    public boolean isLoggedIn() {
        return ChatClient.getInstance().isLoggedInBefore();
    }

    /**
     * 获取本地标记，是否自动登录
     * @return
     */
    public boolean isAutoLogin() {
        return ChatClient.getInstance().getOptions().getAutoLogin();
    }

    /**
     * 获取当前用户
     * @return
     */
    public String getCurrentUser() {
        return ChatClient.getInstance().getCurrentUser();
    }

    /**
     * EMChatManager
     * @return
     */
    public ChatManager getChatManager() {
        return ChatClient.getInstance().chatManager();
    }

    /**
     * EMContactManager
     * @return
     */
    public ContactManager getContactManager() {
        return ChatClient.getInstance().contactManager();
    }

    /**
     * EMGroupManager
     * @return
     */
    public GroupManager getGroupManager() {
        return ChatClient.getInstance().groupManager();
    }

    /**
     * EMChatRoomManager
     * @return
     */
    public ChatRoomManager getChatRoomManager() {
        return ChatClient.getInstance().chatroomManager();
    }


    /**
     * EMPushManager
     * @return
     */
    public PushManager getPushManager() {
        return ChatClient.getInstance().pushManager();
    }

    /**
     * init room
     */
    public void initDb() {
        DemoDbHelper.getInstance(DemoApplication.getInstance()).initDb(getCurrentUser());
    }

    /**
     * EmUserDao
     * @return
     */
    public EmUserDao getUserDao() {
        return DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao();
    }

    /**
     * 在主线程执行
     * @param runnable
     */
    public void runOnMainThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * 在异步线程
     * @param runnable
     */
    public void runOnIOThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnIOThread(runnable);
    }

    public Context getContext() {
        return DemoApplication.getInstance().getApplicationContext();
    }

}
