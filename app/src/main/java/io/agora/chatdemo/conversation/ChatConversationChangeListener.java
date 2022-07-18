package io.agora.chatdemo.conversation;

import io.agora.chat.uikit.conversation.interfaces.OnConversationChangeListener;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;

public class ChatConversationChangeListener implements OnConversationChangeListener {
    @Override
    public void notifyItemChange(int position) {

    }

    @Override
    public void notifyAllChange() {

    }

    @Override
    public void notifyItemRemove(int position) {
        LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE).postValue(new EaseEvent(DemoConstant.CONVERSATION_DELETE, EaseEvent.TYPE.MESSAGE));
    }
}
