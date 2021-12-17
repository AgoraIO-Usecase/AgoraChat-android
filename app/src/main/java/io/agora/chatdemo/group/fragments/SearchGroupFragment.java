package io.agora.chatdemo.group.fragments;

import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Group;
import io.agora.chat.uikit.adapter.EaseBaseRecyclerViewAdapter;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.global.BottomSheetChildHelper;
import io.agora.chatdemo.contact.AddAdapter;
import io.agora.chatdemo.contact.SearchFragment;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.global.AddType;
import io.agora.chatdemo.group.viewmodel.SearchGroupViewModel;
import io.agora.chatdemo.group.viewmodel.GroupContactViewModel;

public class SearchGroupFragment extends SearchFragment<String> implements BottomSheetChildHelper, AddAdapter.OnItemSubViewClickListener {
    private SearchGroupViewModel viewModel;
    private List<String> allJoinedGroupIDs = new ArrayList<>();

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(SearchGroupViewModel.class);
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Group>() {
                @Override
                public void onSuccess(Group data) {
                    String reason = getString(R.string.group_listener_onRequestToJoinReceived, DemoHelper.getInstance().getUsersManager().getCurrentUserID(), data.getGroupName());
                    viewModel.joinGroup(data, reason);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    ToastUtils.showToast(message);
                }
            });
        });
        viewModel.getJoinObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    if(true) {
                        showToast(getResources().getString(R.string.group_application_send));
                    }
                }
            });
        });

        GroupContactViewModel groupViewModel = new ViewModelProvider(mContext).get(GroupContactViewModel.class);
        groupViewModel.getAllGroupsObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<Group>>() {
                @Override
                public void onSuccess(@Nullable List<Group> datas) {
                    getGroupIDs(datas);
                    ((AddAdapter) mListAdapter).setAddedDatas(allJoinedGroupIDs);
                }
            });
        });
        groupViewModel.loadAllGroups();

    }

    @Override
    protected void initListener() {
        super.initListener();
        mListAdapter.setOnItemSubViewClickListener(this);
    }

    private void getGroupIDs(List<Group> datas) {
        allJoinedGroupIDs.clear();
        for (int i = 0; i < datas.size(); i++) {
            allJoinedGroupIDs.add(datas.get(i).getGroupId());
        }
    }

    @Override
    protected void searchText(String search) {

        if (!TextUtils.isEmpty(search)) {
            if (mListAdapter.getData() != null && !mListAdapter.getData().isEmpty()) {
                mListAdapter.clearData();
            }
            mListAdapter.addData(search);
        }

    }

    @Override
    protected EaseBaseRecyclerViewAdapter<String> initAdapter() {
        return new AddAdapter(AddType.GROUP);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    @Override
    public int getTitlebarTitle() {
        return R.string.group_join_a_group;
    }

    @Override
    public boolean isShowTitlebarLeftLayout() {
        return true;
    }

    @Override
    public void onItemSubViewClick(View view, int position) {
        if (!TextUtils.isEmpty(mListAdapter.getData().get(position))) {
            viewModel.getGroup(mListAdapter.getData().get(position));
        }
    }
}
