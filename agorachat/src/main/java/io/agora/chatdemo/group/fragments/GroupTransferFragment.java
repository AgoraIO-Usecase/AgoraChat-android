package io.agora.chatdemo.group.fragments;

import android.os.Bundle;
import android.view.View;

import io.agora.chat.uikit.interfaces.OnItemClickListener;

public class GroupTransferFragment extends GroupAllMembersFragment {
    private boolean isLeaveAfterTransfer;

    @Override
    protected void initArgument() {
        super.initArgument();
        Bundle bundle = getArguments();
        if(bundle != null) {
            isLeaveAfterTransfer = bundle.getBoolean("is_leave_after_transfer", false);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        managersAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {

    }
}
