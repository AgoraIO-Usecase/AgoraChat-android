package io.agora.chatdemo.sign;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import io.agora.chat.ChatClient;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chat.uikit.utils.EaseUtils;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.manager.SoftKeyboardChangeHelper;
import io.agora.chatdemo.main.MainActivity;

public class SignInActivity extends BaseInitActivity {

    private TextView tv_hint;
    private EditText et_agora_id;
    private EditText et_nickname;
    private EditText et_confirm_pwd;
    private Button btn_login;
    private Button btn_register;
    private SignViewModel viewModel;
    private LinearLayout llRoot;
    private boolean isLoginModule = true;  //true Login / false register

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SignInActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sign_in;
    }

    @Override
    protected void initSystemFit() {
        setFitSystemForTheme(false, "#00000000");
        setStatusBarTextColor(false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        tv_hint = findViewById(R.id.tv_hint);
        et_agora_id = findViewById(R.id.et_agora_id);
        et_nickname = findViewById(R.id.et_nickname);
        btn_login = findViewById(R.id.btn_login);
        llRoot = findViewById(R.id.ll_root);
        et_confirm_pwd = findViewById(R.id.et_confirm_pwd);
        btn_register = findViewById(R.id.btn_register);
    }

    public void initListener() {
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
                    btn_login.setEnabled(true);
                    return;
                }
                if(!patternID(content)) {
                    setErrorHint(getString(R.string.sign_error_illegal_character));
                    btn_login.setEnabled(false);
                }else {
                    setErrorHint("");
                    btn_login.setEnabled(true);
                }

                byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                if(contentBytes.length > 64) {
                    setErrorHint(getString(R.string.username_too_long));
                    btn_login.setEnabled(false);
                }
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLoginModule){
                    loginToAgoraChat();
                }
                ChangeUI(true);
            }
        });
        et_nickname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEND && isLoginModule) {
                    loginToAgoraChat();
                    return true;
                }
                return false;
            }
        });
        SoftKeyboardChangeHelper.setOnSoftKeyboardChangeListener(this, new SoftKeyboardChangeHelper.OnSoftKeyboardChangeListener() {
            @Override
            public void keyboardShow(int height) {
                llRoot.scrollBy(0, (int) EaseUtils.dip2px(mContext, 120));
            }

            @Override
            public void keyboardHide(int height) {
                llRoot.scrollBy(0, -(int) EaseUtils.dip2px(mContext, 120));
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoginModule){
                    registerToAgoraChat();
                }
                ChangeUI(false);

            }
        });
    }

    private void loginToAgoraChat() {
        setErrorHint("");
        String agoraID = et_agora_id.getText().toString().trim();
        if(TextUtils.isEmpty(agoraID)) {
            setErrorHint(getString(R.string.sign_error_not_id));
            return;
        }
        String nickname = et_nickname.getText().toString().trim();
        if(TextUtils.isEmpty(nickname)) {
            setErrorHint(getString(R.string.sign_error_not_nickname));
            return;
        }
        btn_login.setEnabled(false);
        viewModel.login(agoraID, nickname);
    }

    public void initData() {
        if(ChatClient.getInstance().isSdkInited()) {
            EaseUser user = DemoHelper.getInstance().getUsersManager().getCurrentUserInfo();
            if(user != null && !TextUtils.isEmpty(user.getUsername())) {
                et_agora_id.setText(user.getUsername());
            }
        }
        viewModel = new ViewModelProvider(this).get(SignViewModel.class);
        viewModel.getLoginObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onLoading(@Nullable Boolean data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    dismissLoading();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> {
                        btn_login.setEnabled(true);
                        setErrorHint(message);
                    });
                }
            });
        });
        viewModel.getRegisterObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    Log.e("getRegisterObservable","onSuccess");
                    ChangeUI(true);
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    runOnUiThread(()-> {
                        ChangeUI(true);
                        setErrorHint(message);
                    });
                }
            });
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

    public void ChangeUI(Boolean module){
        // true Login / false register
        if (module){
            isLoginModule = true;
            et_confirm_pwd.setVisibility(View.GONE);
            btn_login.setVisibility(View.VISIBLE);
        }else {
            isLoginModule = false;
            et_confirm_pwd.setVisibility(View.VISIBLE);
            btn_login.setVisibility(View.GONE);
        }
    }

    public void registerToAgoraChat(){
        setErrorHint("");
        String agoraID = et_agora_id.getText().toString().trim();
        if(TextUtils.isEmpty(agoraID)) {
            setErrorHint(getString(R.string.sign_error_not_id));
            return;
        }
        String pwd = et_nickname.getText().toString().trim();
        if(TextUtils.isEmpty(pwd)) {
            setErrorHint(getString(R.string.sign_error_not_nickname));
            return;
        }
        String confirm_pwd = et_confirm_pwd.getText().toString().trim();
        if (!TextUtils.equals(pwd,confirm_pwd)){
            setErrorHint(getString(R.string.sign_check_pwd));
            return;
        }
        viewModel.register(agoraID, pwd);
    }
}
