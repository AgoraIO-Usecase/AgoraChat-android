package io.agora.chatdemo.common.helper

import android.text.TextUtils
import io.agora.chat.callkit.utils.EaseCallMsgUtils
import io.agora.chat.uikit.common.ChatMessage
import io.agora.chat.uikit.common.ChatMessageType
import io.agora.chat.uikit.menu.chat.ChatUIKitChatMenuHelper

object MenuFilterHelper {
    fun filterMenu(helper: ChatUIKitChatMenuHelper?, message: ChatMessage?){
        message?.let {
            when(it.type){
                ChatMessageType.TXT ->{
                    if (it.ext().containsKey(EaseCallMsgUtils.CALL_MSG_TYPE)){
                        val msgType = it.getStringAttribute(EaseCallMsgUtils.CALL_MSG_TYPE,"")
                        if (TextUtils.equals(msgType, EaseCallMsgUtils.CALL_MSG_INFO)) {
                            helper?.setAllItemsVisible(false)
                            helper?.clearTopView()
                            helper?.findItemVisible(io.agora.chat.uikit.R.id.action_chat_delete,true)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}