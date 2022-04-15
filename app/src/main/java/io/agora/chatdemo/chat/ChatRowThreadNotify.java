package io.agora.chatdemo.chat;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;


import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.widget.chatrow.EaseChatRowText;
import io.agora.chatdemo.R;

/**
 * big emoji icons
 *
 */
public class ChatRowThreadNotify extends EaseChatRowText {
    private TextView tv_thread_notify;

    public ChatRowThreadNotify(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowThreadNotify(Context context, ChatMessage message, int position, BaseAdapter adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(R.layout.layout_row_thread_notify, this);
    }

    @Override
    protected void onFindViewById() {
        tv_thread_notify = (TextView) findViewById(R.id.tv_thread_notify);
    }

    @Override
    public void onSetUpView() {
        if(tv_thread_notify == null) {
            return;
        }
        TextMessageBody textBody = (TextMessageBody) message.getBody();
        String message = textBody.getMessage();
        SpannableStringBuilder builder = new SpannableStringBuilder(message);
        builder.setSpan(new ForegroundColorSpan(this.getResources().getColor(R.color.ease_color_brand)), message.length() - 7, message.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//        builder.setSpan(new ClickableSpan() {
//            @Override
//            public void onClick(@NonNull View widget) {
//                // 跳转到指定页面
//            }
//        }, message.length() - 7, message.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv_thread_notify.setText(builder);
    }
}
