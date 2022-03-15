package io.agora.chatdemo.chat;

import android.view.ViewGroup;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chatdemo.general.constant.DemoConstant;

public class CustomMessageAdapter extends EaseMessageAdapter {
    private static final int CUSTOM_THREAD_NOTIFY = 1000;

    @Override
    public int getItemNotEmptyViewType(int position) {
        ChatMessage message = getData().get(position);
        boolean isThreadNotify = message.getBooleanAttribute(DemoConstant.EM_THREAD_NOTIFICATION_TYPE, false);
        if(isThreadNotify) {
            return CUSTOM_THREAD_NOTIFY;
        }
        return super.getItemNotEmptyViewType(position);
    }

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == CUSTOM_THREAD_NOTIFY) {
            return new EaseThreadNotifyViewHolder(new ChatRowThreadNotify(mContext, false), listener);
        }
        return super.getViewHolder(parent, viewType);
    }
}
