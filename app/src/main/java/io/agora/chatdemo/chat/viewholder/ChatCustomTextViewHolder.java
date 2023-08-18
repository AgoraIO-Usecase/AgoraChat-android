package io.agora.chatdemo.chat.viewholder;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;
import io.agora.chatdemo.chat.adapter.CustomMessageAdapter.TranslationListener;
import io.agora.chatdemo.chat.chatrow.ChatRowCustomTextView;

public class ChatCustomTextViewHolder extends EaseChatRowViewHolder implements ChatRowCustomTextView.CustomTranslationListener {
    private Context context;
    private MessageListItemClickListener mItemClickListener;
    private TranslationListener translationlistener;

    public ChatCustomTextViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
        super(itemView, itemClickListener);
        this.context = itemView.getContext();
        this.mItemClickListener = itemClickListener;
    }

    public ChatCustomTextViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener, TranslationListener listener) {
        super(itemView, itemClickListener);
        this.context = itemView.getContext();
        this.mItemClickListener = itemClickListener;
        this.translationlistener = listener;
        ((ChatRowCustomTextView)itemView).setTranslationListener(this);
    }

    @Override
    public void onBubbleClick(ChatMessage message) {
        super.onBubbleClick(message);
    }

    @Override
    public void onTranslationRetry(ChatMessage message, String languageCode) {
        translationlistener.onTranslationRetry(message,languageCode);
    }
}
