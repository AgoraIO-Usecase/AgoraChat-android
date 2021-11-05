package io.agora.chatdemo.group.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityGroupTransferBinding;
import io.agora.chatdemo.group.fragments.GroupTransferFragment;

public class GroupTransferActivity extends BaseInitActivity {

    private ActivityGroupTransferBinding binding;
    private String groupId;
    private boolean isLeaveAfterTransfer;

    public static void actionStart(Context context, String groupId) {
        actionStart(context, groupId, false);
    }

    public static void actionStart(Context context, String groupId, boolean leave) {
        Intent starter = new Intent(context, GroupTransferActivity.class);
        starter.putExtra("group_id", groupId);
        starter.putExtra("is_leave_after_transfer", leave);
        context.startActivity(starter);
    }

    @Override
    protected View getContentView() {
        binding = ActivityGroupTransferBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("group_id");
        isLeaveAfterTransfer = intent.getBooleanExtra("is_leave_after_transfer", false);
    }

    @Override
    protected void initListener() {
        super.initListener();
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
        GroupTransferFragment fragment = new GroupTransferFragment();
        Bundle bundle = new Bundle();
        bundle.putString("group_id", groupId);
        bundle.putBoolean("is_leave_after_transfer", isLeaveAfterTransfer);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(binding.fcvFragment.getId(), fragment).commit();
    }
}
