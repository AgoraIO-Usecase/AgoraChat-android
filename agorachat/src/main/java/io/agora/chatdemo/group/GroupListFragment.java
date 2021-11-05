package io.agora.chatdemo.group;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import io.agora.chat.Group;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.base.BaseListFragment;


class GroupListFragment extends BaseListFragment<Group> {
    @Override
    protected RecyclerView initRecyclerView() {
        return null;
    }

    @Override
    protected EaseBaseRecyclerViewAdapter<Group> initAdapter() {
        return null;
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    protected int getLayoutId() {
        return 0;
    }
}
