package io.agora.chatdemo.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.agora.chat.Language;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.me.adapter.LanguageAdapter;
import io.agora.util.EMLog;

public class LanguageActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, EaseTitleBar.OnRightClickListener {
    private EaseTitleBar titleBar;
    private RecyclerView rvList;
    private LanguageAdapter adapter;
    private int maxSelectionCount = 1;
    private int languageType;
    private MeViewModel viewModel;
    private String conversationId;

    private List<Language> emLanguageList = new ArrayList<>();

    public static void actionStart(Context context,int type,int maxSelectionCount) {
        Intent starter = new Intent(context, LanguageActivity.class);
        starter.putExtra(DemoConstant.TRANSLATION_TYPE, type);
        starter.putExtra(DemoConstant.TRANSLATION_SELECT_MAX_COUNT, maxSelectionCount);
        context.startActivity(starter);
    }

    public static void actionStart(Context context,int type,int maxSelectionCount,String conversationId) {
        Intent starter = new Intent(context, LanguageActivity.class);
        starter.putExtra(DemoConstant.TRANSLATION_TYPE, type);
        starter.putExtra(DemoConstant.TRANSLATION_SELECT_MAX_COUNT, maxSelectionCount);
        starter.putExtra(DemoConstant.TRANSLATION_SELECT_CONVERSATION_ID, conversationId);
        context.startActivity(starter);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        languageType = intent.getIntExtra(DemoConstant.TRANSLATION_TYPE,0);
        maxSelectionCount = intent.getIntExtra(DemoConstant.TRANSLATION_SELECT_MAX_COUNT,1);
        if (intent.hasExtra(DemoConstant.TRANSLATION_SELECT_CONVERSATION_ID) && languageType == DemoConstant.TRANSLATION_TYPE_AUTO){
            conversationId = intent.getStringExtra(DemoConstant.TRANSLATION_SELECT_CONVERSATION_ID);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_language;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar_language);
        rvList = findViewById(R.id.language_list);

        adapter = new LanguageAdapter(mContext,emLanguageList,maxSelectionCount);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);

        if (languageType == DemoConstant.TRANSLATION_TYPE_PUSH){
            titleBar.setTitle(getResources().getString(R.string.translation_push));
        }else if (languageType == DemoConstant.TRANSLATION_TYPE_AUTO){
            titleBar.setTitle(getResources().getString(R.string.translation_auto));
        }

    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(MeViewModel.class);
        viewModel.fetchSupportLanguages();
        viewModel.getUpdatePushTranslationLanguageObservable().observe(this,response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    EMLog.d("LanguageActivity","setPushTranslationLanguage onSuccess");
                }

                @Override
                public void onError(int code, String message) {
                    EMLog.d("LanguageActivity","setPushTranslationLanguage onError" + code + " - " + message);
                }
            });
        });
        viewModel.getLanguageObservable().observe(this,response->{
            parseResource(response, new OnResourceParseCallback<List<Language>>() {
                @Override
                public void onSuccess(@Nullable List<Language> value) {
                    emLanguageList = value;
                    adapter.refreshData(value);
                    initSelectedLanguage();
                }

                @Override
                public void onError(int code, String errorMsg) {
                    EMLog.e("LanguageActivity","fetchSupportLanguages: " + code + errorMsg);
                }
            });
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        titleBar.setOnRightClickListener(this);
    }


    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }


    @Override
    public void onRightClick(View view) {
        updateLanguage();
        onBackPressed();
    }

    private void initSelectedLanguage() {
        String languageCode = "";
        int selectedIndex = -1;
        if (languageType == DemoConstant.TRANSLATION_TYPE_MESSAGE){
            languageCode = DemoHelper.getInstance().getModel().getTargetLanguage();
        }else if (languageType == DemoConstant.TRANSLATION_TYPE_AUTO){
            languageCode = DemoHelper.getInstance().getModel().getAutoTargetLanguage(conversationId);
        }else {
            languageCode = DemoHelper.getInstance().getModel().getPushLanguage();
        }
        if (!TextUtils.isEmpty(languageCode)){
            for(int index = 0 ; index < emLanguageList.size(); index++) {
                Language language = emLanguageList.get(index);
                if(language.LanguageCode.equals(languageCode)) {
                    selectedIndex = index;
                    break;
                }
            }
            if (selectedIndex != -1)
                adapter.setSelectedIndex(selectedIndex,true);
        }
    }

    private void updateLanguage() {
        List<Integer> selectedPositions = adapter.getSelectedPositions();
        if (selectedPositions.size() > 0){
            String languageCode = emLanguageList.get(selectedPositions.get(0)).LanguageCode;
            if (languageType == DemoConstant.TRANSLATION_TYPE_MESSAGE){
                DemoHelper.getInstance().getModel().setTargetLanguage(languageCode);
            }else if (languageType == DemoConstant.TRANSLATION_TYPE_AUTO){
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put(conversationId,languageCode);
                    DemoHelper.getInstance().getModel().setAutoTargetLanguage(jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                DemoHelper.getInstance().getModel().setPushLanguage(languageCode);
                viewModel.updatePushPerformLanguage(languageCode);
            }
        }else {
            if (languageType == DemoConstant.TRANSLATION_TYPE_MESSAGE){
                DemoHelper.getInstance().getModel().clearTargetLanguage();
            }else if (languageType == DemoConstant.TRANSLATION_TYPE_AUTO){
                if (!TextUtils.isEmpty(conversationId)){
                    DemoHelper.getInstance().getModel().clearAutoTargetLanguage(conversationId);
                }
            }else {
                DemoHelper.getInstance().getModel().clearPushLanguage();
            }
        }
    }

}
