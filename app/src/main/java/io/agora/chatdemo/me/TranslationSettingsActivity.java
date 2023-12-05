package io.agora.chatdemo.me;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.databinding.ActivityTranslationSettingBinding;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.widget.SwitchItemView;

public class TranslationSettingsActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener, SwitchItemView.OnCheckedChangeListener {
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
        mBinding.translationDemand.setOnCheckedChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] targetLanguage = TranslationHelper.getLanguageByType(DemoConstant.TRANSLATION_TYPE_MESSAGE, "");
        String[] pushLanguage = TranslationHelper.getLanguageByType(DemoConstant.TRANSLATION_TYPE_PUSH, "");
        boolean enable = DemoHelper.getInstance().getModel().getDemandTranslationEnable();

        if (enable){
            mBinding.translationSwitchLayout.setVisibility(View.VISIBLE);
        }else {
            mBinding.translationSwitchLayout.setVisibility(View.GONE);
        }
        mBinding.translationDemand.setChecked(enable);

        if (TextUtils.isEmpty(targetLanguage[1])){
            mBinding.settingTargetTranslation.setContent("");
            mBinding.translationSwitchLayout.setVisibility(View.GONE);
        }else {
            mBinding.settingTargetTranslation.setContent(targetLanguage[1]);
            mBinding.translationSwitchLayout.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(pushLanguage[1])){
            mBinding.settingPushTranslation.setContent("");
        }else {
            mBinding.settingPushTranslation.setContent(pushLanguage[1]);
        }
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onBackPress(View view) {
        setResult(Activity.RESULT_OK);
        onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(Activity.RESULT_OK);
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_target_translation:
                LanguageActivity.actionStart(mContext, DemoConstant.TRANSLATION_TYPE_MESSAGE,1);
                break;
            case R.id.setting_push_translation:
                LanguageActivity.actionStart(mContext, DemoConstant.TRANSLATION_TYPE_PUSH,1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.translation_demand:
                DemoHelper.getInstance().getModel().setDemandTranslationEnable(isChecked);
                break;
            default:
                break;
        }
    }
}
