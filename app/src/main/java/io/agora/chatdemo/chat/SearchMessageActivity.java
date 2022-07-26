package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Conversation;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.menu.EaseChatType;
import io.agora.chatdemo.chat.adapter.SearchMessageAdapter;
import io.agora.chatdemo.general.constant.DemoConstant;

public class SearchMessageActivity extends SearchActivity {

    private String mConversationId;
    private EaseChatType mChatType;
    private Conversation mConversation;

    public static void actionStart(Context context, String conversationId, EaseChatType chatType) {
        Intent intent = new Intent(context, SearchMessageActivity.class);
        intent.putExtra(DemoConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(DemoConstant.EXTRA_CHAT_TYPE, chatType.getChatType());
        context.startActivity(intent);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        mConversationId = getIntent().getStringExtra(DemoConstant.EXTRA_CONVERSATION_ID);
        mChatType = EaseChatType.from(getIntent().getIntExtra(DemoConstant.EXTRA_CHAT_TYPE, EaseChatType.SINGLE_CHAT.getChatType()));
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initData() {
        super.initData();
        mConversation = ChatClient.getInstance().chatManager().getConversation(mConversationId);
    }

    @Override
    protected EaseBaseRecyclerViewAdapter getAdapter() {
        return new SearchMessageAdapter();
    }

    @Override
    public void search(String search) {
        List<ChatMessage> data = mConversation.searchMsgFromDB(search, System.currentTimeMillis(), 100, null, Conversation.SearchDirection.UP);
        ((SearchMessageAdapter) mAdapter).setKeyword(search);
        mAdapter.setData(data);
    }

    @Override
    protected void onChildItemClick(View view, int position) {
        ChatMessage item = ((SearchMessageAdapter) mAdapter).getItem(position);

        ChatMessage.ChatType messageChatType = item.getChatType();
        EaseChatType chatType = EaseChatType.SINGLE_CHAT;
        if (ChatMessage.ChatType.Chat == messageChatType) {
            chatType = EaseChatType.SINGLE_CHAT;
        } else if (ChatMessage.ChatType.GroupChat == messageChatType) {
            chatType = EaseChatType.GROUP_CHAT;
        } else if (ChatMessage.ChatType.ChatRoom == messageChatType) {
            chatType = EaseChatType.CHATROOM;
        }
        ChatHistoryActivity.actionStart(mContext, item.conversationId(), chatType);

    }
}
