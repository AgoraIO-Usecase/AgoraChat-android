package io.agora.chatdemo.sign;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.regex.Pattern;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chatdemo.main.MainActivity;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;

public class SignInActivity extends BaseActivity {

    private TextView tv_hint;
    private EditText et_agora_id;
    private EditText et_nickname;
    private Button btn_login;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initView();
        initListener();
    }

    private void initView() {
        tv_hint = findViewById(R.id.tv_hint);
        et_agora_id = findViewById(R.id.et_agora_id);
        et_nickname = findViewById(R.id.et_nickname);
        btn_login = findViewById(R.id.btn_login);
    }

    private void initListener() {
        et_agora_id.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString().trim();
                if(TextUtils.isEmpty(content)) {
                    setErrorHint("");
                    return;
                }
                if(TextUtils.isEmpty(content) && !patternID(content)) {
                    setErrorHint(getString(R.string.sign_error_illegal_character));
                }
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String agoraID = et_agora_id.getText().toString().trim();
                if(TextUtils.isEmpty(agoraID)) {
                    setErrorHint(getString(R.string.sign_error_not_id));
                    return;
                }
                String nickname = et_nickname.getText().toString().trim();
                btn_login.setEnabled(false);
                ChatClient.getInstance().login(agoraID, nickname, new CallBack() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(int code, String error) {
                        runOnUiThread(()-> {
                            btn_login.setEnabled(true);
                            setErrorHint(error);
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        });
    }

    private void setErrorHint(String error) {
        tv_hint.setText(error);
        Drawable fail = getResources().getDrawable(R.drawable.failed);
        showLeftDrawable(tv_hint, fail);
    }

    private boolean patternID(String agoraID) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_.-]+$");
        return pattern.matcher(agoraID).find();
    }

    public void showLeftDrawable(TextView editText, Drawable left) {
        String content = editText.getText().toString().trim();
        editText.setCompoundDrawablesWithIntrinsicBounds(TextUtils.isEmpty(content) ? null : left, null, null, null);
    }
}
