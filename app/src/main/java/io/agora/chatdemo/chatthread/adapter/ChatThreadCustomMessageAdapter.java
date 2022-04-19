package io.agora.chatdemo.chatthread.adapter;

import android.view.ViewGroup;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chatdemo.chat.ChatRowThreadNotify;
import io.agora.chatdemo.chat.EaseThreadNotifyViewHolder;
import io.agora.chatdemo.general.constant.DemoConstant;

public class ChatThreadCustomMessageAdapter extends EaseMessageAdapter {
    private static final int CUSTOM_THREAD_UNSENT_ME = 1001;
    private static final int CUSTOM_THREAD_UNSENT_OTHER = 1002;

    @Override
    public int getItemNotEmptyViewType(int position) {
        ChatMessage message = getData().get(position);
        boolean isThreadUnsent = message.getBooleanAttribute(DemoConstant.MESSAGE_TYPE_RECALL, false);
        ChatMessage.Direct direct = message.direct();
        if(isThreadUnsent) {
            if(direct == ChatMessage.Direct.SEND) {
                return CUSTOM_THREAD_UNSENT_ME;
            }else {
                return CUSTOM_THREAD_UNSENT_OTHER;
            }
        }
        return super.getItemNotEmptyViewType(position);
    }

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == CUSTOM_THREAD_UNSENT_ME || viewType == CUSTOM_THREAD_UNSENT_OTHER) {
            return new ChatThreadUnsentViewHolder(new ChatRowThreadUnsent(mContext, viewType == CUSTOM_THREAD_UNSENT_ME), listener);
        }
        return super.getViewHolder(parent, viewType);
    }
}
