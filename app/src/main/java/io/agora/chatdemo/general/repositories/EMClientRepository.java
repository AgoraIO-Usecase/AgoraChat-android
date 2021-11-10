package io.agora.chatdemo.general.repositories;

import static io.agora.cloud.HttpClientManager.Method_POST;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.CallBack;
import io.agora.Error;
import io.agora.chat.ChatClient;
import io.agora.chat.Group;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.BuildConfig;
import io.agora.chatdemo.DemoApplication;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.callbacks.DemoCallBack;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.manager.PreferenceManager;
import io.agora.chatdemo.general.models.LoginBean;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

/**
 * 作为ChatClient的repository,处理ChatClient相关的逻辑
 */
public class EMClientRepository extends BaseEMRepository{

    private static final String TAG = EMClientRepository.class.getSimpleName();

    /**
     * 登录过后需要加载的数据
     * @return
     */
    public LiveData<Resource<Boolean>> loadAllInfoFromHX() {
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(ResultCallBack<LiveData<Boolean>> callBack) {
                if(isAutoLogin()) {
                    runOnIOThread(() -> {
                        if(isLoggedIn()) {
                            success(null, callBack);
                        }else {
                            callBack.onError(ErrorCode.NOT_LOGIN);
                        }

                    });
                }else {
                    callBack.onError(ErrorCode.NOT_LOGIN);
                }

            }
        }.asLiveData();
    }

    private void initLocalDb() {
        // init demo db
        initDb();
    }

    /**
     * 注册
     * @param userName
     * @param pwd
     * @return
     */
    public LiveData<Resource<String>> registerToHx(String userName, String pwd) {
        return new NetworkOnlyResource<String>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                //注册之前先判断SDK是否已经初始化，如果没有先进行SDK的初始化
                if(!DemoHelper.getInstance().isSDKInit) {
                    DemoHelper.getInstance().init(DemoApplication.getInstance());
                }
                runOnIOThread(() -> {
                    try {
                        ChatClient.getInstance().createAccount(userName, pwd);
                        callBack.onSuccess(createLiveData(userName));
                    } catch (ChatException e) {
                        callBack.onError(e.getErrorCode(), e.getMessage());
                    }
                });
            }

        }.asLiveData();
    }

    /**
     * 登录到服务器，可选择密码登录或者token登录
     * 登录之前先初始化数据库，如果登录失败，再关闭数据库;如果登录成功，则再次检查是否初始化数据库
     * @param userName
     * @param pwd
     * @param isTokenFlag
     * @return
     */
    public LiveData<Resource<EaseUser>> loginToServer(String userName, String pwd, boolean isTokenFlag) {
        return new NetworkOnlyResource<EaseUser>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<EaseUser>> callBack) {
                DemoHelper.getInstance().init(DemoApplication.getInstance());
                if(isTokenFlag) {
                    ChatClient.getInstance().loginWithToken(userName, pwd, new DemoCallBack() {
                        @Override
                        public void onSuccess() {
                            successForCallBack(callBack);
                        }

                        @Override
                        public void onError(int code, String error) {
                            callBack.onError(code, error);
                            closeDb();
                        }
                    });
                }else {
                    ChatClient.getInstance().login(userName, pwd, new DemoCallBack() {
                        @Override
                        public void onSuccess() {
                            successForCallBack(callBack);
                        }

                        @Override
                        public void onError(int code, String error) {
                            callBack.onError(code, error);
                            closeDb();
                        }
                    });
                }

            }

        }.asLiveData();
    }
    
    public LiveData<Resource<Boolean>> loginWithAgoraToken(String userName, String token) {
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().loginWithAgoraToken(userName, token, new DemoCallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, error);
                    }
                });
            }

        }.asLiveData();
    }
    
    public LiveData<Resource<Boolean>> renewAgoraToken(String token) {
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().renewToken(token);
                callBack.onSuccess(createLiveData(true));
            }

        }.asLiveData();
    }

    /**
     * 退出登录
     * @param unbindDeviceToken
     * @return
     */
    public LiveData<Resource<Boolean>> logout(boolean unbindDeviceToken) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().logout(unbindDeviceToken, new CallBack() {

                    @Override
                    public void onSuccess() {
                        DemoHelper.getInstance().logoutSuccess();
                        //reset();
                        if (callBack != null) {
                            callBack.onSuccess(createLiveData(true));
                        }

                    }

                    @Override
                    public void onProgress(int progress, String status) {
                    }

                    @Override
                    public void onError(int code, String error) {
                        //reset();
                        if (callBack != null) {
                            callBack.onError(code, error);
                        }
                    }
                });
            }
        }.asLiveData();
    }

    /**
     * 设置本地标记，是否自动登录
     * @param autoLogin
     */
    public void setAutoLogin(boolean autoLogin) {
        PreferenceManager.getInstance().setAutoLogin(autoLogin);
    }

    private void successForCallBack(@NonNull ResultCallBack<LiveData<EaseUser>> callBack) {
        // ** manually load all local groups and conversation
        initLocalDb();
        //从服务器拉取加入的群，防止进入会话页面只显示id
        DemoHelper.getInstance().getUserProfileManager().initUserInfo();
        // get current user id
        String currentUser = ChatClient.getInstance().getCurrentUser();
        EaseUser user = new EaseUser(currentUser);
        callBack.onSuccess(new MutableLiveData<>(user));
    }

    private void getContactsFromServer() {
        new EMContactManagerRepository().getContactList(new ResultCallBack<List<EaseUser>>() {
            @Override
            public void onSuccess(List<EaseUser> value) {
                if(getUserDao() != null) {
                    getUserDao().clearUsers();
                    getUserDao().insert(EmUserEntity.parseList(value));
                }
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private void getAllJoinGroup() {
        new EMGroupManagerRepository().getAllGroups(new ResultCallBack<List<Group>>() {
            @Override
            public void onSuccess(List<Group> value) {
                //加载完群组信息后，刷新会话列表页面，保证展示群组名称
                EMLog.i("ChatPresenter", "login isGroupsSyncedWithServer success");
                EaseEvent event = EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP);
                LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(event);
            }

            @Override
            public void onError(int error, String errorMsg) {

            }
        });
    }

    private void closeDb() {
        DemoDbHelper.getInstance(DemoApplication.getInstance()).closeDb();
    }

    /**
     * Renew Agora chat by app server
     * @return
     */
    public LiveData<Resource<String>> renewAgoraChatToken() {
        return new NetworkOnlyResource<String>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                loginToAppServer(ChatClient.getInstance().getCurrentUser(), "nickname", new ResultCallBack<LoginBean>() {
                    @Override
                    public void onSuccess(LoginBean value) {
                        if(value != null && !TextUtils.isEmpty(value.getAccessToken())) {
                            ChatClient.getInstance().renewToken(value.getAccessToken());
                            callBack.onSuccess(createLiveData(value.getAccessToken()));
                        }else {
                            callBack.onError(Error.GENERAL_ERROR, "AccessToken is null!");
                        }
                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> loginByAppServer(String username, String nickname) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                loginToAppServer(username, nickname, new ResultCallBack<LoginBean>() {
                    @Override
                    public void onSuccess(LoginBean value) {
                        if(value != null && !TextUtils.isEmpty(value.getAccessToken())) {
                            ChatClient.getInstance().loginWithAgoraToken(username, value.getAccessToken(), new CallBack() {
                                @Override
                                public void onSuccess() {
                                    success(nickname, callBack);
                                }

                                @Override
                                public void onError(int code, String error) {
                                    callBack.onError(code, error);
                                    closeDb();
                                }

                                @Override
                                public void onProgress(int progress, String status) {

                                }
                            });
                        }else {
                            callBack.onError(Error.GENERAL_ERROR, "AccessToken is null!");
                        }

                    }

                    @Override
                    public void onError(int error, String errorMsg) {
                        callBack.onError(error, errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    private void success(String nickname, @NonNull ResultCallBack<LiveData<Boolean>> callBack) {
        // ** manually load all local groups and conversation
        initLocalDb();
        DemoHelper.getInstance().getUserProfileManager().initUserInfo();
        // get current user
        new EMContactManagerRepository().updateCurrentUserNickname(nickname, null);
        callBack.onSuccess(createLiveData(true));
    }

    private void loginToAppServer(String username, String nickname, ResultCallBack<LoginBean> callBack) {
        runOnIOThread(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                JSONObject request = new JSONObject();
                request.putOpt("userAccount", username);
                request.putOpt("userNickname", nickname);

                HttpResponse response = HttpClientManager.httpExecute(BuildConfig.APP_SERVER_URL, headers, request.toString(), Method_POST);
                int code = response.code;
                String responseInfo = response.content;
                if (code == 200) {
                    if (responseInfo != null && responseInfo.length() > 0) {
                        JSONObject object = new JSONObject(responseInfo);
                        String token = object.getString("accessToken");
                        LoginBean bean = new LoginBean();
                        bean.setAccessToken(token);
                        bean.setUserNickname(nickname);
                        if(callBack != null) {
                            callBack.onSuccess(bean);
                        }
                    } else {
                        callBack.onError(code, responseInfo);
                    }
                } else {
                    callBack.onError(code, responseInfo);
                }
            } catch (Exception e) {
                //e.printStackTrace();
                callBack.onError(Error.NETWORK_ERROR, e.getMessage());
            }
        });
    }
}
