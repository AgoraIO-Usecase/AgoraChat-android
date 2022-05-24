package io.agora.chatdemo.chat;

import static io.agora.easecallkit.base.EaseCallType.SINGLE_VOICE_CALL;
import static io.agora.easecallkit.utils.EaseMsgUtils.CALL_COST_TIME;
import static io.agora.easecallkit.utils.EaseMsgUtils.CALL_INVITE_EXT;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import io.agora.chat.uikit.widget.chatrow.EaseChatRow;
import io.agora.chatdemo.R;
import io.agora.easecallkit.base.EaseCallType;
import io.agora.easecallkit.utils.EaseCallAction;
import io.agora.easecallkit.utils.EaseMsgUtils;
import io.agora.exceptions.ChatException;


public class ChatRowCall extends EaseChatRow {

    private TextView title;
    private TextView subtitle;
    private ImageView ivCall;


    public ChatRowCall(Context context, boolean isSender) {
        super(context, isSender);
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
        String action = message.getStringAttribute(EaseMsgUtils.CALL_ACTION, "");
        String callerDevId = message.getStringAttribute(EaseMsgUtils.CALL_DEVICE_ID, "");
        String fromCallId = message.getStringAttribute(EaseMsgUtils.CLL_ID, "");
        String fromUser = message.getFrom();
        String channel = message.getStringAttribute(EaseMsgUtils.CALL_CHANNELNAME, "");
        JSONObject ext = null;
        try {
            ext = message.getJSONObjectAttribute(CALL_INVITE_EXT);
        } catch (ChatException exception) {
            exception.printStackTrace();
        }
        int calltype = message.getIntAttribute(EaseMsgUtils.CALL_TYPE, SINGLE_VOICE_CALL.code);
        EaseCallType callkitType = EaseCallType.getfrom(calltype);
        EaseCallAction callAction = EaseCallAction.getfrom(action);
        if (callAction == EaseCallAction.CALL_INVITE) {
            switch (callkitType) {
                case SINGLE_VOICE_CALL:
                    title.setText(context.getText(R.string.single_audio_call));
                    ivCall.setImageResource(R.drawable.call_voice_green);
                    break;
                case CONFERENCE_VOICE_CALL:
                    title.setText(context.getText(R.string.group_audio_call));
                    ivCall.setImageResource(R.drawable.call_voice_green);
                    break;
                case SINGLE_VIDEO_CALL:
                    title.setText(context.getText(R.string.single_video_call));
                    ivCall.setImageResource(R.drawable.call_video_green);
                    break;
                case CONFERENCE_VIDEO_CALL:
                    title.setText(context.getText(R.string.group_video_call));
                    ivCall.setImageResource(R.drawable.call_video_green);
                    break;
            }
            subtitle.setText(context.getText(R.string.touch_to_join));
        }else if(callAction==EaseCallAction.CALL_CANCEL) {
            switch (callkitType) {
                case SINGLE_VOICE_CALL:
                    title.setText(context.getText(R.string.audio_call_ended));
                    ivCall.setImageResource(R.drawable.call_voice_gray);
                    break;
                case CONFERENCE_VOICE_CALL:
                    title.setText(context.getText(R.string.audio_call_ended));
                    ivCall.setImageResource(R.drawable.call_voice_gray);
                    break;
                case SINGLE_VIDEO_CALL:
                    title.setText(context.getText(R.string.video_call_ended));
                    ivCall.setImageResource(R.drawable.call_video_gray);
                    break;
                case CONFERENCE_VIDEO_CALL:
                    title.setText(context.getText(R.string.video_call_ended));
                    ivCall.setImageResource(R.drawable.call_video_gray);
                    break;
            }
            subtitle.setText(message.getStringAttribute(CALL_COST_TIME,"00:00"));
        }

    }
}
