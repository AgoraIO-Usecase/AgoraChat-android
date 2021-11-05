package io.agora.chatdemo.contact;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.R;

public class ContactListAdapter extends EaseBaseRecyclerViewAdapter<EaseUser> {
    private boolean showInitials;
    private boolean isCheckModel;
    private List<String> adminList;
    private List<String> muteList;
    private List<String> checkedList;
    private String owner;
    private OnSelectListener listener;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(mContext).inflate(R.layout.ease_widget_contact_item, parent, false));
    }

    public void setShowInitials(boolean showInitials) {
        this.showInitials = showInitials;
    }

    public void setCheckModel(boolean isCheckModel) {
        this.isCheckModel = isCheckModel;
        if(isCheckModel) {
            checkedList = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public void setSelectedMembers(List<String> existMembers) {
        checkedList=existMembers;
        notifyDataSetChanged();
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setAdminList(List<String> adminList) {
        this.adminList = adminList;
    }

    public void setMuteList(List<String> muteList) {
        this.muteList = muteList;
    }

    public List<String> getMuteList() {
        return muteList;
    }

    public List<String> getCheckedList() {
        return checkedList;
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
            }else {
                label.setVisibility(View.GONE);
            }
            mName.setText(item.getNickname());
            Glide.with(mContext)
                    .load(item.getAvatar())
                    .error(ContextCompat.getDrawable(mContext, R.drawable.ease_default_avatar))
                    .into(mAvatar);
            if(isCheckModel) {
                cb_select.setVisibility(View.VISIBLE);
                if(checkedList!=null&&checkedList.contains(username)) {
                    cb_select.setSelected(true);
                }else{
                    cb_select.setSelected(false);
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
