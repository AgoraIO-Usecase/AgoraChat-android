package io.agora.chatdemo.chat;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.activities.EaseChatThreadListActivity;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;
import io.agora.chatdemo.chatthread.ChatThreadActivity;
import io.agora.chatdemo.general.constant.DemoConstant;


public class EaseThreadNotifyViewHolder extends EaseChatRowViewHolder {

    public EaseThreadNotifyViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    @Override
    public void onBubbleClick(ChatMessage message) {
        // Skip to Chat thread activity
        String parentMsgId = message.getStringAttribute(DemoConstant.EM_THREAD_PARENT_MSG_ID, "");
        ChatThreadActivity.actionStart(getContext(), parentMsgId, message.getMsgId());
    }

}
