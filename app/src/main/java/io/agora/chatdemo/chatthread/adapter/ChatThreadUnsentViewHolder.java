package io.agora.chatdemo.chatthread.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.activities.EaseChatThreadListActivity;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;


public class ChatThreadUnsentViewHolder extends EaseChatRowViewHolder {

    public ChatThreadUnsentViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

}
