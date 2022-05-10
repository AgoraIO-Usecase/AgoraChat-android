package io.agora.chatdemo.general.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.models.SelectDialogItemBean;

public class SelectDialogAdapter extends EaseBaseRecyclerViewAdapter<SelectDialogItemBean> {
    @Override
    public ViewHolder<SelectDialogItemBean> getViewHolder(ViewGroup parent, int viewType) {
        return new ManageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.select_dialog_menu_item, parent, false));
    }

    private static class ManageViewHolder extends ViewHolder<SelectDialogItemBean> {
        private ImageView image;
        private TextView text;
        private RelativeLayout imageLayout;

        public ManageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            super.initView(itemView);
            image = findViewById(R.id.image);
            text = findViewById(R.id.text);
            imageLayout = findViewById(R.id.image_layout);
        }

        @Override
        public void setData(SelectDialogItemBean item, int position) {
            if (item.getIcon() == 0) {
                imageLayout.setVisibility(View.GONE);
                text.setGravity(Gravity.CENTER_HORIZONTAL);
            } else {
                image.setImageResource(item.getIcon());
            }
            text.setText(item.getTitle());
            if (item.isAlert()) {
                text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.color_alert));
            } else {
                text.setTextColor(ContextCompat.getColor(text.getContext(), R.color.black));
            }
        }
    }
}
