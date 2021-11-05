package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.dialog.GroupMemberManageDialog;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupAllMembersFragment extends BaseContactListFragment<EaseUser> {
    private GroupMemberAuthorityViewModel viewModel;
    private ContactListAdapter listAdapter;
    protected String groupId;
    protected ContactListAdapter managersAdapter;
    private List<EaseUser> mGroupManagerList = new ArrayList<>();
    private List<EaseUser> mMemberList = new ArrayList<>();
    private int groupRole;
    private Group group;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            groupId = bundle.getString("group_id");
            groupRole = bundle.getInt("group_role");
        }
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        // User activity for the ViewModelStoreOwner, not need request data of some common methods
        // For example get group's mute list
        viewModel = new ViewModelProvider(mContext).get(GroupMemberAuthorityViewModel.class);
        viewModel.getGroupManagersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    mGroupManagerList = data;
                    managersAdapter.setData(data);
                    group = ChatClient.getInstance().groupManager().getGroup(groupId);
                    if(group != null) {
                        managersAdapter.setOwner(group.getOwner());
                        managersAdapter.setAdminList(group.getAdminList());
                    }

                }
            });
        });
        viewModel.getMemberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    mMemberList = data;
                    listAdapter.setData(data);
                }
            });
        });
        viewModel.getMuteMembersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Map<String, Long>>() {
                @Override
                public void onSuccess(@Nullable Map<String, Long> data) {
                    listAdapter.setMuteList(new ArrayList<>(data.keySet()));
                }
            });
        });

    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        etSearch.setVisibility(View.VISIBLE);
        listAdapter = (ContactListAdapter) mListAdapter;
        listAdapter.setEmptyView(R.layout.ease_layout_no_data_show_nothing);
    }

    @Override
    protected void initListener() {
        super.initListener();
        managersAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel.getGroupManagers(groupId);
        viewModel.getMembers(groupId);
    }

    @Override
    public void addHeader(ConcatAdapter adapter) {
        managersAdapter = new ContactListAdapter();
        managersAdapter.setEmptyView(R.layout.ease_layout_no_data_show_nothing);
        adapter.addAdapter(managersAdapter);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        viewModel.getGroupManagers(groupId);
        viewModel.getMembers(groupId);
    }

    @Override
    protected void searchText(String content) {
        super.searchText(content);
        checkSearchContent(content);
    }

    @Override
    public void onItemClick(View view, int position) {
        EaseUser item = listAdapter.getItem(position);
        //showManageDialog();
    }

    private void getMenuData(String username) {
        if(TextUtils.equals(username, DemoHelper.getInstance().getCurrentUser())) {
            return;
        }
        if(!GroupHelper.isOwner(group) && !GroupHelper.isAdmin(group)) {
            return;
        }
        List<GroupManageItemBean> data = new ArrayList<>();
        GroupManageItemBean itemBean = new GroupManageItemBean();
        if(groupRole == DemoConstant.GROUP_ROLE_OWNER) {
            // add admin/mute/block
            boolean inAdminList = GroupHelper.isInAdminList(username, group.getAdminList());
            if(inAdminList) {
                itemBean.setIcon(R.drawable.icon);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove_admin));
            }else {
                itemBean.setIcon(R.drawable.icon);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_make_admin));
            }
            itemBean = new GroupManageItemBean();
            if(GroupHelper.isInMuteList(username, listAdapter.getMuteList())) {
                itemBean.setIcon(R.drawable.icon);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_unmute));
            }else {
                itemBean.setIcon(R.drawable.icon);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_mute));
            }
            itemBean = new GroupManageItemBean();
            itemBean.setIcon(R.drawable.icon);
            itemBean.setTitle(getString(R.string.group_members_dialog_menu_block));

        }else if(groupRole == DemoConstant.GROUP_ROLE_ADMIN) {
            if(!GroupHelper.isInAdminList(username, group.getAdminList()) && !TextUtils.equals(username, DemoHelper.getInstance().getCurrentUser())) {
                if(GroupHelper.isInMuteList(username, listAdapter.getMuteList())) {
                    itemBean.setIcon(R.drawable.icon);
                    itemBean.setTitle(getString(R.string.group_members_dialog_menu_unmute));
                }else {
                    itemBean.setIcon(R.drawable.icon);
                    itemBean.setTitle(getString(R.string.group_members_dialog_menu_mute));
                }
                itemBean = new GroupManageItemBean();
                itemBean.setIcon(R.drawable.icon);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_block));
            }
        }
        itemBean = new GroupManageItemBean();
        itemBean.setIcon(R.drawable.icon);
        itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove));
        itemBean.setAlert(true);
    }

    private void showManageDialog() {
        new GroupMemberManageDialog.Builder(mContext)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }
                })
                .setFullWidth()
                .setFromBottomAnimation()
                .setGravity(Gravity.BOTTOM)
                .show();
    }

    protected void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            mListAdapter.setData(mMemberList);
            managersAdapter.setData(mGroupManagerList);
            sideBarContact.setVisibility(View.VISIBLE);
            srlContactRefresh.setEnabled(true);
        }else {
            List<EaseUser> easeUsers = searchContact(content, mListAdapter.getData());
            mListAdapter.setData(easeUsers);
            List<EaseUser> managerList = searchContact(content, managersAdapter.getData());
            managersAdapter.setData(managerList);
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