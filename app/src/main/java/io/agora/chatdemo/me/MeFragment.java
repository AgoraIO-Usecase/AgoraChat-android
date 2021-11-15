package io.agora.chatdemo.me;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
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
import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.UserInfo;
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
import io.agora.chatdemo.general.manager.PreferenceManager;
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
        mViewModel.getUpdatePushNicknameObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
//                    dismissLoading();
                }

                @Override
                public void onLoading(Boolean data) {
                    super.onLoading(data);
//                    showLoading();
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
//                    dismissLoading();
                }
            });
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

        LiveDataBus.get().with(DemoConstant.CURRENT_USER_INFO_CHANGE, EaseEvent.class).observe(this, event -> {
            if (event != null) {
                mBinding.layoutUserinfo.tvNickname.setText(event.message);
            }
        });
    }

    @Override
    protected void initData() {
        super.initData();
        currentUser = DemoHelper.getInstance().getCurrentUser();
        userInfo = DemoHelper.getInstance().getUserInfoManager().getCurrentUserInfo();
        mBinding.layoutUserinfo.tvId.setText(getString(R.string.show_agora_chat_id, userInfo.getUsername()));
        mBinding.layoutUserinfo.tvNickname.setText(userInfo.getNickname());
        DemoHelper.getInstance().setUserInfo(mContext, currentUser, mBinding.layoutUserinfo.tvNickname, mBinding.layoutUserinfo.ivAvatar);
        mBinding.settingAbout.setContent("V" + DemoHelper.getInstance().getAppVersionName(mContext));
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

                break;
            case R.id.setting_notifications:

                break;
            case R.id.setting_privacy:

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
        remind = nickDialog.getViewById(R.id.tv_remind);
        edtNickName.addTextChangedListener(this);
    }

    private void changeNickName() {
        String nick = edtNickName.getText().toString().trim();
        if (nick != null && nick.length() > 0) {
            ChatClient.getInstance().userInfoManager().updateOwnInfoByAttribute(UserInfo.UserInfoType.NICKNAME, nick, new ValueCallBack<String>() {
                @Override
                public void onSuccess(String value) {
                    EMLog.d(TAG, "fetchUserInfoById :" + value);
                    showToast(R.string.nickname_update_success);
                    PreferenceManager.getInstance().setCurrentUserNick(nick);

                    EaseEvent event = EaseEvent.create(DemoConstant.CURRENT_USER_INFO_CHANGE, EaseEvent.TYPE.CONTACT);
                    event.message = nick;
                    LiveDataBus.get().with(DemoConstant.CURRENT_USER_INFO_CHANGE).postValue(event);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mViewModel.updatePushNickname(nick);
                        }
                    });
                }

                @Override
                public void onError(int error, String errorMsg) {
                    EMLog.d(TAG, "fetchUserInfoById  error:" + error + " errorMsg:" + errorMsg);
                    showToast(R.string.nickname_update_failed);
                }
            });
        } else {
            showToast(R.string.nickname_is_empty);
        }
    }

    private void logout() {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.em_login_out_hint)
                .showCancelButton(true)
                .hideConfirmButton(false)
                .setOnConfirmClickListener(R.string.em_dialog_btn_confirm, new SimpleDialog.OnConfirmClickListener() {
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
