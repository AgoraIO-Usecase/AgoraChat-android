package io.agora.chatdemo.me;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityGeneralBinding;
import io.agora.chatdemo.general.models.DemoModel;
import io.agora.chatdemo.general.widget.SwitchItemView;

public class GeneralActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, SwitchItemView.OnCheckedChangeListener {

    private ActivityGeneralBinding mBinding;
    private DemoModel mSettingsModel;
    private ChatOptions mChatOptions;

    @Override
    protected View getContentView() {
        mBinding = ActivityGeneralBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.titleBar.setOnBackPressListener(this);
        mBinding.itemSwitchShowTyping.setOnCheckedChangeListener(this);
        mBinding.itemSwitchAddGroupRequest.setOnCheckedChangeListener(this);
        mBinding.itemSwitchDeleteAfterLeavingGroup.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mSettingsModel = DemoHelper.getInstance().getModel();
        mChatOptions = ChatClient.getInstance().getOptions();

        mBinding.itemSwitchShowTyping.getSwitch().setChecked(mSettingsModel.isShowMsgTyping());
        mBinding.itemSwitchAddGroupRequest.getSwitch().setChecked(mSettingsModel.isAutoAcceptGroupInvitation());
        mBinding.itemSwitchDeleteAfterLeavingGroup.getSwitch().setChecked(mSettingsModel.isDeleteMessagesAsExitGroup());
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.item_switch_show_typing:
                mSettingsModel.showMsgTyping(isChecked);
                break;
            case R.id.item_switch_add_group_request:
                mSettingsModel.setAutoAcceptGroupInvitation(isChecked);
                mChatOptions.setAutoAcceptGroupInvitation(isChecked);
                break;
            case R.id.item_switch_delete_after_leaving_group:
                mSettingsModel.setDeleteMessagesAsExitGroup(isChecked);
                mChatOptions.setDeleteMessagesAsExitGroup(isChecked);
                break;
        }
    }
}