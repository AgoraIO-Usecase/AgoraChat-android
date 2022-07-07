package io.agora.chatdemo.chat;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MenuItemBean menuItemBean = new MenuItemBean(0, R.id.action_chat_report,99, getResources().getString(io.agora.chat.uikit.R.string.ease_action_report));
        menuItemBean.setResourceId(R.drawable.chat_item_menu_report);
        chatLayout.getMenuHelper().addItemMenu(menuItemBean);

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
