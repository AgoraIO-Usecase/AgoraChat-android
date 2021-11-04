package io.agora.chatdemo.me;

import android.os.Bundle;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;

public class UserAvatarSelectActivity extends BaseInitActivity {


    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_avatar_select;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new UserAvatarSelectFragment())
                    .commitNow();
        }
    }
}