package io.agora.chatdemo.contact.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import io.agora.chat.Conversation;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;

public class ContactsViewModel extends AndroidViewModel {
    private SingleSourceLiveData<Conversation> conversationObservable;
    private EaseNotificationMsgManager msgManager;

    public ContactsViewModel(@NonNull Application application) {
        super(application);
        conversationObservable = new SingleSourceLiveData<>();
        msgManager=EaseNotificationMsgManager.getInstance();
    }

    public SingleSourceLiveData<Conversation> getConversationObservable() {
        return conversationObservable;
    }

    public void getMsgConversation() {
        conversationObservable.setValue(msgManager.getConversation());
    }
}
