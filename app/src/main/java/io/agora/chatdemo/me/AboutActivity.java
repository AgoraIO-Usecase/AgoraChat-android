package io.agora.chatdemo.me;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import io.agora.chat.ChatClient;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener {

    private ActivityAboutBinding mBinding;

    @Override
    protected View getContentView() {
        mBinding = ActivityAboutBinding.inflate(getLayoutInflater());
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
        mBinding.itemPolicy.setOnClickListener(this);
        mBinding.itemMore.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        mBinding.itemSdkVersion.setContent("AgoraChat v" + ChatClient.VERSION);
        mBinding.itemLibVersion.setContent("AgoraChat v" + DemoHelper.getInstance().getAppVersionName(this));
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.item_policy:
                startToWeb("https://www.agora.io/cn/v2?utm_source=baidu&utm_medium=cpc&utm_campaign=brand");
                break;
            case  R.id.item_more:
                startToWeb("https://www.agora.io/cn/v2?utm_source=baidu&utm_medium=cpc&utm_campaign=brand");
                break;
        }
    }

    private void startToWeb(String url) {
        Uri uri = Uri.parse(url);
        Intent it = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(it);
    }
}