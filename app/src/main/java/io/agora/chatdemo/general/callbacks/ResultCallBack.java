package io.agora.chatdemo.general.callbacks;


import io.agora.ValueCallBack;

public abstract class ResultCallBack<T> implements ValueCallBack<T> {

    /**
     * For situations where only error code is returned
     * @param error
     */
    public void onError(int error) {
        onError(error, null);
    }
}
