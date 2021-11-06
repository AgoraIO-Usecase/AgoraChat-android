package io.agora.chatdemo.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.databinding.FragmentAboutMeBinding;
import io.agora.chatdemo.general.dialog.AlertDialog;

public class MeFragment extends BaseInitFragment implements View.OnClickListener {

    private static final int REQUEST_CODE = 12;
    private FragmentAboutMeBinding mBinding;
    private MeViewModel mViewModel;
    private AlertDialog alertDialog;

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentAboutMeBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel=new ViewModelProvider(this).get(MeViewModel.class);

    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.settingGeneral.setOnClickListener(this);
        mBinding.settingNotifications.setOnClickListener(this);
        mBinding.settingPrivacy.setOnClickListener(this);
        mBinding.settingAbout.setOnClickListener(this);
        mBinding.btnLogout.setOnClickListener(this);
        mBinding.layoutUserinfo.ivAvatar.setOnClickListener(this);
        mBinding.layoutUserinfo.tvNickname.setOnClickListener(this);
        mBinding.layoutUserinfo.tvId.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_avatar:
            case R.id.tv_nickname:
            case R.id.tv_id:
                showSettingUserInfoDialog();
                break;
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
            case R.id.tv_change_avatar:
                Intent intent=new Intent(mContext,UserAvatarSelectActivity.class);
                startActivityForResult(intent,REQUEST_CODE);
                alertDialog.dismiss();
                break;
            case R.id.tv_change_nickname:
                alertDialog.dismiss();
                break;
            case R.id.tv_copy_id:
                alertDialog.dismiss();
                break;
            case R.id.btn_cancel:
                alertDialog.dismiss();
                break;
        }

    }

    private void showSettingUserInfoDialog() {
        alertDialog = new AlertDialog.Builder(mContext)
                .setContentView(R.layout.dialog_me_setting_userinfo)
                .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .setGravity(Gravity.BOTTOM)
                .setCancelable(true)
                .setOnClickListener(R.id.btn_cancel, this)
                .setOnClickListener(R.id.tv_change_avatar, this)
                .setOnClickListener(R.id.tv_change_nickname, this)
                .setOnClickListener(R.id.tv_copy_id, this)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
