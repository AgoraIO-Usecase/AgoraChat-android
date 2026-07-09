package io.agora.chatdemo.common.helper

import io.agora.chat.Conversation
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageStatus
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.common.ChatType
import io.agora.chat.uikit.provider.getSyncUser

object LocalNotifyHelper {
    /**
     * Create a local message when receive a unsent message.
     */
    fun createContactNotifyMessage(userId:String?): ChatMessage {
        val user = ChatUIKitClient.getUserProvider()?.getSyncUser(userId)
        val msgNotification = ChatMessage.createReceiveMessage(ChatMessageType.TXT)
        val text: String =  DemoHelper.getInstance().context.resources?.
        getString(R.string.demo_contact_added_notify,user?.getNotEmptyName())?:"$userId"
        val txtBody = ChatTextMessageBody(text)
        msgNotification.addBody(txtBody)
        msgNotification.to = ChatUIKitClient.getCurrentUser()?.id
        msgNotification.from = user?.id
        msgNotification.msgTime = System.currentTimeMillis()
        msgNotification.chatType = ChatType.Chat
        msgNotification.setLocalTime(System.currentTimeMillis())
        msgNotification.setAttribute(io.agora.chat.uikit.common.ChatUIKitConstant.MESSAGE_TYPE_CONTACT_NOTIFY, true)
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
                if (msg.ext().containsKey(io.agora.chat.uikit.common.ChatUIKitConstant.MESSAGE_TYPE_CONTACT_NOTIFY)){
                    it.removeMessage(msg.msgId)
                }
            }
        }
    }
}