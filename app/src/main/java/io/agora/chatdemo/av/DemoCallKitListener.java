package io.agora.chatdemo.av;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;

import androidx.annotation.NonNull;


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
import io.agora.chat.uikit.manager.EaseThreadManager;
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


public class DemoCallKitListener implements EaseCallKitListener {

    private final String TAG = getClass().getSimpleName();
    //The URL here is for the demo example, and the actual project users should obtain it from their App Server（此处url为demo示例所用，实际项目用用户应该从自己的App Server去获取）
    private String tokenUrl = "http://" + BuildConfig.APP_SERVER_DOMAIN + "/token/rtc";
    private String uIdUrl = "http://" + BuildConfig.APP_SERVER_DOMAIN + "/agora/channel/mapper";

    private UsersManager mUsersManager;
    private Context mContext;
    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Object obj = msg.obj;
            if( obj instanceof  String) {
                ToastUtils.showToast((String) obj);
            }
        }
    };

    public DemoCallKitListener(Context context, UsersManager usersManager) {
        this.mContext = context;
        this.mUsersManager = usersManager;
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
        Message message = handler.obtainMessage();
        switch (reason) {
            case EaseCallEndReasonHangup://Hang up normally
                message.obj=callString;
                break;
            case EaseCallEndReasonCancel://cancel the call yourself
                break;
            case EaseCallEndReasonRemoteCancel: //The other party cancels the call
                message.obj=callString;
                break;
            case EaseCallEndReasonRefuse://request declined
                message.obj=mContext.getString(R.string.demo_call_end_reason_refuse);
                break;
            case EaseCallEndReasonBusy: //busy
                message.obj=mContext.getString(R.string.demo_call_end_reason_busy);
                break;
            case EaseCallEndReasonNoResponse://not responding
                break;
            case EaseCallEndReasonRemoteNoResponse://No response from peer
                message.obj=mContext.getString(R.string.demo_call_end_reason_busy_remote_no_response);
                break;
            case EaseCallEndReasonHandleOnOtherDeviceAgreed://other devices connected
                message.obj=mContext.getString(R.string.demo_call_end_reason_other_device_agreed);
                break;
            case EaseCallEndReasonHandleOnOtherDeviceRefused://other devices declined
                message.obj=mContext.getString(R.string.demo_call_end_reason_other_device_refused);
                break;
        }
        handler.sendMessage(message);

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

        //get agora RTC token (get Agora RTC token)
        getRtcToken(url.toString(), agoraUid, callback);
    }

    @Override
    public void onReceivedCall(EaseCallType callType, String fromUserId, JSONObject ext) {
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


    private void getRtcToken(String tokenUrl, int agoraUid, EaseCallKitTokenCallback callback) {
        EaseThreadManager.getInstance().runOnIOThread(()-> {
            Pair<Integer, String> response = null;
            try {
                response = EMHttpClient.getInstance().sendRequestWithToken(tokenUrl, null, EMHttpClient.GET);
            } catch (ChatException e) {
                e.printStackTrace();
            }
            if (response != null) {
                try {
                    int resCode = response.first;
                    if (resCode == 200) {
                        String responseInfo = response.second;
                        if (responseInfo != null && responseInfo.length() > 0) {
                            try {
                                JSONObject object = new JSONObject(responseInfo);
                                String token = object.getString("accessToken");
                                //Set your avatar nickname
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
        });
    }

    /**
     * Get the userIDS of all people in the channel based on channelName and agora uId
     * @param uId
     * @param url
     * @param callback
     */
    private void getUserIdByAgoraUid(int uId, String url, EaseCallGetUserAccountCallback callback) {
        EaseThreadManager.getInstance().runOnIOThread(()-> {
            Pair<Integer, String> response = null;
            try {
                response = EMHttpClient.getInstance().sendRequestWithToken(url, null, EMHttpClient.GET);
            } catch (ChatException e) {
                e.printStackTrace();
            }
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
                                        //Obtain information such as userName, profile picture, and nickname of the current user
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
        //set user's nickname and avater
        setEaseCallKitUserInfo(userName);
    }
}
