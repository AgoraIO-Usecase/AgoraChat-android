package io.agora.chatdemo.contact;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.base.BaseContainChildBottomSheetFragment;

public class BottomSheetForwardContactsFragment extends BaseContainChildBottomSheetFragment {
    @NonNull
    @Override
    protected Fragment getChildFragment() {
        ForwardContactsListFragment fragment = new ForwardContactsListFragment();
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
