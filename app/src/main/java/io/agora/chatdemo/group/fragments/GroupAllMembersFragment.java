package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ConcatAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.provider.EaseUserProfileProvider;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.model.GroupManageItemBean;

public class GroupAllMembersFragment extends GroupBaseManageFragment {
    protected ContactListAdapter managersAdapter;
    private List<EaseUser> mGroupManagerList = new ArrayList<>();
    private List<EaseUser> pickAtList = new ArrayList<>();
    private pickAtSelectListener listener;
    private EaseUser currentUserInfo;

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        listAdapter.setEmptyView(R.layout.ease_layout_no_data_show_nothing);
        currentUserInfo = DemoHelper.getInstance().getUsersManager().getCurrentUserInfo();
        if (isPickAt){
            listAdapter.setShowInitials(true);
            managersAdapter.setIsPickAt(true);
            if (groupRole == DemoConstant.GROUP_ROLE_OWNER || groupRole == DemoConstant.GROUP_ROLE_ADMIN){
                checkIfAddHeader();
            }
        }
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        memberAuthorityViewModel.getGroupManagersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    mGroupManagerList = data;
                    group = ChatClient.getInstance().groupManager().getGroup(groupId);
                    if (!isPickAt){
                        managersAdapter.setData(data);
                        if(group != null) {
                            managersAdapter.setOwner(group.getOwner());
                            managersAdapter.setAdminList(group.getAdminList());
                        }
                    }else {
                        EaseUserProfileProvider provider = EaseUIKit.getInstance().getUserProvider();
                        EaseUser ownerInfo = provider.getUser(group.getOwner());
                        if (data != null){
                            pickAtList.addAll(data);
                        }
                        if (currentUserInfo.getUsername().equals(ownerInfo.getUsername())){
                            pickAtList.remove(ownerInfo);
                        }
                        listAdapter.setData(pickAtList);
                    }
                }
            });
        });
        memberAuthorityViewModel.getMemberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> data) {
                    finishRefresh();
                    mDataList = data;
                    if (isPickAt){
                        if (mDataList != null && mDataList.size() > 0){
                            if (data.contains(currentUserInfo)){
                                mDataList.remove(currentUserInfo);
                            }
                            pickAtList.addAll(mDataList);
                        }
                        listAdapter.setData(pickAtList);
                    }else {
                        listAdapter.setData(data);
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> finishRefresh());
                }
            });
        });
        memberAuthorityViewModel.getMuteMembersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Map<String, Long>>() {
                @Override
                public void onSuccess(@Nullable Map<String, Long> data) {
                    listAdapter.setMuteList(new ArrayList<>(data.keySet()));
                }
            });
        });
        memberAuthorityViewModel.getRefreshObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    loadData();
                }
            });
        });
        LiveDataBus.get().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isGroupChange()) {
                loadData();
            }
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
                if(!itemBeans.isEmpty() && !isPickAt) {
                    showManageDialog(itemBeans, item.getNickname());
                }else {
                    if (listener != null){
                        listener.selectItem(item.getUsername());
                    }
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
        memberAuthorityViewModel.getGroupManagers(groupId);
        memberAuthorityViewModel.getMembers(groupId);
        pickAtList.clear();
    }

    @Override
    public void addHeader(ConcatAdapter adapter) {
        managersAdapter = new ContactListAdapter();
        managersAdapter.setGroupId(groupId);
        managersAdapter.setEmptyView(R.layout.ease_layout_no_data_show_nothing);
        adapter.addAdapter(managersAdapter);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        memberAuthorityViewModel.getGroupManagers(groupId);
        memberAuthorityViewModel.getMembers(groupId);
        pickAtList.clear();
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
        if(!itemBeans.isEmpty() && !isPickAt) {
            showManageDialog(itemBeans, item.getNickname());
        }else {
            if (listener != null){
                listener.selectItem(item.getUsername());
            }
        }
    }

    private List<GroupManageItemBean> getMenuData(String username) {
        if(TextUtils.equals(username, DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
            return new ArrayList<>();
        }
        if(GroupHelper.isOwner(group, username)) {
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
            if(!GroupHelper.isInAdminList(username, group.getAdminList()) && !TextUtils.equals(username, DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
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
        addAliasBeanFirst(data,username);
        return data;
    }

    private void addAliasBeanFirst(List<GroupManageItemBean> data, String username) {
        GroupManageItemBean itemBean = new GroupManageItemBean();
        itemBean.setIcon(R.drawable.group_manage_alias);
        itemBean.setTitle(getString(R.string.group_members_dialog_menu_alias));
        itemBean.setId(R.id.action_group_manage_alias);
        itemBean.setUsername(username);
        data.add(0,itemBean);
    }

    protected void checkSearchContent(String content) {
        if(TextUtils.isEmpty(content)) {
            if (isPickAt){
                mListAdapter.setData(pickAtList);
                AddHeader();
            }else {
                mListAdapter.setData(mDataList);
                managersAdapter.setData(mGroupManagerList);
            }
            sideBarContact.setVisibility(View.VISIBLE);
            srlContactRefresh.setEnabled(true);
        }else {
            List<EaseUser> easeUsers;
            if (isPickAt){
                easeUsers = searchContact(content, pickAtList);
            }else {
                easeUsers = searchContact(content, mListAdapter.getData());
            }
            mListAdapter.setData(easeUsers);
            List<EaseUser> managerList = searchContact(content, managersAdapter.getData());
            managersAdapter.setData(managerList);
            sideBarContact.setVisibility(View.GONE);
            srlContactRefresh.setEnabled(false);
        }
    }

    private void checkIfAddHeader() {
        Group group = DemoHelper.getInstance().getGroupManager().getGroup(groupId);
        if(group != null) {
            String owner = group.getOwner();
            if(TextUtils.equals(owner, DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
                AddHeader();
            }
        }
    }

    private void AddHeader() {
        if( managersAdapter != null) {
            EaseUser user = new EaseUser(getString(R.string.demo_pick_at_all_members));
            user.setAvatar(R.drawable.ease_groups_icon+"");
            List<EaseUser> users = new ArrayList<>();
            users.add(user);
            managersAdapter.setData(users);
        }
    }

    public interface pickAtSelectListener{
        void selectItem(String username);
    }

    public void setPickAtSelectListener(pickAtSelectListener listener){
        this.listener = listener;
    }

}
