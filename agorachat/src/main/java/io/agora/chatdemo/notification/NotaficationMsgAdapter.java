package io.agora.chatdemo.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ItemNotificationMsgBinding;
import io.agora.chatdemo.general.db.entity.InviteMessageStatus;

import static io.agora.chatdemo.general.constant.DemoConstant.SYSTEM_MESSAGE_NAME;
import static io.agora.chatdemo.general.constant.DemoConstant.SYSTEM_MESSAGE_STATUS;
import static io.agora.chatdemo.general.db.entity.InviteMessageStatus.BEAPPLYED;
import static io.agora.chatdemo.general.db.entity.InviteMessageStatus.BEINVITEED;
import static io.agora.chatdemo.general.db.entity.InviteMessageStatus.GROUPINVITATION;
import static io.agora.chatdemo.general.manager.PushAndMessageHelper.getSystemMessage;

class NotaficationMsgAdapter extends EaseBaseRecyclerViewAdapter<ChatMessage> {

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        return new MsgViewHolder(ItemNotificationMsgBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }

    class MsgViewHolder extends ViewHolder<ChatMessage> {

        private final ItemNotificationMsgBinding itemBinding;

        public MsgViewHolder(@NonNull ItemNotificationMsgBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        @Override
        public void setData(ChatMessage msg, int position) {
            itemBinding.ivMsgDelete.setImageResource(R.drawable.contacts_notification_delete);
            itemBinding.ivFrom.setImageResource(R.drawable.icon);

            try {
                String groupName = msg.getStringAttribute(SYSTEM_MESSAGE_NAME);
                itemBinding.tvName.setText(groupName);
                String messageStatus = msg.getStringAttribute(SYSTEM_MESSAGE_STATUS);
                InviteMessageStatus status = InviteMessageStatus.valueOf(messageStatus);

                if (status == BEINVITEED) {
                    itemBinding.btnAccept.setVisibility(View.VISIBLE);
                } else if (status == BEAPPLYED) {
                    itemBinding.btnAccept.setVisibility(View.VISIBLE);
                } else if (status == GROUPINVITATION) {
                    itemBinding.btnAccept.setVisibility(View.VISIBLE);
                }else{
                    itemBinding.btnAccept.setVisibility(View.GONE);
                }
                itemBinding.tvType.setText( getSystemMessage(msg));
            } catch (Exception e) {
                e.printStackTrace();
            }

            itemBinding.btnAccept.setOnClickListener(v -> {
                mItemSubViewListener.onItemSubViewClick(v, position);
            });
            itemBinding.ivMsgDelete.setOnClickListener(v -> {
                mItemSubViewListener.onItemSubViewClick(v, position);
            });
        }
    }
}
