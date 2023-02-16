package io.agora.chatdemo.notification.viewmodels;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.entity.InviteMessageStatus;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.livedatas.SingleSourceLiveData;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

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
                    message = getApplication().getString(R.string.system_agree_invite, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
//                    ChatClient.getInstance().contactManager().acceptInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().contactManager().asyncAcceptInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM), new CallBack() {
                        @Override
                        public void onSuccess() {
                            try {
                                saveNotificationMessage(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM),DemoConstant.SYSTEM_ADD_CONTACT,getApplication().getString(R.string.contact_agreed_request));
                            } catch (ChatException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(int code, String error) {
                            EMLog.e("asyncAcceptInvitation",  "error:" + error + " errorMsg:" +error);
                        }
                    });
                } else if (status == InviteMessageStatus.BEAPPLYED) { //accept application to join group
                    message = getApplication().getString(R.string.system_agree_remote_user_apply_to_join_group, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().groupManager().acceptApplication(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID));
                } else if (status == InviteMessageStatus.GROUPINVITATION) {
                    message = getApplication().getString(R.string.system_agree_received_remote_user_invitation, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                    ChatClient.getInstance().groupManager().acceptInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                            , msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                }
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.AGREED.name());
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message);
                TextMessageBody body = new TextMessageBody(message);
                msg.setBody(body);
                EaseNotificationMsgManager.getInstance().updateMessage(msg);
                agreeObservable.postValue(Resource.success(message));
            } catch (ChatException e) {
                e.printStackTrace();
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_EXPIRED, R.string.system_msg_expired);
                agreeObservable.postValue(Resource.error(e.getErrorCode(), e.getMessage(), ""));
            }
            messageChangeObservable.with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY));
        });
    }

    public void refuseInvite(ChatMessage msg) {
        EaseThreadManager.getInstance().runOnIOThread(() -> {
            try {
                String statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS);
                InviteMessageStatus status = InviteMessageStatus.valueOf(statusParams);
                String message = "";
                if (status == InviteMessageStatus.BEINVITEED) {//decline the invitation
                    message = getApplication().getString(R.string.system_decline_invite, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().contactManager().declineInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                } else if (status == InviteMessageStatus.BEAPPLYED) { //decline application to join group
                    message = getApplication().getString(R.string.system_decline_remote_user_apply_to_join_group, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                    ChatClient.getInstance().groupManager().declineApplication(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM)
                            , msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID), "");
                } else if (status == InviteMessageStatus.GROUPINVITATION) {
                    message = getApplication().getString(R.string.system_decline_received_remote_user_invitation, msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                    ChatClient.getInstance().groupManager().declineInvitation(msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                            , msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER), "");
                }
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.REFUSED.name());
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_REASON, message);
                TextMessageBody body = new TextMessageBody(message);
                msg.setBody(body);
                EaseNotificationMsgManager.getInstance().updateMessage(msg);
                refuseObservable.postValue(Resource.success(message));
               } catch (ChatException e) {
                e.printStackTrace();
                msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_EXPIRED, R.string.system_msg_expired);
                refuseObservable.postValue(Resource.error(e.getErrorCode(), e.getMessage(), ""));
            }
            messageChangeObservable.with(DemoConstant.NOTIFY_CHANGE).postValue(EaseEvent.create(DemoConstant.NOTIFY_CHANGE, EaseEvent.TYPE.NOTIFY));
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
        messageChangeObservable.with(DemoConstant.CONTACT_UNREAD_CHANGE).postValue(EaseEvent.create(DemoConstant.CONTACT_UNREAD_CHANGE, EaseEvent.TYPE.NOTIFY));
    }

    public void saveNotificationMessage(String to,String constant,String content){
        ChatMessage msg = ChatMessage.createSendMessage(ChatMessage.Type.TXT);
        msg.setChatType(ChatMessage.ChatType.Chat);
        msg.setTo(to);
        msg.setMsgId(UUID.randomUUID().toString());
        msg.setAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, true);
        msg.setAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE, constant);
        msg.addBody(new TextMessageBody(content));
        msg.setStatus(ChatMessage.Status.SUCCESS);
        // save invitation as messages
        ChatClient.getInstance().chatManager().saveMessage(msg);
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        Intent intent = new Intent(getApplication(), ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, to);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseChatType.SINGLE_CHAT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }
}
