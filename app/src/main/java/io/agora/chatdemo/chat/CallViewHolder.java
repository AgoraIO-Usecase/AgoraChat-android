package io.agora.chatdemo.chat;

import static io.agora.easecallkit.base.EaseCallType.SINGLE_VIDEO_CALL;
import static io.agora.easecallkit.base.EaseCallType.SINGLE_VOICE_CALL;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;
import io.agora.chatdemo.av.VideoCallActivity;
import io.agora.easecallkit.EaseCallKit;
import io.agora.easecallkit.base.EaseCallType;
import io.agora.easecallkit.utils.EaseCallAction;
import io.agora.easecallkit.utils.EaseMsgUtils;


public class CallViewHolder extends EaseChatRowViewHolder {

   public CallViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
      super(itemView, itemClickListener);
   }

   @Override
   public void onBubbleClick(ChatMessage message) {
      super.onBubbleClick(message);
      String action = message.getStringAttribute(EaseMsgUtils.CALL_ACTION, "");
      String fromUser = message.getFrom();
      int calltype = message.getIntAttribute(EaseMsgUtils.CALL_TYPE, SINGLE_VOICE_CALL.code);
      EaseCallType callkitType = EaseCallType.getfrom(calltype);
      EaseCallAction callAction = EaseCallAction.getfrom(action);
      if (callAction == EaseCallAction.CALL_INVITE) {
         switch (callkitType) {
            case SINGLE_VOICE_CALL:
               EaseCallKit.getInstance().startSingleCall(SINGLE_VOICE_CALL,fromUser,null, VideoCallActivity.class);
               break;
            case SINGLE_VIDEO_CALL:
               EaseCallKit.getInstance().startSingleCall(SINGLE_VIDEO_CALL,fromUser,null, VideoCallActivity.class);
               break;
         }
      }
   }
}
