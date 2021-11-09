package io.agora.chatdemo.group.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;


public class JoinedGroupAdapter extends EaseBaseRecyclerViewAdapter<Group> {
    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new GroupViewHolder(LayoutInflater.from(mContext).inflate(R.layout.demo_widget_contact_item, parent, false));
    }

    private class GroupViewHolder extends ViewHolder<Group> {
        private TextView mHeader;
        private EaseImageView mAvatar;
        private TextView mName;
        private TextView mSignature;
        private TextView mLabel;
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
            mLabel = findViewById(R.id.label);
            mUnreadMsgNumber = findViewById(R.id.unread_msg_number);
            memberNum = findViewById(R.id.tv_members_num);
        }

        @Override
        public void setData(Group item, int position) {
            boolean hasProvided = DemoHelper.getInstance().setGroupInfo(mContext, item.getGroupId(), mName, mAvatar);
            if(!hasProvided) {
                mName.setText(item.getGroupName());
            }
            mSignature.setText(item.getGroupId());
            mLabel.setVisibility(View.GONE);
//            if(isOwner(item.getOwner())) {
//                mLabel.setVisibility(View.VISIBLE);
//                mLabel.setText(R.string.group_owner);
//            }
            memberNum.setText("("+item.getMemberCount()+")");
        }
    }

    private boolean isOwner(String owner) {
        return TextUtils.equals(ChatClient.getInstance().getCurrentUser(), owner);
    }
}
