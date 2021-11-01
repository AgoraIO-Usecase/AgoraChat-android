package io.agora.chatdemo.notification.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.entity.InviteMessageStatus;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;

public class NewFriendsViewModel extends AndroidViewModel {
    private SingleSourceLiveData<List<ChatMessage>> inviteMsgObservable;
    private SingleSourceLiveData<List<ChatMessage>> moreInviteMsgObservable;
    private MutableLiveData<Resource<Boolean>> resultObservable;
    private MutableLiveData<Resource<String>> agreeObservable;
    private MutableLiveData<Resource<String>> refuseObservable;
    private LiveDataBus messageChangeObservable = LiveDataBus.get();

    public NewFriendsViewModel(@NonNull Application application) {
        super(application);
        inviteMsgObservable = new SingleSourceLiveData<>();
        moreInviteMsgObservable = new SingleSourceLiveData<>();
        resultObservable = new MutableLiveData<>();
        agreeObservable = new MutableLiveData<>();
        refuseObservable = new MutableLiveData<>();
    }

    public LiveDataBus messageChangeObservable() {
        return messageChangeObservable;
    }

    public LiveData<List<ChatMessage>> inviteMsgObservable() {
        return inviteMsgObservable;
    }

    public LiveData<List<ChatMessage>> moreInviteMsgObservable() {
        return moreInviteMsgObservable;
    }

    public void loadMessages(int limit) {
        List<ChatMessage> emMessages = ChatClient.getInstance().chatManager().searchMsgFromDB(ChatMessage.Type.TXT
                , System.currentTimeMillis(), limit, EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID, Conversation.SearchDirection.UP);
        sortData(emMessages);
        inviteMsgObservable.setSource(new MutableLiveData<>(emMessages));
    }

    public void loadMoreMessages(String targetId, int limit) {
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(EaseConstant.DEFAULT_SYSTEM_MESSAGE_ID, Conversation.ConversationType.Chat, true);
        List<ChatMessage> messages = conversation.loadMoreMsgFromDB(targetId, limit);
        sortData(messages);
        moreInviteMsgObservable.setSource(new MutableLiveData<>(messages));
    }
    
    private void sortData(List<ChatMessage> messages) {
        Collections.sort(messages, new Comparator<ChatMessage>() {
            @Override
            public int compare(ChatMessage o1, ChatMessage o2) {
                long o1MsgTime = o1.getMsgTime();
                long o2MsgTime = o2.getMsgTime();
                return (int) (o2MsgTime - o1MsgTime);
            }
        });
    }

    public LiveData<Resource<Boolean>> resultObservable() {
        return resultObservable;
    }

    public LiveData<Resource<String>> agreeObservable() {
        return agreeObservable;
    }
    public LiveData<Resource<String>> refuseObservable() {
        return refuseObservable;
    }

    public void agreeInvite(ChatMessage msg) {
        EaseThreadManager.getInstance().runOnIOThread(() -> {
            try {
                String statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS);
                InviteMessageStatus status = InviteMessageStatus.valueOf(statusParams);
                String message = "";
                if (status == InviteMessageStatus.BEINVITEED) {//accept be friends
                    message = getApplication().getString(R.string.demo_system_agree_invite, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().contactManager().acceptInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                } else if (status == InviteMessageStatus.BEAPPLYED) { //accept application to join group
                    message = getApplication().getString(R.string.demo_system_agree_remote_user_apply_to_join_group, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().groupManager().acceptApplication(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID));
                } else if (status == InviteMessageStatus.GROUPINVITATION) {
                    message = getApplication().getString(R.string.demo_system_agree_received_remote_user_invitation, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                    ChatClient.getInstance().groupManager().acceptInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                            , msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                }
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.AGREED.name());
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message);
                TextMessageBody body = new TextMessageBody(message);
                msg.setBody(body);
                EaseNotificationMsgManager.getInstance().updateMessage(msg);
                agreeObservable.postValue(Resource.success(message));
                messageChangeObservable.with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY));
            } catch (ChatException e) {
                e.printStackTrace();
                agreeObservable.postValue(Resource.error(e.getErrorCode(), e.getMessage(), ""));
            }
        });
    }

    public void refuseInvite(ChatMessage msg) {
        EaseThreadManager.getInstance().runOnIOThread(() -> {
            try {
                String statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS);
                InviteMessageStatus status = InviteMessageStatus.valueOf(statusParams);
                String message = "";
                if (status == InviteMessageStatus.BEINVITEED) {//decline the invitation
                    message = getApplication().getString(R.string.demo_system_decline_invite, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().contactManager().declineInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                } else if (status == InviteMessageStatus.BEAPPLYED) { //decline application to join group
                    message = getApplication().getString(R.string.demo_system_decline_remote_user_apply_to_join_group, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().groupManager().declineApplication(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                            , msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID), "");
                } else if (status == InviteMessageStatus.GROUPINVITATION) {
                    message = getApplication().getString(R.string.demo_system_decline_received_remote_user_invitation, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                    ChatClient.getInstance().groupManager().declineInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                            , msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER), "");
                }
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.REFUSED.name());
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message);
                TextMessageBody body = new TextMessageBody(message);
                msg.setBody(body);
                EaseNotificationMsgManager.getInstance().updateMessage(msg);
                refuseObservable.postValue(Resource.success(message));
                messageChangeObservable.with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY));
            } catch (ChatException e) {
                e.printStackTrace();
                refuseObservable.postValue(Resource.error(e.getErrorCode(), e.getMessage(), ""));
            }
        });
    }

    public void deleteMsg(ChatMessage message) {
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(DemoConstant.DEFAULT_SYSTEM_MESSAGE_ID, Conversation.ConversationType.Chat, true);
        conversation.removeMessage(message.getMsgId());
        resultObservable.postValue(Resource.success(true));
    }

    public void makeAllMsgRead() {
        Conversation conversation = ChatClient.getInstance().chatManager().getConversation(DemoConstant.DEFAULT_SYSTEM_MESSAGE_ID, Conversation.ConversationType.Chat, true);
        conversation.markAllMessagesAsRead();
        messageChangeObservable.with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY));
    }
}
