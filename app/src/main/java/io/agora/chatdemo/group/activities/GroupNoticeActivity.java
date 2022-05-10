package io.agora.chatdemo.group.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;


import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityGroupNoticeBinding;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.EditInfoDialog;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.group.GroupHelper;

import io.agora.chatdemo.group.viewmodel.GroupDetailViewModel;

public class GroupNoticeActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener {
    private ActivityGroupNoticeBinding binding;
    private String groupId;
    private GroupDetailViewModel viewModel;
    private Group group;

    public static void actionStart(Context context, String groupId) {
        Intent starter = new Intent(context, GroupNoticeActivity.class);
        starter.putExtra("group_id", groupId);
        context.startActivity(starter);
    }

    @Override
    protected View getContentView() {
        binding = ActivityGroupNoticeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("group_id");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        binding.titleBar.setTitle(getString(R.string.group_detail_notice));
        group = ChatClient.getInstance().groupManager().getGroup(groupId);
        if (!isAllowEdit()) {
            binding.titleBar.setRightLayoutVisibility(View.GONE);
        }
    }

    @Override
    protected void initListener() {
        super.initListener();
        binding.titleBar.setOnRightClickListener(this);
        binding.titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
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
                        binding.noneNoticeTipView.setVisibility(View.VISIBLE);
                        binding.groupNotice.setVisibility(View.GONE);
                    } else {
                        binding.noneNoticeTipView.setVisibility(View.GONE);
                        binding.groupNotice.setVisibility(View.VISIBLE);
                        binding.groupNotice.setText(data);
                    }
                }
            });
        });
        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(@Nullable String data) {
                    loadData();
                }
            });
        });

        loadData();
    }

    private void loadData() {
        viewModel.getGroupAnnouncement(groupId);
    }


    @Override
    public void onRightClick(View view) {
        if (!isAllowEdit()) {
            return;
        }

        new EditInfoDialog.Builder(mContext)
                .setOnConfirmClickListener(new EditInfoDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, String content) {
                        if (!TextUtils.equals(group.getAnnouncement(), content)) {
                            viewModel.setGroupAnnouncement(groupId, content);
                            LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(new EaseEvent(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
                        }
                    }
                })
                .setContent(group.getAnnouncement())
                .setTitle(R.string.group_edit_notice_dialog_title)
                .showCancelButton(true)
                .show();
    }

    private boolean isAllowEdit() {
        // Group owner or admin
        if (GroupHelper.isOwner(group) || GroupHelper.isAdmin(group)) {
            return true;
        }
        // Private group which allow member to invite
        if (!group.isPublic() && group.isMemberAllowToInvite()) {
            return true;
        }
        return false;
    }
}
