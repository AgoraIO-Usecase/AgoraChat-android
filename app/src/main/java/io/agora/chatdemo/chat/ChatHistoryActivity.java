package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;

import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.menu.EaseChatType;

public class ChatHistoryActivity extends ChatActivity {
    private String historyMsgId;

    public static void actionStart(Context context, String conversationId, EaseChatType chatType, String historyMsgId) {
        Intent intent = new Intent(context, ChatHistoryActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType.getChatType());
        intent.putExtra(EaseConstant.HISTORY_MSG_ID, historyMsgId);
        context.startActivity(intent);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        historyMsgId = intent.getStringExtra(EaseConstant.HISTORY_MSG_ID);
    }

    @Override
    public EaseChatFragment.Builder setFragmentBuilder(EaseChatFragment.Builder builder) {
        builder.setHistoryMessageId(historyMsgId);
        return builder;
    }
}
