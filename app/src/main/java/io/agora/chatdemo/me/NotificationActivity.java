package io.agora.chatdemo.me;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.agora.chat.ChatClient;
import io.agora.chat.Conversation;
import io.agora.chat.PushManager;
import io.agora.chat.SilentModeParam;
import io.agora.chat.SilentModeResult;
import io.agora.chat.SilentModeTime;
import io.agora.chat.uikit.interfaces.OnItemClickListener;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chat.uikit.widget.dialog.EaseAlertDialog;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityNotificationBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.models.DemoModel;
import io.agora.chatdemo.general.widget.SwitchItemView;
import io.agora.chatdemo.general.dialog.SelectDialog;
import io.agora.chatdemo.general.models.SelectDialogItemBean;

import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_ID;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_CHAT;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_GROUP;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_THREAD;
import static io.agora.chatdemo.general.constant.DemoConstant.DETAIL_TYPE_USER;
import static io.agora.chatdemo.general.constant.DemoConstant.SILENT_DURATION;

public class NotificationActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, SwitchItemView.OnCheckedChangeListener, View.OnClickListener {

    private ActivityNotificationBinding mBinding;
    private DemoModel mSettingsModel;
    private int detailType;
    private String detailId;
    private String mutePromptTitle;
    private String mutePromptContent;
    private MeViewModel mViewModel;
    private boolean isPopup = false;
    private String timeStr;
    private boolean viewInited = false;

    private List<SelectDialogItemBean> mNotificationSettingSelectDialogItemBeans;

    ActivityResultLauncher<Intent> intentActivityResultLauncher;

    private static final int MENU_ID_NOTIFICATION_SETTING_ALL_MESSAGE = 1;
    private static final int MENU_ID_NOTIFICATION_SETTING_ONLY_METIONS = 2;
    private static final int MENU_ID_NOTIFICATION_SETTING_NOTHING = 3;
    private static final int MENU_ID_NOTIFICATION_SETTING_DEFAULT = 4;

    public static void actionStart(Context context, int type, String id) {
        Intent starter = new Intent(context, NotificationActivity.class);
        starter.putExtra(DETAIL_TYPE, type);
        starter.putExtra(DETAIL_ID, id);
        context.startActivity(starter);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        detailType = intent.getIntExtra(DETAIL_TYPE, 0);
        detailId = intent.getStringExtra(DETAIL_ID);
    }

    @Override
    protected View getContentView() {
        mBinding = ActivityNotificationBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setTextStyle(mBinding.titleBar.getTitle(),Typeface.BOLD);
        mutePromptTitle = this.getResources().getString(R.string.notification_turn_off_mute);
        mutePromptContent = this.getResources().getString(R.string.notification_mute_until);
        mBinding.itemNotificationSetting.setContent(this.getResources().getString(R.string.notification_setting_all_message));
        mBinding.itemNotificationDoNotDisturb.setTitle(this.getResources().getString(R.string.notification_do_not_disturb));
        mBinding.itemNotificationDoNotDisturb.setContent(this.getResources().getString(R.string.notification_turn_on));
        if(detailType != DETAIL_TYPE_USER){
            mBinding.pushTitle.setVisibility(View.GONE);
            mBinding.appSettingTitle.setVisibility(View.GONE);
            mBinding.itemSwitchShowPreviewText.setVisibility(View.GONE);
            mBinding.itemSwitchAlertSound.setVisibility(View.GONE);
            mBinding.itemSwitchVibrate.setVisibility(View.GONE);
            mBinding.itemNotificationSetting.setTitle(this.getResources().getString(R.string.notification_frequency));
        }

        if(detailType == DETAIL_TYPE_THREAD){
            mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_thread));
            mBinding.itemNotificationDoNotDisturb.setTitle(this.getResources().getString(R.string.notification_mute_thread));
            mBinding.itemNotificationDoNotDisturb.setContent(this.getResources().getString(R.string.notification_mute_mute));
            mutePromptTitle = this.getResources().getString(R.string.notification_un_mute_thread);
            mutePromptContent = this.getResources().getString(R.string.notification_mute_thread_until);
        } else if(detailType == DETAIL_TYPE_GROUP){
            mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_group));
            mBinding.itemNotificationDoNotDisturb.setTitle(this.getResources().getString(R.string.notification_mute_group));
            mBinding.itemNotificationDoNotDisturb.setContent(this.getResources().getString(R.string.notification_mute_mute));
            mutePromptTitle = this.getResources().getString(R.string.notification_un_mute_group);
            mutePromptContent = this.getResources().getString(R.string.notification_mute_group_until);
        } else if(detailType == DETAIL_TYPE_CHAT){
            mBinding.titleBar.setTitle(this.getResources().getString(R.string.notification_contact));
            mBinding.itemNotificationDoNotDisturb.setTitle(this.getResources().getString(R.string.notification_mute_contact));
            mBinding.itemNotificationDoNotDisturb.setContent(this.getResources().getString(R.string.notification_mute_mute));
            mutePromptTitle = this.getResources().getString(R.string.notification_un_mute_contact);
            mutePromptContent = this.getResources().getString(R.string.notification_mute_contact_until);
        }
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
        mViewModel = new ViewModelProvider(this).get(MeViewModel.class);

        if(detailType == DETAIL_TYPE_USER){
            mBinding.itemSwitchShowPreviewText.getSwitch().setChecked(ChatClient.getInstance().pushManager().getPushConfigs().getDisplayStyle() == PushManager.DisplayStyle.MessageSummary);
            mBinding.itemSwitchAlertSound.getSwitch().setChecked(mSettingsModel.getSettingMsgSound());
            mBinding.itemSwitchVibrate.getSwitch().setChecked(mSettingsModel.getSettingMsgVibrate());
        }

        mViewModel.getFetchAllSilentModeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<SilentModeResult>() {
                @Override
                public void onSuccess(@Nullable SilentModeResult data) {
                    initSelectedView(data);
                }
            });
        });

        mViewModel.getFetchConversationSilentModeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<SilentModeResult>() {
                @Override
                public void onSuccess(@Nullable SilentModeResult data) {
                    initSelectedView(data);
                }
            });
        });

        mViewModel.getUpdatePushStyleObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {

                }

            });
        });
        mViewModel.getUpdateAllSilentModeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<SilentModeResult>() {
                @Override
                public void onSuccess(@Nullable SilentModeResult data) {
                    initSelectedView(data);
                }
            });
        });
        mViewModel.getUpdateConversationSilentModeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<SilentModeResult>() {
                @Override
                public void onSuccess(@Nullable SilentModeResult data) {
                    initSelectedView(data);
                }
            });
        });
        mViewModel.getClearConversationRemindTypeObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {

                }
            });
        });

        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getData() != null && result.getResultCode() == RESULT_OK){
                    int duration = result.getData().getIntExtra(SILENT_DURATION, 0);
                    SilentModeParam param = new SilentModeParam(SilentModeParam.SilentModeParamType.SILENT_MODE_DURATION).setSilentModeDuration(duration);
                    if(detailType == DETAIL_TYPE_USER){
                        mViewModel.updateSilentModeForAll(param);
                    } else {
                        mViewModel.updateSilentModeForConversation(detailId, detailType == DETAIL_TYPE_CHAT? Conversation.ConversationType.Chat: Conversation.ConversationType.GroupChat, param);
                    }
                }
            }
        });

        fetchSilentModeData();
        initDialogData();
        viewInited = true;
    }

    private void fetchSilentModeData(){
        if(detailType == DETAIL_TYPE_USER){
            mViewModel.fetchSilentModeForAll();
        } else {
            mViewModel.fetchSilentModeForConversation(detailId, detailType == DETAIL_TYPE_CHAT? Conversation.ConversationType.Chat: Conversation.ConversationType.GroupChat);
        }
    }

    private void initSelectedView(SilentModeResult data){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                PushManager.PushRemindType type = data.getRemindType();
                long expireTimestamp = data.getExpireTimestamp();

                if(detailType != DETAIL_TYPE_USER && !data.isConversationRemindTypeEnabled()){
                    mBinding.itemNotificationSetting.setContent(mContext.getResources().getString(R.string.notification_setting_default));
                }else {
                    if (type == PushManager.PushRemindType.ALL) {
                        mBinding.itemNotificationSetting.setContent(mContext.getResources().getString(R.string.notification_setting_all_message));
                    } else if (type == PushManager.PushRemindType.MENTION_ONLY) {
                        mBinding.itemNotificationSetting.setContent(mContext.getResources().getString(R.string.notification_setting_only_metions));
                    } else {
                        mBinding.itemNotificationSetting.setContent(mContext.getResources().getString(R.string.notification_setting_nothing));
                    }
                }

                isPopup = false;
                mBinding.itemNotificationDoNotDisturb.setContent(mContext.getResources().getString(R.string.notification_turn_on));
                if (detailType != DETAIL_TYPE_USER) {
                    mBinding.itemNotificationDoNotDisturb.getTvContent().setText(mContext.getResources().getString(R.string.notification_mute_mute));
                }
                mBinding.notDisturbTime.setText("");
                if(expireTimestamp > System.currentTimeMillis()){
                    Date date = new Date(expireTimestamp);
                    SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy/MM/dd/HH:mm");
                    String dateStr = dateFormat.format(date);
                    String[] time = dateStr.split("/");
                    String year = time[0];
                    String month = month2Eng(time[1]);
                    String day = time[2];
                    String hour = time[3];
                    timeStr = month + " " + day + ", " + year + ", " + hour;
                    mBinding.notDisturbTime.setText(String.format(mContext.getResources().getString(R.string.notification_mute_text), timeStr));
                    mBinding.itemNotificationDoNotDisturb.getTvContent().setText(mContext.getResources().getString(R.string.notification_mute_turn_off));
                    if (detailType != DETAIL_TYPE_USER) {
                        mBinding.itemNotificationDoNotDisturb.getTvContent().setText(mContext.getResources().getString(R.string.notification_mute_un_mute));
                    }
                    isPopup = true;
                }
            }
        });
    }

    private String month2Eng(String month){
        int monthNum = Integer.parseInt(month);
        String monthEng = "";
        switch (monthNum){
            case 1:
                monthEng = "Jan";
                break;
            case 2:
                monthEng = "Feb";
                break;
            case 3:
                monthEng = "Mar";
                break;
            case 4:
                monthEng = "Apr";
                break;
            case 5:
                monthEng = "May";
                break;
            case 6:
                monthEng = "Jun";
                break;
            case 7:
                monthEng = "Jul";
                break;
            case 8:
                monthEng = "Aug";
                break;
            case 9:
                monthEng = "Sept";
                break;
            case 10:
                monthEng = "Oct";
                break;
            case 11:
                monthEng = "Nov";
                break;
            case 12:
                monthEng = "Dec";
                break;
        }
        return monthEng;
    }

    private void initDialogData() {
        mNotificationSettingSelectDialogItemBeans = new ArrayList<>(3);
        SelectDialogItemBean bean;
        if(detailType != DETAIL_TYPE_USER){
            if(detailType != DETAIL_TYPE_CHAT){
                mNotificationSettingSelectDialogItemBeans = new ArrayList<>(4);
            }
            bean = new SelectDialogItemBean();
            bean.setTitle(this.getResources().getString(R.string.notification_setting_default));
            bean.setId(MENU_ID_NOTIFICATION_SETTING_DEFAULT);
            mNotificationSettingSelectDialogItemBeans.add(bean);
        }

        bean = new SelectDialogItemBean();
        bean.setTitle(this.getResources().getString(R.string.notification_setting_all_message));
        bean.setId(MENU_ID_NOTIFICATION_SETTING_ALL_MESSAGE);
        mNotificationSettingSelectDialogItemBeans.add(bean);

        if(detailType != DETAIL_TYPE_CHAT){
            bean = new SelectDialogItemBean();
            bean.setTitle(this.getResources().getString(R.string.notification_setting_only_metions));
            bean.setId(MENU_ID_NOTIFICATION_SETTING_ONLY_METIONS);
            mNotificationSettingSelectDialogItemBeans.add(bean);
        }

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
                if(viewInited){
                    mViewModel.updatePushStyle(isChecked? PushManager.DisplayStyle.MessageSummary : PushManager.DisplayStyle.SimpleBanner);
                }
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
        SilentModeParam param = new SilentModeParam(SilentModeParam.SilentModeParamType.REMIND_TYPE);
        if(bean.getId() == MENU_ID_NOTIFICATION_SETTING_DEFAULT){
            if(detailType != DETAIL_TYPE_USER){
                mViewModel.clearRemindTypeForConversation(detailId, detailType == DETAIL_TYPE_CHAT? Conversation.ConversationType.Chat:Conversation.ConversationType.GroupChat);
            }
            return;
        }
        switch (bean.getId()) {
            case MENU_ID_NOTIFICATION_SETTING_ALL_MESSAGE:
                param.setRemindType(PushManager.PushRemindType.ALL);
                break;
            case MENU_ID_NOTIFICATION_SETTING_ONLY_METIONS:
                param.setRemindType(PushManager.PushRemindType.MENTION_ONLY);
                break;
            case MENU_ID_NOTIFICATION_SETTING_NOTHING:
                param.setRemindType(PushManager.PushRemindType.NONE);
                break;
        }
        if(detailType != DETAIL_TYPE_USER){
            mViewModel.updateSilentModeForConversation(detailId, detailType == DETAIL_TYPE_CHAT? Conversation.ConversationType.Chat:Conversation.ConversationType.GroupChat, param);
        }else{
            mViewModel.updateSilentModeForAll(param);
        }
    }

    private void showDoNotDisturbSetting() {
        if(isPopup){
            new SimpleDialog.Builder(mContext)
                    .setTitle(mutePromptTitle)
                    .setContent(String.format(mutePromptContent, timeStr))
                    .showCancelButton(true)
                    .hideConfirmButton(false)
                    .setOnConfirmClickListener(R.string.dialog_btn_confirm, new SimpleDialog.OnConfirmClickListener() {
                        @Override
                        public void onConfirmClick(View view) {
                            SilentModeParam param = new SilentModeParam(SilentModeParam.SilentModeParamType.SILENT_MODE_DURATION).setSilentModeDuration(0);
                            if(detailType == DETAIL_TYPE_USER){
                                mViewModel.updateSilentModeForAll(param);
                            } else {
                                mViewModel.updateSilentModeForConversation(detailId, detailType == DETAIL_TYPE_CHAT? Conversation.ConversationType.Chat: Conversation.ConversationType.GroupChat, param);
                            }
                        }
                    })
                    .show();
        } else {
            Intent intent = new Intent(this, DoNotDisturbActivity.class);
            intent.putExtra(DETAIL_TYPE, detailType);
            intent.putExtra(DETAIL_ID, detailId);
            intentActivityResultLauncher.launch(intent);
        }
    }
}