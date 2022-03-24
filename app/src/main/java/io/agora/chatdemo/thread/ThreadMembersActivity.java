package io.agora.chatdemo.thread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.List;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityThreadMembersBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.thread.adapter.ThreadMemberAdapter;
import io.agora.chatdemo.thread.viewmodel.ThreadMemberListViewModel;

public class ThreadMembersActivity extends BaseInitActivity {
    private ActivityThreadMembersBinding binding;
    private ThreadMemberAdapter mAdapter;
    private String threadId;
    private ThreadMemberListViewModel viewModel;

    public static void actionStart(Context context, String threadId) {
        Intent intent = new Intent(context, ThreadMembersActivity.class);
        intent.putExtra("threadId", threadId);
        context.startActivity(intent);
    }

    @Override
    protected View getContentView() {
        binding = ActivityThreadMembersBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        threadId = intent.getStringExtra("threadId");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        binding.rvList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ThreadMemberAdapter();
        binding.rvList.setAdapter(mAdapter);
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
        binding.srlRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                viewModel.getThreadMembers(threadId);
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ThreadMemberListViewModel.class);
        viewModel.getResultObservable().observe(this, new Observer<Resource<List<EaseUser>>>() {
            @Override
            public void onChanged(Resource<List<EaseUser>> listResource) {
                parseResource(listResource, new OnResourceParseCallback<List<EaseUser>>() {
                    @Override
                    public void onSuccess(@Nullable List<EaseUser> data) {
                        mAdapter.setData(data);
                    }

                    @Override
                    public void onHideLoading() {
                        super.onHideLoading();
                        finishRefresh();
                    }
                });
            }
        });
        viewModel.getThreadMembers(threadId);
    }

    private void finishRefresh() {
        if(binding.srlRefresh != null) {
            binding.srlRefresh.finishRefresh();
        }
    }
}
