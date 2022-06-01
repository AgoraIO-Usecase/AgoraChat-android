package io.agora.chatdemo.chatthread.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.BaseAdapter;
import android.widget.TextView;

import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.widget.chatrow.EaseChatRowText;
import io.agora.chatdemo.R;

/**
 * big emoji icons
 *
 */
public class ChatRowThreadUnsent extends EaseChatRowText {
    private TextView tv_chatcontent;

    public ChatRowThreadUnsent(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowThreadUnsent(Context context, ChatMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(!showSenderType ? R.layout.chat_row_received_unsent
                : R.layout.chat_row_sent_unsent, this);
    }

    @Override
    protected void onFindViewById() {
        tv_chatcontent = (TextView) findViewById(R.id.tv_chatcontent);
    }

    @Override
    public void onSetUpView() {
        if(tv_chatcontent == null) {
            return;
        }
        TextMessageBody textBody = (TextMessageBody) message.getBody();
        String message = textBody.getMessage();
        tv_chatcontent.setText(message);
    }
}
