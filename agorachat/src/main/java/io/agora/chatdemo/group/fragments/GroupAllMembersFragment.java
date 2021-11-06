package io.agora.chatdemo.group.fragments;

import android.annotation.SuppressLint;
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
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
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
        viewModel.getRefreshObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    loadData();
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
                EaseUser item = managersAdapter.getItem(position);
                List<GroupManageItemBean> itemBeans = getMenuData(item.getUsername());
                if(!itemBeans.isEmpty()) {
                    showManageDialog(itemBeans, item.getNickname());
                }
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        loadData();
    }

    private void loadData() {
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
        List<GroupManageItemBean> itemBeans = getMenuData(item.getUsername());
        if(!itemBeans.isEmpty()) {
            showManageDialog(itemBeans, item.getNickname());
        }
    }

    private List<GroupManageItemBean> getMenuData(String username) {
        if(TextUtils.equals(username, DemoHelper.getInstance().getCurrentUser())) {
            return new ArrayList<>();
        }
        if(!GroupHelper.isOwner(group) && !GroupHelper.isAdmin(group)) {
            return new ArrayList<>();
        }
        List<GroupManageItemBean> data = new ArrayList<>();
        GroupManageItemBean itemBean = new GroupManageItemBean();
        if(groupRole == DemoConstant.GROUP_ROLE_OWNER) {
            // add admin/mute/block
            boolean inAdminList = GroupHelper.isInAdminList(username, group.getAdminList());
            if(inAdminList) {
                itemBean.setIcon(R.drawable.group_manage_remove_admin);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove_admin));
                itemBean.setId(R.id.action_group_manage_remove_admin);
            }else {
                itemBean.setIcon(R.drawable.group_manage_make_admin);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_make_admin));
                itemBean.setId(R.id.action_group_manage_make_admin);
            }
            itemBean.setUsername(username);
            data.add(itemBean);
            itemBean = new GroupManageItemBean();
            if(GroupHelper.isInMuteList(username, listAdapter.getMuteList())) {
                itemBean.setIcon(R.drawable.group_manage_unmute);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_unmute));
                itemBean.setId(R.id.action_group_manage_make_unmute);
            }else {
                itemBean.setIcon(R.drawable.group_manage_make_mute);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_mute));
                itemBean.setId(R.id.action_group_manage_make_mute);
            }
            itemBean.setUsername(username);
            data.add(itemBean);
            itemBean = new GroupManageItemBean();
            itemBean.setIcon(R.drawable.group_manage_make_blocked);
            itemBean.setTitle(getString(R.string.group_members_dialog_menu_block));
            itemBean.setId(R.id.action_group_manage_move_to_block);
            itemBean.setUsername(username);
            data.add(itemBean);

        }else if(groupRole == DemoConstant.GROUP_ROLE_ADMIN) {
            if(!GroupHelper.isInAdminList(username, group.getAdminList()) && !TextUtils.equals(username, DemoHelper.getInstance().getCurrentUser())) {
                if(GroupHelper.isInMuteList(username, listAdapter.getMuteList())) {
                    itemBean.setIcon(R.drawable.group_manage_unmute);
                    itemBean.setTitle(getString(R.string.group_members_dialog_menu_unmute));
                    itemBean.setId(R.id.action_group_manage_make_unmute);
                }else {
                    itemBean.setIcon(R.drawable.group_manage_make_mute);
                    itemBean.setTitle(getString(R.string.group_members_dialog_menu_mute));
                    itemBean.setId(R.id.action_group_manage_make_mute);
                }
                itemBean.setUsername(username);
                data.add(itemBean);
                itemBean = new GroupManageItemBean();
                itemBean.setIcon(R.drawable.group_manage_make_blocked);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_block));
                itemBean.setId(R.id.action_group_manage_move_to_block);
                itemBean.setUsername(username);
                data.add(itemBean);
            }
        }
        itemBean = new GroupManageItemBean();
        itemBean.setIcon(R.drawable.group_manage_remove_member);
        itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove));
        itemBean.setAlert(true);
        itemBean.setId(R.id.action_group_manage_remove_from_group);
        itemBean.setUsername(username);
        data.add(itemBean);
        return data;
    }

    private void showManageDialog(List<GroupManageItemBean> itemBeans, String nickname) {
        GroupMemberManageDialog dialog = new GroupMemberManageDialog(mContext);
        dialog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                GroupManageItemBean bean = itemBeans.get(position);
                executeAction(bean);
            }
        });
        dialog.setData(itemBeans);
        dialog.setUsername(nickname);
        dialog.init();
        new EaseAlertDialog.Builder<GroupMemberManageDialog>(mContext)
                .setCustomDialog(dialog)
                .setFullWidth()
                .setGravity(Gravity.BOTTOM)
                .setFromBottomAnimation()
                .show();
    }

    @SuppressLint("NonConstantResourceId")
    private void executeAction(GroupManageItemBean bean) {
        switch (bean.getId()) {
            case R.id.action_group_manage_make_admin:
                viewModel.addGroupAdmin(groupId, bean.getUsername());
                break;
            case R.id.action_group_manage_remove_admin:
                showRemoveAdminDialog(bean);
                break;
            case R.id.action_group_manage_make_mute:
                List<String> mutes = new ArrayList<>();
                mutes.add(bean.getUsername());
                viewModel.muteGroupMembers(groupId, mutes, 24*60*60*1000);
                break;
            case R.id.action_group_manage_make_unmute:
                List<String> unmutes = new ArrayList<>();
                unmutes.add(bean.getUsername());
                viewModel.unMuteGroupMembers(groupId, unmutes);
                break;
            case R.id.action_group_manage_move_to_block:
                showMoveToBlockedList(bean);
                break;
            case R.id.action_group_manage_remove_from_group:
                showRemoveFromGroup(bean);
                break;
        }
    }

    private void showRemoveAdminDialog(GroupManageItemBean bean) {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.group_members_action_remove_admin)
                .setOnConfirmClickListener(R.string.group_members_action_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.removeGroupAdmin(groupId, bean.getUsername());
                    }
                })
                .showCancelButton(true)
                .show();
    }

    private void showMoveToBlockedList(GroupManageItemBean bean) {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.group_members_action_move_to_block)
                .setOnConfirmClickListener(R.string.group_members_action_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.blockUser(groupId, bean.getUsername());
                    }
                })
                .showCancelButton(true)
                .show();
    }

    private void showRemoveFromGroup(GroupManageItemBean bean) {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.group_members_action_remove_from_group)
                .setOnConfirmClickListener(R.string.group_members_action_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.removeUserFromGroup(groupId, bean.getUsername());
                    }
                })
                .showCancelButton(true)
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
