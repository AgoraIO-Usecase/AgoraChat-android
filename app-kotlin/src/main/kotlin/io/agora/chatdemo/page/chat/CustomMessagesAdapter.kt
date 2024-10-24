package io.agora.chatdemo.page.chat

import android.text.TextUtils
import android.view.ViewGroup
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chat.callkit.utils.EaseCallMsgUtils
import io.agora.chatdemo.callkit.MultipleInviteViewHolder
import io.agora.chatdemo.callkit.ChatVoiceCallViewHolder
import io.agora.chatdemo.callkit.views.ChatRowConferenceInvite
import io.agora.chatdemo.callkit.views.ChatRowVoiceCall
import io.agora.uikit.common.ChatMessage
import io.agora.uikit.common.ChatMessageDirection
import io.agora.uikit.feature.chat.adapter.EaseMessagesAdapter

class CustomMessagesAdapter: EaseMessagesAdapter() {

    companion object {
        const val VIEW_TYPE_MESSAGE_CALL_SEND = 1000
        const val VIEW_TYPE_MESSAGE_CALL_RECEIVE = 1001
        const val VIEW_TYPE_MESSAGE_INVITE_SEND = 1002
        const val VIEW_TYPE_MESSAGE_INVITE_RECEIVE = 1003
    }
    //继承EaseMessagesAdapter 重写getItemNotEmptyViewType 添加自定义ViewType
    //下方示例 增加自定义 call 消息提醒类型
    override fun getItemNotEmptyViewType(position: Int): Int {
        getItem(position)?.let {
            val msgType = it.getStringAttribute(EaseCallMsgUtils.CALL_MSG_TYPE,"")
            val callType = it.getIntAttribute(EaseCallMsgUtils.CALL_TYPE, 0)
            if (TextUtils.equals(msgType, EaseCallMsgUtils.CALL_MSG_INFO)) {
                if (callType == EaseCallType.CONFERENCE_VIDEO_CALL.ordinal || callType == EaseCallType.CONFERENCE_VOICE_CALL.ordinal) {
                    return if (it.direct() == ChatMessageDirection.SEND) VIEW_TYPE_MESSAGE_INVITE_SEND else VIEW_TYPE_MESSAGE_INVITE_RECEIVE
                }
                return if (it.direct() == ChatMessageDirection.SEND) VIEW_TYPE_MESSAGE_CALL_SEND else VIEW_TYPE_MESSAGE_CALL_RECEIVE
            }
        }
        return super.getItemNotEmptyViewType(position)
    }

    // 继承EaseMessagesAdapter getViewHolder 添加自定义ViewHolder 和 ui布局
    // 下方示例 增加单聊、群聊 call 消息提醒类型布局和事件处理
    override fun getViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<ChatMessage> {
        when (viewType) {
            VIEW_TYPE_MESSAGE_CALL_SEND, VIEW_TYPE_MESSAGE_CALL_RECEIVE -> {
                return ChatVoiceCallViewHolder(ChatRowVoiceCall(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_CALL_SEND))
            }
            VIEW_TYPE_MESSAGE_INVITE_SEND, VIEW_TYPE_MESSAGE_INVITE_RECEIVE -> {
                return MultipleInviteViewHolder(ChatRowConferenceInvite(parent.context, isSender = viewType == VIEW_TYPE_MESSAGE_INVITE_SEND))
            }
        }
        return super.getViewHolder(parent, viewType)
    }
}