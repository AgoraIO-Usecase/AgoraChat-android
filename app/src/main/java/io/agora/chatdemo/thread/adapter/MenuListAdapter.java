package io.agora.chatdemo.thread.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.thread.bean.MenuItemBean;

public class MenuListAdapter extends EaseBaseRecyclerViewAdapter<MenuItemBean> {
    @Override
    public ViewHolder<MenuItemBean> getViewHolder(ViewGroup parent, int viewType) {
        return new ManageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_menu_item, parent, false));
    }

    private static class ManageViewHolder extends ViewHolder<MenuItemBean> {
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
        public void setData(MenuItemBean item, int position) {
            image.setImageResource(item.getIcon());
            text.setText(item.getTitle());
            if(item.isAlert()) {
                text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.color_alert));
            }else {
                text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.black));
            }
        }
    }
}
