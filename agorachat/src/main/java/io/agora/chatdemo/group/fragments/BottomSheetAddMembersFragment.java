package io.agora.chatdemo.group.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.base.BaseContainChildBottomSheetFragment;

public class BottomSheetAddMembersFragment extends BaseContainChildBottomSheetFragment {
    @NonNull
    @Override
    protected Fragment getChildFragment() {
        GroupAddMembersFragment fragment = new GroupAddMembersFragment();
        fragment.setArguments(getArguments());
        return fragment;
    }

    @Override
    protected void initListener() {
        super.initListener();
        baseBinding.titlebar.getRightText().setOnClickListener(v -> {
                    if (!currentChild.onTitlebarRightTextViewClick()) {
                        hide();
                    }
                }
        );
    }
}
