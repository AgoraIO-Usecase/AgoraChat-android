package io.agora.chatdemo.group;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;

/**
 * Created by 许成谱 on 2021/10/25 0025 19:21.
 * qq:1550540124
 * 热爱生活每一天！
 */
public class GroupContainerFragment extends BaseBottomSheetFragment {

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

}
