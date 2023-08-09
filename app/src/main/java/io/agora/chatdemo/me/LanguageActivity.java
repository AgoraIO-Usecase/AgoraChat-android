package io.agora.chatdemo.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.agora.ValueCallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.Language;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.me.adapter.LanguageAdapter;
import io.agora.util.EMLog;

public class LanguageActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, EaseTitleBar.OnRightClickListener {
    private EaseTitleBar titleBar;
    private RecyclerView rvList;
    private LanguageAdapter adapter;
    private int maxSelectionCount = 1;
    private int languageType;

    private List<Language> emLanguageList = new ArrayList<>();

    public static void actionStart(Context context,int type,int maxSelectionCount) {
        Intent starter = new Intent(context, LanguageActivity.class);
        starter.putExtra(DemoConstant.TRANSLATION_TYPE, type);
        starter.putExtra(DemoConstant.TRANSLATION_SELECT_MAX_COUNT, maxSelectionCount);
        context.startActivity(starter);
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        languageType = intent.getIntExtra(DemoConstant.TRANSLATION_TYPE,0);
        maxSelectionCount = intent.getIntExtra(DemoConstant.TRANSLATION_SELECT_MAX_COUNT,1);
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

        //获取微软支持的翻译语言
        ChatClient.getInstance().chatManager().fetchSupportLanguages(new ValueCallBack<List<Language>>() {
            @Override
            public void onSuccess(List<Language> value) {
                emLanguageList = value;
                adapter.refreshData(value);
                initSelectedLanguage();
            }

            @Override
            public void onError(int error, String errorMsg) {
                EMLog.e("LanguageActivity","fetchSupportLanguages: " + error + errorMsg);
            }
        });

        adapter = new LanguageAdapter(mContext,emLanguageList,maxSelectionCount);
        rvList.setLayoutManager(new LinearLayoutManager(this));
        rvList.setAdapter(adapter);

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
        String languageCode;
        if (languageType == DemoConstant.TRANSLATION_TYPE_MESSAGE){
            int selectedIndex = -1;
            languageCode = DemoHelper.getInstance().getModel().getTargetLanguage();
            for(int index = 0 ; index < emLanguageList.size(); index++) {
                Language language = emLanguageList.get(index);
                if(language.LanguageCode.equals(languageCode)) {
                    selectedIndex = index;
                    break;
                }
            }
            if (selectedIndex != -1)
                adapter.setSelectedIndex(selectedIndex,true);
        }else {
            languageCode = DemoHelper.getInstance().getModel().getPushLanguage();
            if (!TextUtils.isEmpty(languageCode)){
                try {
                    JSONArray array = new JSONArray(languageCode);
                    if (array.length() > 0){
                        for(int index = 0 ; index < emLanguageList.size(); index++) {
                            Language language = emLanguageList.get(index);
                            for (int i = 0; i < array.length(); i++) {
                                if(language.LanguageCode.equals(array.get(i))) {
                                    adapter.setSelectedIndex(index,true);
                                    break;
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateLanguage() {
        List<Integer> selectedPositions = adapter.getSelectedPositions();
        if (selectedPositions.size() > 0){
            if (languageType == DemoConstant.TRANSLATION_TYPE_MESSAGE){
                String languageCode = emLanguageList.get(selectedPositions.get(0)).LanguageCode;
                DemoHelper.getInstance().getModel().setTargetLanguage(languageCode);
            }else {
                JSONArray jsonArray = new JSONArray();
                for (Integer selectedPosition : selectedPositions) {
                    jsonArray.put(emLanguageList.get(selectedPosition).LanguageCode);
                }
                DemoHelper.getInstance().getModel().setPushLanguage(jsonArray.toString());
            }
        }else {
            if (languageType == DemoConstant.TRANSLATION_TYPE_MESSAGE){
                DemoHelper.getInstance().getModel().clearTargetLanguage();
            }else {
                DemoHelper.getInstance().getModel().clearPushLanguage();
            }
        }
    }

}
