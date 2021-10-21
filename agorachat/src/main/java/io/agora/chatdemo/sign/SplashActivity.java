package io.agora.chatdemo.sign;

import android.content.Intent;
import android.os.Bundle;

import io.agora.chat.ChatClient;
import io.agora.chatdemo.main.MainActivity;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;

/**
 * Created by lzan13 on 2016/11/14.
 */

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_splash);
        if (ChatClient.getInstance().isSdkInited() && ChatClient.getInstance().isLoggedInBefore()) {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(SplashActivity.this, SignInActivity.class));
            finish();
        }
    }
}
