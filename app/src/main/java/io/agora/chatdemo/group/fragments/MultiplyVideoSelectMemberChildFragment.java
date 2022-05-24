package io.agora.chatdemo.group.fragments;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.chatdemo.R;
import io.agora.chatdemo.contact.ContactListAdapter;
import io.agora.easecallkit.EaseCallKit;
import io.agora.easecallkit.base.EaseCallType;

public class MultiplyVideoSelectMemberChildFragment extends NewGroupSelectContactsFragment {

    private EaseCallType callType;
    private String groupId;
    private String[] existMembers;

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
    public boolean onTitlebarRightTextViewClick() {
        List<String> checkedList = ((ContactListAdapter) mListAdapter).getCheckedList();
        if (checkedList == null) {
            checkedList = new ArrayList<>();
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
