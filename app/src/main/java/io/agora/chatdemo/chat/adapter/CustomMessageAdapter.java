package io.agora.chatdemo.chat.adapter;

import static io.agora.chatdemo.general.constant.DemoConstant.TEXT_SYSTEM_NOTIFICATION;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CALL_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CALL_OTHER;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VIDEO_CALL;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VOICE_CALL;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CUSTOM_TEXT_OTHER;

import android.text.TextUtils;
import android.view.ViewGroup;

import io.agora.chat.ChatMessage;
import io.agora.chat.callkit.general.EaseCallAction;
import io.agora.chat.callkit.general.EaseCallType;
import io.agora.chat.callkit.utils.EaseCallMsgUtils;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chat.uikit.chat.viewholder.EaseMessageViewType;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chatdemo.chat.CallViewHolder;
import io.agora.chatdemo.chat.ChatRowCall;
import io.agora.chatdemo.chat.chatrow.ChatRowCustomTextView;
import io.agora.chatdemo.chat.viewholder.ChatCustomTextViewHolder;
import io.agora.chatdemo.chat.chatrow.ChatRowSystemNotification;
import io.agora.chatdemo.chat.viewholder.ChatSystemNotificationViewHolder;
import io.agora.chatdemo.general.constant.DemoConstant;

public class CustomMessageAdapter extends EaseMessageAdapter {
    private TranslationListener translationlistener;

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TEXT_SYSTEM_NOTIFICATION) {
            return new ChatSystemNotificationViewHolder(new ChatRowSystemNotification(mContext, true), listener);
        }else if(viewType == VIEW_TYPE_MESSAGE_CALL_ME || viewType == VIEW_TYPE_MESSAGE_CALL_OTHER) {
            return new CallViewHolder(new ChatRowCall(mContext,viewType == VIEW_TYPE_MESSAGE_CALL_ME),listener);
        }else if (viewType == VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME || viewType == VIEW_TYPE_MESSAGE_CUSTOM_TEXT_OTHER){
            return new ChatCustomTextViewHolder(new ChatRowCustomTextView(mContext,viewType == VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME),listener,translationlistener);
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
            boolean isThreadNotify = message.getBooleanAttribute(EaseConstant.EASE_THREAD_NOTIFICATION_TYPE, false);
            if(isThreadNotify) {
                return EaseMessageViewType.VIEW_TYPE_MESSAGE_CHAT_THREAD_NOTIFY.getValue();
            }else {
                if (message.direct() == ChatMessage.Direct.SEND) {
                    return VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME;
                } else {
                    return VIEW_TYPE_MESSAGE_CUSTOM_TEXT_OTHER;
                }
            }
        }

        return super.getItemNotEmptyViewType(position);
    }

    public void setTranslationListener(TranslationListener listener){
        this.translationlistener = listener;
    }

    public interface TranslationListener{
        void onTranslationRetry(ChatMessage message,String languageCode);
    }

}


