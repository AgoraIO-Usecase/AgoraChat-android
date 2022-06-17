package io.agora.chatdemo.chatthread;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chatthread.EaseChatThreadFragment;
import io.agora.chat.uikit.menu.EasePopupWindowHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.utils.ToastUtils;

public class ChatThreadFragment extends EaseChatThreadFragment {
    @Override
    public void onPreMenu(EasePopupWindowHelper helper, ChatMessage message) {
        super.onPreMenu(helper, message);
        boolean isRecall = message.getBooleanAttribute(DemoConstant.MESSAGE_TYPE_RECALL, false);
        if(isRecall) {
            helper.showHeaderView(false);
            helper.findItemVisible(R.id.action_chat_delete, true);
            helper.findItemVisible(R.id.action_chat_unsent, false);
            helper.findItemVisible(R.id.action_chat_recall, false);
            helper.findItemVisible(R.id.action_chat_copy, false);
        }
    }

    @Override
    public void recallSuccess(ChatMessage originalMessage, ChatMessage notification) {
        super.recallSuccess(originalMessage, notification);
        ToastUtils.showToast(R.string.thread_unsent_message_success);
    }

    @Override
    public void recallFail(int code, String errorMsg) {
        super.recallFail(code, errorMsg);
        ToastUtils.showToast(errorMsg);
    }
}
