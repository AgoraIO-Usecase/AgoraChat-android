package io.agora.chatdemo.chatthread;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chatthread.EaseChatThreadFragment;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.utils.ToastUtils;

public class ChatThreadFragment extends EaseChatThreadFragment {
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
