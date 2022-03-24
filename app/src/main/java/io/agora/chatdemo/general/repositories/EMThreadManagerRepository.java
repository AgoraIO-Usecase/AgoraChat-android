package io.agora.chatdemo.general.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;

import io.agora.CallBack;
import io.agora.ValueCallBack;
import io.agora.chat.ChatThread;
import io.agora.chat.CursorResult;
import io.agora.chat.UserInfo;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.enums.Status;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;

public class EMThreadManagerRepository extends BaseEMRepository {
    /**
     * Get thread info from server
     * @param threadId
     * @return
     */
    public LiveData<Resource<ChatThread>> getThreadFromServer(String threadId) {
        return new NetworkOnlyResource<ChatThread>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<ChatThread>> callBack) {
                if(!isLoggedIn()) {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                    return;
                }
                DemoHelper.getInstance().getThreadManager().getThreadFromServer(threadId, new ValueCallBack<ChatThread>() {
                    @Override
                    public void onSuccess(ChatThread value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * Leave from thread
     * @param threadId
     * @return
     */
    public LiveData<Resource<Boolean>> leaveThread(String threadId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().leaveThread(threadId, new CallBack() {
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
     * Destroy thread
     * @param threadId
     * @return
     */
    public LiveData<Resource<Boolean>> destroyThread(String threadId) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().destroyThread(threadId, new CallBack() {
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
     * Change thread name
     * @param threadId
     * @return
     */
    public LiveData<Resource<Boolean>> changeThreadName(String threadId, String newThreadName) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                getThreadManager().changeThreadName(threadId, newThreadName, new CallBack() {
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
     * Get thread member by thread id, limit and cursor
     * @param threadId
     * @param limit
     * @param cursor
     * @return
     */
    public LiveData<Resource<CursorResult<String>>> getThreadMembers(String threadId, int limit, String cursor) {
        return new NetworkOnlyResource<CursorResult<String>>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<CursorResult<String>>> callBack) {
                getThreadManager().getThreadMembers(threadId, limit, cursor, new ValueCallBack<CursorResult<String>>() {
                    @Override
                    public void onSuccess(CursorResult<String> value) {
                        callBack.onSuccess(createLiveData(value));
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * Get thread members by thread id
     * @param threadId
     * @return
     */
    public LiveData<Resource<List<EaseUser>>> getThreadMembers(String threadId) {
        List<String> users = new ArrayList<>();
        LiveData<Resource<CursorResult<String>>> result = getResult(threadId, 20, "", users);
        return Transformations.switchMap(result, response -> {
            if (response.status == Status.SUCCESS) {
                List<EaseUser> easeUsers = EmUserEntity.parse(users);
                return createLiveData(Resource.success(easeUsers));
            } else if (response.status == Status.LOADING) {
                return createLiveData(new Resource<>(Status.LOADING, null, ErrorCode.EM_NO_ERROR));
            }
            return createLiveData(new Resource<>(Status.ERROR, null, response.errorCode, response.getMessage()));
        });
    }

    private LiveData<Resource<CursorResult<String>>> getResult(String threadId, int limit, String cursor, List<String> result) {
        return Transformations.switchMap(getThreadMembers(threadId, limit, cursor), response -> {
            if(response.status == Status.SUCCESS) {
                List<String> data = response.data.getData();
                if(data != null) {
                    result.addAll(data);
                    if(data.size() == limit) {
                        return getResult(threadId, limit, response.data.getCursor(), result);
                    }
                    return createLiveData(response);
                }
                return createLiveData(Resource.error(-1, response.data));

            }
            return createLiveData(response);
        });
    }


}
