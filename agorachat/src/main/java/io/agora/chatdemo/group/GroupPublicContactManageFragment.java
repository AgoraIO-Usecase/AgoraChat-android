package io.agora.chatdemo.group;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.List;

import io.agora.chat.CursorResult;
import io.agora.chat.Group;
import io.agora.chat.GroupInfo;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.group.viewmodel.GroupContactViewModel;


public class GroupPublicContactManageFragment extends BaseInitFragment implements OnRefreshLoadMoreListener, OnItemClickListener {
    public SmartRefreshLayout srlRefresh;
    public RecyclerView rvList;
    public PublicGroupContactAdapter mAdapter;
    private int page_size = 20;
    private String cursor;
    private GroupContactViewModel viewModel;
    private List<Group> allJoinGroups;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_fragment_group_public_contact_manage;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        srlRefresh = findViewById(R.id.srl_refresh);
        rvList = findViewById(R.id.rv_list);
    }

    @Override
    protected void initListener() {
        super.initListener();
        srlRefresh.setOnRefreshLoadMoreListener(this);
    }

    @Override
    protected void initViewModel() {
        viewModel = new ViewModelProvider(mContext).get(GroupContactViewModel.class);
        viewModel.getPublicGroupObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<CursorResult<GroupInfo>>() {
                @Override
                public void onSuccess(CursorResult<GroupInfo> data) {
                    List<GroupInfo> groups = data.getData();
                    cursor = data.getCursor();
                    mAdapter.setData(groups);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    if(srlRefresh != null) {
                        srlRefresh.finishRefresh();
                    }
                }
            });
        });

        viewModel.getMorePublicGroupObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<CursorResult<GroupInfo>>() {
                @Override
                public void onSuccess(CursorResult<GroupInfo> data) {
                    cursor = data.getCursor();
                    List<GroupInfo> groups = data.getData();
                    mAdapter.addData(groups);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    if(srlRefresh != null) {
                        srlRefresh.finishLoadMore();
                    }
                }
            });
        });

        viewModel.getAllGroupsObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<Group>>() {
                @Override
                public void onSuccess(@Nullable List<Group> data) {
                    allJoinGroups = data;
                    //获取完加入的群组信息，再请求数据
                    getData();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    //请求出错后，再请求数据
                    getData();
                }
            });
        });

    }

    @Override
    protected void initData() {
        super.initData();
        rvList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new PublicGroupContactAdapter();
        rvList.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(this);

        //getData();
    }

    public void getData() {
        viewModel.getPublicGroups(page_size);
    }

    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        if(cursor != null) {
            viewModel.getMorePublicGroups(page_size, cursor);
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        getData();
    }

    @Override
    public void onItemClick(View view, int position) {
        GroupInfo item = mAdapter.getItem(position);
        if(GroupHelper.isJoinedGroup(allJoinGroups, item.getGroupId())) {
            ChatActivity.actionStart(mContext, item.getGroupId(), DemoConstant.CHATTYPE_GROUP);
        }else {
//            GroupSimpleDetailActivity.actionStart(mContext, item.getGroupId());
        }
    }
}
