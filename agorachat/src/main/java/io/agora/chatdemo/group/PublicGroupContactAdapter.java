package io.agora.chatdemo.group;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.agora.chat.GroupInfo;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.R;


public class PublicGroupContactAdapter extends EaseBaseRecyclerViewAdapter<GroupInfo> {
    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(mContext).inflate(R.layout.demo_widget_contact_item, parent, false));
    }

    private class GroupViewHolder extends ViewHolder<GroupInfo> {
        private TextView mHeader;
        private EaseImageView mAvatar;
        private TextView mName;
        private TextView mSignature;
        private TextView mUnreadMsgNumber;
        private TextView memberNum;

        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mHeader = findViewById(R.id.header);
            mAvatar = findViewById(R.id.avatar);
            mName = findViewById(R.id.name);
            mSignature = findViewById(R.id.signature);
            mUnreadMsgNumber = findViewById(R.id.unread_msg_number);
            memberNum = findViewById(R.id.tv_members_num);
            mHeader.setVisibility(View.GONE);
        }

        @Override
        public void setData(GroupInfo item, int position) {
            mAvatar.setImageResource(R.drawable.avatar_1);
            mName.setText(item.getGroupName());
        }
    }
}
