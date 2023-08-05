package io.agora.chatdemo.contact.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Group;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ItemForwordContactLayoutBinding;
import io.agora.chatdemo.general.interfaces.OnForwardSendClickListener;

public class ForwardGroupsAdapter extends EaseBaseRecyclerViewAdapter<Group> {
    private OnForwardSendClickListener listener;
    private List<String> sentList = new ArrayList<>();

    @Override
    public ViewHolder<Group> getViewHolder(ViewGroup parent, int viewType) {
        return new ForwardContactsViewHolder(ItemForwordContactLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }

    class ForwardContactsViewHolder extends ViewHolder<Group> {

        private final ItemForwordContactLayoutBinding itemBinding;

        public ForwardContactsViewHolder(@NonNull ItemForwordContactLayoutBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        @Override
        public void setData(Group item, int position) {
            itemBinding.tvAction.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            itemBinding.tvAction.setText(mContext.getString(R.string.forward_contact_send));
            itemBinding.tvAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        listener.onClick(v, item.getGroupId());
                    }
                    sentList.add(item.getGroupId());
                    itemBinding.tvAction.setText(mContext.getString(R.string.forward_contact_sent));
                    itemBinding.tvAction.setTextColor(Color.parseColor("#999999"));
                    itemBinding.tvAction.setEnabled(false);
                }
            });
            if(sentList != null && sentList.contains(item.getGroupId())) {
                itemBinding.tvAction.setText(mContext.getString(R.string.forward_contact_sent));
                itemBinding.tvAction.setTextColor(Color.parseColor("#999999"));
                itemBinding.tvAction.setEnabled(false);
            }
            boolean hasProvided = DemoHelper.getInstance().setGroupInfo(mContext, item.getGroupId(), itemBinding.name, itemBinding.avatar);
            if(!hasProvided) {
                itemBinding.name.setText(item.getGroupName());
            }
        }
    }

    public void setOnForwardSendClickListener(OnForwardSendClickListener listener) {
        this.listener = listener;
    }
}
