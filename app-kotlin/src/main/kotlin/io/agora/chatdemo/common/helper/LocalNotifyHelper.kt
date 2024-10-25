package io.agora.chatdemo.common.helper

import io.agora.chat.Conversation
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.ChatMessageStatus
import io.agora.uikit.common.ChatMessageType
import io.agora.uikit.common.ChatTextMessageBody
import io.agora.uikit.common.ChatType
import io.agora.uikit.provider.getSyncUser

object LocalNotifyHelper {
    /**
     * Create a local message when receive a unsent message.
     */
    fun createContactNotifyMessage(userId:String?): ChatMessage {
        val user = EaseIM.getUserProvider()?.getSyncUser(userId)
        val msgNotification = ChatMessage.createReceiveMessage(ChatMessageType.TXT)
        val text: String =  DemoHelper.getInstance().context.resources?.
        getString(R.string.demo_contact_added_notify,user?.getNotEmptyName())?:"$userId"
        val txtBody = ChatTextMessageBody(text)
        msgNotification.addBody(txtBody)
        msgNotification.to = EaseIM.getCurrentUser()?.id
        msgNotification.from = user?.id
        msgNotification.msgTime = System.currentTimeMillis()
        msgNotification.chatType = ChatType.Chat
        msgNotification.setLocalTime(System.currentTimeMillis())
        msgNotification.setAttribute(io.agora.uikit.common.EaseConstant.MESSAGE_TYPE_CONTACT_NOTIFY, true)
        msgNotification.setStatus(ChatMessageStatus.SUCCESS)
        msgNotification.setIsChatThreadMessage(false)
        return msgNotification
    }

    /**
     * Remove a local message when receive contact notify message.
     */
    fun removeContactNotifyMessage(userId:String?){
        val conversation = ChatClient.getInstance().chatManager().getConversation(userId,Conversation.ConversationType.Chat)
        conversation?.let {
            it.allMessages.map { msg->
                if (msg.ext().containsKey(io.agora.uikit.common.EaseConstant.MESSAGE_TYPE_CONTACT_NOTIFY)){
                    it.removeMessage(msg.msgId)
                }
            }
        }
    }
}