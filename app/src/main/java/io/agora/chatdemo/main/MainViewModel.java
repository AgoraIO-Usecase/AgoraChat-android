package io.agora.chatdemo.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import io.agora.chat.Conversation;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;


public class MainViewModel extends AndroidViewModel {
    private SingleSourceLiveData<Conversation> conversationObservable;
    private SingleSourceLiveData<Integer> switchObservable;
    private MutableLiveData<String> homeUnReadObservable;
    private EaseNotificationMsgManager msgManager;

    public MainViewModel(@NonNull Application application) {
        super(application);
        conversationObservable = new SingleSourceLiveData<>();
        switchObservable = new SingleSourceLiveData<>();
        homeUnReadObservable = new MutableLiveData<>();
        msgManager= EaseNotificationMsgManager.getInstance();
    }
    public SingleSourceLiveData<Conversation> getConversationObservable() {
        return conversationObservable;
    }

    public void getMsgConversation() {
        conversationObservable.setValue(msgManager.getConversation());
    }

    public LiveData<Integer> getSwitchObservable() {
        return switchObservable;
    }

    /**
     * 设置可见的fragment
     * @param title
     */
    public void setVisibleFragment(Integer title) {
        switchObservable.setValue(title);
    }

    public LiveData<String> homeUnReadObservable() {
        return homeUnReadObservable;
    }

    public LiveDataBus messageChangeObservable() {
        return LiveDataBus.get();
    }

    public void checkUnreadMsg() {
        int unreadMessageCount = DemoHelper.getInstance().getChatManager().getUnreadMessageCount();
        int unreadSysMsgCount = msgManager.getConversation().getUnreadMsgCount();
        String count = getUnreadCount(unreadMessageCount-unreadSysMsgCount);
        homeUnReadObservable.postValue(count);
    }

    /**
     * 获取未读消息数目
     * @param count
     * @return
     */
    private String getUnreadCount(int count) {
        if(count <= 0) {
            return null;
        }
        if(count > 99) {
            return "99+";
        }
        return String.valueOf(count);
    }

}
