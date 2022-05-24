package io.agora.chatdemo.chat;

import static io.agora.chat.callkit.base.EaseCallType.SINGLE_VIDEO_CALL;
import static io.agora.chat.callkit.base.EaseCallType.SINGLE_VOICE_CALL;

import android.view.View;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;
import io.agora.chatdemo.av.CallSingleBaseActivity;
import io.agora.chat.callkit.EaseCallKit;
import io.agora.chat.callkit.base.EaseCallType;
import io.agora.chat.callkit.utils.EaseCallAction;
import io.agora.chat.callkit.utils.EaseCallMsgUtils;


public class CallViewHolder extends EaseChatRowViewHolder {

   public CallViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
      super(itemView, itemClickListener);
   }

   @Override
   public void onBubbleClick(ChatMessage message) {
      super.onBubbleClick(message);
      String action = message.getStringAttribute(EaseCallMsgUtils.CALL_ACTION, "");
      String fromUser = message.getFrom();
      int calltype = message.getIntAttribute(EaseCallMsgUtils.CALL_TYPE, SINGLE_VOICE_CALL.code);
      EaseCallType callkitType = EaseCallType.getfrom(calltype);
      EaseCallAction callAction = EaseCallAction.getfrom(action);
      if (callAction == EaseCallAction.CALL_INVITE) {
         switch (callkitType) {
            case SINGLE_VOICE_CALL:
               EaseCallKit.getInstance().startSingleCall(SINGLE_VOICE_CALL,fromUser,null, CallSingleBaseActivity.class);
               break;
            case SINGLE_VIDEO_CALL:
               EaseCallKit.getInstance().startSingleCall(SINGLE_VIDEO_CALL,fromUser,null, CallSingleBaseActivity.class);
               break;
         }
      }
   }
}
