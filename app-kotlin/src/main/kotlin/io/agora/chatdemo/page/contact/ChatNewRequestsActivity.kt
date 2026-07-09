package io.agora.chatdemo.page.contact

import io.agora.chatdemo.common.helper.LocalNotifyHelper
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.feature.invitation.ChatUIKitNewRequestsActivity
import io.agora.chat.uikit.model.ChatUIKitEvent

class ChatNewRequestsActivity:ChatUIKitNewRequestsActivity() {

    override fun agreeInviteSuccess(userId: String, msg: ChatMessage) {
        super.agreeInviteSuccess(userId, msg)
        val notifyMsg = LocalNotifyHelper.createContactNotifyMessage(userId)
        ChatClient.getInstance().chatManager().saveMessage(notifyMsg)
        mContext.let {
            ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD.name)
                .post(it.mainScope(), ChatUIKitEvent(ChatUIKitEvent.EVENT.ADD.name, ChatUIKitEvent.TYPE.CONTACT))
        }
    }

}