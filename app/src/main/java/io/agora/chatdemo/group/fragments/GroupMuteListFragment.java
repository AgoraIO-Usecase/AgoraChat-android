package io.agora.chatdemo.group.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupMuteListFragment extends GroupBaseManageFragment {

    @Override
    protected void initViewModel() {
        super.initViewModel();
        // User activity for the ViewModelStoreOwner, not need request data of some common methods
        viewModel = new ViewModelProvider(mContext).get(GroupMemberAuthorityViewModel.class);
        viewModel.getMuteMembersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Map<String, Long>>() {
                @Override
                public void onSuccess(@Nullable Map<String, Long> data) {
                    finishRefresh();
                    Set<String> muteList = data.keySet();
                    if(muteList != null && !muteList.isEmpty()) {
                        List<EaseUser> users = new ArrayList<>();
                        for (String username : muteList){
                            EaseUser user = DemoHelper.getInstance().getUsersManager().getUserInfo(username);
                            if(user != null) {
                                users.add(user);
                            }
                        }
                        sortData(users);
                        mDataList = users;
                        listAdapter.setData(users);
                    }else {
                        listAdapter.clearData();
                    }
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> finishRefresh());
                }
            });
        });
    }

    @Override
    public void onRefresh() {
        super.onRefresh();
        viewModel.getMuteMembers(groupId);
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
        itemBean.setIcon(R.drawable.group_manage_unmute);
        itemBean.setTitle(getString(R.string.group_members_dialog_menu_unmute));
        itemBean.setId(R.id.action_group_manage_make_unmute);
        itemBean.setUsername(username);
        data.add(itemBean);
        return data;
    }
}
