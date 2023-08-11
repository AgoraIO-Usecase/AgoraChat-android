package io.agora.chatdemo.group.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.RecyclerViewUtils;
import io.agora.chatdemo.group.dialog.EditMemberAliasDialog;
import io.agora.chatdemo.group.dialog.GroupMemberManageDialog;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.group.model.MemberAttributeBean;
import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupBaseManageFragment extends BaseContactListFragment<EaseUser> {
    protected GroupMemberAuthorityViewModel memberAuthorityViewModel;
    protected GroupDetailViewModel groupDetailViewModel;
    protected ContactListAdapter listAdapter;
    protected String groupId;
    protected Group group;
    protected boolean isPickAt;
    protected int groupRole;
    protected List<EaseUser> mDataList = new ArrayList<>();
    protected boolean isFirstMeasure = true;

    @Override
    protected void initViewModel() {
        super.initViewModel();
        // User activity for the ViewModelStoreOwner, not need request data of some common methods
        memberAuthorityViewModel = new ViewModelProvider(this).get(GroupMemberAuthorityViewModel.class);
        groupDetailViewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
    }

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            groupId = bundle.getString("group_id");
            groupRole = bundle.getInt("group_role");
            isPickAt = bundle.getBoolean("group_at",false);
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        etSearch.setVisibility(View.VISIBLE);
        listAdapter = (ContactListAdapter) mListAdapter;
        listAdapter.setGroupId(groupId);
    }

    @Override
    protected void initListener() {
        super.initListener();
        groupDetailViewModel.setMemberAttributeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Map<String, MemberAttributeBean>>() {
                @Override
                public void onSuccess(@Nullable Map<String, MemberAttributeBean> data) {
                    if (data != null) {
                        for (Map.Entry<String, MemberAttributeBean> entry : data.entrySet()) {
                            listAdapter.notifyDataSetChanged();
                            //发送通知
                            EaseEvent event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
                            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(event);
                        }
                    }
                }
            });
        });
        groupDetailViewModel.getFetchMemberAttributesObservable().observe(this,response ->{
            if(response == null) {
                return;
            }
            if(response.status == Status.SUCCESS) {
                listAdapter.notifyDataSetChanged();
            }
        });
        LiveDataBus.get().with(DemoConstant.GROUP_MEMBER_ATTRIBUTE_CHANGE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            listAdapter.notifyDataSetChanged();
        });

        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if(event == null) {
                return;
            }
            if(event.isContactChange()) {
              listAdapter.notifyDataSetChanged();
            }
        });
        listenerRecyclerViewItemFinishLayout();
    }
    private void listenerRecyclerViewItemFinishLayout() {

        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (isFirstMeasure && mRecyclerView.getLayoutManager() != null && !CollectionUtils.isEmpty(mDataList)) {
                isFirstMeasure = false;
                int[] positionArray = RecyclerViewUtils.rangeMeasurement(mRecyclerView);
                getGroupUserInfo(positionArray[0], positionArray[1]);
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int[] positionArray = RecyclerViewUtils.rangeMeasurement(recyclerView);
                    getGroupUserInfo(positionArray[0], positionArray[1]);
                }
            }
        });
    }

    public void getGroupUserInfo(int start, int end) {
        if (start <= end && end >= 0) {
            Set<String> nameSet = new HashSet<>();
            for (int i = start; i <= end; i++) {
                List<EaseUser> users = listAdapter.getData();
                if(users!=null) {
                    if (i >= 0 && i <users .size()) {
                        EaseUser easeUser = users.get(i);
                        if (easeUser != null) {
                            nameSet.add(easeUser.getUsername());
                        }
                    }
                }

            }
            Iterator<String> iterator = nameSet.iterator();
            while (iterator.hasNext()) {
                String userId = iterator.next();
                MemberAttributeBean bean = DemoHelper.getInstance().getMemberAttribute(groupId, userId);
                if (bean == null) {
                    //当从本地获取bean对象为空时 默认创建bean对象 并赋值nickName为userId
                    MemberAttributeBean emptyBean = new MemberAttributeBean();
                    emptyBean.setNickName(userId);
                    DemoHelper.getInstance().saveMemberAttribute(groupId, userId, emptyBean);
                } else {
                    iterator.remove();
                }
            }
            if (nameSet.isEmpty()) {
                return;
            }
            List<String> userIds = new ArrayList<>(nameSet);
            groupDetailViewModel.fetchGroupMemberAttribute(groupId, userIds);
        }
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
                memberAuthorityViewModel.addGroupAdmin(groupId, bean.getUsername());
                break;
            case R.id.action_group_manage_remove_admin:
                showRemoveAdminDialog(bean);
                break;
            case R.id.action_group_manage_make_mute:
                List<String> mutes = new ArrayList<>();
                mutes.add(bean.getUsername());
                memberAuthorityViewModel.muteGroupMembers(groupId, mutes, 24*60*60*1000);
                break;
            case R.id.action_group_manage_make_unmute:
                List<String> unmutes = new ArrayList<>();
                unmutes.add(bean.getUsername());
                memberAuthorityViewModel.unMuteGroupMembers(groupId, unmutes);
                break;
            case R.id.action_group_manage_move_to_block:
                showMoveToBlockedList(bean);
                break;
            case R.id.action_group_manage_remove_from_block:
                memberAuthorityViewModel.unblockUser(groupId, bean.getUsername());
                break;
            case R.id.action_group_manage_remove_from_group:
                showRemoveFromGroup(bean);
                break;
            case R.id.action_group_manage_alias:
                showEditGroupMemberAliasNameDialog(bean);
                break;
        }
    }

    private void showEditGroupMemberAliasNameDialog(GroupManageItemBean bean) {
        if(bean==null) {
            return;
        }
        //此页面获取的也是单个userId的群成员属性
        MemberAttributeBean memberAttributeBean = DemoHelper.getInstance().getMemberAttribute(groupId, bean.getUsername());
        SimpleDialog dialog = new EditMemberAliasDialog.Builder(mContext)
                .setConfirmClickListener(new EditMemberAliasDialog.ConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, String content) {
                        if (!TextUtils.equals(group.getGroupName(), content)) {
                            groupDetailViewModel.setGroupMemberAttributes(groupId,bean.getUsername(), content);
                        }
                    }
                })
                .setHint(getString(R.string.demo_admin_edit_alias_hint))
                .setContent(memberAttributeBean==null?"":memberAttributeBean.getNickName())
                .setTitle(R.string.demo_admin_edit_alias_title)
                .showCancelButton(true)
                .hideConfirmButton(false)
                .show();

    }

    private void showRemoveAdminDialog(GroupManageItemBean bean) {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.group_members_action_remove_admin)
                .setOnConfirmClickListener(R.string.group_members_action_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        memberAuthorityViewModel.removeGroupAdmin(groupId, bean.getUsername());
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
                        memberAuthorityViewModel.blockUser(groupId, bean.getUsername());
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
                        memberAuthorityViewModel.removeUserFromGroup(groupId, bean.getUsername());
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
