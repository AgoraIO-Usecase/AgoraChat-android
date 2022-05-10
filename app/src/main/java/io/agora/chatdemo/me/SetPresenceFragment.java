package io.agora.chatdemo.me;

import static io.agora.chat.uikit.utils.EaseImageUtils.setDrawableSize;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.Presence;
import io.agora.chat.uikit.utils.EasePresenceUtil;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chat.uikit.widget.PresenceData;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitFragment;
import io.agora.chatdemo.conversation.viewmodel.PresenceViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.chatdemo.general.utils.UIUtils;

public class SetPresenceFragment extends BaseInitFragment implements View.OnClickListener, EaseTitleBar.OnBackPressListener, RadioGroup.OnCheckedChangeListener, EaseTitleBar.OnRightClickListener {

    private RadioButton rbOnline;
    private RadioButton rbBusy;
    private RadioButton rbNotDisturb;
    private RadioButton rbLeave;
    private RadioButton rbCustom;
    private PresenceViewModel viewModel;
    private EaseTitleBar titleBar;
    private RadioGroup radioGroup;
    private int currentSelectedId = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_set_presence;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        rbOnline = findViewById(R.id.rb_online);
        rbBusy = findViewById(R.id.rb_busy);
        rbNotDisturb = findViewById(R.id.rb_not_disturb);
        rbLeave = findViewById(R.id.rb_leave);
        rbCustom = findViewById(R.id.rb_custom);
        titleBar = findViewById(R.id.title_bar);
        radioGroup = findViewById(R.id.rg_presence);
        titleBar.setRightTitle(getString(R.string.ease_presence_done));
        titleBar.setTitle(getString(R.string.ease_presence_custom));
        titleBar.setTitlePosition(EaseTitleBar.TitlePosition.Left);
        titleBar.setRightTitleColor(R.color.group_blue_154dfe);

        setDrawableSize(rbOnline, UIUtils.dp2px(mContext, 20));
        setDrawableSize(rbBusy, UIUtils.dp2px(mContext, 20));
        setDrawableSize(rbNotDisturb, UIUtils.dp2px(mContext, 20));
        setDrawableSize(rbLeave, UIUtils.dp2px(mContext, 20));
        setDrawableSize(rbCustom, UIUtils.dp2px(mContext, 20));
    }

    @Override
    protected void initViewModel() {
        super.initViewModel();
        viewModel = new ViewModelProvider(this).get(PresenceViewModel.class);
        viewModel.getPublishObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    dismissLoading();
                    mContext.finish();
                }

                @Override
                public void onLoading(@Nullable Boolean data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    dismissLoading();
                    ToastUtils.showToast(message);
                }
            });
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        titleBar.setOnRightClickListener(this);
        rbCustom.setOnClickListener(this);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_custom:
                Intent intent = new Intent(mContext, CustomPresenceActivity.class);
                startActivityForResult(intent, DemoConstant.PRESENCE_CUSTOM_REQUESTCODE_FORM_SETPRESENCEFRAGAGMENT);
                break;
        }
    }

    @Override
    protected void initData() {
        super.initData();
        Presence presence = DemoHelper.getInstance().getPresences().get(DemoHelper.getInstance().getUsersManager().getCurrentUserID());
        String presenceString = EasePresenceUtil.getPresenceString(mContext, presence);
        if (TextUtils.equals(presenceString, getString(PresenceData.ONLINE.getPresence()))) {
            radioGroup.check(R.id.rb_online);
        } else if (TextUtils.equals(presenceString, getString(PresenceData.BUSY.getPresence()))) {
            radioGroup.check(R.id.rb_busy);
        } else if (TextUtils.equals(presenceString, getString(PresenceData.DO_NOT_DISTURB.getPresence()))) {
            radioGroup.check(R.id.rb_not_disturb);
        } else if (TextUtils.equals(presenceString, getString(PresenceData.LEAVE.getPresence()))) {
            radioGroup.check(R.id.rb_leave);
        } else {
            radioGroup.check(R.id.rb_custom);
            rbCustom.setText(presenceString);
        }
    }

    @Override
    public void onBackPress(View view) {
        getActivity().finish();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        currentSelectedId = checkedId;
    }

    private void showDialog() {
        String target= getTargetString(currentSelectedId);
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.dialog_clear_presence_title)
                .setContent(getString(R.string.dialog_clear_presence_content, rbCustom.getText().toString().trim(),target))
                .showCancelButton(true)
                .hideConfirmButton(false)
                .setOnConfirmClickListener(R.string.dialog_btn_to_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        publishPresence();
                    }
                })
                .show();
    }

    private String getTargetString(int currentSelectedId) {
        switch (currentSelectedId) {
            case R.id.rb_online:
                return getString(PresenceData.ONLINE.getPresence());
            case R.id.rb_busy:
                return getString(PresenceData.BUSY.getPresence());
            case R.id.rb_not_disturb:
                return getString(PresenceData.DO_NOT_DISTURB.getPresence());
            case R.id.rb_leave:
                return getString(PresenceData.LEAVE.getPresence());
        }
        return "";
    }

    private void publishPresence() {
        switch (currentSelectedId) {
            case R.id.rb_online:
                viewModel.publishPresence("");
                break;
            case R.id.rb_busy:
                viewModel.publishPresence(getString(PresenceData.BUSY.getPresence()));
                break;
            case R.id.rb_not_disturb:
                viewModel.publishPresence(getString(PresenceData.DO_NOT_DISTURB.getPresence()));
                break;
            case R.id.rb_leave:
                viewModel.publishPresence(getString(PresenceData.LEAVE.getPresence()));
                break;
            case R.id.rb_custom:
                viewModel.publishPresence(rbCustom.getText().toString().trim());
                break;
        }
    }

    @Override
    public void onRightClick(View view) {
        if (!TextUtils.equals(rbCustom.getText(), getString(R.string.ease_presence_custom))&&currentSelectedId!=R.id.rb_custom) {
            showDialog();
        } else {
            publishPresence();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DemoConstant.PRESENCE_CUSTOM_REQUESTCODE_FORM_SETPRESENCEFRAGAGMENT && resultCode == DemoConstant.PRESENCE_RESULTCODE) {
            String customPresence = data.getStringExtra(DemoConstant.PRESENCE_CUSTOM);
            if (!TextUtils.isEmpty(customPresence)) {
                rbCustom.setText(customPresence);
            }
        }

    }
}
