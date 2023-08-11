package io.agora.chatdemo.chat;

import java.util.ArrayList;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.Group;
import io.agora.chat.uikit.chat.presenter.EaseHandleMessagePresenterImpl;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.manager.EaseAtMessageHelper;
import io.agora.util.EMLog;

public class ChatCustomPresenter extends EaseHandleMessagePresenterImpl {
    private static final String TAG = ChatCustomPresenter.class.getSimpleName();
    private ArrayList<ChatMessage> messages = new ArrayList<>();

    @Override
    public void sendAtMessage(String content) {
        messages.clear();
        if(!isGroupChat()){
            EMLog.e(TAG, "only support group chat message");
            if(isActive()) {
                runOnUI(()-> mView.sendMessageFail("only support group chat message"));
            }
            return;
        }
        ChatMessage message = ChatMessage.createTxtSendMessage(content, toChatUsername);
        if (message != null){
            message.setChatType(ChatMessage.ChatType.GroupChat);
            Group group = ChatClient.getInstance().groupManager().getGroup(toChatUsername);
            if(ChatClient.getInstance().getCurrentUser().equals(group.getOwner()) && EaseAtMessageHelper.get().containsAtAll(content)){
                message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG, EaseConstant.MESSAGE_ATTR_VALUE_AT_MSG_ALL);
            }else {
                message.setAttribute(EaseConstant.MESSAGE_ATTR_AT_MSG,
                        EaseAtMessageHelper.get().atListToJsonArray(EaseAtMessageHelper.get().getAtMessageUsernames(content)));
            }
        }
        messages.add(message);
        EaseAtMessageHelper.get().parseMessages(messages);
        sendMessage(message);
    }
}
