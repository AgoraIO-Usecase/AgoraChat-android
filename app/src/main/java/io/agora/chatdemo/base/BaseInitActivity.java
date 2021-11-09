package io.agora.chatdemo.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

public abstract class BaseInitActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        if(layoutId != 0) {
            setContentView(layoutId);
        }else {
            setContentView(getContentView());
        }
        initSystemFit();
        initIntent(getIntent());
        initView(savedInstanceState);
        initListener();
        initData();
    }

    protected View getContentView() {
        return null;
    }

    /**
     * get layout id
     * @return
     */
    protected int getLayoutId() {
        return 0;
    }

    protected void initSystemFit() {
        setFitSystemForTheme(true);
    }


    /**
     * init intent
     * @param intent
     */
    protected void initIntent(Intent intent) { }

    /**
     * init view
     * @param savedInstanceState
     */
    protected void initView(Bundle savedInstanceState) {

    }

    /**
     * init listener
     */
    protected void initListener() { }

    /**
     * init data
     */
    protected void initData() { }

}
