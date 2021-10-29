package io.agora.chatdemo.notification.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;

/**
 * Created by 许成谱 on 2021/10/28 0028 19:11.
 * qq:1550540124
 * 热爱生活每一天！
 */
public class NotifiationMsgsViewModel extends AndroidViewModel {

    private SingleSourceLiveData<List<ChatMessage>> chatMessageObservable;
    private EaseNotificationMsgManager msgManager;


    public NotifiationMsgsViewModel(@NonNull Application application) {
        super(application);
        chatMessageObservable = new SingleSourceLiveData<>();
        msgManager=EaseNotificationMsgManager.getInstance();
    }

    public SingleSourceLiveData<List<ChatMessage>> getChatMessageObservable() {
        return chatMessageObservable;
    }

    public void getChatMessages(){
        chatMessageObservable.setValue(msgManager.getAllMessages());
    }

    public void searchMsgs(String content) {

    }
}
