package io.agora.chatdemo.chat;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;

public class UrlPreViewHolder extends EaseChatRowViewHolder {

    public UrlPreViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
    }

    @Override
    public void onBubbleClick(ChatMessage message) {
        super.onBubbleClick(message);
    }

    @Override
    public void refreshView() {
        getAdapter().notifyDataSetChanged();
    }
}
