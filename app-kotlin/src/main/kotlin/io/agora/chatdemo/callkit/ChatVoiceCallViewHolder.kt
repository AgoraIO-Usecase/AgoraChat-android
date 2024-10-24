package io.agora.chatdemo.callkit

import android.view.View
import io.agora.chat.callkit.EaseCallKit
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chat.callkit.utils.EaseCallMsgUtils
import io.agora.chatdemo.callkit.activity.CallSingleBaseActivity
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.ChatMessageDirection
import io.agora.uikit.feature.chat.viewholders.EaseChatRowViewHolder

class ChatVoiceCallViewHolder(itemView: View): EaseChatRowViewHolder(itemView) {

    override fun onBubbleClick(message: ChatMessage?) {
        super.onBubbleClick(message)
        message?.let {
            if (it.getIntAttribute(EaseCallMsgUtils.CALL_TYPE, 0) == EaseCallType.SINGLE_VOICE_CALL.ordinal) {
                if (it.direct() == ChatMessageDirection.RECEIVE) {
                    // answer call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VOICE_CALL, message.from, null,
                        CallSingleBaseActivity::class.java
                    )
                } else {
                    // make call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VOICE_CALL, message.to, null,
                        CallSingleBaseActivity::class.java
                    )
                }
            } else {
                if (it.direct() == ChatMessageDirection.RECEIVE) {
                    // answer call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VIDEO_CALL, message.from, null,
                        CallSingleBaseActivity::class.java
                    )
                } else {
                    // make call
                    EaseCallKit.getInstance().startSingleCall(
                        EaseCallType.SINGLE_VIDEO_CALL, message.to, null,
                        CallSingleBaseActivity::class.java
                    )
                }
            }
        }
    }
}