package io.agora.chatdemo.me;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import io.agora.CallBack;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.databinding.FragmentAboutMeBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.AlertDialog;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.CommonUtils;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.sign.SignInActivity;
import io.agora.util.EMLog;

public class MeFragment extends BaseInitFragment implements View.OnClickListener, TextWatcher {

    private static final int REQUEST_CODE = 12;
    private FragmentAboutMeBinding mBinding;
    private MeViewModel mViewModel;
    private AlertDialog settingUserInfoDialog;
    private String currentUser;
    private EditText edtNickName;
    private TextView remind;
    private AlertDialog nickDialog;
    private String TAG = getClass().getSimpleName();
    private EaseUser userInfo;

    @Override
    protected View getContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentAboutMeBinding.inflate(inflater);
        return mBinding.getRoot();
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        mViewModel = new ViewModelProvider(this).get(MeViewModel.class);
        mViewModel.getUpdateNicknameObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EaseUser>() {
                @Override
                public void onSuccess(EaseUser data) {
                    setUserInfo();
                }

            });
        });
        LiveDataBus.get().with(DemoConstant.CURRENT_USER_INFO_CHANGE, EaseEvent.class).observe(mContext, event -> {
            if (event != null) {
                EMLog.e(TAG, "receive CURRENT_USER_INFO_CHANGE");
                setUserInfo();
            }
        });
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
        mBinding.settingAbout.setContent("AgoraChat v" + DemoHelper.getInstance().getAppVersionName(mContext));
        setUserInfo();
    }

    private void setUserInfo() {
        currentUser = DemoHelper.getInstance().getUsersManager().getCurrentUserID();
        userInfo = DemoHelper.getInstance().getUsersManager().getCurrentUserInfo();
        mBinding.layoutUserinfo.tvId.setText(getString(R.string.show_agora_chat_id, userInfo.getUsername()));
        DemoHelper.getInstance().getUsersManager().setUserInfo(mContext, currentUser, mBinding.layoutUserinfo.tvNickname, mBinding.layoutUserinfo.ivAvatar);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.iv_avatar:
            case R.id.tv_nickname:
            case R.id.tv_id:
                showSettingUserInfoDialog();
                break;
            case R.id.setting_general:
                intent = new Intent(mContext, GeneralActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_notifications:
                intent = new Intent(mContext, NotificationActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_privacy:
                intent = new Intent(mContext, PrivacyActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_about:
                intent = new Intent(mContext, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_logout:
                logout();
                break;
            case R.id.tv_change_avatar:
                intent = new Intent(mContext, UserAvatarSelectActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                settingUserInfoDialog.dismiss();
                break;
            case R.id.tv_change_nickname:
                showChangeNickNameDialog();
                settingUserInfoDialog.dismiss();
                break;
            case R.id.tv_copy_id:
                CommonUtils.copyContentToClipboard(mContext, currentUser);
                settingUserInfoDialog.dismiss();
                showToast(R.string.me_copy_success);
                break;
            case R.id.btn_cancel:
                settingUserInfoDialog.dismiss();
                break;
            case R.id.btn_dialog_cancel:
                nickDialog.dismiss();
                break;
            case R.id.btn_dialog_confirm:
                changeNickName();
                nickDialog.dismiss();
                break;
            case R.id.iv_nickname_delete:
                edtNickName.setText("");
                break;
        }

    }

    private void showChangeNickNameDialog() {
        nickDialog = new AlertDialog.Builder(mContext)
                .setContentView(R.layout.dialog_change_nickname)
                .setLayoutParams(UIUtils.dp2px(mContext, 270), ViewGroup.LayoutParams.WRAP_CONTENT)
                .setOnClickListener(R.id.btn_dialog_cancel, this)
                .setOnClickListener(R.id.btn_dialog_confirm, this)
                .setOnClickListener(R.id.iv_nickname_delete, this)
                .show();
        edtNickName = nickDialog.getViewById(R.id.edt_target_nickname);
        edtNickName.setText(userInfo.getNickname());
        remind = nickDialog.getViewById(R.id.tv_remind);
        edtNickName.addTextChangedListener(this);
    }

    private void changeNickName() {
        String nick = edtNickName.getText().toString().trim();
        if(!TextUtils.isEmpty(nick)) {
            mViewModel.updateNickname(nick);
        }else {
            showToast(R.string.nickname_is_empty);
        }
    }

    private void logout() {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.login_out_hint)
                .showCancelButton(true)
                .hideConfirmButton(false)
                .setOnConfirmClickListener(R.string.dialog_btn_to_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        DemoHelper.getInstance().logout(true, new CallBack() {
                            @Override
                            public void onSuccess() {
                                SignInActivity.actionStart(mContext);
                                mContext.finish();
                            }

                            @Override
                            public void onError(int code, String error) {
                                EaseThreadManager.getInstance().runOnMainThread(() -> showToast(error));
                            }

                            @Override
                            public void onProgress(int progress, String status) {

                            }
                        });
                    }
                })
                .show();
    }

    private void showSettingUserInfoDialog() {
        settingUserInfoDialog = new AlertDialog.Builder(mContext)
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE && resultCode == RESULT_OK)) {
            if (data != null) {
                int headImg = data.getIntExtra("headImage", R.drawable.ease_default_avatar);
                Glide.with(mContext).load(headImg).placeholder(R.drawable.ease_default_avatar).into(mBinding.layoutUserinfo.ivAvatar);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
