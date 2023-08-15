package io.agora.chatdemo.contact.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.databinding.ItemForwordContactLayoutBinding;
import io.agora.chatdemo.general.interfaces.OnForwardSendClickListener;

public class ForwardContactsAdapter extends EaseBaseRecyclerViewAdapter<EaseUser> {
    private OnForwardSendClickListener listener;
    private List<String> sentList = new ArrayList<>();

    @Override
    public ViewHolder<EaseUser> getViewHolder(ViewGroup parent, int viewType) {
        return new ForwardContactsViewHolder(ItemForwordContactLayoutBinding.inflate(LayoutInflater.from(mContext), parent, false));
    }

    class ForwardContactsViewHolder extends ViewHolder<EaseUser> {

        private final ItemForwordContactLayoutBinding itemBinding;

        public ForwardContactsViewHolder(@NonNull ItemForwordContactLayoutBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }

        @Override
        public void setData(EaseUser item, int position) {
            itemBinding.tvAction.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            itemBinding.tvAction.setText(mContext.getString(R.string.forward_contact_send));
            itemBinding.tvAction.setEnabled(true);
            itemBinding.tvAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        listener.onClick(v, item.getUsername());
                    }
                    sentList.add(item.getUsername());
                    itemBinding.tvAction.setText(mContext.getString(R.string.forward_contact_sent));
                    itemBinding.tvAction.setTextColor(Color.parseColor("#999999"));
                    itemBinding.tvAction.setEnabled(false);
                }
            });
            if(sentList != null && sentList.contains(item.getUsername())) {
                itemBinding.tvAction.setText(mContext.getString(R.string.forward_contact_sent));
                itemBinding.tvAction.setTextColor(Color.parseColor("#999999"));
                itemBinding.tvAction.setEnabled(false);
            }
            DemoHelper.getInstance().getUsersManager().setUserInfo(mContext,
                    item.getUsername(), itemBinding.name, itemBinding.avatar);
        }
    }

    public void setOnForwardSendClickListener(OnForwardSendClickListener listener) {
        this.listener = listener;
    }
}
