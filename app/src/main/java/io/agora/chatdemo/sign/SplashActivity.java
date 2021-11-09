package io.agora.chatdemo.sign;

import android.animation.Animator;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.main.MainActivity;


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
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        loginSDK();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .start();

        iv_brand_icon.animate()
                .alpha(1)
                .setDuration(500)
                .start();
    }

    private void loginSDK() {
        model.getLoginData().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>(true) {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    MainActivity.actionStart(mContext);
                    finish();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    SignInActivity.actionStart(mContext);
                    finish();
                }
            });
        });
    }
}
