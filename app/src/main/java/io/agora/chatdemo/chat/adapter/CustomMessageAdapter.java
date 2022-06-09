package io.agora.chatdemo.chat.adapter;

import android.view.ViewGroup;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chatdemo.chat.chatrow.ChatRowSystemNotification;
import io.agora.chatdemo.chat.viewholder.ChatSystemNotificationViewHolder;
import io.agora.chatdemo.general.constant.DemoConstant;

public class CustomMessageAdapter extends EaseMessageAdapter {
    private static final int TEXT_SYSTEM_NOTIFICATION = 66;

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TEXT_SYSTEM_NOTIFICATION) {
            return new ChatSystemNotificationViewHolder(new ChatRowSystemNotification(mContext, true), listener);
        }
        return super.getViewHolder(parent, viewType);
    }

    @Override
    public int getItemNotEmptyViewType(int position) {
        ChatMessage message = getData().get(position);
        boolean isSystemNotification = message.getBooleanAttribute(DemoConstant.EASE_SYSTEM_NOTIFICATION_TYPE, false);
        if (isSystemNotification){
            return TEXT_SYSTEM_NOTIFICATION;
        }
        return super.getItemNotEmptyViewType(position);
    }

}


