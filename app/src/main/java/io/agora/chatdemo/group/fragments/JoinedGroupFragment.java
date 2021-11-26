package io.agora.chatdemo.group.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scwang.smart.refresh.layout.api.RefreshLayout;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Group;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.group.activities.GroupDetailActivity;
import io.agora.chatdemo.group.adapter.JoinedGroupAdapter;
import io.agora.chatdemo.group.viewmodel.GroupContactViewModel;


public class JoinedGroupFragment extends PublicGroupFragment {
    private GroupContactViewModel mViewModel;
    private JoinedGroupAdapter mAdapter;
    private int pageIndex;
    private static final int PAGE_SIZE = 20;
    protected List<Group> lastData;

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel = new ViewModelProvider(mContext).get(GroupContactViewModel.class);
        mViewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<Group>>() {
                @Override
                public void onSuccess(List<Group> data) {
                    lastData.clear();
                    lastData.addAll(data);
                    mAdapter.setData(data);
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    if(srlContactRefresh != null) {
                        srlContactRefresh.finishRefresh();
                    }
                }
            });
        });
        mViewModel.getMoreGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<Group>>() {
                @Override
                public void onSuccess(List<Group> data) {
                    if(data != null) {
                        lastData.addAll(data);
                        mAdapter.addData(data);
                    }
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    if(srlContactRefresh != null) {
                        srlContactRefresh.finishLoadMore();
                    }
                }
            });
        });
        mViewModel.getMessageObservable().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isGroupChange() || event.isGroupLeave()) {
                getData();
            }
        });
    }

    @Override
    protected void initData() {
        lastData = new ArrayList<>();
        rvList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new JoinedGroupAdapter();
        rvList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        getData();
    }

    @Override
    public void getData() {
        pageIndex = 0;
        mViewModel.loadGroupListFromServer(pageIndex, PAGE_SIZE);
    }

        @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        pageIndex += PAGE_SIZE;
        mViewModel.loadMoreGroupListFromServer(pageIndex, PAGE_SIZE);
    }

    @Override
    public void onItemClick(View view, int position) {
        Group group = mAdapter.getItem(position);
        GroupDetailActivity.actionStart(mContext, group.getGroupId());
    }

    @Override
    protected void searchText(String content) {
        if (TextUtils.isEmpty(content)) {
            mAdapter.setData(lastData);
        } else {
            ArrayList<Group> groupInfos = new ArrayList<>(lastData);
            for (int i = 0; i < groupInfos.size(); i++) {
                if (!groupInfos.get(i).getGroupName().contains(content)) {
                    groupInfos.remove(i);
                    i--;
                }
            }
            mAdapter.setData(groupInfos);
        }
    }
}
