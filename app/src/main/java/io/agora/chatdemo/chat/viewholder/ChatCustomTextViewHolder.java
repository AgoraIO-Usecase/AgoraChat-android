package io.agora.chatdemo.chat.viewholder;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;

public class ChatCustomTextViewHolder extends EaseChatRowViewHolder {

    public ChatCustomTextViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public void onBubbleClick(ChatMessage message) {
        super.onBubbleClick(message);
    }

}
