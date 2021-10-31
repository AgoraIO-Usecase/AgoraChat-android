package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.chat.interfaces.OnChatExtendMenuItemClickListener;
import io.agora.chat.uikit.chat.interfaces.OnChatInputChangeListener;
import io.agora.chat.uikit.chat.interfaces.OnOtherTypingListener;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.util.EMLog;

public class ChatActivity extends BaseInitActivity {
    private String conversationId;
    private int chatType;
    private String historyMsgId;

    public static void actionStart(Context context, String conversationId, int chatType) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(EaseConstant.EXTRA_CONVERSATION_ID, conversationId);
        intent.putExtra(EaseConstant.EXTRA_CHAT_TYPE, chatType);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        conversationId = intent.getStringExtra(EaseConstant.EXTRA_CONVERSATION_ID);
        chatType = intent.getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
        historyMsgId = intent.getStringExtra(DemoConstant.HISTORY_MSG_ID);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        initChatFragment();
    }

    private void initChatFragment() {
        EaseChatFragment fragment = new EaseChatFragment.Builder(conversationId, chatType, historyMsgId)
                .setUseHeader(true)
                .setHeaderTitle(conversationId)
                .setHeaderEnableBack(true)
                .setUseRoamMessage(DemoHelper.getInstance().getModel().isMsgRoaming())
                .setOnChatExtendMenuItemClickListener(new OnChatExtendMenuItemClickListener() {
                    @Override
                    public boolean onChatExtendMenuItemClick(View view, int itemId) {
                        EMLog.e("TAG", "onChatExtendMenuItemClick");
                        if(itemId == R.id.extend_item_take_picture) {
                            return true;
                        }
                        return false;
                    }
                })
                .turnOnTypingMonitor(true)
                .setOnOtherTypingListener(new OnOtherTypingListener() {
                    @Override
                    public void onOtherTyping(String action) {

                    }
                })
                .setOnChatInputChangeListener(new OnChatInputChangeListener() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        EMLog.e("TAG", "onTextChanged: s: "+s.toString());
                    }
                })
                .hideChatSendAvatar(true)
                .build();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment, fragment, "chat").commit();
    }
}
