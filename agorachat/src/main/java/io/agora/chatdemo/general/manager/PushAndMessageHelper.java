package io.agora.chatdemo.general.manager;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Map;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.ImageMessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.utils.EaseFileUtils;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.entity.InviteMessage;
import io.agora.chatdemo.general.db.entity.InviteMessageStatus;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.exceptions.ChatException;

/**
 * 用于处理推送及消息相关
 */
public class PushAndMessageHelper {

    private static boolean isLock;


    /**
     * 转发消息
     * @param toChatUsername
     * @param msgId
     */
    public static void sendForwardMessage(String toChatUsername, String msgId) {
        if(TextUtils.isEmpty(msgId)) {
            
            return;
        }
        ChatMessage message = DemoHelper.getInstance().getChatManager().getMessage(msgId);
        ChatMessage.Type type = message.getType();
        switch (type) {
            case TXT:
                if(message.getBooleanAttribute(EaseConstant.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)){
                    sendBigExpressionMessage(toChatUsername, ((TextMessageBody) message.getBody()).getMessage(),
                            message.getStringAttribute(EaseConstant.MESSAGE_ATTR_EXPRESSION_ID, null));
                }else{
                    // get the content and send it
                    String content = ((TextMessageBody) message.getBody()).getMessage();
                    sendTextMessage(toChatUsername, content);
                }
                break;
            case IMAGE:
                // send image
                Uri uri = getImageForwardUri((ImageMessageBody) message.getBody());
                if(uri != null) {
                    sendImageMessage(toChatUsername, uri);
                }else {
                    LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD)
                            .postValue(new EaseEvent("不存在图片资源", EaseEvent.TYPE.MESSAGE));
                }
                break;
        }
    }

    public static Uri getImageForwardUri(ImageMessageBody body) {
        if(body == null) {
            return null;
        }
        Uri localUri = body.getLocalUri();
        Context context = DemoApplication.getInstance().getApplicationContext();
        if(EaseFileUtils.isFileExistByUri(context, localUri)) {
            return localUri;
        }
        localUri = body.thumbnailLocalUri();
        if(EaseFileUtils.isFileExistByUri(context, localUri)) {
            return localUri;
        }
        return null;
    }

    /**
     * 获取系统消息内容
     * @param msg
     * @return
     */
    public static String getSystemMessage(InviteMessage msg) {
        InviteMessageStatus status = msg.getStatusEnum();
        if(status == null) {
            return "";
        }
        String messge;
        Context context = DemoApplication.getInstance();
        StringBuilder builder = new StringBuilder(context.getString(status.getMsgContent()));
        switch (status) {
            case BEINVITEED:
            case AGREED:
            case BEREFUSED:
                messge = String.format(builder.toString(), msg.getFrom());
                break;
            case BEAGREED:
                messge = builder.toString();
                break;
            case BEAPPLYED:
            case GROUPINVITATION:
                messge = String.format(builder.toString(), msg.getFrom(), msg.getGroupName());
                break;
            case GROUPINVITATION_ACCEPTED:
            case GROUPINVITATION_DECLINED:
            case MULTI_DEVICE_GROUP_APPLY_ACCEPT:
            case MULTI_DEVICE_GROUP_APPLY_DECLINE:
            case MULTI_DEVICE_GROUP_INVITE:
            case MULTI_DEVICE_GROUP_INVITE_ACCEPT:
            case MULTI_DEVICE_GROUP_INVITE_DECLINE:
            case MULTI_DEVICE_GROUP_KICK:
            case MULTI_DEVICE_GROUP_BAN:
            case MULTI_DEVICE_GROUP_ALLOW:
            case MULTI_DEVICE_GROUP_ASSIGN_OWNER:
            case MULTI_DEVICE_GROUP_ADD_ADMIN:
            case MULTI_DEVICE_GROUP_REMOVE_ADMIN:
            case MULTI_DEVICE_GROUP_ADD_MUTE:
            case MULTI_DEVICE_GROUP_REMOVE_MUTE:
                messge = String.format(builder.toString(), msg.getGroupInviter());
                break;
            case MULTI_DEVICE_CONTACT_ADD:
            case MULTI_DEVICE_CONTACT_BAN:
            case MULTI_DEVICE_CONTACT_ALLOW:
            case MULTI_DEVICE_CONTACT_ACCEPT:
            case MULTI_DEVICE_CONTACT_DECLINE:
                messge = String.format(builder.toString(), msg.getFrom());
                break;
            case REFUSED:
            case MULTI_DEVICE_GROUP_APPLY:
                messge = builder.toString();
                break;
            default:
                messge = "";
                break;
        }
        return messge;
    }

    /**
     * 获取系统消息内容
     * @param msg
     * @return
     */
    public static String getSystemMessage(ChatMessage msg)  throws ChatException {
        String messageStatus = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS);
        if(TextUtils.isEmpty(messageStatus)) {
            return "";
        }
        InviteMessageStatus status = InviteMessageStatus.valueOf(messageStatus);
        if(status == null) {
            return "";
        }
        String messge;
        Context context = DemoApplication.getInstance();
        StringBuilder builder = new StringBuilder(context.getString(status.getMsgContent()));
        switch (status) {
            case BEINVITEED:
            case AGREED:
            case BEREFUSED:
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case BEAGREED:
            case MULTI_DEVICE_GROUP_LEAVE:
                messge = builder.toString();
                break;
            case BEAPPLYED:
            case GROUPINVITATION:
                String name = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_NAME);
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM), name);
                break;
            case GROUPINVITATION_ACCEPTED:
            case GROUPINVITATION_DECLINED:
            case MULTI_DEVICE_GROUP_APPLY_ACCEPT:
            case MULTI_DEVICE_GROUP_APPLY_DECLINE:
            case MULTI_DEVICE_GROUP_INVITE:
            case MULTI_DEVICE_GROUP_INVITE_ACCEPT:
            case MULTI_DEVICE_GROUP_INVITE_DECLINE:
            case MULTI_DEVICE_GROUP_KICK:
            case MULTI_DEVICE_GROUP_BAN:
            case MULTI_DEVICE_GROUP_ALLOW:
            case MULTI_DEVICE_GROUP_ASSIGN_OWNER:
            case MULTI_DEVICE_GROUP_ADD_ADMIN:
            case MULTI_DEVICE_GROUP_REMOVE_ADMIN:
            case MULTI_DEVICE_GROUP_ADD_MUTE:
            case MULTI_DEVICE_GROUP_REMOVE_MUTE:
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_INVITER));
                break;
            case MULTI_DEVICE_CONTACT_ADD:
            case MULTI_DEVICE_CONTACT_BAN:
            case MULTI_DEVICE_CONTACT_ALLOW:
            case MULTI_DEVICE_CONTACT_ACCEPT:
            case MULTI_DEVICE_CONTACT_DECLINE:
                messge = String.format(builder.toString(), msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case REFUSED:
            case MULTI_DEVICE_GROUP_APPLY:
                messge = builder.toString();
                break;
            default:
                messge = "";
                break;
        }
        return messge;
    }

    /**
     * 获取系统消息内容
     * @param msg
     * @return
     */
    public static String getSystemMessage(Map<String, Object> msg)  throws NullPointerException {
        String messageStatus = (String) msg.get(DemoConstant.SYSTEM_MESSAGE_STATUS);
        if(TextUtils.isEmpty(messageStatus)) {
            return "";
        }
        InviteMessageStatus status = InviteMessageStatus.valueOf(messageStatus);
        if(status == null) {
            return "";
        }
        String messge;
        Context context = DemoApplication.getInstance();
        StringBuilder builder = new StringBuilder(context.getString(status.getMsgContent()));
        switch (status) {
            case BEINVITEED:
            case AGREED:
            case BEREFUSED:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case BEAGREED:
            case MULTI_DEVICE_GROUP_LEAVE:
                messge = builder.toString();
                break;
            case BEAPPLYED:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_FROM), (String) msg.get(DemoConstant.SYSTEM_MESSAGE_NAME));
                break;
            case GROUPINVITATION:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_INVITER), (String) msg.get(DemoConstant.SYSTEM_MESSAGE_NAME));
                break;
            case GROUPINVITATION_ACCEPTED:
            case GROUPINVITATION_DECLINED:
            case MULTI_DEVICE_GROUP_APPLY_ACCEPT:
            case MULTI_DEVICE_GROUP_APPLY_DECLINE:
            case MULTI_DEVICE_GROUP_INVITE:
            case MULTI_DEVICE_GROUP_INVITE_ACCEPT:
            case MULTI_DEVICE_GROUP_INVITE_DECLINE:
            case MULTI_DEVICE_GROUP_KICK:
            case MULTI_DEVICE_GROUP_BAN:
            case MULTI_DEVICE_GROUP_ALLOW:
            case MULTI_DEVICE_GROUP_ASSIGN_OWNER:
            case MULTI_DEVICE_GROUP_ADD_ADMIN:
            case MULTI_DEVICE_GROUP_REMOVE_ADMIN:
            case MULTI_DEVICE_GROUP_ADD_MUTE:
            case MULTI_DEVICE_GROUP_REMOVE_MUTE:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_INVITER));
                break;
            case MULTI_DEVICE_CONTACT_ADD:
            case MULTI_DEVICE_CONTACT_BAN:
            case MULTI_DEVICE_CONTACT_ALLOW:
            case MULTI_DEVICE_CONTACT_ACCEPT:
            case MULTI_DEVICE_CONTACT_DECLINE:
                messge = String.format(builder.toString(), msg.get(DemoConstant.SYSTEM_MESSAGE_FROM));
                break;
            case REFUSED:
            case MULTI_DEVICE_GROUP_APPLY:
                messge = builder.toString();
                break;
            default:
                messge = "";
                break;
        }
        return messge;
    }

    /**
     * send big expression message
     * @param toChatUsername
     * @param name
     * @param identityCode
     */
    private static void sendBigExpressionMessage(String toChatUsername, String name, String identityCode){
        ChatMessage message = EaseUtils.createExpressionMessage(toChatUsername, name, identityCode);
        sendMessage(message);
    }

    /**
     * 发送文本消息
     * @param toChatUsername
     * @param content
     */
    private static void sendTextMessage(String toChatUsername, String content) {
        ChatMessage message = ChatMessage.createTxtSendMessage(content, toChatUsername);
        sendMessage(message);
    }

    /**
     * send image message
     * @param toChatUsername
     * @param imageUri
     */
    private static void sendImageMessage(String toChatUsername, Uri imageUri) {
        ChatMessage message = ChatMessage.createImageSendMessage(imageUri, false, toChatUsername);
        sendMessage(message);
    }

    /**
     * send image message
     * @param toChatUsername
     * @param imagePath
     */
    private static void sendImageMessage(String toChatUsername, String imagePath) {
        ChatMessage message = ChatMessage.createImageSendMessage(imagePath, false, toChatUsername);
        sendMessage(message);
    }


    /**
     * send message
     * @param message
     */
    private static void sendMessage(ChatMessage message) {
        message.setMessageStatusCallback(new CallBack() {
            @Override
            public void onSuccess() {
                LiveDataBus.get().with(DemoConstant.MESSAGE_FORWARD)
                        .postValue(new EaseEvent(DemoApplication.getInstance().getString(R.string.has_been_send), EaseEvent.TYPE.MESSAGE));
            }

            @Override
            public void onError(int code, String error) {

            }

            @Override
            public void onProgress(int progress, String status) {

            }
        });
        // send message
        ChatClient.getInstance().chatManager().sendMessage(message);

    }
}
