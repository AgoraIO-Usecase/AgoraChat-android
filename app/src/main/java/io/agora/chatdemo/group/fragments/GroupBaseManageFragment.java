package io.agora.chatdemo.group.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.BaseContactListFragment;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.dialog.GroupMemberManageDialog;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupBaseManageFragment extends BaseContactListFragment<EaseUser> {
    protected GroupMemberAuthorityViewModel viewModel;
    protected ContactListAdapter listAdapter;
    protected String groupId;
    protected Group group;
    protected int groupRole;
    protected List<EaseUser> mDataList = new ArrayList<>();

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
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        etSearch.setVisibility(View.VISIBLE);
        listAdapter = (ContactListAdapter) mListAdapter;
    }

    @Override
    protected void initListener() {
        super.initListener();
        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
              listAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    protected void showManageDialog(List<GroupManageItemBean> itemBeans, String nickname) {
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

    protected List<EaseUser> searchContact(String keyword, List<EaseUser> data) {
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

    protected void sortData(List<EaseUser> data) {
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
