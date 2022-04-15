package io.agora.chatdemo.thread.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;

public class ChatThreadMemberAdapter extends EaseBaseRecyclerViewAdapter<EaseUser> {
    private boolean showInitials;
    private List<String> adminList;
    private String owner;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(mContext).inflate(R.layout.ease_widget_contact_item, parent, false));
    }

    public void setShowInitials(boolean showInitials) {
        this.showInitials = showInitials;
    }

    public void setOwner(String owner) {
        this.owner = owner;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAdminList(List<String> adminList) {
        this.adminList = adminList;
        notifyDataSetChanged();
    }

    private class ContactViewHolder extends ViewHolder<EaseUser> {
        private TextView mHeader;
        private EaseImageView mAvatar;
        private TextView mName;
        private TextView mSignature;
        private TextView mUnreadMsgNumber;
        private ConstraintLayout clUser;
        private CheckBox cb_select;
        private TextView label;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mHeader = findViewById(R.id.header);
            mAvatar = findViewById(R.id.avatar);
            mName = findViewById(R.id.name);
            mSignature = findViewById(R.id.signature);
            mUnreadMsgNumber = findViewById(R.id.unread_msg_number);
            clUser = findViewById(R.id.cl_user);
            cb_select = findViewById(R.id.cb_select);
            label = findViewById(R.id.label);
            EaseUserUtils.setUserAvatarStyle(mAvatar);
        }

        @Override
        public void setData(EaseUser item, int position) {
            EaseUserProfileProvider provider = EaseUIKit.getInstance().getUserProvider();
            String username = item.getUsername();
            if(provider != null) {
                EaseUser user = provider.getUser(username);
                if(user != null) {
                    item = user;
                }
            }
            if(showInitials) {
                String header = item.getInitialLetter();
                mHeader.setVisibility(View.GONE);
                if(position == 0 || (header != null && !header.equals(getItem(position -1).getInitialLetter()))) {
                    if(!TextUtils.isEmpty(header)) {
                        mHeader.setVisibility(View.VISIBLE);
                        mHeader.setText(header);
                    }
                }
            }
            if(isContains(adminList, username)) {
                setLabel(label, mContext.getString(R.string.group_role_admin));
            }else {
                label.setVisibility(View.GONE);
            }
            if(TextUtils.equals(owner, username)) {
                setLabel(label, mContext.getString(R.string.group_role_owner));
            }
            DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, TextUtils.isEmpty(item.getNickname())?item.getNickname():item.getUsername(), mName, mAvatar);
        }
    }
    
    private void setLabel(TextView tv, String label) {
        if(!TextUtils.isEmpty(label)) {
            tv.setVisibility(View.VISIBLE);
            tv.setText(label);
        }else {
            tv.setVisibility(View.GONE);
        }
    }

    private boolean isContains(List<String> data, String username) {
        return data != null && data.contains(username);
    }
}
