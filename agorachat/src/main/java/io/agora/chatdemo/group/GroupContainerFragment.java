package io.agora.chatdemo.group;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.general.utils.CommonUtils;

public  class GroupContainerFragment extends BaseBottomSheetFragment {

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container,new GroupCreateFragment())
                .addToBackStack("GroupCreateFragment")
                .commit();
        setTopOffset(300);
    }
    @Override
    protected String getTitle() {
        return CommonUtils.getString(requireContext(),R.string.group_create_title);
    }

}
