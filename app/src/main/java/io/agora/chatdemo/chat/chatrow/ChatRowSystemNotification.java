package io.agora.chatdemo.chat.chatrow;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;

import io.agora.chat.ChatMessage;
import io.agora.chat.TextMessageBody;
import io.agora.chat.uikit.widget.chatrow.EaseChatRowText;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.exceptions.ChatException;

public class ChatRowSystemNotification extends EaseChatRowText {

    private TextView tv_chatcontent;

    public ChatRowSystemNotification(Context context, boolean isSender) {
        super(context, isSender);
    }

    public ChatRowSystemNotification(Context context, ChatMessage message, int position, Object adapter) {
        super(context, message, position, adapter);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate( R.layout.activity_system_notification, this);
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
        String text = textBody.getMessage();
        tv_chatcontent.setText(text);
        if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_CREATE_GROUP)){
            tv_chatcontent.setText(getStrAfter(0,3));
        }else if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_ADD_CONTACT)){
            tv_chatcontent.setText(getStrAfter(0,3));
        }else if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_INVITATION_CONTACT)){
            tv_chatcontent.setText(getStrAfter(0,3));
        }else if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_JOINED_GROUP)){
            tv_chatcontent.setText(getStrAfter(0,3));
        }else if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_GROUP_INVITE_ACCEPT)){
            tv_chatcontent.setText(getStrAfter(0,message.getFrom().length()));
        }else if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_CHANGE_OWNER)){
            if (message.ext().containsKey(DemoConstant.ID_OR_NICKNAME)){
                try {
                    tv_chatcontent.setText(getStrAfter(0,message.getStringAttribute(DemoConstant.ID_OR_NICKNAME).length()));
                } catch (ChatException e) {
                    e.printStackTrace();
                }
            }
        }else if (TextUtils.equals(message.getStringAttribute(DemoConstant.SYSTEM_NOTIFICATION_TYPE,""),DemoConstant.SYSTEM_CHANGE_GROUP_NAME)){
            if (message.ext().containsKey(DemoConstant.SYSTEM_CHANGE_GROUP_NAME)){
                try {
                    tv_chatcontent.setText(getStrAfter(tv_chatcontent.getText().length()-message.getStringAttribute(DemoConstant.SYSTEM_CHANGE_GROUP_NAME).length(),tv_chatcontent.getText().length()));
                } catch (ChatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public SpannableStringBuilder getStrAfter(int start,int end){
        SpannableStringBuilder builder = new SpannableStringBuilder(tv_chatcontent.getText().toString());
        ForegroundColorSpan graySpan = new ForegroundColorSpan(getResources().getColor(io.agora.chat.uikit.R.color.blue));
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        builder.setSpan(styleSpan,start,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return builder;
    }

}
