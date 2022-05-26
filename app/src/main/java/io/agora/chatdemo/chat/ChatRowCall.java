package io.agora.chatdemo.chat;

import static io.agora.chat.callkit.base.EaseCallType.SINGLE_VOICE_CALL;
import static io.agora.chat.callkit.utils.EaseCallMsgUtils.CALL_COST_TIME;
import static io.agora.chat.callkit.utils.EaseCallMsgUtils.CALL_INVITE_EXT;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import io.agora.chat.callkit.base.EaseCallType;
import io.agora.chat.callkit.utils.EaseCallAction;
import io.agora.chat.callkit.utils.EaseCallMsgUtils;
import io.agora.chat.uikit.widget.chatrow.EaseChatRow;
import io.agora.chatdemo.R;
import io.agora.exceptions.ChatException;


public class ChatRowCall extends EaseChatRow {

    private final SimpleDateFormat dateFormat;
    private TextView title;
    private TextView subtitle;
    private ImageView ivCall;


    public ChatRowCall(Context context, boolean isSender) {
        super(context, isSender);
        dateFormat = new SimpleDateFormat("HH:mm MMM dd",java.util.Locale.US);
    }

    @Override
    protected void onInflateView() {
        inflater.inflate(!showSenderType ? R.layout.call_row_recieved
                : R.layout.call_row_send, this);
    }

    @Override
    protected void onFindViewById() {
        title = findViewById(R.id.tv_title);
        subtitle = findViewById(R.id.tv_subtitle);
        ivCall = findViewById(R.id.iv_call);
    }

    @Override
    protected void onSetUpView() {
        String action = message.getStringAttribute(EaseCallMsgUtils.CALL_ACTION, "");
        String callerDevId = message.getStringAttribute(EaseCallMsgUtils.CALL_DEVICE_ID, "");
        String fromCallId = message.getStringAttribute(EaseCallMsgUtils.CLL_ID, "");
        long time = message.getLongAttribute(EaseCallMsgUtils.CLL_TIMESTRAMEP, 0);
        String fromUser = message.getFrom();
        String channel = message.getStringAttribute(EaseCallMsgUtils.CALL_CHANNELNAME, "");
        JSONObject ext = null;
        try {
            ext = message.getJSONObjectAttribute(CALL_INVITE_EXT);
        } catch (ChatException exception) {
            exception.printStackTrace();
        }
        int calltype = message.getIntAttribute(EaseCallMsgUtils.CALL_TYPE, SINGLE_VOICE_CALL.code);
        EaseCallType callkitType = EaseCallType.getfrom(calltype);
        EaseCallAction callAction = EaseCallAction.getfrom(action);
        if (callAction == EaseCallAction.CALL_INVITE) {
            switch (callkitType) {
                case SINGLE_VOICE_CALL:
                    title.setText(context.getText(R.string.ease_call_single_audio_call));
                    ivCall.setImageResource(R.drawable.call_voice_green);
                    break;
                case CONFERENCE_VOICE_CALL:
                    title.setText(context.getText(R.string.ease_call_group_audio_call));
                    ivCall.setImageResource(R.drawable.call_voice_green);
                    break;
                case SINGLE_VIDEO_CALL:
                    title.setText(context.getText(R.string.ease_call_single_video_call));
                    ivCall.setImageResource(R.drawable.call_video_green);
                    break;
                case CONFERENCE_VIDEO_CALL:
                    title.setText(context.getText(R.string.ease_call_group_video_call));
                    ivCall.setImageResource(R.drawable.call_video_green);
                    break;
            }
            String str = dateFormat.format(new Date(time));
            subtitle.setText(str);
        }else if(callAction==EaseCallAction.CALL_CANCEL) {
            switch (callkitType) {
                case SINGLE_VOICE_CALL:
                    title.setText(context.getText(R.string.ease_call_audio_call_ended));
                    ivCall.setImageResource(R.drawable.call_voice_gray);
                    break;
                case CONFERENCE_VOICE_CALL:
                    title.setText(context.getText(R.string.ease_call_audio_call_ended));
                    ivCall.setImageResource(R.drawable.call_voice_gray);
                    break;
                case SINGLE_VIDEO_CALL:
                    title.setText(context.getText(R.string.ease_call_video_call_ended));
                    ivCall.setImageResource(R.drawable.call_video_gray);
                    break;
                case CONFERENCE_VIDEO_CALL:
                    title.setText(context.getText(R.string.ease_call_video_call_ended));
                    ivCall.setImageResource(R.drawable.call_video_gray);
                    break;
            }
            subtitle.setText(message.getStringAttribute(CALL_COST_TIME,"00:00"));
        }

    }
}
