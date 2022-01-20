package io.agora.chatdemo.conversation;

import io.agora.chat.uikit.conversation.interfaces.OnConversationChangeListener;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;

/**
 * Created by 许成谱 on 2022/1/20 15:21.
 * qq:1550540124
 * 热爱生活每一天！
 */
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
