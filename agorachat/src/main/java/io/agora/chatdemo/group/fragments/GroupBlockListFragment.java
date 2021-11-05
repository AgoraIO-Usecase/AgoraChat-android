package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.manager.UserInfoHelper;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupBlockListFragment extends BaseContactListFragment<EaseUser> {
    private GroupMemberAuthorityViewModel viewModel;
    private ContactListAdapter listAdapter;
    private String groupId;
    private List<EaseUser> mDataList = new ArrayList<>();

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
        viewModel = new ViewModelProvider(this).get(GroupMemberAuthorityViewModel.class);
        viewModel.getBlockObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(@Nullable List<String> data) {
                    if(data != null && data.isEmpty()) {
                        List<EaseUser> users = new ArrayList<>();
                        for (String username : data){
                            EaseUser user = UserInfoHelper.getUserInfo(username);
                            if(user != null) {
                                users.add(user);
                            }
                        }
                        sortData(users);
                        mDataList = users;
                        listAdapter.setData(users);
                    }
                }
            });
        });

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        etSearch.setVisibility(View.VISIBLE);
        listAdapter = (ContactListAdapter) mListAdapter;
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
        viewModel.getBlockMembers(groupId);
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
            mListAdapter.setData(mDataList);
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

    private void sortData(List<EaseUser> data) {
        if(data == null || data.isEmpty()) {
            return;
        }
        Collections.sort(data, new Comparator<EaseUser>() {

            @Override
            public int compare(EaseUser lhs, EaseUser rhs) {
                if(lhs.getInitialLetter().equals(rhs.getInitialLetter())){
                    return lhs.getNickname().compareTo(rhs.getNickname());
                }else{
                    if("#".equals(lhs.getInitialLetter())){
                        return 1;
                    }else if("#".equals(rhs.getInitialLetter())){
                        return -1;
                    }
                    return lhs.getInitialLetter().compareTo(rhs.getInitialLetter());
                }

            }
        });
    }
}
