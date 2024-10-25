package io.agora.chatdemo.chat.viewholder.pinmessage;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.agora.chat.ChatMessage;
import io.agora.chat.ImageMessageBody;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;

public class PinImageMessageViewHolder extends EaseBaseRecyclerViewAdapter.ViewHolder<ChatMessage> {
    private final EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener mItemSubViewListener;
    private TextView from;
    private ImageView content;
    private TextView time;
    private ImageView state;
    public PinImageMessageViewHolder(EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener mItemSubViewListener, @NonNull View itemView) {
        super(itemView);
        this.mItemSubViewListener = mItemSubViewListener;
    }

    @Override
    public void initView(View itemView) {
        super.initView(itemView);
        from = findViewById(R.id.tv_from);
        content = findViewById(R.id.iv_content);
        time = findViewById(R.id.tv_time);
        state=findViewById(R.id.iv_state);
    }

    @Override
    public void setData(ChatMessage message, int position) {
        String operatorId = message.pinnedInfo().operatorId();
        long pinTime = message.pinnedInfo().pinTime();

        from.setText(operatorId +" pinned "+message.getFrom()+"'s message");

        ImageMessageBody body = (ImageMessageBody) message.getBody();
        Glide.with(itemView.getContext()).load(body.getRemoteUrl()).into(content);

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd, HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        String formattedDate = sdf.format(new Date(pinTime));
        time.setText(formattedDate);

        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mItemSubViewListener!=null) {
                    mItemSubViewListener.onItemSubViewClick(v, position);
                }
            }
        });
    }
}
