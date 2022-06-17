package io.agora.chatdemo.chat;

import android.os.Bundle;

import androidx.annotation.Nullable;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.menu.MenuItemBean;
import io.agora.chatdemo.R;

public class CustomChatFragment extends EaseChatFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public boolean onMenuItemClick(MenuItemBean item, ChatMessage message) {
        if(item.getItemId() == R.id.action_chat_report){
            if (message.status() == ChatMessage.Status.SUCCESS)
                ChatReportActivity.actionStart(getActivity(),message.getMsgId());
        }
        return super.onMenuItemClick(item, message);

    }
}
