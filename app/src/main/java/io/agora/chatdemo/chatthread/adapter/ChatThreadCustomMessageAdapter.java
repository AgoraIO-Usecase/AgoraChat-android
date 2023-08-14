package io.agora.chatdemo.chatthread.adapter;

import static io.agora.chatdemo.general.constant.DemoConstant.CUSTOM_THREAD_UNSENT_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.CUSTOM_THREAD_UNSENT_OTHER;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME;
import static io.agora.chatdemo.general.constant.DemoConstant.VIEW_TYPE_MESSAGE_CUSTOM_TEXT_OTHER;

import android.view.ViewGroup;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.adapter.EaseMessageAdapter;
import io.agora.chatdemo.chat.chatrow.ChatRowCustomTextView;
import io.agora.chatdemo.chat.viewholder.ChatCustomTextViewHolder;
import io.agora.chatdemo.general.constant.DemoConstant;

public class ChatThreadCustomMessageAdapter extends EaseMessageAdapter {

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
        // Replace of default TextMessage view.
        if (message.getType() == ChatMessage.Type.TXT){
            if (message.direct() == ChatMessage.Direct.SEND) {
                return VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME;
            } else {
                return VIEW_TYPE_MESSAGE_CUSTOM_TEXT_OTHER;
            }
        }
        return super.getItemNotEmptyViewType(position);
    }

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        if(viewType == CUSTOM_THREAD_UNSENT_ME || viewType == CUSTOM_THREAD_UNSENT_OTHER) {
            return new ChatThreadUnsentViewHolder(new ChatRowThreadUnsent(mContext, viewType == CUSTOM_THREAD_UNSENT_ME), listener);
        }else if (viewType == VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME || viewType == VIEW_TYPE_MESSAGE_CUSTOM_TEXT_OTHER){
            return new ChatCustomTextViewHolder(new ChatRowCustomTextView(mContext,viewType == VIEW_TYPE_MESSAGE_CUSTOM_TEXT_ME),listener);
        }
        return super.getViewHolder(parent, viewType);
    }
}
