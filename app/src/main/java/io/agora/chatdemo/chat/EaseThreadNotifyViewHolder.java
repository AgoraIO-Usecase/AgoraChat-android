package io.agora.chatdemo.chat;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.activities.EaseThreadListActivity;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;


public class EaseThreadNotifyViewHolder extends EaseChatRowViewHolder {

    public EaseThreadNotifyViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    @Override
    public void onBubbleClick(ChatMessage message) {
        // 跳转到Thread列表页面
        EaseThreadListActivity.actionStart(getContext(), message.conversationId());
    }

}
