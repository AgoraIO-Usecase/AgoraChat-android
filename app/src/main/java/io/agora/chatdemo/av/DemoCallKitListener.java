package io.agora.chatdemo.av;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.haoge.easyandroid.easy.EasyExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.TimeZone;

import io.agora.chat.ChatClient;
import io.agora.chat.callkit.EaseCallKit;
import io.agora.chat.callkit.bean.EaseCallUserInfo;
import io.agora.chat.callkit.bean.EaseUserAccount;
import io.agora.chat.callkit.general.EaseCallEndReason;
import io.agora.chat.callkit.general.EaseCallError;
import io.agora.chat.callkit.general.EaseCallType;
import io.agora.chat.callkit.listener.EaseCallGetUserAccountCallback;
import io.agora.chat.callkit.listener.EaseCallKitListener;
import io.agora.chat.callkit.listener.EaseCallKitTokenCallback;
import io.agora.chat.uikit.models.EaseUser;
import io.agora.chatdemo.BuildConfig;
import io.agora.chatdemo.R;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.livedatas.EaseEvent;
import io.agora.chatdemo.general.livedatas.LiveDataBus;
import io.agora.chatdemo.general.manager.UsersManager;
import io.agora.chatdemo.general.utils.ToastUtils;
import io.agora.cloud.EMHttpClient;
import io.agora.exceptions.ChatException;
import io.agora.util.EMLog;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


public class DemoCallKitListener implements EaseCallKitListener {

    private final String TAG = getClass().getSimpleName();
    private String tokenUrl = "http://" + BuildConfig.APP_SERVER_DOMAIN + "/token/rtc";
    private String uIdUrl = "http://" + BuildConfig.APP_SERVER_DOMAIN + "/agora/channel/mapper";
    private UsersManager mUsersManager;
    private Context mContext;
    private Handler handler;
    private final EasyExecutor executor;

    public DemoCallKitListener(Context context, UsersManager usersManager) {
        this.mContext = context;
        this.mUsersManager = usersManager;
        handler = new Handler(Looper.getMainLooper());
        executor = EasyExecutor.newBuilder(0)
                .build();
    }

    @Override
    public void onInviteUsers(EaseCallType callType, String existMembers[], JSONObject ext) {

        String groupId = null;
        if (ext != null && ext.length() > 0) {
            try {
                groupId = ext.getString("groupId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable("easeCallType", callType);
        bundle.putString("groupId", groupId);
        bundle.putStringArray("existMembers", existMembers);

        Intent intent = new Intent(mContext, CallInviteUsersActivity.class);
        intent.putExtra("invite_params", bundle);
        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public void onEndCallWithReason(EaseCallType callType, String channelName, EaseCallEndReason reason, long callTime) {
        EMLog.d(TAG, "onEndCallWithReason" + (callType != null ? callType.name() : " callType is null ") + " reason:" + reason + " time:" + callTime);
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        String callString = mContext.getString(R.string.ease_call_duration);
        callString += formatter.format(callTime);
        switch (reason) {
            case EaseCallEndReasonHangup://正常挂断
                ToastUtils.showToast(callString);
                break;
            case EaseCallEndReasonCancel://自己取消通话
                break;
            case EaseCallEndReasonRemoteCancel: //对方取消通话
                ToastUtils.showToast(callString);
                break;
            case EaseCallEndReasonRefuse://拒绝接听
                ToastUtils.showToast(mContext.getString(R.string.demo_call_end_reason_refuse));
                break;
            case EaseCallEndReasonBusy: //忙线中
                ToastUtils.showToast(mContext.getString(R.string.demo_call_end_reason_busy));
                break;
            case EaseCallEndReasonNoResponse://自己无响应
                break;
            case EaseCallEndReasonRemoteNoResponse://对端无响应
                ToastUtils.showToast(mContext.getString(R.string.demo_call_end_reason_busy_remote_no_response));
                break;
            case EaseCallEndReasonHandleOnOtherDeviceAgreed://在其他设备同意
                ToastUtils.showToast(mContext.getString(R.string.demo_call_end_reason_other_device_agreed));
                break;
            case EaseCallEndReasonHandleOnOtherDeviceRefused://在其他设备拒绝
                ToastUtils.showToast(mContext.getString(R.string.demo_call_end_reason_other_device_refused));
                break;
        }

    }

    @Override
    public void onGenerateRTCToken(String userAccount, String channelName, EaseCallKitTokenCallback callback) {
        EMLog.d(TAG, "onGenerateToken userId:" + userAccount + " channelName:" + channelName);
        int agoraUid = mUsersManager.getCurrentUserAgoraUid();
        StringBuilder url = new StringBuilder(tokenUrl)
                .append("/channel/")
                .append(channelName)
                .append("/agorauid/")
                .append(agoraUid)
                .append("?")
                .append("userAccount=")
                .append(userAccount);

        //获取声网Token
        getRtcToken(url.toString(), agoraUid, callback);
    }

    @Override
    public void onReceivedCall(EaseCallType callType, String fromUserId, JSONObject ext) {
        //收到接听电话
        EMLog.d(TAG, "onRecivedCall" + callType.name() + " fromUserId:" + fromUserId);
    }

    @Override
    public void onCallError(EaseCallError type, int errorCode, String description) {
        EMLog.d(TAG, "onCallError" + type.name() + " description:" + description);
        if(type== EaseCallError.PROCESS_ERROR) {
            ToastUtils.showToast(description);
        }
    }

    @Override
    public void onInViteCallMessageSent() {
        LiveDataBus.get().with(DemoConstant.MESSAGE_CHANGE_CHANGE).postValue(new EaseEvent(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.TYPE.MESSAGE));
    }

    @Override
    public void onRemoteUserJoinChannel(String channelName, String userName, int uid, EaseCallGetUserAccountCallback callback) {
        StringBuilder url = new StringBuilder(uIdUrl)
                .append("?")
                .append("channelName=")
                .append(channelName)
                .append("&userAccount=")
                .append(userName);
        getUserIdByAgoraUid(uid, url.toString(), callback);
    }

    /**
     * 获取声网Token
     */
    private void getRtcToken(String tokenUrl, int agoraUid, EaseCallKitTokenCallback callback) {
        executor.asyncResult(new Function1<Pair<Integer, String>, Unit>() {
            @Override
            public Unit invoke(Pair<Integer, String> response) {
                if (response != null) {
                    try {
                        int resCode = response.first;
                        if (resCode == 200) {
                            String responseInfo = response.second;
                            if (responseInfo != null && responseInfo.length() > 0) {
                                try {
                                    JSONObject object = new JSONObject(responseInfo);
                                    String token = object.getString("accessToken");
                                    //设置自己头像昵称
                                    setEaseCallKitUserInfo(ChatClient.getInstance().getCurrentUser());
                                    callback.onSetToken(token, agoraUid);
                                } catch (Exception e) {
                                    e.getStackTrace();
                                }
                            } else {
                                callback.onGetTokenError(response.first, response.second);
                            }
                        } else {
                            callback.onGetTokenError(response.first, response.second);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onSetToken(null, 0);
                }
                return null;
            }
        })
                .asyncTask(notifier -> {
                    try {
                        Pair<Integer, String> response = EMHttpClient.getInstance().sendRequestWithToken(tokenUrl, null, EMHttpClient.GET);
                        return response;
                    } catch (ChatException exception) {
                        exception.printStackTrace();
                    }
                    return null;
                });
    }

    /**
     * 根据channelName和声网uId获取频道内所有人的UserId
     *
     * @param uId
     * @param url
     * @param callback
     */
    private void getUserIdByAgoraUid(int uId, String url, EaseCallGetUserAccountCallback callback) {
        executor.asyncResult(new Function1<Pair<Integer, String>, Unit>() {
            @Override
            public Unit invoke(Pair<Integer, String> response) {
                if (response != null) {
                    try {
                        int resCode = response.first;
                        if (resCode == 200) {
                            String responseInfo = response.second;
                           EaseUserAccount userAccount =null;
                            if (responseInfo != null && responseInfo.length() > 0) {
                                try {
                                    JSONObject object = new JSONObject(responseInfo);
                                    JSONObject resToken = object.getJSONObject("result");
                                    Iterator it = resToken.keys();
                                    while (it.hasNext()) {
                                        String uIdStr = it.next().toString();
                                        int uid = Integer.valueOf(uIdStr).intValue();
                                        String username = resToken.optString(uIdStr);
                                        if (uid == uId) {
                                            //获取到当前用户的userName 设置头像昵称等信息
                                            userAccount=new EaseUserAccount(uid, username);
                                        }
                                    }
                                    callback.onUserAccount(userAccount);
                                } catch (Exception e) {
                                    e.getStackTrace();
                                }
                            } else {
                                callback.onSetUserAccountError(response.first, response.second);
                            }
                        } else {
                            callback.onSetUserAccountError(response.first, response.second);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    callback.onSetUserAccountError(100, "response is null");
                }
                return null;
            }
        }).asyncTask(notifier -> {
            try {
                Pair<Integer, String> response = EMHttpClient.getInstance().sendRequestWithToken(url, null, EMHttpClient.GET);
                return response;
            } catch (ChatException exception) {
                exception.printStackTrace();
            }
            return null;
        });
    }

    private void setEaseCallKitUserInfo(String userName) {
        EaseUser user = mUsersManager.getUserInfo(userName);
        EaseCallUserInfo userInfo = new EaseCallUserInfo();
        if (user != null) {
            userInfo.setNickName(user.getNickname());
            userInfo.setHeadImage(user.getAvatar());
        }
        EaseCallKit.getInstance().getCallKitConfig().setUserInfo(userName, userInfo);
    }

    @Override
    public void onUserInfoUpdate(String userName) {
        //设置用户昵称 头像
        setEaseCallKitUserInfo(userName);
    }
}
