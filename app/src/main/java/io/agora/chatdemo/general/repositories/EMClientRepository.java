package io.agora.chatdemo.general.repositories;

import static io.agora.cloud.HttpClientManager.Method_POST;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.callbacks.DemoCallBack;
import io.agora.chatdemo.general.callbacks.ResultCallBack;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.db.DemoDbHelper;
import io.agora.chatdemo.general.db.entity.EmUserEntity;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.models.LoginBean;
import io.agora.chatdemo.general.net.ErrorCode;
import io.agora.chatdemo.general.net.Resource;
import io.agora.cloud.HttpClientManager;
import io.agora.cloud.HttpResponse;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;

/**
 * Handle ChatClient related logic
 */
public class EMClientRepository extends BaseEMRepository{

    private static final String TAG = EMClientRepository.class.getSimpleName();

    /**
     * Data to be loaded after login
     * @return
     */
    public LiveData<Resource<Boolean>> loadAllInfoFromHX() {
        return new NetworkOnlyResource<Boolean>() {

            @Override
            protected void createCall(ResultCallBack<LiveData<Boolean>> callBack) {
                if(isAutoLogin()) {
                    runOnIOThread(() -> {
                        if(isLoggedIn()) {
                            success("", callBack);
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
     * Sign in
     * @param userName
     * @param pwd
     * @return
     */
    public LiveData<Resource<String>> registerToHx(String userName, String pwd) {
        return new NetworkOnlyResource<String>() {

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<String>> callBack) {
                //Before registering, determine whether the SDK has been initialized. If not, perform SDK initialization first.
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
     * Log in to the server, you can choose password login or token login
     * Initialize the database before logging in, if the login fails, then close the database;
     * if the login is successful, check again whether the database is initialized
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
     * Sign out
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
                            callBack.onError(code, getErrorMsg(code, error));
                        }
                    }
                });
            }
        }.asLiveData();
    }

    private void successForCallBack(@NonNull ResultCallBack<LiveData<EaseUser>> callBack) {
        // ** manually load all local groups and conversation
        initLocalDb();
        //Pull the joined group from the server to prevent only the id from entering the conversation page
        DemoHelper.getInstance().getUsersManager().initUserInfo();
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
                //After loading the group information, refresh the session list page to ensure that the group name is displayed
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
                loginToAppServer(ChatClient.getInstance().getCurrentUser(), decryptData(), new ResultCallBack<LoginBean>() {
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
                        callBack.onError(error, getErrorMsg(error, errorMsg));
                        EMLog.e("renewAgoraChatToken error : ", error + " " +errorMsg);
                    }
                });
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> loginByAppServer(String username, String pwd) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                loginToAppServer(username, pwd, new ResultCallBack<LoginBean>() {
                    @Override
                    public void onSuccess(LoginBean value) {
                        if(value != null && !TextUtils.isEmpty(value.getAccessToken())) {
                            ChatClient.getInstance().loginWithAgoraToken(username, value.getAccessToken(), new CallBack() {
                                @Override
                                public void onSuccess() {
                                    DemoHelper.getInstance().getUsersManager().setCurrentUserAgoraUid(value.getAgoraUid());
                                    success(pwd, callBack);
                                }

                                @Override
                                public void onError(int code, String error) {
                                    callBack.onError(code, getErrorMsg(code, error));
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
                        callBack.onError(error, getErrorMsg(error, errorMsg));
                    }
                });

            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> loginByPassword(String username, String pwd) {
        return new NetworkOnlyResource<Boolean>() {
            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                ChatClient.getInstance().login(username, pwd, new CallBack() {
                    @Override
                    public void onSuccess() {
                        success(username, callBack);
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, getErrorMsg(code, error));
                        closeDb();
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }
                });

            }
        }.asLiveData();
    }

    private void success(String pwd, @NonNull ResultCallBack<LiveData<Boolean>> callBack) {
        if (!TextUtils.isEmpty(pwd)){
            encryptData(pwd);
            // ** manually load all local groups and conversation
            initLocalDb();
            //Pull the joined group from the server to prevent only the id from entering the conversation page
            DemoHelper.getInstance().getUsersManager().initUserInfo();

            EaseUser user =  DemoHelper.getInstance().getUsersManager().getUserInfo(getCurrentUser());
            if (!TextUtils.isEmpty(user.getNickname()))
            new EMContactManagerRepository().updateCurrentUserNickname(user.getNickname(), null);
        }
        callBack.onSuccess(createLiveData(true));
    }

    private void encryptData(String data){
        DemoHelper.getInstance().getEncryptUtils().initAESgcm(getContext().getString(R.string.sign_aes).getBytes());
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = setting.edit();
        try {
            String encryptedData = DemoHelper.getInstance().getEncryptUtils().aesGcmEncrypt(data,1);
            editor.putString(getContext().getString(R.string.sign_gcm_key), encryptedData);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
            EMLog.e("EMClientRepository : ",e.getMessage());
        }
    }

    private String decryptData(){
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        String encryptedData = setting.getString(getContext().getString(R.string.sign_gcm_key), "");
        return DemoHelper.getInstance().getEncryptUtils().aesGcmDecrypt(encryptedData,getContext().getString(R.string.sign_aes).getBytes(),1);
    }

    private void loginToAppServer(String username, String nickname, ResultCallBack<LoginBean> callBack) {
        runOnIOThread(() -> {
            try {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");

                JSONObject request = new JSONObject();
                request.putOpt("userAccount", username);
                request.putOpt("userPassword", nickname);

                String url = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_SERVER_URL;
                HttpResponse response = HttpClientManager.httpExecute(url, headers, request.toString(), Method_POST);
                int code = response.code;
                String responseInfo = response.content;
                if (code == 200) {
                    EMLog.e("loginToAppServer success : ", responseInfo);
                    if (responseInfo != null && responseInfo.length() > 0) {
                        JSONObject object = new JSONObject(responseInfo);
                        String token = object.getString("accessToken");
                        int agoraUid = object.getInt("agoraUid");
                        LoginBean bean = new LoginBean();
                        bean.setAccessToken(token);
                        bean.setUserNickname(nickname);
                        bean.setAgoraUid(agoraUid);
                        if(callBack != null) {
                            callBack.onSuccess(bean);
                        }
                    } else {
                        callBack.onError(code, responseInfo);
                    }
                } else {
                    if (responseInfo != null && responseInfo.length() > 0) {
                        JSONObject object = new JSONObject(responseInfo);
                        callBack.onError(code, object.getString("errorInfo"));
                    }else {
                        callBack.onError(code, responseInfo);
                    }
                }
            } catch (Exception e) {
                //e.printStackTrace();
                callBack.onError(Error.NETWORK_ERROR, e.getMessage());
            }
        });
    }

    public LiveData<Resource<Boolean>> registerByAppServer(String username, String pwd) {
        return new NetworkOnlyResource<Boolean>(){

            @Override
            protected void createCall(@NonNull ResultCallBack<LiveData<Boolean>> callBack) {
                registerToAppServer(username, pwd, new CallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess(createLiveData(true));
                    }

                    @Override
                    public void onError(int code, String error) {
                        callBack.onError(code, getErrorMsg(code, error));
                    }
                });
            }
        }.asLiveData();
    }

    public void registerToAppServer(String username, String pwd, CallBack callBack){
        runOnIOThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    JSONObject request = new JSONObject();
                    request.putOpt("userAccount", username);
                    request.putOpt("userPassword", pwd);

                    String url = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_SERVER_REGISTER;
                    HttpResponse response = HttpClientManager.httpExecute(url, headers, request.toString(), Method_POST);
                    int code = response.code;
                    String responseInfo = response.content;
                    if (code == 200) {
                        callBack.onSuccess();
                    } else {
                        if (responseInfo != null && responseInfo.length() > 0) {
                            JSONObject object = new JSONObject(responseInfo);
                            callBack.onError(code, object.getString("errorInfo"));
                        }else {
                            callBack.onError(code, responseInfo);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
