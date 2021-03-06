package io.agora.chatdemo.global;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.base.BaseContainChildBottomSheetFragment;
import io.agora.chatdemo.group.fragments.BottomSheetMenuFragment;

public class BottomSheetContainerFragment extends BaseContainChildBottomSheetFragment {

    @NonNull
    @Override
    protected Fragment getChildFragment() {
        return new BottomSheetMenuFragment();
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
