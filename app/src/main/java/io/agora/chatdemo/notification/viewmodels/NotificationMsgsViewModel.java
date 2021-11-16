package io.agora.chatdemo.notification.viewmodels;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.MessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.exceptions.ChatException;


public class NotificationMsgsViewModel extends AndroidViewModel {

    private SingleSourceLiveData<List<ChatMessage>> chatMessageObservable;
    private SingleSourceLiveData<List<ChatMessage>> searchResultObservable;

    private EaseNotificationMsgManager msgManager;
    private LiveDataBus messageObservable;


    public NotificationMsgsViewModel(@NonNull Application application) {
        super(application);
        chatMessageObservable = new SingleSourceLiveData<>();
        searchResultObservable = new SingleSourceLiveData<>();
        msgManager=EaseNotificationMsgManager.getInstance();
        messageObservable = LiveDataBus.get();
    }


    public SingleSourceLiveData<List<ChatMessage>> getChatMessageObservable() {
        return chatMessageObservable;
    }

    public void getAllMessages(){
        chatMessageObservable.setValue(msgManager.getAllMessages());
    }

    public LiveData<List<ChatMessage>> getSearchResultObservable() {
        return searchResultObservable;
    }

    public void searchMsgs(String keyword) {
        EaseThreadManager.getInstance().runOnIOThread(() -> {
            List<ChatMessage> messages = msgManager.getAllMessages();
            List<ChatMessage> result = new ArrayList<>();
            if(messages != null && !messages.isEmpty()) {
                for (ChatMessage message : messages){
                    if(!msgManager.isNotificationMessage(message)) {
                        continue;
                    }
                    String groupId = null;

                    try {
                        groupId = message.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    } catch (ChatException e) {
                        e.printStackTrace();
                    }
                    // Check id and content
                    if(!TextUtils.isEmpty(groupId)) {
                        if(groupId.contains(keyword)) {
                            result.add(message);
                            continue;
                        }
                        String groupName = GroupHelper.getGroupName(groupId);
                        if(groupName.contains(keyword)) {
                            result.add(message);
                            continue;
                        }
                    }
                    String from = null;
                    try {
                        from = message.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM);
                    } catch (ChatException e) {
                        e.printStackTrace();
                    }
                    if(!TextUtils.isEmpty(from)) {
                        if(from.contains(keyword)) {
                            result.add(message);
                            continue;
                        }
                        EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(groupId);
                        if(user != null && user.getNickname().contains(keyword)) {
                            result.add(message);
                            continue;
                        }
                    }
                    MessageBody body = message.getBody();
                    if(body instanceof TextMessageBody) {
                        String content = ((TextMessageBody) body).getMessage();
                        if(!TextUtils.isEmpty(content) && content.contains(keyword)) {
                            result.add(message);
                        }
                    }
                }
            }
            searchResultObservable.postValue(result);
        });
    }

    public void setMessageChange(EaseEvent change) {
        messageObservable.with(change.event).postValue(change);
    }

    public LiveDataBus getMessageChange() {
        return messageObservable;
    }
}
