package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;

import io.agora.chat.uikit.constants.EaseConstant;

public class ChatHistoryActivity extends ChatActivity {
    public static void actionStart(Context context, String conversationId, int chatType) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType);
        context.startActivity(intent);
    }
}
