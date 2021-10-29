package io.agora.chatdemo.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseBottomSheetFragment;
import io.agora.chatdemo.base.CustomTitleBarFragment;
import io.agora.chatdemo.general.utils.CommonUtils;

public  class GroupContainerFragment extends BaseBottomSheetFragment {

    private CustomTitleBarFragment currentFragment;
    protected TextView titlebarRightText;

    @Override
    public void onViewCreated(@NonNull  View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GroupCreateFragment groupCreateFragment = new GroupCreateFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container,groupCreateFragment)
                .addToBackStack("GroupCreateFragment")
                .commit();
        setTopOffset(300);
        currentFragment=groupCreateFragment;
        currentFragment.initTitle(baseBinding.titlebar.getTitle());
        currentFragment.initRightText(titlebarRightText);
    }

    public void startFragment(@NonNull Fragment fragment,  @Nullable String tag){
        if(TextUtils.isEmpty(tag)) {
            tag=fragment.getClass().getSimpleName();
        }
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container,fragment,tag)
                .addToBackStack(tag)
                .commit();
        currentFragment= (CustomTitleBarFragment) fragment;
        if(currentFragment!=null) {
            currentFragment.initTitle(baseBinding.titlebar.getTitle());
            currentFragment.initRightText(titlebarRightText);
        }
    }

    @Override
    protected void initView() {
        super.initView();
        titlebarRightText = baseBinding.titlebar.getRightText();
        baseBinding.titlebar.setRightLayoutVisibility(View.VISIBLE);
        baseBinding.titlebar.setLeftLayoutVisibility(View.GONE);
        titlebarRightText.setTextSize(CommonUtils.getSpDimen(requireContext(), R.dimen.text_size_big));
        baseBinding.titlebar.setTitleSize(CommonUtils.getSpDimen(requireContext(),R.dimen.text_size_big_18));
    }

    @Override
    protected void initData() {
        super.initData();
        titlebarRightText.setText(R.string.cancel);
        titlebarRightText.setTextColor(ContextCompat.getColor(requireContext(), R.color.group_blue_154dfe));
        baseBinding.titlebar.setTitle(CommonUtils.getString(requireContext(),R.string.group_create_title));


    }

    @Override
    protected void initListener() {
        super.initListener();
        titlebarRightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!currentFragment.onRightTextViewClick()) {
                    hide();
                }
            }
        });
    }
}
