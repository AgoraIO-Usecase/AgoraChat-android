package io.agora.chatdemo.general.interfaces;

import io.agora.chat.ChatMessage;

public interface TranslationListener{
    void onTranslationRetry(ChatMessage message, String languageCode);
}