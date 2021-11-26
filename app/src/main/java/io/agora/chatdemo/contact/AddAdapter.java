package io.agora.chatdemo.contact;

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
import io.agora.chatdemo.global.AddType;

public class AddAdapter extends EaseBaseRecyclerViewAdapter<String> {
    private AddType mType=AddType.CONTACT;
    private List<String> mAddedDatas;


    public AddAdapter(AddType type) {
        this.mType=type;
    }

    public ViewHolder getViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(mContext).inflate(R.layout.demo_item_search_list, parent, false));
    }


    private class MyViewHolder extends ViewHolder<String> {
        private EaseImageView mIvUserIcon;
        private TextView mTvName;
        private TextView mTvUserId;
        private TextView mtvAdd;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(View itemView) {
            mIvUserIcon = itemView.findViewById(R.id.iv_search_user_icon);
            mTvName = itemView.findViewById(R.id.tv_search_name);
            mTvUserId = itemView.findViewById(R.id.tv_search_user_id);
            mtvAdd = itemView.findViewById(R.id.tv_add);
        }

        @Override
        public void setData(String item, int position) {
            mtvAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mtvAdd.setText(mType==AddType.CONTACT?R.string.contact_adding:R.string.group_applying);
                    mtvAdd.setTextColor(ContextCompat.getColor(mContext,R.color.color_light_gray_999999));
                    mtvAdd.setEnabled(false);
                    if(mItemSubViewListener != null) {
                        mItemSubViewListener.onItemSubViewClick(v, position);
                    }
                }
            });
            String showID="";
            if(mType==AddType.CONTACT) {
                showID = mContext.getString(R.string.show_agora_chat_id, item);
            }else if(mType==AddType.GROUP) {
                showID = mContext.getString(R.string.show_agora_group_id, item);
            }
            mTvName.setText(showID);
            if(mAddedDatas != null && mAddedDatas.contains(item)) {
                mtvAdd.setText(mType==AddType.CONTACT?R.string.contact_added:R.string.group_applied);
                mtvAdd.setTextColor(ContextCompat.getColor(mContext,R.color.color_light_gray_999999));
                mtvAdd.setEnabled(false);
            }else {
                mtvAdd.setText(mType==AddType.CONTACT?R.string.contact_add:R.string.group_apply);
                mtvAdd.setTextColor(ContextCompat.getColor(mContext,R.color.contacts_blue_005fff));
                mtvAdd.setEnabled(true);
            }
        }
    }

    public void setAddedDatas(List<String> addedDatas) {
        this.mAddedDatas = addedDatas;
    }
}
