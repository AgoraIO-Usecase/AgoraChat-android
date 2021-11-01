package io.agora.chatdemo.contact;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.List;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chatdemo.R;

public class AddContactAdapter extends EaseBaseRecyclerViewAdapter<String> {
    private List<String> mContacts;

    private OnItemAddClickListener mListener;

    @Override
    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.demo_item_search_list, parent, false));
    }


    private class MyViewHolder extends ViewHolder<String> {
        private EaseImageView mIvSearchUserIcon;
        private TextView mTvSearchName;
        private TextView mTvSearchUserId;
        private TextView mtvSearchAdd;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mIvSearchUserIcon = itemView.findViewById(R.id.iv_search_user_icon);
            mTvSearchName = itemView.findViewById(R.id.tv_search_name);
            mTvSearchUserId = itemView.findViewById(R.id.tv_search_user_id);
            mtvSearchAdd = itemView.findViewById(R.id.tv_search_add);
        }

        @Override
        public void setData(String item, int position) {
            mtvSearchAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mtvSearchAdd.setText(R.string.contact_adding);
                    mtvSearchAdd.setTextColor(ContextCompat.getColor(mContext,R.color.color_light_gray_999999));
                    mtvSearchAdd.setEnabled(false);
                    if(mListener != null) {
                        mListener.onItemAddClick(v, position);
                    }
                }
            });
            if(TextUtils.isEmpty(item)) {
                mTvSearchName.setText("");
                return;
            }
            mTvSearchName.setText(item);
            if(mContacts != null && mContacts.contains(item)) {
                mtvSearchAdd.setText(R.string.contact_added);
                mtvSearchAdd.setTextColor(ContextCompat.getColor(mContext,R.color.color_light_gray_999999));
                mtvSearchAdd.setEnabled(false);
            }else {
                mtvSearchAdd.setText(R.string.contact_add);
                mtvSearchAdd.setTextColor(ContextCompat.getColor(mContext,R.color.contacts_blue_005fff));
                mtvSearchAdd.setEnabled(true);
            }
        }
    }

    /**
     * 设置点击事件
     * @param listener
     */
    public void setOnItemAddClickListener(OnItemAddClickListener listener) {
        this.mListener = listener;
    }

    /**
     * 条目添加事件
     */
    public interface OnItemAddClickListener {
        void onItemAddClick(View view, int position);
    }

    public void addLocalContacts(List<String> contacts) {
        this.mContacts = contacts;
    }
}
