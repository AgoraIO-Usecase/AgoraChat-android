package io.agora.chatdemo.general.widget;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.util.EMLog;

public class PinInfoView extends RelativeLayout {

    private List<ChatMessage> pinMessages=new ArrayList<>();
    private View primaryView;
    private PinMessageListViewGroup pinMessageListView;

    public PinInfoView(Context context) {
        super(context);
        init();
    }

    public PinInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        primaryView = LayoutInflater.from(getContext()).inflate(R.layout.pin_info_view, this, false);
        addView(primaryView);
        findViewById(R.id.tv_info2).setOnClickListener(v->{
            showPinListView();
        });

        pinMessageListView = new PinMessageListViewGroup(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        pinMessageListView.setVisibility(View.GONE);
        addView(pinMessageListView, layoutParams);
    }

    private void showPinListView() {
        primaryView.setVisibility(GONE);
        pinMessageListView.show(pinMessages);
    }
    private void showPrimaryView() {
        setVisibility(VISIBLE);
        primaryView.setVisibility(VISIBLE);
        pinMessageListView.setVisibility(GONE);
    }

    public void setData(List<ChatMessage> messages) {
        pinMessages.clear();
        if(!CollectionUtils.isEmpty(messages)) {
            pinMessages.addAll(0,messages);
        }
        setVisibility(VISIBLE);
    }

    public void removeData(ChatMessage message) {
        if(message!=null) {
            for (int i = 0; i < pinMessages.size(); i++) {
                if(pinMessages.get(i).getMsgId().equals(message.getMsgId())) {
                    pinMessages.remove(message);
                    break;
                }
            }
            pinMessageListView.removeData(message);
            if(pinMessages.isEmpty()) {
                setVisibility(GONE);
            }
        }
    }

    public void addData(ChatMessage message) {
        if(message!=null) {
            pinMessages.add(0,message);
        }
        showPrimaryView();
    }

    public void restView() {
        primaryView.setVisibility(VISIBLE);
        pinMessageListView.setVisibility(GONE);
    }

    public List<ChatMessage> getPinMessages() {
        return pinMessages;
    }

    public void setOnItemClickListener(PinMessageListViewGroup.OnItemClickListener listener) {
        pinMessageListView.setOnItemClickListener(listener);
    }

    public void setOnItemSubViewClickListener(EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener onItemSubViewClickListener) {
        pinMessageListView.setOnItemSubViewClickListener(onItemSubViewClickListener);
    }

    public void setInnerLayoutMaxHeight(int height) {
        if(pinMessageListView!=null) {
            EMLog.d("PinInfoView", "setInnerLayoutMaxHeight: " + height);
            pinMessageListView.setConstraintLayoutMaxHeight(height);
        }
    }
}



