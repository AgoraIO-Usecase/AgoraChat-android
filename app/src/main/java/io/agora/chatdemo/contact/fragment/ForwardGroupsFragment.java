package io.agora.chatdemo.contact.fragment;


import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshLoadMoreListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.agora.chat.ChatMessage;
import io.agora.chat.Group;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.SearchFragment;
import io.agora.chatdemo.contact.adapter.ForwardGroupsAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.activities.GroupDetailActivity;
import io.agora.chatdemo.group.viewmodel.GroupContactViewModel;

public class ForwardGroupsFragment extends SearchFragment<Group> implements OnRefreshLoadMoreListener {
    public RecyclerView rvList;
    private int pageIndex;
    private static final int PAGE_SIZE = 20;
    private GroupContactViewModel mViewModel;
    private boolean isFromChatThread = false;

    @Override
    protected EaseBaseRecyclerViewAdapter<Group> initAdapter() {
        return new ForwardGroupsAdapter();
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            isFromChatThread = bundle.getBoolean("isFromChatThread");
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.GONE);
        rvList = findViewById(R.id.recycleview);
        rvList.setNestedScrollingEnabled(false);
    }

    @Override
    protected void initListener() {
        super.initListener();
        srlContactRefresh.setOnRefreshLoadMoreListener(this);
        if(mListAdapter instanceof ForwardGroupsAdapter) {
            ((ForwardGroupsAdapter) mListAdapter).setOnForwardSendClickListener((view, to) -> {
                LiveDataBus.get().with(DemoConstant.EVENT_SEND_COMBINE).postValue(EaseEvent.create(DemoConstant.EVENT_SEND_COMBINE, EaseEvent.TYPE.MESSAGE, createJsonObject(to)));
            });
        }
    }

    private String createJsonObject(String to) {
        JSONObject object = new JSONObject();
        try {
            object.put("to", to);
            object.put("chatType", ChatMessage.ChatType.Chat.name());
            object.put("isFromChatThread", isFromChatThread);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel = new ViewModelProvider(mContext).get(GroupContactViewModel.class);
        mViewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<Group>>() {
                @Override
                public void onSuccess(List<Group> data) {
                    mListAdapter.setData(data);
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
                        mListAdapter.addData(data);
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
        super.initData();
        rvList.setLayoutManager(new LinearLayoutManager(mContext));
        getData();
    }

    public void getData() {
        pageIndex = 0;
        mViewModel.loadGroupListFromServer(pageIndex, PAGE_SIZE);
    }

    @Override
    public void onItemClick(View view, int position) {
        Group group = mListAdapter.getItem(position);
        GroupDetailActivity.actionStart(mContext, group.getGroupId());
    }

    @Override
    protected void searchText(String content) {

    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        pageIndex += PAGE_SIZE;
        mViewModel.loadMoreGroupListFromServer(pageIndex, PAGE_SIZE);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        getData();
    }
}
