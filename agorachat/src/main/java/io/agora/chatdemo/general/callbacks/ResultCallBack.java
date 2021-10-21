package io.agora.chatdemo.general.callbacks;


import io.agora.ValueCallBack;

public abstract class ResultCallBack<T> implements ValueCallBack<T> {

    /**
     * 针对只返回error code的情况
     * @param error
     */
    public void onError(int error) {
        onError(error, null);
    }
}
