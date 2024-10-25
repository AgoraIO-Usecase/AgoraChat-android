package io.agora.chatdemo.callkit.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import io.agora.chatdemo.R
import io.agora.uikit.common.ChatTextMessageBody
import io.agora.uikit.widget.chatrow.EaseChatRow

@SuppressLint("ViewConstructor")
class ChatRowVoiceCall @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    isSender: Boolean
) : EaseChatRow(context, attrs, defStyleAttr, isSender) {
    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }
    private val ivCallIcon: ImageView by lazy { findViewById(R.id.iv_call_icon) }
    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.demo_row_received_voice_call else R.layout.demo_row_sent_voice_call,
            this
        )
    }

    override fun onSetUpView() {
        (message?.body as? ChatTextMessageBody)?.let {
            contentView?.text = it.message
        }
        message?.let {
//            val type = it.getIntAttribute(EaseMsgUtils.CALL_TYPE, 0)
//            if (type == EaseCallType.SINGLE_VIDEO_CALL.ordinal) {
//                ivCallIcon.setImageResource(com.hyphenate.easecallkit.R.drawable.d_chat_video_call_self)
//            } else {
//                ivCallIcon.setImageResource(com.hyphenate.easecallkit.R.drawable.d_chat_voice_call)
//            }
        }
    }

}
