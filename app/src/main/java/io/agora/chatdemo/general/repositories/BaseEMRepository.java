package io.agora.chatdemo.general.repositories;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatManager;
import io.agora.chat.ChatRoomManager;
import io.agora.chat.ContactManager;
import io.agora.chat.GroupManager;
import io.agora.chat.PushManager;
import io.agora.chat.ThreadManager;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.R;
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
        return ChatClient.getInstance().isSdkInited() && ChatClient.getInstance().isLoggedInBefore();
    }

    /**
     * Whether to log in automatically
     * @return
     */
    public boolean isAutoLogin() {
        return ChatClient.getInstance().isSdkInited() && ChatClient.getInstance().getOptions().getAutoLogin();
    }

    /**
     * Get current user
     * @return
     */
    public String getCurrentUser() {
        return ChatClient.getInstance().getCurrentUser();
    }

    /**
     * ChatManager
     * @return
     */
    public ChatManager getChatManager() {
        return ChatClient.getInstance().chatManager();
    }

    /**
     * ContactManager
     * @return
     */
    public ContactManager getContactManager() {
        return ChatClient.getInstance().contactManager();
    }

    /**
     * GroupManager
     * @return
     */
    public GroupManager getGroupManager() {
        return ChatClient.getInstance().groupManager();
    }

    /**
     * ThreadManager
     * @return
     */
    public ThreadManager getThreadManager() {
        return ChatClient.getInstance().threadManager();
    }

    /**
     * ChatRoomManager
     * @return
     */
    public ChatRoomManager getChatRoomManager() {
        return ChatClient.getInstance().chatroomManager();
    }


    /**
     * PushManager
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
     * Run on UI thread
     * @param runnable
     */
    public void runOnMainThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnMainThread(runnable);
    }

    /**
     * In asynchronous thread
     * @param runnable
     */
    public void runOnIOThread(Runnable runnable) {
        EaseThreadManager.getInstance().runOnIOThread(runnable);
    }

    public Context getContext() {
        return DemoApplication.getInstance().getApplicationContext();
    }

    public String getErrorMsg(int code, String errorMsg) {
        if(code == 408 && !TextUtils.isEmpty(errorMsg) && errorMsg.contains("Unable to resolve host")) {
            errorMsg = DemoApplication.getInstance().getString(R.string.network_disconnect);
        }
        return errorMsg;
    }

}
