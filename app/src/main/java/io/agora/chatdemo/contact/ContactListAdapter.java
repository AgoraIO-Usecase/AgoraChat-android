package io.agora.chatdemo.contact;

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
import java.util.concurrent.ConcurrentHashMap;

import io.agora.chat.Presence;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chatdemo.general.utils.EasePresenceUtil;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.group.model.MemberAttributeBean;

public class ContactListAdapter extends EaseBaseRecyclerViewAdapter<EaseUser> {
    private boolean showInitials;
    private boolean isCheckModel;
    private String groupId;
    private List<String> adminList;
    private List<String> muteList;
    private List<String> checkedList;
    private String owner;
    private OnSelectListener listener;
    private List<String> memberList;
    private ConcurrentHashMap<String,Presence> presences;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(mContext).inflate(R.layout.ease_widget_contact_item, parent, false));
    }

    public void setShowInitials(boolean showInitials) {
        this.showInitials = showInitials;
    }

    public void setPresences(ConcurrentHashMap<String,Presence> presences){
        this.presences=presences;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCheckModel(boolean isCheckModel) {
        this.isCheckModel = isCheckModel;
        if(isCheckModel) {
            checkedList = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectedMembers(List<String> existMembers) {
        checkedList=existMembers;
        notifyDataSetChanged();
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    @SuppressLint("NotifyDataSetChanged")
    public void setMuteList(List<String> muteList) {
        this.muteList = muteList;
        notifyDataSetChanged();
    }

    public List<String> getMuteList() {
        return muteList;
    }

    public List<String> getCheckedList() {
        if(this.memberList != null) {
            if(checkedList == null) {
                return memberList;
            }else {
                checkedList.addAll(memberList);
            }
        }
        return checkedList;
    }

    public void setGroupMemberList(List<String> memberList) {
        this.memberList = memberList;
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
        private TextView originNickName;
        private View presenceGroup;
        private EaseImageView ivPresence;

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
            ivPresence = findViewById(R.id.iv_presence);
            presenceGroup = findViewById(R.id.presence_group);
            originNickName = findViewById(R.id.tv_origin_nickName);
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
            if(label.getVisibility() == View.VISIBLE) {
                if(isContains(muteList, username)) {
                    setLabel(label, mContext.getString(R.string.group_admin_muted));
                }
            }else {
                if(isContains(muteList, username)) {
                    setLabel(label, mContext.getString(R.string.group_permission_mute));
                }
            }
            if(TextUtils.equals(owner, username)) {
                setLabel(label, mContext.getString(R.string.group_role_owner));
            }
            if(!TextUtils.isEmpty(groupId)) {
                MemberAttributeBean groupBean = DemoHelper.getInstance().getMemberAttribute(groupId,username);
                if(groupBean!=null&&!TextUtils.isEmpty(groupBean.getNickName())) {
                    originNickName.setVisibility(View.VISIBLE);
                    EaseUserUtils.setUserAvatar(mContext, groupId, item.getUsername(), mAvatar);
                    EaseUserUtils.setUserNick(groupId,item.getUsername(), mName);
                    originNickName.setText(TextUtils.isEmpty(item.getNickname())?item.getNickname():item.getUsername());
                }else{
                    originNickName.setVisibility(View.GONE);
                    DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, TextUtils.isEmpty(item.getNickname())?item.getNickname():item.getUsername(), mName, mAvatar);
                }
            }else{
                originNickName.setVisibility(View.GONE);
                DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, TextUtils.isEmpty(item.getNickname())?item.getNickname():item.getUsername(), mName, mAvatar);
            }
            if(isCheckModel) {
                cb_select.setVisibility(View.VISIBLE);
                if(checkedList!=null&&checkedList.contains(username)) {
                    cb_select.setSelected(true);
                }else{
                    cb_select.setSelected(false);
                }
                if(isContains(memberList, username)) {
                    cb_select.setSelected(true);
                    cb_select.setEnabled(false);
                    this.itemView.setEnabled(false);
                }else {
                    cb_select.setEnabled(true);
                    this.itemView.setEnabled(true);
                }
                if(mOnItemClickListener != null) {
                    this.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean isSelected = cb_select.isSelected();
                            cb_select.setSelected(!isSelected);
                            if(checkedList == null) {
                                checkedList=new ArrayList<>();
                            }
                            if(cb_select.isSelected() && !isContains(checkedList, username)) {
                                checkedList.add(username);
                            }
                            if(!cb_select.isSelected()) {
                                checkedList.remove(username);
                            }
                            if(listener != null) {
                                listener.onSelected(v, checkedList);
                            }
                        }
                    });
                }
            }

            if(presences!=null&&!presences.isEmpty()) {
                presenceGroup.setVisibility(View.VISIBLE);
                Presence presence = presences.get(username);
                mSignature.setText(EasePresenceUtil.getPresenceString(mContext,presence));
                ivPresence.setImageResource(EasePresenceUtil.getPresenceIcon(mContext,presence));
            }else {
                presenceGroup.setVisibility(View.GONE);
            }
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

    public void setOnSelectListener(OnSelectListener listener) {
        this.listener = listener;
    }

    public interface OnSelectListener {
        void onSelected(View v, List<String> selectedMembers);
    }
}
