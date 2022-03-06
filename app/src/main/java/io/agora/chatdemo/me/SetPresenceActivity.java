package io.agora.chatdemo.me;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseInitActivity;

public class SetPresenceActivity extends BaseInitActivity {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SetPresenceActivity.class);
        context.startActivity(intent);
    }
    @Override
    protected int getLayoutId() {
        return R.layout.activity_set_presence;
    }


    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        setFitSystemForTheme(true,R.color.black);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new SetPresenceFragment())
                    .commitNow();
        }
    }
}