package io.agora.chatdemo.group.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.model.GroupManageItemBean;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupAdminsFragment extends GroupBaseManageFragment {

    @Override
    protected void initViewModel() {
        super.initViewModel();
        // User activity for the ViewModelStoreOwner, not need request data of some common methods
        viewModel = new ViewModelProvider(mContext).get(GroupMemberAuthorityViewModel.class);
        viewModel.getGroupManagersObservable().observe(getViewLifecycleOwner(), response -> {
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
        if(!GroupHelper.isOwner(group) || GroupHelper.isOwner(group, username)) {
            return new ArrayList<>();
        }
        List<GroupManageItemBean> data = new ArrayList<>();
        GroupManageItemBean itemBean = new GroupManageItemBean();
        // remove admin
        itemBean.setIcon(R.drawable.group_manage_remove_admin);
        itemBean.setTitle(getString(R.string.group_members_dialog_menu_remove_admin));
        itemBean.setId(R.id.action_group_manage_remove_admin);
        itemBean.setUsername(username);
        data.add(itemBean);
        return data;
    }
}
