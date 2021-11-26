package io.agora.chatdemo.general.callbacks;


import io.agora.CallBack;

public abstract class DemoCallBack implements CallBack {

    @Override
    public void onError(int code, String error) {
        // do something for error
    }

    @Override
    public void onProgress(int progress, String status) {
        // do something in progress
    }
}
