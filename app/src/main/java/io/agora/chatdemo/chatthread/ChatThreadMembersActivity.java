package io.agora.chatdemo.chatthread;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.chatthread.EaseChatThreadRole;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityThreadMembersBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.net.Resource;
import io.agora.chatdemo.chatthread.adapter.ChatThreadMemberAdapter;
import io.agora.chatdemo.chatthread.bean.MenuItemBean;
import io.agora.chatdemo.chatthread.dialog.MenuDialog;
import io.agora.chatdemo.chatthread.viewmodel.ChatThreadMemberListViewModel;

public class ChatThreadMembersActivity extends BaseInitActivity {
    private ActivityThreadMembersBinding binding;
    private ChatThreadMemberAdapter mAdapter;
    private String threadId;
    private ChatThreadMemberListViewModel viewModel;
    private List<EaseUser> mData = new ArrayList<>();
    private EaseChatThreadRole threadRole;

    public static void actionStart(Context context, String threadId, int role) {
        Intent intent = new Intent(context, ChatThreadMembersActivity.class);
        intent.putExtra("threadId", threadId);
        intent.putExtra("threadRole", role);
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
        int role = intent.getIntExtra("threadRole", EaseChatThreadRole.MEMBER.ordinal());
        threadRole = EaseChatThreadRole.getThreadRole(role);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        binding.rvList.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new ChatThreadMemberAdapter();
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
                if(threadRole == EaseChatThreadRole.GROUP_ADMIN) {
                    String username = mData.get(position).getUsername();
                    if(!TextUtils.equals(username, ChatClient.getInstance().getCurrentUser())) {
                        showRemoveDialog(username);
                    }
                }
            }
        });
        binding.srlRefresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                viewModel.getThreadMembers(threadId);
            }
        });
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkSearchContent(s.toString().trim());
            }
        });
    }

    protected void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            mAdapter.setData(mData);
            binding.srlRefresh.setEnabled(true);
        }else {
            viewModel.searchContact(mData, content);
            binding.srlRefresh.setEnabled(false);
        }
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(ChatThreadMemberListViewModel.class);
        viewModel.getResultObservable().observe(this, new Observer<Resource<List<EaseUser>>>() {
            @Override
            public void onChanged(Resource<List<EaseUser>> listResource) {
                parseResource(listResource, new OnResourceParseCallback<List<EaseUser>>() {
                    @Override
                    public void onSuccess(@Nullable List<EaseUser> data) {
                        mData = data;
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
        viewModel.getSearchResultObservable().observe(this, new Observer<List<EaseUser>>() {
            @Override
            public void onChanged(List<EaseUser> easeUsers) {
                mAdapter.setData(easeUsers);
            }
        });
        viewModel.getRemoveResultObservable().observe(this, new Observer<Resource<Boolean>>() {
            @Override
            public void onChanged(Resource<Boolean> booleanResource) {
                parseResource(booleanResource, new OnResourceParseCallback<Boolean>() {
                    @Override
                    public void onSuccess(@Nullable Boolean data) {
                        viewModel.getThreadMembers(threadId);
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

    public void showRemoveDialog(String username) {
        String title = username;
        EaseUser userInfo = EaseUserUtils.getUserInfo(username);
        if(userInfo != null) {
            title = userInfo.getNickname();
        }
        MenuItemBean item = new MenuItemBean();
        item.setIcon(R.drawable.group_manage_remove_admin);
        item.setTitle(getString(R.string.thread_remove_member_hint));
        item.setAlert(true);
        List<MenuItemBean> data = new ArrayList<>();
        data.add(item);
        new MenuDialog.Builder(mContext)
                .setTitle(title)
                .setMenus(data)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        viewModel.removeThreadMember(threadId, username);
                    }
                })
                .setFullWidth()
                .setGravity(Gravity.BOTTOM)
                .show();
    }
}
