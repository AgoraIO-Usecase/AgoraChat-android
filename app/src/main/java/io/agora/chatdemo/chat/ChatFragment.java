package io.agora.chatdemo.chat;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.EaseChatFragment;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;

public class ChatFragment extends EaseChatFragment {
    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);
        boolean isThreadNotify = message.getBooleanAttribute(DemoConstant.EM_THREAD_NOTIFICATION_TYPE, false);
        if(isThreadNotify) {
            helper.findItemVisible(R.id.action_chat_copy, false);
            helper.findItemVisible(R.id.action_chat_reply, false);
            helper.findItemVisible(R.id.action_chat_unsent, false);
            helper.findItemVisible(R.id.action_chat_delete, true);
        }
    }
}
