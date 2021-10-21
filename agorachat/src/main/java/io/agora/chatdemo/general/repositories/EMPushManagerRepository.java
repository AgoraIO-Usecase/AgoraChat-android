package io.agora.chatdemo.general.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.PushConfigs;
import io.agora.chat.PushManager;
import io.agora.chat.uikit.manager.EaseThreadManager;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.net.Resource;
import io.agora.exceptions.ChatException;


public class EMPushManagerRepository extends BaseEMRepository {

    /**
     * 获取推送配置
     * @return
     */
    public LiveData<Resource<PushConfigs>> getPushConfigsFromServer() {
        return new NetworkBoundResource<PushConfigs, PushConfigs>() {
            @Override
            protected boolean shouldFetch(PushConfigs data) {
                return true;
            }

            @Override
            protected LiveData<PushConfigs> loadFromDb() {
                return createLiveData(getPushManager().getPushConfigs());
            }

            @Override
            protected void createCall(ResultCallBack<LiveData<PushConfigs>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    PushConfigs configs = null;
                    try {
                        configs = getPushManager().getPushConfigsFromServer();
                        callBack.onSuccess(createLiveData(configs));
                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                });
            }

            @Override
            protected void saveCallResult(PushConfigs item) {

            }
        }.asLiveData();
    }

    /**
     * 获取推送配置
     * @return
     */
    public PushConfigs fetchPushConfigsFromServer() {
        try {
            return getPushManager().getPushConfigsFromServer();
        } catch (ChatException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置免打扰时间段
     * 如果end小于start,则end为第二天的hour
     * @param start
     * @param end
     * @return
     */
    public LiveData<Resource<Boolean>> disableOfflinePush(int start, int end) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    try {
                        ChatClient.getInstance().pushManager().disableOfflinePush(start, end);
                        callBack.onSuccess(createLiveData(true));
                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getDescription());
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 允许离线推送
     * @return
     */
    public LiveData<Resource<Boolean>> enableOfflinePush() {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                EaseThreadManager.getInstance().runOnIOThread(()-> {
                    try {
                        ChatClient.getInstance().pushManager().enableOfflinePush();
                        callBack.onSuccess(createLiveData(true));
                    } catch (ChatException e) {
                        e.printStackTrace();
                        callBack.onError(e.getErrorCode(), e.getDescription());
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 更新推送昵称
     * @param nickname
     * @return
     */
    public LiveData<Resource<Boolean>> updatePushNickname(String nickname) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getPushManager().asyncUpdatePushNickname(nickname, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 设置推送消息样式
     * @param style
     * @return
     */
    public LiveData<Resource<Boolean>> updatePushStyle(PushManager.DisplayStyle style) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getPushManager().asyncUpdatePushDisplayStyle(style, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });
            }
        }.asLiveData();
    }
}
