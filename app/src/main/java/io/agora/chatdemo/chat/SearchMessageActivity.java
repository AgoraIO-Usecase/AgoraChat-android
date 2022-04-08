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
import io.agora.chatdemo.chat.adapter.SearchMessageAdapter;
import io.agora.chatdemo.general.constant.DemoConstant;

public class SearchMessageActivity extends SearchActivity {

    private String mConversationId;
    private int mChatType;
    private Conversation mConversation;

    public static void actionStart(Context context, String conversationId, int chatType) {
        Intent intent = new Intent(context, SearchMessageActivity.class);
        intent.putExtra(DemoConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(DemoConstant.EXTRA_CHAT_TYPE, chatType);
        context.startActivity(intent);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        mConversationId = getIntent().getStringExtra(DemoConstant.EXTRA_CONVERSATION_ID);
        mChatType = getIntent().getIntExtra(DemoConstant.EXTRA_CHAT_TYPE, DemoConstant.CHATTYPE_SINGLE);
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
        int chatType = EaseConstant.CHATTYPE_SINGLE;
        if (ChatMessage.ChatType.Chat == messageChatType) {
            chatType = EaseConstant.CHATTYPE_SINGLE;
        } else if (ChatMessage.ChatType.GroupChat == messageChatType) {
            chatType = EaseConstant.CHATTYPE_GROUP;
        } else if (ChatMessage.ChatType.ChatRoom == messageChatType) {
            chatType = EaseConstant.CHATTYPE_CHATROOM;
        }
        ChatHistoryActivity.actionStart(this.getApplicationContext(), item.conversationId(), chatType);

    }
}
