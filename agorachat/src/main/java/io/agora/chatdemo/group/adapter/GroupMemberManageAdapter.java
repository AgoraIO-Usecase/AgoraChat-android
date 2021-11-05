package io.agora.chatdemo.group.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.group.model.GroupManageItemBean;

public class GroupMemberManageAdapter extends EaseBaseRecyclerViewAdapter<GroupManageItemBean> {
    @Override
    public ViewHolder<GroupManageItemBean> getViewHolder(ViewGroup parent, int viewType) {
        return new ManageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.group_member_manage_menu_item, parent, false));
    }

    private static class ManageViewHolder extends ViewHolder<GroupManageItemBean> {
        private ImageView image;
        private TextView text;

        public ManageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            super.initView(itemView);
            image = findViewById(R.id.image);
            text = findViewById(R.id.text);
        }

        @Override
        public void setData(GroupManageItemBean item, int position) {
            image.setImageResource(item.getIcon());
            text.setText(item.getTitle());
        }
    }
}
