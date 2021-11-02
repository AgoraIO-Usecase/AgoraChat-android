package io.agora.chatdemo.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.CursorResult;
import io.agora.chat.Group;
import io.agora.chat.GroupInfo;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BottomSheetChildHelper;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.contact.SearchFragment;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.group.viewmodel.GroupContactViewModel;


public class GroupPublicContactManageFragment extends SearchFragment<GroupInfo> implements OnRefreshLoadMoreListener, OnItemClickListener, BottomSheetChildHelper {
    public RecyclerView rvList;
    private int page_size = 20;
    private String cursor;
    private GroupContactViewModel viewModel;
    private List<Group> allJoinGroups;
    protected List<GroupInfo> lastData=new ArrayList<>();


    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rvList = findViewById(R.id.recycleview);

        rvList.setNestedScrollingEnabled(false);
    }

    @Override
    protected void initListener() {
        super.initListener();
        srlContactRefresh.setOnRefreshLoadMoreListener(this);
    }

    @Override
    protected void searchText(String content) {
        if (TextUtils.isEmpty(content)) {
            mListAdapter.setData(lastData);
        } else {
            ArrayList<GroupInfo> groupInfos = new ArrayList<>(lastData);
            for (int i = 0; i < groupInfos.size(); i++) {
                if (!groupInfos.get(i).getGroupName().contains(content)) {
                    groupInfos.remove(i);
                    i--;
                }
            }
            mListAdapter.setData(groupInfos);
        }
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
                    lastData.clear();
                    lastData.addAll(groups);
                    mListAdapter.setData(groups);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    if (srlContactRefresh != null) {
                        srlContactRefresh.finishRefresh();
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
                    lastData.addAll(groups);
                    mListAdapter.addData(groups);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    if (srlContactRefresh != null) {
                        srlContactRefresh.finishLoadMore();
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
        getData();
    }

    @Override
    protected EaseBaseRecyclerViewAdapter initAdapter() {
        return new PublicGroupContactAdapter();
    }

    public void getData() {
        viewModel.getPublicGroups(page_size);
    }

    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {
        if (cursor != null) {
            viewModel.getMorePublicGroups(page_size, cursor);
        }
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        getData();
    }

    @Override
    public void onItemClick(View view, int position) {
        GroupInfo item = mListAdapter.getItem(position);
        ChatActivity.actionStart(mContext, item.getGroupId(), DemoConstant.CHATTYPE_GROUP);
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }
}
