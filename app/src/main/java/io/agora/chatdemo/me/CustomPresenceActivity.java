package io.agora.chatdemo.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.general.constant.DemoConstant;

public class CustomPresenceActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener, View.OnClickListener, EaseTitleBar.OnBackPressListener {

    private EaseTitleBar titleBar;
    private EditText edtCustom;
    private ImageView ivDelete;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, CustomPresenceActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_custom_presence;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setFitSystemForTheme(true,R.color.black);
        titleBar = findViewById(R.id.title_bar);
        edtCustom = findViewById(R.id.edt_custom);
        ivDelete = findViewById(R.id.iv_delete);

        titleBar.setRightTitle(getString(R.string.ease_presence_done));
        titleBar.setTitle(getString(R.string.ease_presence_custom));
        titleBar.setTitlePosition(EaseTitleBar.TitlePosition.Left);
        titleBar.setRightTitleColor(R.color.group_blue_154dfe);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnRightClickListener(this);
        titleBar.setOnBackPressListener(this);
        edtCustom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s!=null) {
                    ivDelete.setVisibility(View.VISIBLE);
                }else{
                    ivDelete.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        ivDelete.setOnClickListener(this);
    }

    @Override
    public void onRightClick(View view) {

        setResult(DemoConstant.PRESENCE_RESULTCODE,
                getIntent().putExtra(DemoConstant.PRESENCE_CUSTOM, edtCustom.getText().toString().trim()));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case  R.id.iv_delete:
                edtCustom.setText("");
                break;
        }
    }

    @Override
    public void onBackPress(View view) {
        finish();
    }
}