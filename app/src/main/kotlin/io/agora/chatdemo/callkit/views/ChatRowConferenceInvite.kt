package io.agora.chatdemo.callkit.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import io.agora.chatdemo.R
import io.agora.chat.uikit.common.ChatTextMessageBody
import io.agora.chat.uikit.widget.chatrow.ChatUIKitRow

@SuppressLint("ViewConstructor")
class ChatRowConferenceInvite @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    isSender: Boolean
) : ChatUIKitRow(context, attrs, defStyleAttr, isSender) {
    protected val contentView: TextView? by lazy { findViewById(R.id.tv_chatcontent) }
    override fun onInflateView() {
        inflater.inflate(
            if (!isSender) R.layout.demo_row_received_conference_invite else R.layout.demo_row_sent_conference_invite,
            this
        )
    }

    override fun onSetUpView() {
        (message?.body as? ChatTextMessageBody)?.let {
            var message = it.message
            if (message.isNullOrEmpty().not() && message.contains("-")) {
                message = """
                ${message.substring(0, message.indexOf("-") + 1)}
                ${message.substring(message.indexOf("-") + 1)}
                """.trimIndent()
            }
            contentView?.text = message
        }
    }

}
