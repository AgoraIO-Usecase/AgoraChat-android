package io.agora.chatdemo.chat;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.CustomMessageBody;
import io.agora.chat.LocationMessageBody;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
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

    }

    @Override
    public void initView() {
        super.initView();
        MenuItemBean menuItemBean = new MenuItemBean(0, R.id.action_chat_report,99, getResources().getString(io.agora.chat.uikit.R.string.ease_action_report));
        menuItemBean.setResourceId(R.drawable.chat_item_menu_report);
        chatLayout.getMenuHelper().addItemMenu(menuItemBean);
    }

    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);
        if (TextUtils.equals(message.getFrom(), ChatClient.getInstance().getCurrentUser()) ||
                message.getBody() instanceof LocationMessageBody || message.getBody() instanceof CustomMessageBody){
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_report,false);
        }else {
            chatLayout.getMenuHelper().findItemVisible(R.id.action_chat_report,true);
        }
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
