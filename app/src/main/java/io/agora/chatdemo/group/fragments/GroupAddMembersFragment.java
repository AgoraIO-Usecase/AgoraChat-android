package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.viewmodel.GroupMemberAuthorityViewModel;

public class GroupAddMembersFragment extends NewGroupSelectContactsFragment{

    private String groupId;
    private Group group;
    private GroupMemberAuthorityViewModel authorityViewModel;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            groupId = bundle.getString("group_id");
        }
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        authorityViewModel = new ViewModelProvider(mContext).get(GroupMemberAuthorityViewModel.class);
        authorityViewModel.getAllMembersObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<List<String>>() {
                @Override
                public void onSuccess(@Nullable List<String> data) {
                    ((ContactListAdapter)mListAdapter).setGroupMemberList(data);
                    headContainer.setGroupMembers(data);
                }
            });
        });
        viewModel.addMemberObservable().observe(getViewLifecycleOwner(), response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), getString(R.string.group_invitation_members_notification), Toast.LENGTH_SHORT).show();
                            hide();
                        }
                    });
                }
            });
        });
    }

    @Override
    public int getTitleBarRightText() {
        return R.string.group_done_title;
    }

    @Override
    public boolean onTitlebarRightTextViewClick() {
        List<String> checkedList = ((ContactListAdapter) mListAdapter).getCheckedList();
        if(checkedList==null) {
            checkedList=new ArrayList<>();
        }
        viewModel.addGroupMembers(GroupHelper.isOwner(group), groupId, checkedList);
        LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
        return true;
    }
}
