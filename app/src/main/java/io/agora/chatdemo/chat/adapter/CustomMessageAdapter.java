package io.agora.chatdemo.chat.adapter;

import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CALL_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CALL_OTHER;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VIDEO_CALL;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VOICE_CALL;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_URL_PREVIEW_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_URL_PREVIEW_OTHER;

import android.text.TextUtils;
import android.view.ViewGroup;

import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.callkit.general.EaseCallAction;
import io.agora.chat.callkit.general.EaseCallType;
import io.agora.chat.callkit.utils.EaseCallMsgUtils;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.chat.CallViewHolder;
import io.agora.chatdemo.chat.ChatRowCall;
import io.agora.chatdemo.chat.ChatRowUrlPreView;
import io.agora.chatdemo.chat.UrlPreViewHolder;
import io.agora.chatdemo.chat.chatrow.ChatRowSystemNotification;
import io.agora.chatdemo.chat.viewholder.ChatSystemNotificationViewHolder;
import io.agora.chatdemo.general.constant.DemoConstant;

public class CustomMessageAdapter extends EaseMessageAdapter {
    private static final int TEXT_SYSTEM_NOTIFICATION = 66;

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TEXT_SYSTEM_NOTIFICATION) {
            return new ChatSystemNotificationViewHolder(new ChatRowSystemNotification(mContext, true), listener);
        }else if(viewType == VIEW_TYPE_MESSAGE_CALL_ME || viewType == VIEW_TYPE_MESSAGE_CALL_OTHER) {
            return new CallViewHolder(new ChatRowCall(mContext,viewType == VIEW_TYPE_MESSAGE_CALL_ME),listener);
        }else if (viewType == VIEW_TYPE_MESSAGE_URL_PREVIEW_ME || viewType == VIEW_TYPE_MESSAGE_URL_PREVIEW_OTHER){
            return new UrlPreViewHolder(new ChatRowUrlPreView(mContext,viewType == VIEW_TYPE_MESSAGE_URL_PREVIEW_ME),listener);
        }
        return super.getViewHolder(parent, viewType);
    }

    @Override
    public int getItemNotEmptyViewType(int position) {
        ChatMessage message = getData().get(position);
        String messageType = message.getStringAttribute(EaseCallMsgUtils.CALL_MSG_TYPE, "");
        String action = message.getStringAttribute(EaseCallMsgUtils.CALL_ACTION, "");
        int callType = message.getIntAttribute(EaseCallMsgUtils.CALL_TYPE, SINGLE_VOICE_CALL.code);
        EaseCallType callKitType = EaseCallType.getfrom(callType);
        EaseCallAction callAction = EaseCallAction.getfrom(action);

        boolean isSystemNotification = message.getBooleanAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, false);
        if (isSystemNotification){
            return TEXT_SYSTEM_NOTIFICATION;
        }

        if (TextUtils.equals(messageType, EaseCallMsgUtils.CALL_MSG_INFO)) {
            if(callAction == EaseCallAction.CALL_INVITE
                    &&(callKitType == SINGLE_VOICE_CALL || callKitType == SINGLE_VIDEO_CALL)) {
                return super.getItemNotEmptyViewType(position);
            }
            if(message.direct()==ChatMessage.Direct.SEND) {
                return VIEW_TYPE_MESSAGE_CALL_ME;
            }else {
                return VIEW_TYPE_MESSAGE_CALL_OTHER;
            }
        }

        if (message.getType() == ChatMessage.Type.TXT){
            if (DemoHelper.getInstance().containsUrl(((TextMessageBody) message.getBody()).getMessage())){
                if(message.direct()==ChatMessage.Direct.SEND) {
                    return VIEW_TYPE_MESSAGE_URL_PREVIEW_ME;
                }else {
                    return VIEW_TYPE_MESSAGE_URL_PREVIEW_OTHER;
                }
            }
        }
        return super.getItemNotEmptyViewType(position);
    }

}


