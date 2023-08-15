package io.agora.chatdemo.group.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.model.GroupManageItemBean;

public class GroupBlockListFragment extends GroupBaseManageFragment {

    @Override
    protected void initViewModel() {
        super.initViewModel();
        memberAuthorityViewModel.getBlockObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(@Nullable List<String> data) {
                    if(data != null && !data.isEmpty()) {
                        List<EaseUser> users = new ArrayList<>();
                        for (String username : data){
                            EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(username);
                            if(user != null) {
                                users.add(user);
                            }
                        }
                        sortData(users);
                        mDataList = users;
                        listAdapter.setData(users);
                    }else {
                        mDataList = null;
                        listAdapter.clearData();
                    }
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    runOnUiThread(()->finishRefresh());
                }
            });
        });
        memberAuthorityViewModel.getRefreshObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    memberAuthorityViewModel.getBlockMembers(groupId);
                    EaseEvent easeEvent = new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
                    easeEvent.message = groupId;
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(easeEvent);
                }
            });
        });

    }

    @Override
    protected void initData() {
        super.initData();
        memberAuthorityViewModel.getBlockMembers(groupId);
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        memberAuthorityViewModel.getBlockMembers(groupId);
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
        if(TextUtils.equals(username, DemoHelper.getInstance().getUsersManager().getCurrentUserID())) {
            return new ArrayList<>();
        }
        if(!GroupHelper.isOwner(group) && !GroupHelper.isAdmin(group)) {
            return new ArrayList<>();
        }
        List<GroupManageItemBean> data = new ArrayList<>();
        GroupManageItemBean itemBean = new GroupManageItemBean();
        itemBean.setIcon(R.drawable.group_manage_unblock);
        itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove_block));
        itemBean.setId(R.id.action_group_manage_remove_from_block);
        itemBean.setUsername(username);
        data.add(itemBean);
        return data;
    }

}
