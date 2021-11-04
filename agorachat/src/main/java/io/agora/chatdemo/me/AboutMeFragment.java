package io.agora.chatdemo.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.databinding.FragmentAboutMeBinding;

public class AboutMeFragment extends BaseInitFragment implements View.OnClickListener {

    private FragmentAboutMeBinding mBinding;

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentAboutMeBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.settingGeneral.setOnClickListener(this);
        mBinding.settingNotifications.setOnClickListener(this);
        mBinding.settingPrivacy.setOnClickListener(this);
        mBinding.settingAbout.setOnClickListener(this);
        mBinding.btnLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_general:

                break;
            case R.id.setting_notifications:

                break;
            case R.id.setting_privacy:

                break;
            case R.id.setting_about:

                break;
            case R.id.btn_logout:

                break;
        }

    }
}
