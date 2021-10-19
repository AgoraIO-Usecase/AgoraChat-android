package io.agora.chatdemo;

import android.app.Application;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatOptions;

public class DemoApplication extends Application {
    private static DemoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        ChatOptions options = new ChatOptions();
        options.setAppKey(getString(R.string.ac_app_key));
        ChatClient.getInstance().init(this, options);
    }

    public static DemoApplication getInstance() {
        return instance;
    }
}
