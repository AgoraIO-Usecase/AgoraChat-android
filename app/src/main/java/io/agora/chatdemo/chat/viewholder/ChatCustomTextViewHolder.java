package io.agora.chatdemo.chat.viewholder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;

public class ChatCustomTextViewHolder extends EaseChatRowViewHolder {
    private Context context;
    private MessageListItemClickListener mItemClickListener;

    public ChatCustomTextViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
        this.context = itemView.getContext();
        this.mItemClickListener = itemClickListener;
    }

    @Override
    public void onBubbleClick(ChatMessage message) {
        super.onBubbleClick(message);
    }

}
