package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupAdminsFragment extends BaseContactListFragment<EaseUser> {
    private GroupMemberAuthorityViewModel viewModel;
    private ContactListAdapter listAdapter;
    private String groupId;
    private List<EaseUser> mGroupManagerList = new ArrayList<>();

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            groupId = bundle.getString("group_id");
        }
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        // User activity for the ViewModelStoreOwner, not need request data of some common methods
        viewModel = new ViewModelProvider(mContext).get(GroupMemberAuthorityViewModel.class);
        viewModel.getGroupManagersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    mGroupManagerList = data;
                    listAdapter.setData(data);
                }
            });
        });
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
        listAdapter = (ContactListAdapter) mListAdapter;
        listAdapter.setEmptyView(R.layout.ease_layout_no_data_show_nothing);
    }

    @Override
    protected void initListener() {
        super.initListener();
    }

    @Override
    protected void initData() {
        super.initData();
        // activity has call it
        // viewModel.getGroupManagers(groupId);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        viewModel.getGroupManagers(groupId);
    }

    @Override
    protected void searchText(String content) {
        super.searchText(content);
        checkSearchContent(content);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    protected void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            mListAdapter.setData(mGroupManagerList);
            sideBarContact.setVisibility(View.VISIBLE);
            srlContactRefresh.setEnabled(true);
        }else {
            List<EaseUser> easeUsers = searchContact(content, mListAdapter.getData());
            mListAdapter.setData(easeUsers);
            sideBarContact.setVisibility(View.GONE);
            srlContactRefresh.setEnabled(false);
        }
    }

    private List<EaseUser> searchContact(String keyword, List<EaseUser> data) {
        List<EaseUser> list = new ArrayList<>();
        if(data != null && !data.isEmpty()) {
            for (EaseUser user : data) {
                if(user.getUsername().contains(keyword) || (!TextUtils.isEmpty(user.getNickname()) && user.getNickname().contains(keyword))) {
                    list.add(user);
                }
            }
        }
        return list;
    }
}
