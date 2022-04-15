package io.agora.chatdemo.me;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityNotificationBinding;
import io.agora.chatdemo.general.models.DemoModel;
import io.agora.chatdemo.general.widget.SwitchItemView;
import io.agora.chatdemo.general.dialog.SelectDialog;
import io.agora.chatdemo.general.models.SelectDialogItemBean;

public class NotificationActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, SwitchItemView.OnCheckedChangeListener, View.OnClickListener {

    private ActivityNotificationBinding mBinding;
    private DemoModel mSettingsModel;

    private List<SelectDialogItemBean> mNotificationSettingSelectDialogItemBeans;

    private static final int MENU_ID_NOTIFICATION_SETTING_ALL_MESSAGE = 1;
    private static final int MENU_ID_NOTIFICATION_SETTING_ONLY_METIONS = 2;
    private static final int MENU_ID_NOTIFICATION_SETTING_NOTHING = 3;

    @Override
    protected View getContentView() {
        mBinding = ActivityNotificationBinding.inflate(getLayoutInflater());
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

        mBinding.itemNotificationSetting.setOnClickListener(this);
        mBinding.itemNotificationDoNotDisturb.setOnClickListener(this);
        mBinding.itemSwitchShowPreviewText.setOnCheckedChangeListener(this);

        mBinding.itemSwitchAlertSound.setOnCheckedChangeListener(this);
        mBinding.itemSwitchVibrate.setOnCheckedChangeListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mSettingsModel = DemoHelper.getInstance().getModel();

        mBinding.itemNotificationSetting.setContent(this.getResources().getString(R.string.notification_setting_all_message));

        mBinding.itemSwitchAlertSound.getSwitch().setChecked(mSettingsModel.getSettingMsgSound());
        mBinding.itemSwitchVibrate.getSwitch().setChecked(mSettingsModel.getSettingMsgVibrate());

        initDialogData();
    }

    private void initDialogData() {
        mNotificationSettingSelectDialogItemBeans = new ArrayList<>(3);
        SelectDialogItemBean bean;
        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.notification_setting_all_message));
        bean.setId(MENU_ID_NOTIFICATION_SETTING_ALL_MESSAGE);
        mNotificationSettingSelectDialogItemBeans.add(bean);

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.notification_setting_only_metions));
        bean.setId(MENU_ID_NOTIFICATION_SETTING_ONLY_METIONS);
        mNotificationSettingSelectDialogItemBeans.add(bean);

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.notification_setting_nothing));
        bean.setId(MENU_ID_NOTIFICATION_SETTING_NOTHING);
        mNotificationSettingSelectDialogItemBeans.add(bean);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.item_switch_show_preview_text:
                break;
            case R.id.item_switch_alert_sound:
                mSettingsModel.setSettingMsgSound(isChecked);
                break;
            case R.id.item_switch_vibrate:
                mSettingsModel.setSettingMsgVibrate(isChecked);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.item_notification_setting:
                showNotificationSettingDialog();
                break;
            case R.id.item_notification_do_not_disturb:
                showDoNotDisturbSetting();
                break;
        }
    }

    private void showNotificationSettingDialog() {
        SelectDialog dialog = new SelectDialog(mContext);
        dialog.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                SelectDialogItemBean bean = mNotificationSettingSelectDialogItemBeans.get(position);
                executeNotificationSettingAction(bean);
            }
        });
        dialog.setData(mNotificationSettingSelectDialogItemBeans);
        dialog.init();
        new EaseAlertDialog.Builder<SelectDialog>(mContext)
                .setCustomDialog(dialog)
                .setFullWidth()
                .setGravity(Gravity.BOTTOM)
                .setFromBottomAnimation()
                .show();
    }

    private void executeNotificationSettingAction(SelectDialogItemBean bean) {
        mBinding.itemNotificationSetting.setContent(bean.getTitle());
        switch (bean.getId()) {
            case MENU_ID_NOTIFICATION_SETTING_ALL_MESSAGE:
                break;
            case MENU_ID_NOTIFICATION_SETTING_ONLY_METIONS:
                break;
            case MENU_ID_NOTIFICATION_SETTING_NOTHING:
                break;
        }
    }

    private void showDoNotDisturbSetting() {
        //TO DO
    }
}