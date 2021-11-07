package io.agora.chatdemo.me;

import android.view.View;

import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityAboutBinding;

public class AboutActivity extends BaseInitActivity {

    private ActivityAboutBinding mBinding;

    @Override
    protected View getContentView() {
        mBinding = ActivityAboutBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }
}