package io.agora.chatdemo.chat.chatrow;

import android.content.Context;
import android.view.View;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.widget.chatrow.EaseChatRowText;
import io.agora.chatdemo.R;

public class ChatRowTextTranslation extends EaseChatRowText {
    private translationClickListener listener;

    public ChatRowTextTranslation(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowTextTranslation(Context context, ChatMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(!showSenderType ? R.layout.layout_chat_translation_recieved
                : R.layout.layout_chat_translation_send, this);
    }

    @Override
    protected void onFindViewById() {

    }

    @Override
    public void onSetUpView() {

        //todo 添加切换翻译和原文的按钮
    }

    public void setTranslationOnClickListener(translationClickListener listener){
        this.listener = listener;
    }

    public interface translationClickListener{
        void onTranslationClick(View view, ChatMessage message);
        void onTranslationLongClick(View view,ChatMessage message);
    }

}
