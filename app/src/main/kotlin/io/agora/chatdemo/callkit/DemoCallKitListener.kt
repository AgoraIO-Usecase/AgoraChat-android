package io.agora.chatdemo.callkit

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.SpannableStringBuilder
import com.hyphenate.chatdemo.callkit.extensions.getStringOrNull
import io.agora.chat.callkit.EaseCallKit
import io.agora.chat.callkit.bean.EaseUserAccount
import io.agora.chat.callkit.general.EaseCallEndReason
import io.agora.chat.callkit.general.EaseCallError
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chat.callkit.listener.EaseCallGetUserAccountCallback
import io.agora.chat.callkit.listener.EaseCallKitListener
import io.agora.chat.callkit.listener.EaseCallKitTokenCallback
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.callkit.activity.CallMultipleInviteActivity
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.utils.ToastUtils.showToast
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.common.ChatClient
import io.agora.chat.uikit.common.bus.ChatUIKitFlowBus
import io.agora.chat.uikit.common.extensions.mainScope
import io.agora.chat.uikit.model.ChatUIKitEvent
import io.agora.chat.uikit.provider.getSyncUser
import io.agora.util.EMLog
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.TimeZone

class DemoCallKitListener(val mContext: Context): EaseCallKitListener {

    companion object{
        private val TAG = DemoCallKitListener::class.java.simpleName
        private const val PARAM_USER = "userAccount="
        private const val PARAM_CHANNEL_NAME = "channelName="
    }


    //The URL here is for the demo example, and the actual project users should obtain it from their App Server
    private val FETCH_TOKEN_URL = BuildConfig.APP_SERVER_PROTOCOL+ "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_RTC_TOKEN_URL
    private val FETCH_USER_MAPPER = BuildConfig.APP_SERVER_PROTOCOL+ "://" + BuildConfig.APP_SERVER_DOMAIN + BuildConfig.APP_RTC_CHANNEL_MAPPER_URL

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val obj = msg.obj
            if (obj is String) {
                showToast(obj)
            }
        }
    }

    override fun onInviteUsers(
        callType: EaseCallType?,
        users: Array<String>?,
        ext: JSONObject?
    ) {
        CallKitManager.currentCallGroupId = ext?.getStringOrNull(CallKitManager.KEY_GROUP_ID)
        Intent(mContext, CallMultipleInviteActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(CallKitManager.EXTRA_CONFERENCE_GROUP_ID,CallKitManager.currentCallGroupId)
            putExtra(CallKitManager.EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS,users)
            mContext.startActivity(this)
        }
    }

    override fun onEndCallWithReason(
        callType: EaseCallType?,
        channelName: String?,
        reason: EaseCallEndReason,
        callTime: Long
    ) {
        EMLog.d( TAG,
            "onEndCallWithReason" + (callType?.name
                ?: " callType is null ") + " reason:" + reason + " time:" + callTime
        )
        val formatter = SimpleDateFormat("mm:ss")
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        val callString: String = mContext.getString(R.string.call_duration,(formatter.format(callTime))?:"")
        val message = handler.obtainMessage()
        when (reason) {
            EaseCallEndReason.EaseCallEndReasonHangup -> message.obj = callString
            EaseCallEndReason.EaseCallEndReasonCancel -> {}
            EaseCallEndReason.EaseCallEndReasonRemoteCancel -> message.obj = callString
            EaseCallEndReason.EaseCallEndReasonRefuse -> message.obj =
                mContext.getString(R.string.demo_call_end_reason_refuse)

            EaseCallEndReason.EaseCallEndReasonBusy -> message.obj =
                mContext.getString(R.string.demo_call_end_reason_busy)

            EaseCallEndReason.EaseCallEndReasonNoResponse -> {}
            EaseCallEndReason.EaseCallEndReasonRemoteNoResponse -> message.obj =
                mContext.getString(R.string.demo_call_end_reason_busy_remote_no_response)

            EaseCallEndReason.EaseCallEndReasonHandleOnOtherDeviceAgreed -> message.obj =
                mContext.getString(R.string.demo_call_end_reason_other_device_agreed)

            EaseCallEndReason.EaseCallEndReasonHandleOnOtherDeviceRefused -> message.obj =
                mContext.getString(R.string.demo_call_end_reason_other_device_refused)
        }
        CallKitManager.channelName = ""
        handler.sendMessage(message)
    }

    override fun onGenerateRTCToken(
        userAccount: String,
        channelName: String,
        callback: EaseCallKitTokenCallback
    ) {
        val agoraUid: Int =DemoHelper.getInstance().getDataModel().getCurrentUserAgoraUid()
        CallKitManager.channelName = channelName
        EMLog.d(TAG, "onGenerateToken userId:$userAccount channelName:$channelName agoraUid:$agoraUid")
        SpannableStringBuilder(FETCH_TOKEN_URL).apply {
            append("/$channelName?$PARAM_USER$userAccount")
            CallKitManager.getRtcToken(this.toString(), callback)
        }
    }

    override fun onReceivedCall(callType: EaseCallType, fromUserId: String, ext: JSONObject?) {
        EMLog.d(TAG, "onRecivedCall" + callType.name + " fromUserId:" + fromUserId)
        ext?.getStringOrNull(CallKitManager.KEY_GROUP_ID)?.let { groupId ->
            CallKitManager.currentCallGroupId = groupId
            CallUserInfo(fromUserId).getUserInfo(groupId).parse().apply {
                EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
            }
        } ?: kotlin.run {
            CallKitManager.currentCallGroupId = null
            CallUserInfo(fromUserId).apply {
                ChatUIKitClient.getUserProvider()?.getSyncUser(userId)?.let { user ->
                    this.nickName = user.getNotEmptyName()
                    this.headImage = user.avatar
                }
                EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this.parse())
            } // Single call
        }
    }

    override fun onCallError(type: EaseCallError, errorCode: Int, description: String) {
        EMLog.d(TAG, "onCallError" + type.name + " description:" + description)
        if (type == EaseCallError.PROCESS_ERROR) {
            showToast(description)
        }
    }

    override fun onInViteCallMessageSent() {
        if (ChatClient.getInstance().options.isIncludeSendMessageInMessageListener.not()) {
            ChatUIKitFlowBus.with<ChatUIKitEvent>(ChatUIKitEvent.EVENT.ADD + ChatUIKitEvent.TYPE.MESSAGE)
                .post(DemoHelper.getInstance().context.mainScope(), ChatUIKitEvent(DemoConstant.CALL_INVITE_MESSAGE, ChatUIKitEvent.TYPE.MESSAGE))
        }
    }

    override fun onRemoteUserJoinChannel(
        channelName: String?,
        userName: String?,
        uid: Int,
        callback: EaseCallGetUserAccountCallback
    ) {
        // Only multi call callback this method
        if (userName.isNullOrEmpty()) {
            SpannableStringBuilder(FETCH_USER_MAPPER).apply {
                append("?$PARAM_CHANNEL_NAME$channelName")
                CallKitManager.getUserIdByAgoraUid(uid,this.toString(), callback)
            }
        } else {
            // Set user info to call kit.
            CallUserInfo(userName).getUserInfo(CallKitManager.currentCallGroupId).parse().apply {
                EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
            }
            callback.onUserAccount(EaseUserAccount(uid, userName))
        }
    }

    override fun onUserInfoUpdate(userName: String) {
        //set user's nickname and avater
        CallKitManager.setEaseCallKitUserInfo(userName)
    }
}
