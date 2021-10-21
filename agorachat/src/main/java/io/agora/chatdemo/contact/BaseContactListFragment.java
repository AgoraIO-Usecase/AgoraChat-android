package io.agora.chatdemo.contact;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseListFragment;
import io.agora.chatdemo.general.manager.SidebarPresenter;
import io.agora.chatdemo.general.widget.EaseSidebar;

public abstract class BaseContactListFragment<T> extends BaseListFragment<T> implements SwipeRefreshLayout.OnRefreshListener {
    protected SwipeRefreshLayout srlContactRefresh;
    private EaseSidebar sideBarContact;
    private TextView floatingHeader;

    private SidebarPresenter sidebarPresenter;

    private boolean canUseRefresh = true;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_contact_list;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        srlContactRefresh = findViewById(R.id.srl_contact_refresh);
        sideBarContact = findViewById(R.id.side_bar_contact);
        floatingHeader = findViewById(R.id.floating_header);

        srlContactRefresh.setEnabled(canUseRefresh);

        sidebarPresenter = new SidebarPresenter();
        sidebarPresenter.setupWithRecyclerView(mRecyclerView, mListAdapter, floatingHeader);
        sideBarContact.setOnTouchEventListener(sidebarPresenter);
    }

    @Override
    protected RecyclerView initRecyclerView() {
        return findViewById(R.id.contact_list);
    }

    @Override
    protected EaseBaseRecyclerViewAdapter<T> initAdapter() {
        EaseBaseRecyclerViewAdapter adapter = new ContactListAdapter();
        return adapter;
    }

    @Override
    protected void initListener() {
        super.initListener();
        srlContactRefresh.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {

    }
}
