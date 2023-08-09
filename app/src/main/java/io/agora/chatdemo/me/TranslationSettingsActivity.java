package io.agora.chatdemo.me;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityTranslationSettingBinding;
import io.agora.chatdemo.general.constant.DemoConstant;

public class TranslationSettingsActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener {
    private ActivityTranslationSettingBinding mBinding;

    public static void actionStart(Context context) {
        Intent starter = new Intent(context, TranslationSettingsActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected View getContentView() {
        mBinding = ActivityTranslationSettingBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setTextStyle(mBinding.titleBar.getTitle(), Typeface.BOLD);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBinding.titleBar.setOnBackPressListener(this);
        mBinding.settingTargetTranslation.setOnClickListener(this);
        mBinding.settingPushTranslation.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_target_translation:
                LanguageActivity.actionStart(mContext, DemoConstant.TRANSLATION_TYPE_MESSAGE,1);
                break;
            case R.id.setting_push_translation:
                LanguageActivity.actionStart(mContext, DemoConstant.TRANSLATION_TYPE_PUSH,3);
                break;
        }
    }
}
