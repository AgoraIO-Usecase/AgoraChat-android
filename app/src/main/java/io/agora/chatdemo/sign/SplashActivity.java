package io.agora.chatdemo.sign;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.main.MainActivity;
import io.agora.util.EMLog;


public class SplashActivity extends BaseActivity {

    private SplashViewModel model;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_splash);
        setFitSystemForTheme(true);
        ImageView iv_icon = findViewById(R.id.iv_icon);
        ImageView iv_brand_icon = findViewById(R.id.iv_brand_icon);

        model = new ViewModelProvider(this).get(SplashViewModel.class);
        iv_icon.animate()
                .alpha(1)
                .setDuration(500)
                .start();

        iv_brand_icon.animate()
                .alpha(1)
                .setDuration(500)
                .start();

        iv_brand_icon.postDelayed(this::loginSDK, 500);
    }

    private void loginSDK() {
        EMLog.d("splash", "loginSDK");
        // check if token expired
        long timeStamp = System.currentTimeMillis();
        if (timeStamp < DemoHelper.getInstance().getUsersManager().getTokenExpireTs()) {
            model.getLoginData().observe(this, response -> {
                parseResource(response, new OnResourceParseCallback<Boolean>(true) {
                    @Override
                    public void onSuccess(@Nullable Boolean data) {
                        EMLog.d("splash", "loginSDK onSuccess");
                        MainActivity.actionStart(mContext);
                        finish();
                    }

                    @Override
                    public void onError(int code, String message) {
                        super.onError(code, message);
                        EMLog.e("splash", "loginSDK onError");
                        SignInActivity.actionStart(mContext);
                        finish();
                    }
                });
            });
        } else {
            EMLog.d("splash", "token expired, wait for re-login in global monitor");
            MainActivity.actionStart(mContext);
            finish();
        }
    }
}
