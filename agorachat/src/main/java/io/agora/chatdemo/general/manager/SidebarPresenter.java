package io.agora.chatdemo.general.manager;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.general.widget.EaseSidebar;

public class SidebarPresenter implements EaseSidebar.OnTouchEventListener{
    private TextView mFloatingHeader;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    public void setupWithRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter, TextView floatingHeader) {
        this.mRecyclerView = recyclerView;
        this.mAdapter = adapter;
        this.mFloatingHeader = floatingHeader;
    }

    @Override
    public void onActionDown(MotionEvent event, String pointer) {
        showFloatingHeader(pointer);
        moveToRecyclerItem(pointer);
    }

    @Override
    public void onActionMove(MotionEvent event, String pointer) {
        showFloatingHeader(pointer);
        moveToRecyclerItem(pointer);
    }

    @Override
    public void onActionUp(MotionEvent event) {
        hideFloatingHeader();
    }

    private void moveToRecyclerItem(String pointer) {
        if(mAdapter == null) {
            return;
        }
        if(!(mAdapter instanceof EaseBaseRecyclerViewAdapter)) {
            return;
        }
        List data = ((EaseBaseRecyclerViewAdapter)mAdapter).getData();
        if(data == null || data.isEmpty()) {
            return;
        }
        Object object = data.get(0);
        if(!(object instanceof EaseUser)) {
            return;
        }
        for(int i = 0; i < data.size(); i++) {
            EaseUser item = (EaseUser) data.get(i);
            if(TextUtils.equals(item.getInitialLetter(), pointer)) {
                LinearLayoutManager manager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                if(manager != null) {
                    manager.scrollToPositionWithOffset(i, 0);
                }
            }
        }
    }

    /**
     * 展示滑动的字符
     * @param pointer
     */
    private void showFloatingHeader(String pointer) {
        if(TextUtils.isEmpty(pointer)) {
            hideFloatingHeader();
            return;
        }
        mFloatingHeader.setText(pointer);
        mFloatingHeader.setVisibility(View.VISIBLE);
    }

    private void hideFloatingHeader() {
        mFloatingHeader.setVisibility(View.GONE);
    }
}
