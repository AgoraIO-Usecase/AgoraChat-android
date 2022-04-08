package io.agora.chatdemo.me;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityPrivacyBinding;

public class PrivacyActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener {

    private ActivityPrivacyBinding mBinding;

    @Override
    protected View getContentView() {
        mBinding = ActivityPrivacyBinding.inflate(getLayoutInflater());
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
        mBinding.itemBlockedList.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();

    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.item_blocked_list:
                intent = new Intent(mContext, ContactBlackListActivity.class);
                startActivity(intent);
        }
    }
}