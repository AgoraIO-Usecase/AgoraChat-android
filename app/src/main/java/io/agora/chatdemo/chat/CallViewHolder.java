package io.agora.chatdemo.chat;

import static io.agora.chat.callkit.general.EaseCallType.SINGLE_VOICE_CALL;

import android.view.View;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.agora.chat.ChatMessage;
import io.agora.chat.callkit.general.EaseCallType;
import io.agora.chat.callkit.general.EaseCallAction;
import io.agora.chat.callkit.utils.EaseCallMsgUtils;
import io.agora.chat.uikit.chat.viewholder.EaseChatRowViewHolder;
import io.agora.chat.uikit.interfaces.MessageListItemClickListener;


public class CallViewHolder extends EaseChatRowViewHolder {
   private Map<String, Object> ext= new HashMap<>();

   public CallViewHolder(@NonNull View itemView, MessageListItemClickListener itemClickListener) {
      super(itemView, itemClickListener);
   }

   @Override
   public void onBubbleClick(ChatMessage message) {
      super.onBubbleClick(message);
      ext.clear();
      String action = message.getStringAttribute(EaseCallMsgUtils.CALL_ACTION, "");
      String fromUser = message.getFrom();
      int calltype = message.getIntAttribute(EaseCallMsgUtils.CALL_TYPE, SINGLE_VOICE_CALL.code);
      String channelName = message.getStringAttribute(EaseCallMsgUtils.CALL_CHANNELNAME, "");
      EaseCallType callkitType = EaseCallType.getfrom(calltype);
      EaseCallAction callAction = EaseCallAction.getfrom(action);
      if (callAction == EaseCallAction.CALL_INVITE||callAction==EaseCallAction.CALL_CANCEL) {
         switch (callkitType) {
            case SINGLE_VOICE_CALL:
//               EaseCallKit.getInstance().startSingleCall(SINGLE_VOICE_CALL,fromUser,null, CallSingleBaseActivity.class);
               break;
            case SINGLE_VIDEO_CALL:
//               EaseCallKit.getInstance().startSingleCall(SINGLE_VIDEO_CALL,fromUser,null, CallSingleBaseActivity.class);
               break;
            case CONFERENCE_VIDEO_CALL:
//               ext.put("groupId", message.conversationId());
//               ext.put(EaseCallMsgUtils.CALL_CHANNELNAME, channelName);
//               EaseCallKit.getInstance().startInviteMultipleCall(CONFERENCE_VIDEO_CALL, null, ext);
               break;
            case CONFERENCE_VOICE_CALL:
//               ext.put("groupId", message.conversationId());
//               ext.put(EaseCallMsgUtils.CALL_CHANNELNAME, channelName);
//               EaseCallKit.getInstance().startInviteMultipleCall(CONFERENCE_VOICE_CALL, null, ext);
               break;
         }
      }
   }
}
