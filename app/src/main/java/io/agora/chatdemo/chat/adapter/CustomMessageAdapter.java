package io.agora.chatdemo.chat.adapter;

import static io.agora.chat.uikit.chat.viewholder.EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_ME;
import static io.agora.chat.uikit.chat.viewholder.EaseMessageViewType.VIEW_TYPE_MESSAGE_TXT_OTHER;
import static io.agora.chatdemo.general.constant.DemoConstant.TEXT_SYSTEM_NOTIFICATION;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CALL_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CALL_OTHER;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VIDEO_CALL;
import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VOICE_CALL;

import android.text.TextUtils;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.callkit.general.EaseCallAction;
import io.agora.chat.callkit.general.EaseCallType;
import io.agora.chat.callkit.utils.EaseCallMsgUtils;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chatdemo.chat.CallViewHolder;
import io.agora.chatdemo.chat.ChatRowCall;
import io.agora.chatdemo.chat.chatrow.ChatRowCustomTextView;
import io.agora.chatdemo.chat.viewholder.ChatCustomTextViewHolder;
import io.agora.chatdemo.chat.chatrow.ChatRowSystemNotification;
import io.agora.chatdemo.chat.viewholder.ChatSystemNotificationViewHolder;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.interfaces.TranslationListener;

public class CustomMessageAdapter extends EaseMessageAdapter {
    private TranslationListener translationlistener;

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TEXT_SYSTEM_NOTIFICATION) {
            return new ChatSystemNotificationViewHolder(new ChatRowSystemNotification(mContext, true));
        }else if(viewType == VIEW_TYPE_MESSAGE_CALL_ME || viewType == VIEW_TYPE_MESSAGE_CALL_OTHER) {
            return new CallViewHolder(new ChatRowCall(mContext,viewType == VIEW_TYPE_MESSAGE_CALL_ME));
        }else if (viewType == VIEW_TYPE_MESSAGE_TXT_ME.getValue() || viewType == VIEW_TYPE_MESSAGE_TXT_OTHER.getValue()){
            return new ChatCustomTextViewHolder(new ChatRowCustomTextView(mContext,viewType == VIEW_TYPE_MESSAGE_TXT_ME.getValue()));
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

        return super.getItemNotEmptyViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.itemView instanceof ChatRowCustomTextView){
            ((ChatRowCustomTextView)holder.itemView).setTranslationListener(translationlistener);
        }
    }

    public void setTranslationListener(TranslationListener listener){
        this.translationlistener = listener;
        notifyDataSetChanged();
    }

}


