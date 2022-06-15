package io.agora.chatdemo.av;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityCallInviteUsersBinding;
import io.agora.chatdemo.group.fragments.MultiplyVideoSelectMemberContainerFragment;

public class CallInviteUsersActivity extends BaseInitActivity {
    private ActivityCallInviteUsersBinding mBinding;
    private Bundle bundle;

    @Override
    protected View getContentView() {
        mBinding = ActivityCallInviteUsersBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        bundle = intent.getBundleExtra("invite_params");
       }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        MultiplyVideoSelectMemberContainerFragment fragment = new MultiplyVideoSelectMemberContainerFragment();
        fragment.setArguments(bundle);
        fragment.show(getSupportFragmentManager(), "MultiplyVideoSelectMemberContainerFragment");
    }

}