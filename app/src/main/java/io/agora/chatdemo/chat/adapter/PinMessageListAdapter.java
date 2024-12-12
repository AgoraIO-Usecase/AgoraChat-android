package io.agora.chatdemo.chat.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.viewholder.pinmessage.PinDefaultViewHolder;
import io.agora.chatdemo.chat.viewholder.pinmessage.PinImageMessageViewHolder;
import io.agora.chatdemo.chat.viewholder.pinmessage.PinTextMessageViewHolder;


public class PinMessageListAdapter extends EaseBaseRecyclerViewAdapter<ChatMessage> {

    @Override
    public int getItemNotEmptyViewType(int position) {
        return mData.get(position).getType().ordinal();
    }

    @Override
    public ViewHolder<ChatMessage> getViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder ;
        switch (ChatMessage.Type.values()[viewType]) {
            case TXT:
                viewHolder=new PinTextMessageViewHolder(mItemSubViewListener,LayoutInflater.from(mContext).inflate(R.layout.pinlist_text, parent, false));
                break;
            case IMAGE:
                viewHolder=new PinImageMessageViewHolder(mItemSubViewListener,LayoutInflater.from(mContext).inflate(R.layout.pinlist_image, parent, false));
                break;
            default:
                viewHolder=new PinDefaultViewHolder(mItemSubViewListener,LayoutInflater.from(mContext).inflate(R.layout.pinlist_default, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder<ChatMessage> holder, int position) {
        super.onBindViewHolder(holder, position);
    }
}
