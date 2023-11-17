package io.agora.chatdemo.sign;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import io.agora.chatdemo.general.interfaces.SimpleTextWatcher;
import io.agora.chatdemo.general.manager.SoftKeyboardChangeHelper;
import io.agora.chatdemo.general.utils.MyTextUtils;
import io.agora.chatdemo.main.MainActivity;
import io.agora.util.EMLog;

public class SignInActivity extends BaseInitActivity implements View.OnClickListener{

    private TextView tv_hint;
    private EditText et_agora_id;
    private EditText et_password;
    private EditText et_confirm_pwd;
    private Button btn_login;
    private TextView btn_register;
    private TextView btn_back_login;
    private SignViewModel viewModel;
    private LinearLayout llRoot;
    private boolean isLoginModule = true;  //true Login / false register
    private ImageView img_clear;
    private ImageView img_see_pwd;
    private ImageView img_confirm_pwd;
    private ImageView img_subtitle;
    private RelativeLayout confirm_layout;

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
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        llRoot = findViewById(R.id.ll_root);
        et_confirm_pwd = findViewById(R.id.et_confirm_pwd);
        btn_register = findViewById(R.id.btn_register);
        img_clear = findViewById(R.id.clear_agora_id);
        img_see_pwd = findViewById(R.id.see_pwd);
        img_confirm_pwd = findViewById(R.id.see_confirm_pwd);
        btn_back_login = findViewById(R.id.btn_back_login);
        confirm_layout = findViewById(R.id.confirm_pwd_layout);
        img_subtitle = findViewById(R.id.sub_title);

        String register_content = btn_register.getText().toString();
        SpannableStringBuilder builder = new SpannableStringBuilder(register_content);
        ForegroundColorSpan graySpan = new ForegroundColorSpan(getResources().getColor(R.color.sign_btn_bg));
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        builder.setSpan(styleSpan,register_content.length()-8,register_content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(graySpan,register_content.length()-8,register_content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        btn_register.setText(builder);
    }

    public void initListener() {
        img_clear.setOnClickListener(this);
        img_see_pwd.setOnClickListener(this);
        img_confirm_pwd.setOnClickListener(this);
        btn_register.setOnClickListener(this);
        btn_back_login.setOnClickListener(this);
        btn_login.setOnClickListener(this);

        et_agora_id.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString().trim();
                int length = MyTextUtils.getBytesLength(content);
                if(length == 0) {
                    setErrorHint("");
                    btn_login.setEnabled(true);
                    img_clear.setVisibility(View.GONE);
                    return;
                }
                img_clear.setVisibility(View.VISIBLE);
                if(!patternID(content)) {
                    setErrorHint(getString(R.string.sign_error_illegal_character));
                    btn_login.setEnabled(false);
                }else {
                    setErrorHint("");
                    btn_login.setEnabled(true);
                }
                if(length > 64) {
                    setErrorHint(getString(R.string.username_too_long));
                    btn_login.setEnabled(false);
                }
            }
        });
        et_password.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString().trim();
                int length = MyTextUtils.getBytesLength(content);
                if(length > 64) {
                    content = content.substring(0, 64);
                    int length1 = MyTextUtils.getBytesLength(content);
                    et_password.setText(content);
                }
            }
        });
        et_confirm_pwd.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString().trim();
                int length = MyTextUtils.getBytesLength(content);
                if(length > 64) {
                    content = content.substring(0, 64);
                    et_confirm_pwd.setText(content);
                }
            }
        });
        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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

    }

    private void loginToAgoraChat() {
        setErrorHint("");
        String agoraID = et_agora_id.getText().toString().trim().toLowerCase();
        if(TextUtils.isEmpty(agoraID)) {
            setErrorHint(getString(R.string.sign_error_not_id));
            return;
        }
        String password = et_password.getText().toString().trim();
        if(TextUtils.isEmpty(password)) {
            setErrorHint(getString(R.string.sign_error_not_nickname));
            return;
        }
        btn_login.setEnabled(false);
        viewModel.login(agoraID, password);
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
                    EMLog.d("SignInActivity", "observed login success, finish");
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
                    EMLog.i("getRegisterObservable","onSuccess");
                    changeUI(true);
                    showToast(R.string.sign_register_suc);
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
                        changeUI(true);
                        setErrorHint(message);
                    });
                }
            });
        });
        checkLoginStatus();
    }

    private void checkLoginStatus() {
        viewModel.checkLogin();
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

    public void changeUI(Boolean module){
        // true Login / false register
        if (module){
            isLoginModule = true;
            confirm_layout.setVisibility(View.GONE);
            btn_back_login.setVisibility(View.GONE);
            btn_login.setText(getString(R.string.sign_login));
            btn_register.setVisibility(View.VISIBLE);
            img_subtitle.setVisibility(View.GONE);
        }else {
            isLoginModule = false;
            confirm_layout.setVisibility(View.VISIBLE);
            btn_back_login.setVisibility(View.VISIBLE);
            btn_login.setText(getString(R.string.sign_up));
            btn_register.setVisibility(View.GONE);
            img_subtitle.setVisibility(View.VISIBLE);
        }
    }

    public void registerToAgoraChat(){
        setErrorHint("");
        String agoraID = et_agora_id.getText().toString().trim().toLowerCase();
        if(TextUtils.isEmpty(agoraID)) {
            setErrorHint(getString(R.string.sign_error_not_id));
            return;
        }
        String pwd = et_password.getText().toString().trim();
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_agora_id:
                et_agora_id.setText("");
                break;
            case R.id.see_pwd:
                img_see_pwd.setSelected(!img_see_pwd.isSelected());
                if (img_see_pwd.isSelected()){
                    et_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                break;
            case R.id.see_confirm_pwd:
                img_confirm_pwd.setSelected(!img_confirm_pwd.isSelected());
                if (img_confirm_pwd.isSelected()){
                    et_confirm_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }else {
                    et_confirm_pwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                break;
            case R.id.btn_register:
                changeUI(false);
                break;
            case R.id.btn_back_login:
                changeUI(true);
                break;
            case R.id.btn_login:
                if (isLoginModule){
                    loginToAgoraChat();
                }else {
                    registerToAgoraChat();
                }
                break;

        }
    }
}
