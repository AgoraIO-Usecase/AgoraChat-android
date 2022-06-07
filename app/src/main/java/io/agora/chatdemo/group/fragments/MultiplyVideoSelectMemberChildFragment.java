package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agora.chat.ChatClient;
import io.agora.chat.callkit.EaseCallKit;
import io.agora.chat.callkit.general.EaseCallType;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.group.viewmodel.GroupContactViewModel;

public class MultiplyVideoSelectMemberChildFragment extends NewGroupSelectContactsFragment {

    private EaseCallType callType;
    private String groupId;
    private String[] existMembers;
    private GroupContactViewModel viewModel;
    private Set<String> finalUsers=new HashSet<>();

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if (bundle != null) {
            callType = (EaseCallType) bundle.getSerializable("easeCallType");
            groupId = bundle.getString("groupId");
            existMembers = bundle.getStringArray("existMembers");
        }
    }

    @Override
    protected void initViewModel() {
        initPresenceViewModel();
        viewModel = new ViewModelProvider(mContext).get(GroupContactViewModel.class);

        viewModel.getGroupMember().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> users) {
                    finishRefresh();
                    mData.clear();
                    for (EaseUser user : users) {
                        if(!TextUtils.equals(user.getUsername(),ChatClient.getInstance().getCurrentUser())) {
                            mData.add(user);
                        }
                    }
                    presenceViewModel.subscribePresences(users, 7 * 24 * 60 * 60);
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
    protected void initData() {
        mRecyclerView.setAdapter(concatAdapter);
        ((ContactListAdapter) mListAdapter).setCheckModel(true);
        viewModel.getGroupMembers(groupId);
    }

    @Override
    public void onRefresh() {
        viewModel.getGroupMembers(groupId);
    }

    @Override
    public boolean onTitlebarRightTextViewClick() {
        finalUsers.clear();
        List<String> checkedList = ((ContactListAdapter) mListAdapter).getCheckedList();
        if (checkedList == null) {
            checkedList = new ArrayList<>();
        }
        for (String user : checkedList) {
            finalUsers.add(user);
        }
        if(existMembers!=null) {
            for (String existMember : existMembers) {
                finalUsers.add(existMember);
            }
        }
        if(finalUsers.size()>EaseCallKit.getInstance().getLargestNumInChannel()-1) {
            ToastUtils.showToast(getString(R.string.ease_call_max_people_in_channel));
            return true;
        }
        Map<String, Object> ext = new HashMap<>();
        ext.put("groupId", groupId);
        EaseCallKit.getInstance().startInviteMultipleCall(callType, checkedList.toArray(new String[checkedList.size()]), ext);
        getActivity().finish();
        return true;
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.group_done_title;
    }

    @Override
    public int getTitlebarTitle() {
        return R.string.demo_multiply_video_select_members;
    }

}
