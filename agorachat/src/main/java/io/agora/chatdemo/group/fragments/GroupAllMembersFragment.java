package io.agora.chatdemo.group.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ConcatAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupAllMembersFragment extends GroupBaseManageFragment {
    protected ContactListAdapter managersAdapter;
    private List<EaseUser> mGroupManagerList = new ArrayList<>();

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
                    finishRefresh();
                    mDataList = data;
                    listAdapter.setData(data);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> finishRefresh());
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

    protected void loadData() {
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
        boolean inAdminList = GroupHelper.isInAdminList(username, group.getAdminList());
        if(groupRole == DemoConstant.GROUP_ROLE_OWNER) {
            // add admin/mute/block
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

            if(!inAdminList) {
                itemBean = new GroupManageItemBean();
                itemBean.setIcon(R.drawable.group_manage_make_blocked);
                itemBean.setTitle(getString(R.string.group_members_dialog_menu_block));
                itemBean.setId(R.id.action_group_manage_move_to_block);
                itemBean.setUsername(username);
                data.add(itemBean);
            }

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

        if(!inAdminList) {
            itemBean = new GroupManageItemBean();
            itemBean.setIcon(R.drawable.group_manage_remove_member);
            itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove));
            itemBean.setAlert(true);
            itemBean.setId(R.id.action_group_manage_remove_from_group);
            itemBean.setUsername(username);
            data.add(itemBean);
        }
        return data;
    }

    protected void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            mListAdapter.setData(mDataList);
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

}
