package io.agora.chatdemo.group;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import io.agora.chat.Group;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.base.BaseListFragment;

/**
 * Created by 许成谱 on 2021/10/27 0027 15:48.
 * qq:1550540124
 * 热爱生活每一天！
 */
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
