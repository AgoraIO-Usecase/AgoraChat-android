package io.agora.chatdemo.page.contact

import io.agora.chatdemo.common.helper.LocalNotifyHelper
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.common.extensions.mainScope
import io.agora.uikit.feature.invitation.EaseNewRequestsActivity
import io.agora.uikit.model.EaseEvent

class ChatNewRequestsActivity:EaseNewRequestsActivity() {

    override fun agreeInviteSuccess(userId: String, msg: ChatMessage) {
        super.agreeInviteSuccess(userId, msg)
        val notifyMsg = LocalNotifyHelper.createContactNotifyMessage(userId)
        ChatClient.getInstance().chatManager().saveMessage(notifyMsg)
        mContext.let {
            EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.ADD.name)
                .post(it.mainScope(), EaseEvent(EaseEvent.EVENT.ADD.name, EaseEvent.TYPE.CONTACT))
        }
    }

}