package io.agora.chatdemo.group.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.chat.ChatActivity;
import io.agora.chatdemo.databinding.ActivityGroupDetailBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.AlertDialog;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.utils.CommonUtils;
import io.agora.chatdemo.group.GroupHelper;
import io.agora.chatdemo.group.dialog.DisbandGroupDialog;
import io.agora.chatdemo.general.dialog.EditInfoDialog;
import io.agora.chatdemo.group.dialog.EditGroupInfoDialog;
import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;

public class GroupDetailActivity extends BaseInitActivity implements View.OnClickListener {

    private String groupId;
    private Group group;
    protected ActivityGroupDetailBinding binding;
    private GroupDetailViewModel viewModel;
    private boolean fromChat;
    private AlertDialog dialog;

    public static void actionStart(Context context, String groupId) {
        actionStart(context, groupId, false);
    }

    public static void actionStart(Context context, String groupId, boolean isFromChat) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra("group_id", groupId);
        intent.putExtra("from_chat", isFromChat);
        context.startActivity(intent);
    }

    @Override
    protected View getContentView() {
        binding = ActivityGroupDetailBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = getIntent().getStringExtra("group_id");
        fromChat = getIntent().getBooleanExtra("from_chat", false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        binding.itemGroupTransfer.setVisibility(View.GONE);
        binding.itemDisbandGroup.setVisibility(View.GONE);
        binding.itemLeaveGroup.setVisibility(View.GONE);
        binding.itemGroupNotice.setVisibility(View.VISIBLE);
        binding.itemGroupFiles.setVisibility(View.VISIBLE);
        if (fromChat) {
            binding.includeInfo.ivChat.setVisibility(View.GONE);
            binding.includeInfo.tvChat.setVisibility(View.GONE);
        }
        setGroupView();
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.includeInfo.getRoot().setOnClickListener(this);
        binding.itemGroupMembers.setOnClickListener(this);
        binding.itemGroupNotice.setOnClickListener(this);
        binding.itemGroupFiles.setOnClickListener(this);
        binding.itemGroupTransfer.setOnClickListener(this);
        binding.itemLeaveGroup.setOnClickListener(this);
        binding.itemDisbandGroup.setOnClickListener(this);
        binding.toolbarGroupDetail.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        binding.includeInfo.ivChat.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.include_info:
                showEditDialog();
                break;
            case R.id.item_group_members:
                skipToMemberList();
                break;
            case R.id.item_group_notice:
                skipToNotice();
                break;
            case R.id.item_group_files:
                skipToFiles();
                break;
            case R.id.item_group_transfer:
                skipToTransfer(false);
                break;
            case R.id.item_leave_group:
                leaveGroup();
                break;
            case R.id.item_disband_group:
                disbandGroup();
                break;
            case R.id.iv_chat:
                ChatActivity.actionStart(mContext, groupId, DemoConstant.CHATTYPE_GROUP);
                break;
        }
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getAnnouncementObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    if (TextUtils.isEmpty(data)) {
                        binding.tvNotice.setVisibility(View.GONE);
                    } else {
                        binding.tvNotice.setVisibility(View.VISIBLE);
                        binding.tvNotice.setText(data);
                    }
                }
            });
        });
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Group>() {
                @Override
                public void onSuccess(@Nullable Group data) {
                    group = data;
                    setGroupView();
                }
            });
        });
        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    loadGroup();
                }
            });
        });
        viewModel.getLeaveGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    finish();
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_LEAVE, EaseEvent.TYPE.GROUP, groupId));
                }
            });
        });
        LiveDataBus.get().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event.isGroupLeave() && TextUtils.equals(groupId, event.message)) {
                finish();
                return;
            }
            if(event.isGroupChange()) {
                loadGroup();
            }
        });
        loadGroup();
    }

    private void loadGroup() {
        viewModel.getGroup(groupId);
        viewModel.getGroupAnnouncement(groupId);
    }

    private void setGroupView() {
        if(group == null) {
            return;
        }
        if(GroupHelper.isOwner(group)) {
            binding.itemGroupTransfer.setVisibility(View.VISIBLE);
            binding.itemDisbandGroup.setVisibility(View.VISIBLE);
            binding.itemLeaveGroup.setVisibility(View.GONE);
        }else if(GroupHelper.isAdmin(group)) {
            binding.itemGroupTransfer.setVisibility(View.GONE);
            binding.itemLeaveGroup.setVisibility(View.VISIBLE);
            binding.itemDisbandGroup.setVisibility(View.GONE);
        }else {
            binding.itemGroupTransfer.setVisibility(View.GONE);
            binding.itemLeaveGroup.setVisibility(View.VISIBLE);
            binding.itemDisbandGroup.setVisibility(View.GONE);
        }

        boolean hasProvided = DemoHelper.getInstance().setGroupInfo(mContext, groupId, binding.includeInfo.tvName, binding.includeInfo.ivAvatar);
        if(!hasProvided) {
            setGroupInfo();
        }
        binding.includeInfo.tvId.setText(getString(R.string.show_agora_chat_id, groupId));
        if(!TextUtils.isEmpty(group.getDescription())) {
            binding.includeInfo.tvDescription.setVisibility(View.VISIBLE);
            binding.includeInfo.tvDescription.setText(group.getDescription());
        }

        binding.itemGroupMembers.setContent(String.valueOf(group.getMemberCount()));
    }

    private void setGroupInfo() {
        String title = GroupHelper.getGroupName(groupId);
        binding.includeInfo.tvName.setText(title);
    }

    private void showEditDialog() {
        if(GroupHelper.isOwner(group) || GroupHelper.isAdmin(group)) {
            dialog = new AlertDialog.Builder(mContext)
                    .setContentView(R.layout.dialog_group_info_edit)
                    .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .setGravity(Gravity.BOTTOM)
                    .setCancelable(true)
                    .setOnClickListener(R.id.btn_cancel, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    })
                    .setOnClickListener(R.id.tv_change_group_name, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            showModifyGroupNameDialog();
                        }
                    })
                    .setOnClickListener(R.id.tv_change_description, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showModifyGroupDescriptionDialog();
                            dialog.dismiss();
                        }
                    })
                    .setOnClickListener(R.id.tv_copy_id, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            CommonUtils.copyContentToClipboard(mContext, groupId);
                        }
                    })
                    .show();
        }
    }

    private void showModifyGroupNameDialog() {
        new EditGroupInfoDialog.Builder(mContext)
                .setConfirmClickListener(new EditGroupInfoDialog.ConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, String content) {
                        if(!TextUtils.equals(group.getGroupName(), content)) {
                            viewModel.setGroupName(groupId, content);
                            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP ));
                        }
                    }
                })
                .setHint(getString(R.string.group_detail_chang_group_name_dialog_hint))
                .setContent(group.getGroupName())
                .setTitle(R.string.group_detail_chang_group_name_dialog_title)
                .showCancelButton(true)
                .show();
    }

    private void showModifyGroupDescriptionDialog() {
        new EditInfoDialog.Builder(mContext)
                .setOnConfirmClickListener(new EditInfoDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, String content) {
                        if (!TextUtils.equals(group.getGroupName(), content)) {
                            viewModel.setGroupDescription(groupId, content);
                            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
                        }
                    }
                })
                .setContent(group.getDescription())
                .setTitle(R.string.group_detail_change_description_dialog_title)
                .showCancelButton(true)
                .show();
    }

    protected void skipToMemberList() {
        GroupMembersActivity.actionStart(mContext, groupId);
    }

    private void skipToNotice() {
        GroupNoticeActivity.actionStart(mContext, groupId);
    }

    private void skipToFiles() {
        GroupFilesActivity.actionStart(mContext, groupId);
    }

    private void skipToTransfer(boolean leave) {
        if(GroupHelper.isOwner(group)) {
            // Skip to transfer activity
            GroupTransferActivity.actionStart(mContext, groupId, leave);
        }
    }

    private void leaveGroup() {
        new SimpleDialog.Builder(mContext)
                .setTitle(R.string.group_detail_leave_hint_title)
                .setContent(R.string.group_detail_leave_hint_content)
                .setOnConfirmClickListener(R.string.group_detail_leave_hint_confirm, new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.leaveGroup(groupId);
                    }
                })
                .setConfirmColor(R.color.color_alert)
                .showCancelButton(true)
                .show();
    }

    private void disbandGroup() {
        new DisbandGroupDialog.Builder(mContext)
                .setOnTransferClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // skip to transfer
                        skipToTransfer(true);
                    }
                })
                .setTitle(R.string.group_detail_disband_hint_title)
                .setContent(R.string.group_detail_disband_hint_content)
                .setOnConfirmClickListener(new SimpleDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        // disband group
                        viewModel.destroyGroup(groupId);
                    }
                })
                .setOnCancelClickListener(new SimpleDialog.onCancelClickListener() {
                    @Override
                    public void onCancelClick(View view) {
                        // do nothing
                    }
                })
                .show();
    }
}
