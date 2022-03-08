package io.agora.chatdemo.conversation;

import static io.agora.chat.uikit.utils.EaseImageUtils.setDrawableSize;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.Conversation;
import io.agora.chat.Presence;
import io.agora.chat.uikit.conversation.EaseConversationListFragment;
import io.agora.chat.uikit.conversation.model.EaseConversationInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EasePresenceUtil;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EasePresenceView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chat.uikit.widget.PresenceData;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.conversation.viewmodel.PresenceViewModel;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.AlertDialog;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.UIUtils;
import io.agora.chatdemo.global.BottomSheetContainerFragment;
import io.agora.chatdemo.me.CustomPresenceActivity;

public class ConversationListFragment extends EaseConversationListFragment implements EasePresenceView.OnPresenceClickListener, View.OnClickListener {

    private EaseUser userInfo;
    private EasePresenceView presenceView;
    private PresenceViewModel viewModel;
    private AlertDialog changePresenceDialog;
    private int currentPresence;
    private TextView customView;
    private String presenceString;

    @Override
    public void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar.setRightImageResource(R.drawable.main_add_top);
        titleBar.setRightLayoutVisibility(View.VISIBLE);
        // Set toolbar's icon
        EaseImageView icon = titleBar.getIcon();
        titleBar.setIcon(R.drawable.chat_toolbar_icon);
        icon.setShapeType(0);
        ViewGroup.LayoutParams layoutParams = icon.getLayoutParams();
        layoutParams.height = (int) EaseUtils.dip2px(mContext, 20);
        layoutParams.width = (int) EaseUtils.dip2px(mContext, 65);

        presenceView = titleBar.getPresenceView();
        presenceView.setVisibility(View.VISIBLE);
        presenceView.setPresenceTextViewArrowVisiable(true);
        presenceView.setNameTextViewVisiablity(View.GONE);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) presenceView.getLayoutParams();
        params.setMargins(UIUtils.dp2px(mContext, 16), 0, 0, 0);
        presenceView.setLayoutParams(params);

        userInfo = DemoHelper.getInstance().getUsersManager().getCurrentUserInfo();
        updatePresence();
    }

    @Override
    public void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(PresenceViewModel.class);
        viewModel.getPublishObservable().observe(this, response -> {
            if (response.status == Status.SUCCESS) {
                updatePresence();
            }
        });
    }

    private void updatePresence() {
        DemoHelper.getInstance().getUsersManager().updateUserPresenceView(userInfo.getUsername(), presenceView);
    }

    @Override
    public void initListener() {
        super.initListener();
        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                new BottomSheetContainerFragment().show(getChildFragmentManager(), "ContainerFragment");
            }
        });
        LiveDataBus.get().with(DemoConstant.CONTACT_UPDATE, EaseEvent.class).observe(getViewLifecycleOwner(), event -> {
            if (event == null) {
                return;
            }
            if (event.isContactChange()) {
                refreshList();
            }
        });
        LiveDataBus.get().with(DemoConstant.PRESENCES_CHANGED).observe(getViewLifecycleOwner(), event -> {
            updatePresence();
        });

        presenceView.setOnPresenceClickListener(this);
    }

    @Override
    public void onItemClick(View view, int position) {
        super.onItemClick(view, position);
        EaseConversationInfo item = conversationListLayout.getListAdapter().getItem(position);
        if (item.getInfo() instanceof Conversation) {
            ChatActivity.actionStart(mContext, ((Conversation) item.getInfo()).conversationId(), EaseUtils.getChatType((Conversation) item.getInfo()));
        }

    }

    @Override
    public void onPresenceClick(View v) {
        showChangePresenceDialog();
    }

    private void showChangePresenceDialog() {
        if (changePresenceDialog == null) {
            changePresenceDialog = new AlertDialog.Builder(mContext)
                    .setContentView(R.layout.dialog_conversation_fg_change_presence)
                    .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setGravity(Gravity.BOTTOM)
                    .setCancelable(true)
                    .setOnClickListener(R.id.btn_cancel, this)
                    .setOnClickListener(R.id.tv_online, this)
                    .setOnClickListener(R.id.tv_busy, this)
                    .setOnClickListener(R.id.tv_not_disturb, this)
                    .setOnClickListener(R.id.tv_leave, this)
                    .setOnClickListener(R.id.tv_custom, this)
                    .show();
            initDialogIcon();
        } else {
            changePresenceDialog.show();
        }
        initDialog();
    }

    private void initDialog() {
        customView = changePresenceDialog.getViewById(R.id.tv_custom);
        Presence presence = DemoHelper.getInstance().getPresences().get(DemoHelper.getInstance().getUsersManager().getCurrentUserID());
        presenceString = EasePresenceUtil.getPresenceString(mContext, presence);
        if (TextUtils.equals(presenceString, getString(PresenceData.ONLINE.getPresence()))) {
            currentPresence = R.id.tv_online;
        } else if (TextUtils.equals(presenceString, getString(PresenceData.BUSY.getPresence()))) {
            currentPresence = R.id.tv_busy;
        } else if (TextUtils.equals(presenceString, getString(PresenceData.DO_NOT_DISTURB.getPresence()))) {
            currentPresence = R.id.tv_not_disturb;
        } else if (TextUtils.equals(presenceString, getString(PresenceData.LEAVE.getPresence()))) {
            currentPresence = R.id.tv_leave;
        } else {
            currentPresence = R.id.tv_custom;
            customView.setText(presenceString);
        }
    }

    private void initDialogIcon() {
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_online), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_busy), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_not_disturb), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_leave), UIUtils.dp2px(mContext, 20));
        setDrawableSize(changePresenceDialog.getViewById(R.id.tv_custom), UIUtils.dp2px(mContext, 20));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                changePresenceDialog.dismiss();
                break;
            case R.id.tv_online:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId());
                }else {
                    viewModel.publishPresence("");
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_online;
                break;
            case R.id.tv_busy:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId());
                }else {
                    viewModel.publishPresence(getString(PresenceData.BUSY.getPresence()));
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_busy;
                break;
            case R.id.tv_not_disturb:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId());
                }else {
                    viewModel.publishPresence(getString(PresenceData.DO_NOT_DISTURB.getPresence()));
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_not_disturb;
                break;
            case R.id.tv_leave:
                if (currentPresence == R.id.tv_custom) {
                    showConfirmDialog(v.getId());
                }else {
                    viewModel.publishPresence(getString(PresenceData.LEAVE.getPresence()));
                    changePresenceDialog.dismiss();
                }
                currentPresence = R.id.tv_leave;
                break;
            case R.id.tv_custom:
                Intent intent = new Intent(mContext, CustomPresenceActivity.class);
                startActivityForResult(intent, DemoConstant.PRESENCE_CUSTOM_REQUESTCODE_FORM_CONVERSATIONFRAGMENT);
                changePresenceDialog.dismiss();
                break;
        }


    }

    private void showConfirmDialog(int id) {
        new SimpleDialog.Builder((BaseActivity) mContext)
                .setTitle(R.string.dialog_clear_presence_title)
                .setContent(getString(R.string.dialog_clear_presence_content, customView.getText().toString().trim()))
                .showCancelButton(true)
                .hideConfirmButton(false)
                .setOnConfirmClickListener(R.string.dialog_btn_to_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        switch (id) {
                            case R.id.tv_online:
                                viewModel.publishPresence("");
                                break;
                            case R.id.tv_busy:
                                viewModel.publishPresence(getString(PresenceData.BUSY.getPresence()));
                                break;
                            case R.id.tv_not_disturb:
                                viewModel.publishPresence(getString(PresenceData.DO_NOT_DISTURB.getPresence()));
                                break;
                            case R.id.tv_leave:
                                viewModel.publishPresence(getString(PresenceData.LEAVE.getPresence()));
                                break;
                        }
                        customView.setText(R.string.ease_presence_custom);
                        changePresenceDialog.dismiss();
                    }
                })
                .setOnCancelClickListener(new SimpleDialog.onCancelClickListener() {
                    @Override
                    public void onCancelClick(View view) {
                        currentPresence = R.id.tv_custom;
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DemoConstant.PRESENCE_CUSTOM_REQUESTCODE_FORM_CONVERSATIONFRAGMENT && resultCode == DemoConstant.PRESENCE_RESULTCODE) {
            String customPresence = data.getStringExtra(DemoConstant.PRESENCE_CUSTOM);
            if (!TextUtils.isEmpty(customPresence)) {
                viewModel.publishPresence(customPresence);
            }
        }

    }
}
