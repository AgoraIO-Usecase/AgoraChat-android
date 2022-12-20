package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<String> existMembers=new ArrayList<>();
    private GroupContactViewModel groupContactViewModel;
    private Set<String> finalUsers=new HashSet<>();

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if (bundle != null) {
            callType = (EaseCallType) bundle.getSerializable("easeCallType");
            groupId = bundle.getString("groupId");
            String[] existMembersArray = bundle.getStringArray("existMembers");
            existMembers.clear();
            if(existMembersArray!=null&&existMembersArray.length>0) {
                existMembers.addAll(Arrays.asList(existMembersArray));
            }
        }
    }

    @Override
    protected void initViewModel() {
        initPresenceViewModel();
        groupContactViewModel = new ViewModelProvider(mContext).get(GroupContactViewModel.class);

        groupContactViewModel.getGroupMember().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(@Nullable List<EaseUser> users) {
                    finishRefresh();
                    mData.clear();
                    for (EaseUser user : users) {
                        if(!TextUtils.equals(user.getUsername(),ChatClient.getInstance().getCurrentUser())
                        &&!existMembers.contains(user.getUsername())) {
                            mData.add(user);
                        }
                    }
                    mListAdapter.setData(mData);
                    presenceViewModel.subscribePresences(users, 7 * 24 * 60 * 60);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> finishRefresh());
                }

                @Override
                public void onLoading(@Nullable List<EaseUser> data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    dismissLoading();
                }
            });
        });
    }

    protected String getHeadName(){
        return getString(R.string.group_chat_members);
    }

    @Override
    protected void initData() {
        mRecyclerView.setAdapter(concatAdapter);
        ((ContactListAdapter) mListAdapter).setCheckModel(true);
        groupContactViewModel.getGroupMembers(groupId);
    }

    @Override
    protected void checkSearchContent(String keyword) {
        if(mData == null || mData.isEmpty()) {
            return;
        }
        if(TextUtils.isEmpty(keyword)) {
            mListAdapter.setData(mData);
        }else {
            List<EaseUser> list = new ArrayList<>();
            for (EaseUser user : mData) {
                if(user.getUsername().contains(keyword) || (!TextUtils.isEmpty(user.getNickname()) && user.getNickname().contains(keyword))) {
                    list.add(user);
                }
            }
            mListAdapter.setData(list);
        }
    }

    @Override
    public void onRefresh() {
        groupContactViewModel.getGroupMembers(groupId);
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
