package io.agora.chatdemo.global;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.agora.CallBack;
import io.agora.ChatRoomChangeListener;
import io.agora.ConnectionListener;
import io.agora.ContactListener;
import io.agora.ConversationListener;
import io.agora.Error;
import io.agora.PresenceListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.MucSharedFile;
import io.agora.chat.Presence;
import io.agora.chat.TextMessageBody;
import io.agora.chat.UserInfo;
import io.agora.chat.adapter.EMAChatRoomManagerListener;
import io.agora.chat.uikit.interfaces.EaseGroupListener;
import io.agora.chat.uikit.manager.EaseAtMessageHelper;
import io.agora.chat.uikit.manager.EaseChatPresenter;
import io.agora.chat.uikit.manager.EaseNotificationMsgManager;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.db.entity.InviteMessageStatus;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.manager.PushAndMessageHelper;
import io.agora.chatdemo.general.models.LoginBean;
import io.agora.chatdemo.general.repositories.EMClientRepository;
import io.agora.chatdemo.general.utils.GsonTools;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.model.MemberAttributeBean;
import io.agora.chatdemo.main.MainActivity;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

/**
 * Mainly used to set up the global monitoring of Agora Chat SDK in the project
 */
public class GlobalEventsMonitor extends EaseChatPresenter {
    private static final String TAG = GlobalEventsMonitor.class.getSimpleName();
    private static final int HANDLER_SHOW_TOAST = 0;
    private static GlobalEventsMonitor instance;
    private LiveDataBus messageChangeLiveData;
    private boolean isGroupsSyncedWithServer = false;
    private boolean isContactsSyncedWithServer = false;
    private boolean isBlackListSyncedWithServer = false;
    private boolean isPushConfigsWithServer = false;
    private Context appContext;
    protected Handler handler;
    private ConcurrentHashMap<String,Presence> mPresences=new ConcurrentHashMap<>();

    Queue<String> msgQueue = new ConcurrentLinkedQueue<>();
    private List<Activity> resumeActivityList = new ArrayList<>();
    boolean mIsAppForeground = true;

    private GlobalEventsMonitor() {
        appContext = DemoApplication.getInstance();
        initHandler(appContext.getMainLooper());
        messageChangeLiveData = LiveDataBus.get();
        //Add network connection status monitoring
//        EaseUIKit.getInstance().setOnEaseChatConnectionListener(new ChatConnectionListener());

        ChatClient.getInstance().addConnectionListener(connectionListener);

        //Add group change listener
        DemoHelper.getInstance().getGroupManager().addGroupChangeListener(new ChatGroupListener());
        //Add contact listener
        DemoHelper.getInstance().getContactManager().setContactListener(new ChatContactListener());
        //Add chat room change listener
        DemoHelper.getInstance().getChatroomManager().addChatRoomChangeListener(new ChatRoomListener());
        //Add monitoring of conversation (listening to read receipts)
        DemoHelper.getInstance().getChatManager().addConversationListener(new ChatConversationListener());
        //Initialize presence
        initPresence();
        registerActivityLifecycleCallbacks();
    }

    public static GlobalEventsMonitor getInstance() {
        if(instance == null) {
            synchronized (GlobalEventsMonitor.class) {
                if(instance == null) {
                    instance = new GlobalEventsMonitor();
                }
            }
        }
        return instance;
    }

    public void initHandler(Looper looper) {
        handler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                Object obj = msg.obj;
                switch (msg.what) {
                    case HANDLER_SHOW_TOAST :
                        if(obj instanceof String) {
                            String str = (String) obj;
                            //ToastUtils.showToast(str);
                            Toast.makeText(appContext, str, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        };
        while (!msgQueue.isEmpty()) {
            showToast(msgQueue.remove());
        }
    }

    private void registerActivityLifecycleCallbacks() {
        Object lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStopped(Activity activity) {
                resumeActivityList.remove(activity);
                if (resumeActivityList.isEmpty()) {
                    // app is on background
                    mIsAppForeground = false;
                    EMLog.d(TAG, "app on background");
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
            }

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if(!resumeActivityList.contains(activity)) {
                    resumeActivityList.add(activity);
                    if(resumeActivityList.size() == 1) {
                        // app is on foreground
                        mIsAppForeground = true;
                        EMLog.d(TAG, "app on foreground");

                        // check if token expired
                        long timeStamp = System.currentTimeMillis();

                        if (timeStamp > DemoHelper.getInstance().getUsersManager().getTokenExpireTs()) {
                            EMLog.d(TAG, "token expired, relogin");
                            EMClientRepository repo = new EMClientRepository();
                            String userName = DemoHelper.getInstance().getUsersManager().getCurrentUser();
                            String pw = repo.decryptData();
                            if (userName== null || pw == null ||userName.isEmpty() || pw.isEmpty()) {
                                EMLog.d(TAG, "user logout, just return");
                                EaseEvent event = EaseEvent.create(DemoConstant.LOGIN_SUCESS, EaseEvent.TYPE.NOTIFY);
                                messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);
                                return;
                            }

                            DemoHelper.getInstance().logout(false, new CallBack() {
                                @Override
                                public void onSuccess() {

                                    repo.reLogin(userName, pw, new ResultCallBack<LoginBean>() {
                                        @Override
                                        public void onSuccess(LoginBean value) {
                                            EMLog.e(TAG, "reLogin success");
                                            EaseEvent event = EaseEvent.create(DemoConstant.LOGIN_SUCESS, EaseEvent.TYPE.NOTIFY);
                                            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);
                                        }

                                        @Override
                                        public void onError(int error, String errorMsg) {
                                            EMLog.e(TAG, "reLogin failed: error code = " + error + " error message = " + errorMsg);
                                        }
                                    });
                                }

                                @Override
                                public void onError(int i, String s) {
                                    EMLog.e("logout", "logout error: error code = " + i + " error message = " + s);
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }

        };
        ((Application) appContext).registerActivityLifecycleCallbacks((Application.ActivityLifecycleCallbacks) lifecycleCallbacks);
    }

    void showToast(@StringRes int mesId) {
        showToast(context.getString(mesId));
    }

    void showToast(final String message) {
        EMLog.d(TAG, "receive invitation to join the group：" + message);
        if (handler != null) {
            Message msg = Message.obtain(handler, HANDLER_SHOW_TOAST, message);
            handler.sendMessage(msg);
        } else {
            msgQueue.add(message);
        }
    }

    private void initPresence() {
        ChatClient.getInstance().presenceManager().addListener(new PresenceListener() {
            @Override
            public void onPresenceUpdated(List<Presence> presences) {
                for (Presence presence : presences) {
                    Log.d("TAG", presence.toString());
                    mPresences.put(presence.getPublisher(),presence);
                }
                LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).postValue(mPresences);
            }
        });
    }

    public ConcurrentHashMap<String, Presence> getPresences() {
        return mPresences;
    }

    @Override
    public void onMessageReceived(List<ChatMessage> messages) {
        super.onMessageReceived(messages);
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        for (ChatMessage message : messages) {
            EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
            EMLog.d(TAG, "onMessageReceived: " + message.getType());
            // If you set the group offline message do not disturb, no message notification will be made
            List<String> disabledIds = DemoHelper.getInstance().getPushManager().getNoPushGroups();
            if(disabledIds != null && disabledIds.contains(message.conversationId())) {
                return;
            }
            // Not notify if message is chat thread message
            if(message.isChatThreadMessage()) {
                return;
            }
            // in background, do not refresh UI, notify it in notification bar
            if(!DemoApplication.getInstance().getLifecycleCallbacks().isFront()){
                getNotifier().notify(message);
            }
            //notify new message
            getNotifier().vibrateAndPlayTone(message);
        }
    }

    @Override
    public void onMessageContentChanged(ChatMessage message, String operatorId, long operationTime) {
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        EMLog.d(TAG, "onMessageContentChanged id : " + message.getMsgId());
        EMLog.d(TAG, "onMessageContentChanged type: " + message.getType());
    }


    /**
     * Determine whether MainActivity has been started
     * @return
     */
    private synchronized boolean isAppLaunchMain() {
        List<Activity> activities = DemoApplication.getInstance().getLifecycleCallbacks().getActivityList();
        if(activities != null && !activities.isEmpty()) {
            for(int i = activities.size() - 1; i >= 0 ; i--) {
                if(activities.get(i) instanceof MainActivity) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onCmdMessageReceived(List<ChatMessage> messages) {
        super.onCmdMessageReceived(messages);
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_CMD_RECEIVE, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
    }

    @Override
    public void onMessageRead(List<ChatMessage> messages) {
        super.onMessageRead(messages);
    }

    @Override
    public void onMessageRecalled(List<ChatMessage> messages) {
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        for (ChatMessage msg : messages) {
            if(msg.getChatType() == ChatMessage.ChatType.GroupChat && EaseAtMessageHelper.get().isAtMeMsg(msg)){
                EaseAtMessageHelper.get().removeAtMeGroup(msg.getTo());
            }
            ChatMessage msgNotification = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
            String content;
            if(TextUtils.equals(msg.getFrom(), ChatClient.getInstance().getCurrentUser())) {
                msgNotification.setDirection(ChatMessage.Direct.SEND);
                content = context.getString(R.string.ease_msg_recall_by_self);
            }else {
                msgNotification.setDirection(ChatMessage.Direct.RECEIVE);
                content = String.format(context.getString(R.string.ease_msg_recall_by_user), msg.getFrom());
            }
            TextMessageBody txtBody = new TextMessageBody(content);
            msgNotification.addBody(txtBody);
            msgNotification.setFrom(msg.getFrom());
            msgNotification.setTo(msg.getTo());
            msgNotification.setUnread(false);
            msgNotification.setMsgTime(msg.getMsgTime());
            msgNotification.setLocalTime(msg.getMsgTime());
            msgNotification.setChatType(msg.getChatType());
            msgNotification.setAttribute(DemoConstant.MESSAGE_TYPE_RECALL, true);
            msgNotification.setStatus(ChatMessage.Status.SUCCESS);
            msgNotification.setIsChatThreadMessage(msg.isChatThreadMessage());
            ChatClient.getInstance().chatManager().saveMessage(msgNotification);
        }
    }

    private class ChatConversationListener implements ConversationListener {

        @Override
        public void onConversationUpdate() {
            
        }

        @Override
        public void onConversationRead(String from, String to) {
            EaseEvent event = EaseEvent.create(DemoConstant.CONVERSATION_READ, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.CONVERSATION_READ).postValue(event);
        }
    }

    private ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnected() {
            EMLog.i(TAG, "onConnected");
            DemoHelper.getInstance().getUsersManager().initUserInfo();
            EaseEvent event = EaseEvent.create(DemoConstant.LOGIN_SUCESS, EaseEvent.TYPE.NOTIFY);
            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);
        }

        @Override
        public void onDisconnected(int error) {
            EMLog.i(TAG, "onDisconnected ="+error);
            if (error == Error.USER_REMOVED
                    || error == Error.USER_LOGIN_ANOTHER_DEVICE
                    || error == Error.SERVER_SERVICE_RESTRICTED
                    || error == Error.USER_KICKED_BY_CHANGE_PASSWORD
                    || error == Error.USER_KICKED_BY_OTHER_DEVICE) {

                LiveDataBus.get().with(DemoConstant.ACCOUNT_CHANGE).postValue(new EaseEvent(String.valueOf(error), EaseEvent.TYPE.ACCOUNT));
            }
        }

        @Override
        public void onTokenExpired() {
            EMLog.d(TAG, "onTokenExpired");

            if (!mIsAppForeground) {
                EMLog.d(TAG, "app is in background, ignore it");
                return;
            }

            DemoHelper.getInstance().logout(false, new CallBack() {
                @Override
                public void onSuccess() {
                    EMClientRepository repo = new EMClientRepository();
                    String userName = DemoHelper.getInstance().getUsersManager().getCurrentUser();
                    String pw = repo.decryptData();
                    if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pw)) {
                        EMLog.d(TAG, "user logout, just return");
                        return;
                    }

                    repo.reLogin(userName, pw, new ResultCallBack<LoginBean>() {
                        @Override
                        public void onSuccess(LoginBean value) {
                            EMLog.e(TAG, "reLogin success");
                            EaseEvent event = EaseEvent.create(DemoConstant.LOGIN_SUCESS, EaseEvent.TYPE.NOTIFY);
                            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);
                        }

                        @Override
                        public void onError(int error, String errorMsg) {
                            EMLog.e(TAG, "reLogin failed: error code = " + error + " error message = " + errorMsg);
                        }
                    });
                }

                @Override
                public void onError(int i, String s) {
                    EMLog.e("logout", "logout error: error code = "+ i + " error message = "+ s);
                }
            });
        }

        @Override
        public void onTokenWillExpire() {
            new EMClientRepository().renewAgoraChatToken();
        }
    };


    private class ChatGroupListener extends EaseGroupListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
            super.onInvitationReceived(groupId, groupName, inviter, reason);
            // Remove the same request
            List<ChatMessage> allMessages = EaseNotificationMsgManager.getInstance().getAllMessages();
            if(allMessages != null && !allMessages.isEmpty()) {
                for (ChatMessage message : allMessages) {
                    Map<String, Object> ext = message.ext();
                    if(ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID) && TextUtils.equals(groupId, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)))
                            && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_INVITER) && TextUtils.equals(inviter, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_INVITER)))) {
                        EaseNotificationMsgManager.getInstance().removeMessage(message);
                    }
                }
            }
            groupName = TextUtils.isEmpty(groupName) ? groupId : groupName;
            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_GROUP_ID, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_NAME, groupName);
            ext.put(DemoConstant.SYSTEM_MESSAGE_INVITER, inviter);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.GROUPINVITATION.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.NOTIFY_GROUP_INVITE_RECEIVE, EaseEvent.TYPE.NOTIFY);
            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);

//            showToast(context.getString(InviteMessageStatus.GROUPINVITATION.getMsgContent(), inviter, groupName));
            EMLog.i(TAG, context.getString(InviteMessageStatus.GROUPINVITATION.getMsgContent(), inviter, groupName));
        }

        @Override
        public void onInvitationAccepted(String groupId, String invitee, String reason) {
            super.onInvitationAccepted(groupId, invitee, reason);
            //user accept your invitation
            String groupName = GroupHelper.getGroupName(groupId);

            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_GROUP_ID, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_NAME, groupName);
            ext.put(DemoConstant.SYSTEM_MESSAGE_INVITER, invitee);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.GROUPINVITATION_ACCEPTED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.NOTIFY_GROUP_INVITE_ACCEPTED, EaseEvent.TYPE.NOTIFY);
            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);

//            showToast(context.getString(InviteMessageStatus.GROUPINVITATION_ACCEPTED.getMsgContent(), invitee));
            EMLog.i(TAG, context.getString(InviteMessageStatus.GROUPINVITATION_ACCEPTED.getMsgContent(), invitee));
        }

        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {
            super.onInvitationDeclined(groupId, invitee, reason);
            //user declined your invitation
            String groupName = GroupHelper.getGroupName(groupId);

            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_GROUP_ID, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_NAME, groupName);
            ext.put(DemoConstant.SYSTEM_MESSAGE_INVITER, invitee);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.GROUPINVITATION_DECLINED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.NOTIFY_GROUP_INVITE_DECLINED, EaseEvent.TYPE.NOTIFY);
            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);

            EMLog.i(TAG, context.getString(InviteMessageStatus.GROUPINVITATION_DECLINED.getMsgContent(), invitee));
        }

        @Override
        public void onUserRemoved(String groupId, String groupName) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP_LEAVE);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            EMLog.i(TAG, context.getString(R.string.group_listener_onUserRemoved, groupName));
        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP_LEAVE);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            EMLog.i(TAG, context.getString(R.string.group_listener_onGroupDestroyed, groupName));
        }

        @Override
        public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {
            super.onRequestToJoinReceived(groupId, groupName, applicant, reason);
            //Remove the same request
            List<ChatMessage> allMessages = EaseNotificationMsgManager.getInstance().getAllMessages();
            if(allMessages != null && !allMessages.isEmpty()) {
                for (ChatMessage message : allMessages) {
                    Map<String, Object> ext = message.ext();
                    if(ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID) && TextUtils.equals(groupId, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)))
                            && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM) && TextUtils.equals(applicant, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                        EaseNotificationMsgManager.getInstance().removeMessage(message);
                    }
                }
            }
            // user apply to join group
            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, applicant);
            ext.put(DemoConstant.SYSTEM_MESSAGE_GROUP_ID, groupId);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_NAME, groupName);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAPPLYED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.NOTIFY_GROUP_JOIN_RECEIVE, EaseEvent.TYPE.NOTIFY);
            messageChangeLiveData.with(DemoConstant.NOTIFY_CHANGE).postValue(event);

            EMLog.i(TAG, context.getString(InviteMessageStatus.BEAPPLYED.getMsgContent(), applicant, groupName));
        }

        @Override
        public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {
            super.onRequestToJoinAccepted(groupId, groupName, accepter);
            // your application was accepted
            ChatMessage msg = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
            msg.setChatType(ChatMessage.ChatType.GroupChat);
            msg.setFrom(accepter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true);
            msg.addBody(new TextMessageBody(context.getString(R.string.group_listener_onRequestToJoinAccepted, accepter, groupName)));
            msg.setStatus(ChatMessage.Status.SUCCESS);
            // save accept message
            ChatClient.getInstance().chatManager().saveMessage(msg);
            // notify the accept message
            getNotifier().vibrateAndPlayTone(msg);

            EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_GROUP_JOIN_ACCEPTED, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);

            EMLog.i(TAG, context.getString(R.string.group_listener_onRequestToJoinAccepted, accepter, groupName));

            EaseEvent groupEvent = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(groupEvent);
        }

        @Override
        public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
            super.onRequestToJoinDeclined(groupId, groupName, decliner, reason);
//            showToast(context.getString(R.string.group_listener_onRequestToJoinDeclined, decliner, groupName));
            EMLog.i(TAG, context.getString(R.string.group_listener_onRequestToJoinDeclined, decliner, groupName));
        }

        @Override
        public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {
            super.onAutoAcceptInvitationFromGroup(groupId, inviter, inviteMessage);
            String groupName = GroupHelper.getGroupName(groupId);
            ChatMessage msg = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
            msg.setChatType(ChatMessage.ChatType.GroupChat);
            msg.setFrom(inviter);
            msg.setTo(groupId);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.setAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, true);
            msg.setAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE, DemoConstant.SYSTEM_JOINED_GROUP);
            msg.addBody(new TextMessageBody(context.getString(R.string.group_joined)));
            msg.setStatus(ChatMessage.Status.SUCCESS);
            // save invitation as messages
            ChatClient.getInstance().chatManager().saveMessage(msg);
            // notify invitation message
            getNotifier().vibrateAndPlayTone(msg);
            EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_GROUP_AUTO_ACCEPT, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);

            EMLog.i(TAG, context.getString(R.string.group_listener_onAutoAcceptInvitationFromGroup, groupName));
        }

        @Override
        public void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {
            super.onMuteListAdded(groupId, mutes, muteExpire);
            String content = getContentFromList(mutes);
            EMLog.i(TAG, context.getString(R.string.group_listener_onMuteListAdded, content));
        }

        @Override
        public void onMuteListRemoved(String groupId, List<String> mutes) {
            super.onMuteListRemoved(groupId, mutes);
            String content = getContentFromList(mutes);
            EMLog.i(TAG, context.getString(R.string.group_listener_onMuteListRemoved, content));
        }

        @Override
        public void onWhiteListAdded(String groupId, List<String> whitelist) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            String content = getContentFromList(whitelist);
            EMLog.i(TAG, context.getString(R.string.group_listener_onWhiteListAdded, content));
        }

        @Override
        public void onWhiteListRemoved(String groupId, List<String> whitelist) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            String content = getContentFromList(whitelist);
            EMLog.i(TAG, context.getString(R.string.group_listener_onWhiteListRemoved, content));
        }

        @Override
        public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            EMLog.i(TAG, context.getString(isMuted ? R.string.group_listener_onAllMemberMuteStateChanged_mute
                    : R.string.group_listener_onAllMemberMuteStateChanged_not_mute));
        }

        @Override
        public void onAdminAdded(String groupId, String administrator) {
            super.onAdminAdded(groupId, administrator);
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onAdminAdded, administrator));
        }

        @Override
        public void onAdminRemoved(String groupId, String administrator) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onAdminRemoved, administrator));
        }

        @Override
        public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_OWNER_TRANSFER, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onOwnerChanged, oldOwner, newOwner));
        }

        @Override
        public void onMemberJoined(String groupId, String member) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onMemberJoined, member));
        }

        @Override
        public void onMemberExited(String groupId, String member) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onMemberExited, member));
        }

        @Override
        public void onAnnouncementChanged(String groupId, String announcement) {
            EMLog.i(TAG, context.getString(R.string.group_listener_onAnnouncementChanged));
        }

        @Override
        public void onSharedFileAdded(String groupId, MucSharedFile sharedFile) {
            LiveDataBus.get().with(DemoConstant.GROUP_SHARE_FILE_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_SHARE_FILE_CHANGE, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onSharedFileAdded, sharedFile.getFileName()));
        }

        @Override
        public void onSharedFileDeleted(String groupId, String fileId) {
            LiveDataBus.get().with(DemoConstant.GROUP_SHARE_FILE_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_SHARE_FILE_CHANGE, EaseEvent.TYPE.GROUP));
            EMLog.i(TAG, context.getString(R.string.group_listener_onSharedFileDeleted, fileId));
        }

        @Override
        public void onGroupMemberAttributeChanged(String groupId,String userId,Map<String, String> attribute, String from) {
            if ( attribute != null && attribute.size() > 0){
                EMLog.d(TAG,"onGroupMemberAttributeChanged: " + groupId +" - "+ attribute.toString());
                MemberAttributeBean bean = GsonTools.changeGsonToBean(new JSONObject(attribute).toString(), MemberAttributeBean.class);
                if (bean != null && bean.getNickName() != null){
                    DemoHelper.getInstance().saveMemberAttribute(groupId,userId,bean);
                    LiveDataBus.get().with(DemoConstant.GROUP_MEMBER_ATTRIBUTE_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_MEMBER_ATTRIBUTE_CHANGE, EaseEvent.TYPE.MESSAGE));
                }
            }
        }

    }

    private class ChatContactListener implements ContactListener {

        @Override
        public void onContactAdded(String username) {
            EMLog.i("ChatContactListener", "onContactAdded");
            String[] userId = new String[1];
            userId[0] = username;
            ChatClient.getInstance().userInfoManager().fetchUserInfoByUserId(userId, new ValueCallBack<Map<String, UserInfo>>() {
                @Override
                public void onSuccess(Map<String, UserInfo> value) {
                    UserInfo userInfo = value.get(username);
                    EmUserEntity entity = new EmUserEntity();
                    entity.setUsername(username);
                    if(userInfo != null){
                        entity.setNickname(userInfo.getNickName());
                        entity.setEmail(userInfo.getEmail());
                        entity.setAvatar(userInfo.getAvatarUrl());
                        entity.setBirth(userInfo.getBirth());
                        entity.setGender(userInfo.getGender());
                        entity.setExt(userInfo.getExt());
                        entity.setContact(0);
                        entity.setSign(userInfo.getSignature());
                    }
                    DemoHelper.getInstance().getModel().insert(entity);
                    DemoHelper.getInstance().updateContactList();
                    EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_ADD, EaseEvent.TYPE.CONTACT);
                    event.message = username;
                    messageChangeLiveData.with(DemoConstant.CONTACT_ADD).postValue(event);

//                    showToast(context.getString(R.string.contact_listener_onContactAdded, username));
                    EMLog.i(TAG, context.getString(R.string.contact_listener_onContactAdded, username));
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.i(TAG, context.getString(R.string.contact_get_userInfo_failed) +  username + "error:" + error + " errorMsg:" +errorMsg);
                }
            });
        }

        @Override
        public void onContactDeleted(String username) {
            EMLog.i("ChatContactListener", "onContactDeleted");
            boolean deleteUsername = DemoHelper.getInstance().getModel().isDeleteUsername(username);
            int num = DemoHelper.getInstance().deleteContact(username);
            DemoHelper.getInstance().updateContactList();
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_DELETE, EaseEvent.TYPE.CONTACT);
            event.message = username;
            messageChangeLiveData.with(DemoConstant.CONTACT_DELETE).postValue(event);

            if(deleteUsername || num == 0) {
//                showToast(context.getString(R.string.contact_listener_onContactDeleted, username));
                EMLog.i(TAG, context.getString(R.string.contact_listener_onContactDeleted, username));
            }else {
                //showToast(context.getString(R.string.demo_contact_listener_onContactDeleted_by_other, username));
                EMLog.i(TAG, context.getString(R.string.contact_listener_onContactDeleted_by_other, username));
            }
        }



        @Override
        public void onContactInvited(String username, String reason) {
            EMLog.i("ChatContactListener", "onContactInvited");
            List<ChatMessage> allMessages = EaseNotificationMsgManager.getInstance().getAllMessages();
            if(allMessages != null && !allMessages.isEmpty()) {
                for (ChatMessage message : allMessages) {
                    Map<String, Object> ext = message.ext();
                    if(ext != null && !ext.containsKey(DemoConstant.SYSTEM_MESSAGE_GROUP_ID)
                            && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM) && TextUtils.equals(username, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                        EaseNotificationMsgManager.getInstance().removeMessage(message);
                    }
                }
            }

            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEINVITEED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);

//            showToast(context.getString(InviteMessageStatus.BEINVITEED.getMsgContent(), username));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEINVITEED.getMsgContent(), username));
        }

        @Override
        public void onFriendRequestAccepted(String username) {
            EMLog.i("ChatContactListener", "onFriendRequestAccepted" + username);
            List<ChatMessage> allMessages = EaseNotificationMsgManager.getInstance().getAllMessages();
            if(allMessages != null && !allMessages.isEmpty()) {
                for (ChatMessage message : allMessages) {
                    Map<String, Object> ext = message.ext();
                    if(ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM)
                            && TextUtils.equals(username, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                        updateMessage(message);
                        return;
                    }
                }
            }

            String[] userId = new String[1];
            userId[0] = username;
            DemoHelper.getInstance().getUsersManager().getUserInfo(username);

            ChatMessage msg = ChatMessage.createSendMessage(ChatMessage.Type.TXT);
            msg.setChatType(ChatMessage.ChatType.Chat);
            msg.setTo(username);
            msg.setMsgId(UUID.randomUUID().toString());
            msg.setAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, true);
            msg.setAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE, DemoConstant.SYSTEM_INVITATION_CONTACT);

            msg.addBody(new TextMessageBody(context.getString(R.string.contact_approved)));
            msg.setStatus(ChatMessage.Status.SUCCESS);
            // save invitation as messages
            ChatClient.getInstance().chatManager().saveMessage(msg);

            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);
            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);

//            showToast(context.getString(InviteMessageStatus.BEAGREED.getMsgContent()));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEAGREED.getMsgContent()));
        }

        @Override
        public void onFriendRequestDeclined(String username) {
            EMLog.i("ChatContactListener", "onFriendRequestDeclined");
            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEREFUSED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);

            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);
//            showToast(context.getString(InviteMessageStatus.BEREFUSED.getMsgContent(), username));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEREFUSED.getMsgContent(), username));
        }
    }

    private void updateMessage(ChatMessage message) {
        message.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name());
        TextMessageBody body = new TextMessageBody(PushAndMessageHelper.getSystemMessage(message.ext()));
        message.addBody(body);
        EaseNotificationMsgManager.getInstance().updateMessage(message);
    }

    @Override
    public void onContactEvent(int event, String target, String ext) {
        EMLog.i(TAG, "onContactEvent event"+event);
        DemoDbHelper dbHelper = DemoDbHelper.getInstance(DemoApplication.getInstance());
        String message = null;
        switch (event) {
            case CONTACT_REMOVE: //The friend has been removed from another devices
                EMLog.i("ChatMultiDeviceListener", "CONTACT_REMOVE");
                message = DemoConstant.CONTACT_REMOVE;
                if(dbHelper.getUserDao() != null) {
                    dbHelper.getUserDao().deleteUser(target);
                }
                removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM);
                DemoHelper.getInstance().getChatManager().deleteConversation(target, false);

                break;
            case CONTACT_ACCEPT: //The friend request has been approved on another devices
                EMLog.i("ChatMultiDeviceListener", "CONTACT_ACCEPT");
                message = DemoConstant.CONTACT_ACCEPT;
                EmUserEntity  entity = new EmUserEntity();
                entity.setUsername(target);
                if(dbHelper.getUserDao() != null) {
                    dbHelper.getUserDao().insert(entity);
                }
                updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT);

                break;
            case CONTACT_DECLINE: //The friend request has been rejected on other devices
                EMLog.i("ChatMultiDeviceListener", "CONTACT_DECLINE");
                message = DemoConstant.CONTACT_DECLINE;
                updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE);

                break;
            case CONTACT_BAN: //The current user adds someone to the blacklist on other devices
                EMLog.i("ChatMultiDeviceListener", "CONTACT_BAN");
                message = DemoConstant.CONTACT_BAN;
                if(dbHelper.getUserDao() != null) {
                    dbHelper.getUserDao().deleteUser(target);
                }
                removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM);
                DemoHelper.getInstance().getChatManager().deleteConversation(target, false);
                updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_BAN);

                showToast("CONTACT_BAN");
                break;
            case CONTACT_ALLOW: // Friends are removed from the blacklist on other devices
                EMLog.i("ChatMultiDeviceListener", "CONTACT_ALLOW");
                message = DemoConstant.CONTACT_ALLOW;
                updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW);

                break;
        }
        if(!TextUtils.isEmpty(message)) {
            EaseEvent easeEvent = EaseEvent.create(message, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(message).postValue(easeEvent);
        }
    }

    @Override
    public void onGroupEvent(int event, String groupId, List<String> usernames) {
        EMLog.i(TAG, "onGroupEvent event"+event);
        String message = null;
        switch (event) {
            case GROUP_CREATE:
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_CREATE);

                break;
            case GROUP_DESTROY:
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_DESTROY);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_JOIN:
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_JOIN);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_LEAVE:
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_LEAVE);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_APPLY:
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY);

                break;
            case GROUP_APPLY_ACCEPT:
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID, usernames.get(0), DemoConstant.SYSTEM_MESSAGE_FROM);
                // TODO: person, reason from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT);

                break;
            case GROUP_APPLY_DECLINE:
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID, usernames.get(0), DemoConstant.SYSTEM_MESSAGE_FROM);
                // TODO: person, reason from ext
//                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE);

                break;
            case GROUP_INVITE:
                // TODO: person, reason from ext
//                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE);

                break;
            case GROUP_INVITE_ACCEPT:
                String st3 = context.getString(R.string.Invite_you_to_join_a_group_chat);
                ChatMessage msg = ChatMessage.createReceiveMessage(ChatMessage.Type.TXT);
                msg.setChatType(ChatMessage.ChatType.GroupChat);
                // TODO: person, reason from ext
                String from = "";
                if (usernames != null && usernames.size() > 0) {
                    msg.setFrom(usernames.get(0));
                }
                msg.setTo(groupId);
                msg.setMsgId(UUID.randomUUID().toString());
//                msg.setAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, true);
                msg.setAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE, DemoConstant.SYSTEM_GROUP_INVITE_ACCEPT);
                msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true);
                msg.addBody(new TextMessageBody(msg.getFrom() + " " +st3));
                msg.setStatus(ChatMessage.Status.SUCCESS);
                // save invitation as messages
                ChatClient.getInstance().chatManager().saveMessage(msg);

                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                // TODO: person, reason from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_INVITE_DECLINE:
                removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                // TODO: person, reason from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE);

                break;
            case GROUP_KICK:
                // TODO: person, reason from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_KICK);
                message = DemoConstant.GROUP_CHANGE;

                showToast("GROUP_KICK");
                break;
            case GROUP_BAN:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_BAN);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_ALLOW:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW);

                break;
            case GROUP_BLOCK:
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_BLOCK);

                showToast("GROUP_BLOCK");
                break;
            case GROUP_UNBLOCK:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_UNBLOCK);

                showToast("GROUP_UNBLOCK");
                break;
            case GROUP_ASSIGN_OWNER:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ASSIGN_OWNER);

                break;
            case GROUP_ADD_ADMIN:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_REMOVE_ADMIN:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN);
                message = DemoConstant.GROUP_CHANGE;

                break;
            case GROUP_ADD_MUTE:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_MUTE);

                showToast("GROUP_ADD_MUTE");
                break;
            case GROUP_REMOVE_MUTE:
                // TODO: person from ext
                saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_MUTE);

                showToast("GROUP_REMOVE_MUTE");
                break;
            default:
                break;
        }
        if(!TextUtils.isEmpty(message)) {
            EaseEvent easeEvent = EaseEvent.create(message, EaseEvent.TYPE.GROUP);
            messageChangeLiveData.with(message).postValue(easeEvent);
        }
    }

    @Override
    public void onChatThreadEvent(int event, String target, List<String> usernames) {
        super.onChatThreadEvent(event, target, usernames);
        EMLog.i(TAG, "onThreadEvent event: "+event);
    }

    /**
     * Remove all message records of the target, if the target is deleted
     * @param target
     */
    private void removeTargetSystemMessage(String target, String params) {
        Conversation conversation = EaseNotificationMsgManager.getInstance().getConversation();
        List<ChatMessage> messages = conversation.getAllMessages();
        if(messages != null && !messages.isEmpty()) {
            for (ChatMessage message : messages) {
                String from = null;
                try {
                    from = message.getStringAttribute(params);
                } catch (ChatException e) {
                    e.printStackTrace();
                }
                if(TextUtils.equals(from, target)) {
                    conversation.removeMessage(message.getMsgId());
                }
            }
        }
    }

    /**
     * Remove all message records of the target, if the target is deleted
     * @param target1
     */
    private void removeTargetSystemMessage(String target1, String params1, String target2, String params2) {
        Conversation conversation = EaseNotificationMsgManager.getInstance().getConversation();
        List<ChatMessage> messages = conversation.getAllMessages();
        if(messages != null && !messages.isEmpty()) {
            for (ChatMessage message : messages) {
                String targetParams1 = null;
                String targetParams2 = null;
                try {
                    targetParams1 = message.getStringAttribute(params1);
                    targetParams2 = message.getStringAttribute(params2);
                } catch (ChatException e) {
                    e.printStackTrace();
                }
                if(TextUtils.equals(targetParams1, target1) && TextUtils.equals(targetParams2, target2)) {
                    conversation.removeMessage(message.getMsgId());
                }
            }
        }
    }


    private void notifyNewInviteMessage(ChatMessage msg) {
        // notify there is new message
        getNotifier().vibrateAndPlayTone(null);
    }

    private void updateContactNotificationStatus(String from, String reason, InviteMessageStatus status) {
        ChatMessage msg = null;
        Conversation conversation = EaseNotificationMsgManager.getInstance().getConversation();
        List<ChatMessage> allMessages = conversation.getAllMessages();
        if(allMessages != null && !allMessages.isEmpty()) {
            for (ChatMessage message : allMessages) {
                Map<String, Object> ext = message.ext();
                if(ext != null && (ext.containsKey(DemoConstant.SYSTEM_MESSAGE_FROM)
                        && TextUtils.equals(from, (String)ext.get(DemoConstant.SYSTEM_MESSAGE_FROM)))) {
                    msg = message;
                }
            }
        }

        if (msg != null) {
            msg.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name());
            EaseNotificationMsgManager.getInstance().updateMessage(msg);
        } else {
            // save invitation as message
            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, from);
            ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name());
            msg = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);
            notifyNewInviteMessage(msg);
        }
    }

    private void saveGroupNotification(String groupId, String groupName, String inviter, String reason, InviteMessageStatus status) {
        Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
        ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, groupId);
        ext.put(DemoConstant.SYSTEM_MESSAGE_GROUP_ID, groupId);
        ext.put(DemoConstant.SYSTEM_MESSAGE_REASON, reason);
        ext.put(DemoConstant.SYSTEM_MESSAGE_NAME, groupName);
        ext.put(DemoConstant.SYSTEM_MESSAGE_INVITER, inviter);
        ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, status.name());
        ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

        notifyNewInviteMessage(message);
    }

    private class ChatRoomListener implements ChatRoomChangeListener {

        @Override
        public void onChatRoomDestroyed(String roomId, String roomName) {
            setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM_LEAVE);
//            showToast(context.getString(R.string.chat_room_listener_onChatRoomDestroyed, roomName));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onChatRoomDestroyed, roomName));
        }

        @Override
        public void onMemberJoined(String roomId, String participant) {
            setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM);
//            showToast(context.getString(R.string.chat_room_listener_onMemberJoined, participant));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onMemberJoined, participant));
        }

        @Override
        public void onMemberExited(String roomId, String roomName, String participant) {
            setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM);
//            showToast(context.getString(R.string.chat_room_listener_onMemberExited, participant));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onMemberExited, participant));
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            if(TextUtils.equals(DemoHelper.getInstance().getUsersManager().getCurrentUserID(), participant)) {
                setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM);
                if(reason == EMAChatRoomManagerListener.BE_KICKED) {
//                    showToast(R.string.quiting_the_chat_room);
//                    showToast(R.string.quiting_the_chat_room);
                }else {
//                    showToast(context.getString(R.string.chat_room_listener_onRemovedFromChatRoom, participant));
                    EMLog.i(TAG, context.getString(R.string.chat_room_listener_onRemovedFromChatRoom, participant));
                }

            }
        }

        @Override
        public void onMuteListAdded(String chatRoomId, List<String> mutes, long expireTime) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

            String content = getContentFromList(mutes);
//            showToast(context.getString(R.string.chat_room_listener_onMuteListAdded, content));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onMuteListAdded, content));
        }

        @Override
        public void onMuteListRemoved(String chatRoomId, List<String> mutes) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);
            String content = getContentFromList(mutes);
//            showToast(context.getString(R.string.chat_room_listener_onMuteListRemoved, content));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onMuteListRemoved, content));
        }

        @Override
        public void onWhiteListAdded(String chatRoomId, List<String> whitelist) {
            String content = getContentFromList(whitelist);
//            showToast(context.getString(R.string.chat_room_listener_onWhiteListAdded, content));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onWhiteListAdded, content));
        }

        @Override
        public void onWhiteListRemoved(String chatRoomId, List<String> whitelist) {
            String content = getContentFromList(whitelist);
//            showToast(context.getString(R.string.chat_room_listener_onWhiteListRemoved, content));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onWhiteListRemoved, content));
        }

        @Override
        public void onAllMemberMuteStateChanged(String chatRoomId, boolean isMuted) {
//            showToast(context.getString(isMuted ? R.string.chat_room_listener_onAllMemberMuteStateChanged_mute
//                    : R.string.chat_room_listener_onAllMemberMuteStateChanged_note_mute));
            EMLog.i(TAG, context.getString(isMuted ? R.string.chat_room_listener_onAllMemberMuteStateChanged_mute
                    : R.string.chat_room_listener_onAllMemberMuteStateChanged_note_mute));
        }

        @Override
        public void onAdminAdded(String chatRoomId, String admin) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

//            showToast(context.getString(R.string.chat_room_listener_onAdminAdded, admin));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onAdminAdded, admin));
        }

        @Override
        public void onAdminRemoved(String chatRoomId, String admin) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

//            showToast(context.getString(R.string.chat_room_listener_onAdminRemoved, admin));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onAdminRemoved, admin));
        }

        @Override
        public void onOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

//            showToast(context.getString(R.string.chat_room_listener_onOwnerChanged, oldOwner, newOwner));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onOwnerChanged, oldOwner, newOwner));
        }

        @Override
        public void onAnnouncementChanged(String chatRoomId, String announcement) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);
//            showToast(context.getString(R.string.chat_room_listener_onAnnouncementChanged));
            EMLog.i(TAG, context.getString(R.string.chat_room_listener_onAnnouncementChanged));
        }
    }

    private void setChatRoomEvent(String roomId, EaseEvent.TYPE type) {
        EaseEvent easeEvent = new EaseEvent(DemoConstant.CHAT_ROOM_CHANGE, type);
        easeEvent.message = roomId;
        messageChangeLiveData.with(DemoConstant.CHAT_ROOM_CHANGE).postValue(easeEvent);
    }

    private String getContentFromList(List<String> members) {
        StringBuilder sb = new StringBuilder();
        for (String member : members) {
            if(!TextUtils.isEmpty(sb.toString().trim())) {
                sb.append(",");
            }
            sb.append(member);
        }
        String content = sb.toString();
        if(content.contains(ChatClient.getInstance().getCurrentUser())) {
            content = "You";
        }
        return content;
    }
}
