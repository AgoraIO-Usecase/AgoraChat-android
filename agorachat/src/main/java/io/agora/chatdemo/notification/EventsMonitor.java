package io.agora.chatdemo.notification;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.agora.ChatRoomChangeListener;
import io.agora.ContactListener;
import io.agora.ConversationListener;
import io.agora.MultiDeviceListener;
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.MucSharedFile;
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
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.db.entity.InviteMessageStatus;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.manager.PushAndMessageHelper;
import io.agora.chatdemo.general.repositories.EMClientRepository;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.main.MainActivity;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

/**
 * 主要用于chat过程中的全局监听，并对相应的事件进行处理
 * {@link #init()}方法建议在登录成功以后进行调用
 */
public class EventsMonitor extends EaseChatPresenter {
    private static final String TAG = EventsMonitor.class.getSimpleName();
    private static final int HANDLER_SHOW_TOAST = 0;
    private static EventsMonitor instance;
    private LiveDataBus messageChangeLiveData;
    private boolean isGroupsSyncedWithServer = false;
    private boolean isContactsSyncedWithServer = false;
    private boolean isBlackListSyncedWithServer = false;
    private boolean isPushConfigsWithServer = false;
    private Context appContext;
    protected Handler handler;
    private EMClientRepository mRepository;

    Queue<String> msgQueue = new ConcurrentLinkedQueue<>();

    private EventsMonitor() {
        appContext = DemoApplication.getInstance();
        initHandler(appContext.getMainLooper());
        messageChangeLiveData = LiveDataBus.get();
        //添加网络连接状态监听
//        DemoHelper.getInstance().getChatClient().addConnectionListener(new ChatConnectionListener());
        //添加多端登录监听
//        DemoHelper.getInstance().getChatClient().addMultiDeviceListener(new ChatMultiDeviceListener());
        //添加群组监听
        DemoHelper.getInstance().getGroupManager().addGroupChangeListener(new ChatGroupListener());
        //添加联系人监听
        DemoHelper.getInstance().getContactManager().setContactListener(new ChatContactListener());
        //添加聊天室监听
        DemoHelper.getInstance().getChatroomManager().addChatRoomChangeListener(new ChatRoomListener());
        //添加对会话的监听（监听已读回执）
        DemoHelper.getInstance().getChatManager().addConversationListener(new ChatConversationListener());

        mRepository = new EMClientRepository();

    }

    public static EventsMonitor getInstance() {
        if(instance == null) {
            synchronized (EventsMonitor.class) {
                if(instance == null) {
                    instance = new EventsMonitor();
                }
            }
        }
        return instance;
    }

    /**
     * 将需要登录成功进入MainActivity中初始化的逻辑，放到此处进行处理
     */
    public void init() {

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

    void showToast(@StringRes int mesId) {
        showToast(context.getString(mesId));
    }

    void showToast(final String message) {
        Log.d(TAG, "receive invitation to join the group：" + message);
        if (handler != null) {
            Message msg = Message.obtain(handler, HANDLER_SHOW_TOAST, message);
            handler.sendMessage(msg);
        } else {
            msgQueue.add(message);
        }
    }

    @Override
    public void onMessageReceived(List<ChatMessage> messages) {
        super.onMessageReceived(messages);
        EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECEIVE, EaseEvent.TYPE.MESSAGE);
        messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
        for (ChatMessage message : messages) {
            EMLog.d(TAG, "onMessageReceived id : " + message.getMsgId());
            EMLog.d(TAG, "onMessageReceived: " + message.getType());
            // 如果设置群组离线消息免打扰，则不进行消息通知
            List<String> disabledIds = DemoHelper.getInstance().getPushManager().getNoPushGroups();
            if(disabledIds != null && disabledIds.contains(message.conversationId())) {
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



    /**
     * 判断是否已经启动了MainActivity
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
//        if(!(DemoApplication.getInstance().getLifecycleCallbacks().current() instanceof ChatActivity)) {
//            EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_CHANGE_RECALL, EaseEvent.TYPE.MESSAGE);
//            messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);
//        }
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
            TextMessageBody txtBody = new TextMessageBody(String.format(context.getString(R.string.msg_recall_by_user), msg.getFrom()));
            msgNotification.addBody(txtBody);
            msgNotification.setFrom(msg.getFrom());
            msgNotification.setTo(msg.getTo());
            msgNotification.setUnread(false);
            msgNotification.setMsgTime(msg.getMsgTime());
            msgNotification.setLocalTime(msg.getMsgTime());
            msgNotification.setChatType(msg.getChatType());
            msgNotification.setAttribute(DemoConstant.MESSAGE_TYPE_RECALL, true);
            msgNotification.setStatus(ChatMessage.Status.SUCCESS);
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

//    private class ChatConnectionListener implements ConnectionListener {
//
//        @Override
//        public void onConnected() {
//            EMLog.i(TAG, "onConnected");
//            if(!DemoHelper.getInstance().isLoggedIn()) {
//                return;
//            }
//            if(!isGroupsSyncedWithServer) {
//                EMLog.i(TAG, "isGroupsSyncedWithServer");
//                new EMGroupManagerRepository().getAllGroups(new ResultCallBack<List<EMGroup>>() {
//                    @Override
//                    public void onSuccess(List<EMGroup> value) {
//                        //加载完群组信息后，刷新会话列表页面，保证展示群组名称
//                        EMLog.i(TAG, "isGroupsSyncedWithServer success");
//                        EaseEvent event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
//                        messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(event);
//                    }
//
//                    @Override
//                    public void onError(int error, String errorMsg) {
//
//                    }
//                });
//                isGroupsSyncedWithServer = true;
//            }
//            if(!isContactsSyncedWithServer) {
//                EMLog.i(TAG, "isContactsSyncedWithServer");
//                new EMContactManagerRepository().getContactList(new ResultCallBack<List<EaseUser>>() {
//                    @Override
//                    public void onSuccess(List<EaseUser> value) {
//                        EmUserDao userDao = DemoDbHelper.getInstance(DemoApplication.getInstance()).getUserDao();
//                        if(userDao != null) {
//                            userDao.clearUsers();
//                            userDao.insert(EmUserEntity.parseList(value));
//                        }
//                    }
//
//                    @Override
//                    public void onError(int error, String errorMsg) {
//
//                    }
//                });
//                isContactsSyncedWithServer = true;
//            }
//            if(!isBlackListSyncedWithServer) {
//                EMLog.i(TAG, "isBlackListSyncedWithServer");
//                new EMContactManagerRepository().getBlackContactList(null);
//                isBlackListSyncedWithServer = true;
//            }
//            if(!isPushConfigsWithServer) {
//                EMLog.i(TAG, "isPushConfigsWithServer");
//                //首先获取push配置，否则获取push配置项会为空
//                new EMPushManagerRepository().fetchPushConfigsFromServer();
//                isPushConfigsWithServer = true;
//            }
//        }
//
//        /**
//         * 用来监听账号异常
//         * @param error
//         */
//        @Override
//        public void onDisconnected(int error) {
//            EMLog.i(TAG, "onDisconnected ="+error);
//            String event = null;
//            if (error == EMError.USER_REMOVED) {
//                event = DemoConstant.ACCOUNT_REMOVED;
//            } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
//                event = DemoConstant.ACCOUNT_CONFLICT;
//            } else if (error == EMError.SERVER_SERVICE_RESTRICTED) {
//                event = DemoConstant.ACCOUNT_FORBIDDEN;
//            } else if (error == EMError.USER_KICKED_BY_CHANGE_PASSWORD) {
//                event = DemoConstant.ACCOUNT_KICKED_BY_CHANGE_PASSWORD;
//            } else if (error == EMError.USER_KICKED_BY_OTHER_DEVICE) {
//                event = DemoConstant.ACCOUNT_KICKED_BY_OTHER_DEVICE;
//            }
//            if(!TextUtils.isEmpty(event)) {
//                LiveDataBus.get().with(DemoConstant.ACCOUNT_CHANGE).postValue(new EaseEvent(event, EaseEvent.TYPE.ACCOUNT));
//                EMLog.i(TAG, event);
//            }
//        }
//
//        @Override
//        public void onTokenExpired() {
//            String event = DemoConstant.ACCOUNT_TOKENEXPIRED;
//            LiveDataBus.get().with(DemoConstant.ACCOUNT_CHANGE).postValue(new EaseEvent(event, EaseEvent.TYPE.ACCOUNT));
//            EMLog.i(TAG, event);
//        }
//
//        @Override
//        public void onTokenWillExpire() {
//            mRepository.reNewToken();
//            EMLog.i(TAG, "onTokenWillExpire");
//        }
//    }

    private class ChatGroupListener extends EaseGroupListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {
            super.onInvitationReceived(groupId, groupName, inviter, reason);
            //移除相同的请求

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

            showToast(context.getString(InviteMessageStatus.GROUPINVITATION.getMsgContent(), inviter, groupName));
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

            showToast(context.getString(InviteMessageStatus.GROUPINVITATION_ACCEPTED.getMsgContent(), invitee));
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

            showToast(context.getString(InviteMessageStatus.GROUPINVITATION_DECLINED.getMsgContent(), invitee));
            EMLog.i(TAG, context.getString(InviteMessageStatus.GROUPINVITATION_DECLINED.getMsgContent(), invitee));
        }

        @Override
        public void onUserRemoved(String groupId, String groupName) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP_LEAVE);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            showToast(context.getString(R.string.demo_group_listener_onUserRemoved, groupName));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onUserRemoved, groupName));
        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP_LEAVE);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            showToast(context.getString(R.string.demo_group_listener_onGroupDestroyed, groupName));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onGroupDestroyed, groupName));
        }

        @Override
        public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {
            super.onRequestToJoinReceived(groupId, groupName, applicant, reason);
            //移除相同的请求
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

            showToast(context.getString(InviteMessageStatus.BEAPPLYED.getMsgContent(), applicant, groupName));
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
            msg.addBody(new TextMessageBody(context.getString(R.string.demo_group_listener_onRequestToJoinAccepted, accepter, groupName)));
            msg.setStatus(ChatMessage.Status.SUCCESS);
            // save accept message
            ChatClient.getInstance().chatManager().saveMessage(msg);
            // notify the accept message
            getNotifier().vibrateAndPlayTone(msg);

            EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_GROUP_JOIN_ACCEPTED, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);

            showToast(context.getString(R.string.demo_group_listener_onRequestToJoinAccepted, accepter, groupName));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onRequestToJoinAccepted, accepter, groupName));
        }

        @Override
        public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {
            super.onRequestToJoinDeclined(groupId, groupName, decliner, reason);
            showToast(context.getString(R.string.demo_group_listener_onRequestToJoinDeclined, decliner, groupName));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onRequestToJoinDeclined, decliner, groupName));
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
            msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true);
            msg.addBody(new TextMessageBody(context.getString(R.string.demo_group_listener_onAutoAcceptInvitationFromGroup, groupName)));
            msg.setStatus(ChatMessage.Status.SUCCESS);
            // save invitation as messages
            ChatClient.getInstance().chatManager().saveMessage(msg);
            // notify invitation message
            getNotifier().vibrateAndPlayTone(msg);
            EaseEvent event = EaseEvent.create(DemoConstant.MESSAGE_GROUP_AUTO_ACCEPT, EaseEvent.TYPE.MESSAGE);
            messageChangeLiveData.with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(event);

            showToast(context.getString(R.string.demo_group_listener_onAutoAcceptInvitationFromGroup, groupName));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onAutoAcceptInvitationFromGroup, groupName));
        }

        @Override
        public void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {
            super.onMuteListAdded(groupId, mutes, muteExpire);
            String content = getContentFromList(mutes);
            showToast(context.getString(R.string.demo_group_listener_onMuteListAdded, content));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onMuteListAdded, content));
        }

        @Override
        public void onMuteListRemoved(String groupId, List<String> mutes) {
            super.onMuteListRemoved(groupId, mutes);
            String content = getContentFromList(mutes);
            showToast(context.getString(R.string.demo_group_listener_onMuteListRemoved, content));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onMuteListRemoved, content));
        }

        @Override
        public void onWhiteListAdded(String groupId, List<String> whitelist) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            String content = getContentFromList(whitelist);
            showToast(context.getString(R.string.demo_group_listener_onWhiteListAdded, content));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onWhiteListAdded, content));
        }

        @Override
        public void onWhiteListRemoved(String groupId, List<String> whitelist) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            String content = getContentFromList(whitelist);
            showToast(context.getString(R.string.demo_group_listener_onWhiteListRemoved, content));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onWhiteListRemoved, content));
        }

        @Override
        public void onAllMemberMuteStateChanged(String groupId, boolean isMuted) {
            EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
            easeEvent.message = groupId;
            messageChangeLiveData.with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);

            showToast(context.getString(isMuted ? R.string.demo_group_listener_onAllMemberMuteStateChanged_mute
                    : R.string.demo_group_listener_onAllMemberMuteStateChanged_not_mute));

            EMLog.i(TAG, context.getString(isMuted ? R.string.demo_group_listener_onAllMemberMuteStateChanged_mute
                    : R.string.demo_group_listener_onAllMemberMuteStateChanged_not_mute));
        }

        @Override
        public void onAdminAdded(String groupId, String administrator) {
            super.onAdminAdded(groupId, administrator);
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onAdminAdded, administrator));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onAdminAdded, administrator));
        }

        @Override
        public void onAdminRemoved(String groupId, String administrator) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onAdminRemoved, administrator));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onAdminRemoved, administrator));
        }

        @Override
        public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_OWNER_TRANSFER, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onOwnerChanged, oldOwner, newOwner));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onOwnerChanged, oldOwner, newOwner));
        }

        @Override
        public void onMemberJoined(String groupId, String member) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onMemberJoined, member));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onMemberJoined, member));
        }

        @Override
        public void onMemberExited(String groupId, String member) {
            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onMemberExited, member));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onMemberExited, member));
        }

        @Override
        public void onAnnouncementChanged(String groupId, String announcement) {
            showToast(context.getString(R.string.demo_group_listener_onAnnouncementChanged));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onAnnouncementChanged));
        }

        @Override
        public void onSharedFileAdded(String groupId, MucSharedFile sharedFile) {
            LiveDataBus.get().with(DemoConstant.GROUP_SHARE_FILE_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_SHARE_FILE_CHANGE, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onSharedFileAdded, sharedFile.getFileName()));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onSharedFileAdded, sharedFile.getFileName()));
        }

        @Override
        public void onSharedFileDeleted(String groupId, String fileId) {
            LiveDataBus.get().with(DemoConstant.GROUP_SHARE_FILE_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_SHARE_FILE_CHANGE, EaseEvent.TYPE.GROUP));
            showToast(context.getString(R.string.demo_group_listener_onSharedFileDeleted, fileId));
            EMLog.i(TAG, context.getString(R.string.demo_group_listener_onSharedFileDeleted, fileId));
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

                    showToast(context.getString(R.string.demo_contact_listener_onContactAdded, username));
                    EMLog.i(TAG, context.getString(R.string.demo_contact_listener_onContactAdded, username));
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.i(TAG, context.getString(R.string.demo_contact_get_userInfo_failed) +  username + "error:" + error + " errorMsg:" +errorMsg);
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
                showToast(context.getString(R.string.demo_contact_listener_onContactDeleted, username));
                EMLog.i(TAG, context.getString(R.string.demo_contact_listener_onContactDeleted, username));
            }else {
                //showToast(context.getString(R.string.demo_contact_listener_onContactDeleted_by_other, username));
                EMLog.i(TAG, context.getString(R.string.demo_contact_listener_onContactDeleted_by_other, username));
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

            showToast(context.getString(InviteMessageStatus.BEINVITEED.getMsgContent(), username));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEINVITEED.getMsgContent(), username));
        }

        @Override
        public void onFriendRequestAccepted(String username) {
            EMLog.i("ChatContactListener", "onFriendRequestAccepted");
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
            Map<String, Object> ext = EaseNotificationMsgManager.getInstance().createMsgExt();
            ext.put(DemoConstant.SYSTEM_MESSAGE_FROM, username);
            ext.put(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name());
            ChatMessage message = EaseNotificationMsgManager.getInstance().createMessage(PushAndMessageHelper.getSystemMessage(ext), ext);

            notifyNewInviteMessage(message);
            EaseEvent event = EaseEvent.create(DemoConstant.CONTACT_CHANGE, EaseEvent.TYPE.CONTACT);
            messageChangeLiveData.with(DemoConstant.CONTACT_CHANGE).postValue(event);

            showToast(context.getString(InviteMessageStatus.BEAGREED.getMsgContent()));
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
            showToast(context.getString(InviteMessageStatus.BEREFUSED.getMsgContent(), username));
            EMLog.i(TAG, context.getString(InviteMessageStatus.BEREFUSED.getMsgContent(), username));
        }
    }


    private void updateMessage(ChatMessage message) {
        message.setAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS, InviteMessageStatus.BEAGREED.name());
        TextMessageBody body = new TextMessageBody(PushAndMessageHelper.getSystemMessage(message.ext()));
        message.addBody(body);
        EaseNotificationMsgManager.getInstance().updateMessage(message);
    }

    private class ChatMultiDeviceListener implements MultiDeviceListener {


        @Override
        public void onContactEvent(int event, String target, String ext) {
            EMLog.i(TAG, "onContactEvent event"+event);
            DemoDbHelper dbHelper = DemoDbHelper.getInstance(DemoApplication.getInstance());
            String message = null;
            switch (event) {
                case CONTACT_REMOVE: //好友已经在其他机子上被移除
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_REMOVE");
                    message = DemoConstant.CONTACT_REMOVE;
                    if(dbHelper.getUserDao() != null) {
                        dbHelper.getUserDao().deleteUser(target);
                    }
                    removeTargetSystemMessage(target, DemoConstant.SYSTEM_MESSAGE_FROM);
                    // TODO: 2020/1/16 0016 确认此处逻辑，是否是删除当前的target
                    DemoHelper.getInstance().getChatManager().deleteConversation(target, false);

                    showToast("CONTACT_REMOVE");
                    break;
                case CONTACT_ACCEPT: //好友请求已经在其他机子上被同意
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_ACCEPT");
                    message = DemoConstant.CONTACT_ACCEPT;
                    EmUserEntity  entity = new EmUserEntity();
                    entity.setUsername(target);
                    if(dbHelper.getUserDao() != null) {
                        dbHelper.getUserDao().insert(entity);
                    }
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_ACCEPT);

                    showToast("CONTACT_ACCEPT");
                    break;
                case CONTACT_DECLINE: //好友请求已经在其他机子上被拒绝
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_DECLINE");
                    message = DemoConstant.CONTACT_DECLINE;
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_DECLINE);

                    showToast("CONTACT_DECLINE");
                    break;
                case CONTACT_BAN: //当前用户在其他设备加某人进入黑名单
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
                case CONTACT_ALLOW: // 好友在其他设备被移出黑名单
                    EMLog.i("ChatMultiDeviceListener", "CONTACT_ALLOW");
                    message = DemoConstant.CONTACT_ALLOW;
                    updateContactNotificationStatus(target, "", InviteMessageStatus.MULTI_DEVICE_CONTACT_ALLOW);

                    showToast("CONTACT_ALLOW");
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

                    showToast("GROUP_CREATE");
                    break;
                case GROUP_DESTROY:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_DESTROY);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_DESTROY");
                    break;
                case GROUP_JOIN:
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_JOIN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_JOIN");
                    break;
                case GROUP_LEAVE:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_LEAVE);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_LEAVE");
                    break;
                case GROUP_APPLY:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY);

                    showToast("GROUP_APPLY");
                    break;
                case GROUP_APPLY_ACCEPT:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID, usernames.get(0), DemoConstant.SYSTEM_MESSAGE_FROM);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_ACCEPT);

                    showToast("GROUP_APPLY_ACCEPT");
                    break;
                case GROUP_APPLY_DECLINE:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID, usernames.get(0), DemoConstant.SYSTEM_MESSAGE_FROM);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_APPLY_DECLINE);

                    showToast("GROUP_APPLY_DECLINE");
                    break;
                case GROUP_INVITE:
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE);

                    showToast("GROUP_INVITE");
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
                    msg.setAttribute(DemoConstant.EM_NOTIFICATION_TYPE, true);
                    msg.addBody(new TextMessageBody(msg.getFrom() + " " +st3));
                    msg.setStatus(ChatMessage.Status.SUCCESS);
                    // save invitation as messages
                    ChatClient.getInstance().chatManager().saveMessage(msg);

                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/"", /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_ACCEPT);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_INVITE_ACCEPT");
                    break;
                case GROUP_INVITE_DECLINE:
                    removeTargetSystemMessage(groupId, DemoConstant.SYSTEM_MESSAGE_GROUP_ID);
                    // TODO: person, reason from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_INVITE_DECLINE);

                    showToast("GROUP_INVITE_DECLINE");
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

                    showToast("GROUP_BAN");
                    break;
                case GROUP_ALLOW:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ALLOW);

                    showToast("GROUP_ALLOW");
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

                    showToast("GROUP_ASSIGN_OWNER");
                    break;
                case GROUP_ADD_ADMIN:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_ADD_ADMIN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_ADD_ADMIN");
                    break;
                case GROUP_REMOVE_ADMIN:
                    // TODO: person from ext
                    saveGroupNotification(groupId, /*groupName*/"",  /*person*/usernames.get(0), /*reason*/"", InviteMessageStatus.MULTI_DEVICE_GROUP_REMOVE_ADMIN);
                    message = DemoConstant.GROUP_CHANGE;

                    showToast("GROUP_REMOVE_ADMIN");
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
    }

    /**
     * 移除目标所有的消息记录，如果目标被删除
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
     * 移除目标所有的消息记录，如果目标被删除
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
            showToast(context.getString(R.string.demo_chat_room_listener_onChatRoomDestroyed, roomName));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onChatRoomDestroyed, roomName));
        }

        @Override
        public void onMemberJoined(String roomId, String participant) {
            setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM);
            showToast(context.getString(R.string.demo_chat_room_listener_onMemberJoined, participant));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onMemberJoined, participant));
        }

        @Override
        public void onMemberExited(String roomId, String roomName, String participant) {
            setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM);
            showToast(context.getString(R.string.demo_chat_room_listener_onMemberExited, participant));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onMemberExited, participant));
        }

        @Override
        public void onRemovedFromChatRoom(int reason, String roomId, String roomName, String participant) {
            if(TextUtils.equals(DemoHelper.getInstance().getCurrentUser(), participant)) {
                setChatRoomEvent(roomId, EaseEvent.TYPE.CHAT_ROOM);
                if(reason == EMAChatRoomManagerListener.BE_KICKED) {
                    showToast(R.string.quiting_the_chat_room);
                    showToast(R.string.quiting_the_chat_room);
                }else {
                    showToast(context.getString(R.string.demo_chat_room_listener_onRemovedFromChatRoom, participant));
                    EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onRemovedFromChatRoom, participant));
                }

            }
        }

        @Override
        public void onMuteListAdded(String chatRoomId, List<String> mutes, long expireTime) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

            String content = getContentFromList(mutes);
            showToast(context.getString(R.string.demo_chat_room_listener_onMuteListAdded, content));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onMuteListAdded, content));
        }

        @Override
        public void onMuteListRemoved(String chatRoomId, List<String> mutes) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);
            String content = getContentFromList(mutes);
            showToast(context.getString(R.string.demo_chat_room_listener_onMuteListRemoved, content));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onMuteListRemoved, content));
        }

        @Override
        public void onWhiteListAdded(String chatRoomId, List<String> whitelist) {
            String content = getContentFromList(whitelist);
            showToast(context.getString(R.string.demo_chat_room_listener_onWhiteListAdded, content));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onWhiteListAdded, content));
        }

        @Override
        public void onWhiteListRemoved(String chatRoomId, List<String> whitelist) {
            String content = getContentFromList(whitelist);
            showToast(context.getString(R.string.demo_chat_room_listener_onWhiteListRemoved, content));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onWhiteListRemoved, content));
        }

        @Override
        public void onAllMemberMuteStateChanged(String chatRoomId, boolean isMuted) {
            showToast(context.getString(isMuted ? R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_mute
                    : R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_note_mute));
            EMLog.i(TAG, context.getString(isMuted ? R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_mute
                    : R.string.demo_chat_room_listener_onAllMemberMuteStateChanged_note_mute));
        }

        @Override
        public void onAdminAdded(String chatRoomId, String admin) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

            showToast(context.getString(R.string.demo_chat_room_listener_onAdminAdded, admin));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onAdminAdded, admin));
        }

        @Override
        public void onAdminRemoved(String chatRoomId, String admin) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

            showToast(context.getString(R.string.demo_chat_room_listener_onAdminRemoved, admin));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onAdminRemoved, admin));
        }

        @Override
        public void onOwnerChanged(String chatRoomId, String newOwner, String oldOwner) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);

            showToast(context.getString(R.string.demo_chat_room_listener_onOwnerChanged, oldOwner, newOwner));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onOwnerChanged, oldOwner, newOwner));
        }

        @Override
        public void onAnnouncementChanged(String chatRoomId, String announcement) {
            setChatRoomEvent(chatRoomId, EaseEvent.TYPE.CHAT_ROOM);
            showToast(context.getString(R.string.demo_chat_room_listener_onAnnouncementChanged));
            EMLog.i(TAG, context.getString(R.string.demo_chat_room_listener_onAnnouncementChanged));
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
            content = "您";
        }
        return content;
    }
}
