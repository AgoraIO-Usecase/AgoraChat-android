package io.agora.chatdemo.general.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.widget.EaseRecyclerView;
import io.agora.chatdemo.R;
import io.agora.chatdemo.chat.PinListItemSpaceDecoration;
import io.agora.chatdemo.chat.adapter.PinMessageListAdapter;

public class PinMessageListViewGroup extends LinearLayout {

    private ConstraintLayout constraintLayout;
    private EaseRecyclerView recyclerView;
    private PinMessageListAdapter adapter;
    private OnItemClickListener itemClickListener;
    private TextView tvCount;
    private View clBottom;
    private EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener itemSubViewClickListener;

    public PinMessageListViewGroup(Context context) {
        super(context);
        init();
    }

    public PinMessageListViewGroup(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setClickable(true);
        setBackgroundColor(Color.parseColor("#80000000"));

        constraintLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.pin_message_list_view_group, this, false);
        addView(constraintLayout);

        tvCount = findViewById(R.id.tv_count);
        recyclerView = findViewById(R.id.rv_list);
        clBottom = findViewById(R.id.cl_bottom);


        adapter = new PinMessageListAdapter();


        int space = dpToPx(8);
        PinListItemSpaceDecoration itemDecoration = new PinListItemSpaceDecoration(space);
        recyclerView.addItemDecoration(itemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter.setOnItemClickListener(new io.agora.chat.uikit.interfaces.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(adapter.getItem(position));
                }
            }
        });
        adapter.setOnItemSubViewClickListener(new EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener() {
            @Override
            public void onItemSubViewClick(View view, int position) {
                if (itemSubViewClickListener != null) {
                    itemSubViewClickListener.onItemSubViewClick(view, position);
                }
            }
        });

        recyclerView.setAdapter(adapter);

    }

    long startY = 0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = (long) event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if (!recyclerView.canScrollVertically(-1) && startY-event.getY()> 20) {
                    ((PinInfoView) getParent()).restView();
                    return true;
                }
                if (event.getX() < 0 || event.getX() > getWidth() ||
                        event.getY() < 0 || event.getY() > getHeight()
                        || event.getY() > clBottom.getTop()) {
                    ((PinInfoView) getParent()).restView();
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    public void setData(List<ChatMessage> data) {
        tvCount.setText(String.valueOf(data.size()) + " Pin Message");
        adapter.setData(data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemSubViewClickListener(EaseBaseRecyclerViewAdapter.OnItemSubViewClickListener listener) {
        this.itemSubViewClickListener = listener;
    }

    public void show(List<ChatMessage> messages) {
        setVisibility(VISIBLE);
        setData(messages);
    }

    public void removeData(ChatMessage message) {
        List<ChatMessage> messageList = adapter.getData();
        if (messageList != null && message != null) {
            for (int i = 0; i < messageList.size(); i++) {
                if (messageList.get(i).getMsgId().equals(message.getMsgId())) {
                    messageList.remove(message);
                    break;
                }
            }
            adapter.notifyDataSetChanged();
        }
        if (CollectionUtils.isEmpty(adapter.getData())) {
            setVisibility(GONE);
        }
    }

    public void setConstraintLayoutMaxHeight(int height) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        int recyclerViewId = R.id.rv_list;
        int rvHeight = height - tvCount.getHeight() - clBottom.getHeight();
        constraintSet.constrainMaxHeight(recyclerViewId, rvHeight);
        constraintSet.applyTo(constraintLayout);
    }

    public interface OnItemClickListener {
        void onItemClick(ChatMessage message);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}

