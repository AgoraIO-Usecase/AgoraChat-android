package io.agora.chatdemo.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.general.constant.DemoConstant;

public class CustomPresenceActivity extends BaseInitActivity implements EaseTitleBar.OnRightClickListener, View.OnClickListener, EaseTitleBar.OnBackPressListener {

    private EaseTitleBar titleBar;
    private EditText edtCustom;
    private ImageView ivDelete;
    private TextView tvCount;

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
        setFitSystemForTheme(true,R.color.white);
        titleBar = findViewById(R.id.title_bar);
        edtCustom = findViewById(R.id.edt_custom);
        ivDelete = findViewById(R.id.iv_delete);
        tvCount = findViewById(R.id.tvCount);

        titleBar.setRightTitle(getString(R.string.ease_presence_done));
        titleBar.setTitle(getString(R.string.ease_presence_custom));
        titleBar.setTitlePosition(EaseTitleBar.TitlePosition.Left);
        titleBar.setRightTitleColor(R.color.color_light_gray_999999);
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
                String content = edtCustom.getText().toString();
                if (content.length() > 0){
                    tvCount.setText(String.format(getString(R.string.report_input_count),content.length()));
                }
                checkDone();
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
                tvCount.setText(String.format(getString(R.string.report_input_count),0));
                break;
        }
    }

    @Override
    public void onBackPress(View view) {
        finish();
    }

    private void checkDone(){
        if (TextUtils.equals(edtCustom.getText(),getString(R.string.report_input_hint_count))
                || TextUtils.isEmpty(edtCustom.getText())){
            titleBar.setRightTitleColor(R.color.color_light_gray_999999);
            titleBar.getRightText().setEnabled(false);
        }else {
            titleBar.setRightTitleColor(R.color.color_main_blue);
            titleBar.getRightText().setEnabled(true);
        }
    }
}