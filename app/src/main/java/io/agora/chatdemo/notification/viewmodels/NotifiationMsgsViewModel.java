package io.agora.chatdemo.notification.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;


public class NotifiationMsgsViewModel extends AndroidViewModel {

    private SingleSourceLiveData<List<ChatMessage>> chatMessageObservable;

    private EaseNotificationMsgManager msgManager;
    private LiveDataBus messageObservable;


    public NotifiationMsgsViewModel(@NonNull Application application) {
        super(application);
        chatMessageObservable = new SingleSourceLiveData<>();

        msgManager=EaseNotificationMsgManager.getInstance();
        messageObservable = LiveDataBus.get();
    }


    public SingleSourceLiveData<List<ChatMessage>> getChatMessageObservable() {
        return chatMessageObservable;
    }

    public void getAllMessages(){
        chatMessageObservable.setValue(msgManager.getAllMessages());
    }


    public void searchMsgs(String content) {

    }

    public void setMessageChange(EaseEvent change) {
        messageObservable.with(change.event).postValue(change);
    }

    public LiveDataBus getMessageChange() {
        return messageObservable;
    }
}
