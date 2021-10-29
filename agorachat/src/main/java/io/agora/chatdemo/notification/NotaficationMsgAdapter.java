package io.agora.chatdemo.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ItemNotificationMsgBinding;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.exceptions.ChatException;

/**
 * Created by 许成谱 on 2021/10/27 0027 19:28.
 * qq:1550540124
 * 热爱生活每一天！
 */
class NotaficationMsgAdapter extends EaseBaseRecyclerViewAdapter<ChatMessage> {

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        return new MsgViewHolder(ItemNotificationMsgBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }


    class MsgViewHolder extends ViewHolder<ChatMessage>{

        private final ItemNotificationMsgBinding itemBinding;

        public MsgViewHolder(@NonNull ItemNotificationMsgBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding=itemBinding;
        }

        @Override
        public void initView(View itemView) {

        }

        @Override
        public void setData(ChatMessage msg, int position) {
            try {
                String statusParams = msg.getStringAttribute(DemoConstant.SYSTEM_MESSAGE_STATUS);



            } catch (ChatException e) {
                e.printStackTrace();
            }
            itemBinding.ivUser.setImageResource(R.drawable.delete);
        }
    }
}
