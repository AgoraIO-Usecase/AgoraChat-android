package io.agora.chatdemo.callkit

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import io.agora.chat.callkit.EaseCallKit
import io.agora.chat.callkit.bean.EaseCallUserInfo
import io.agora.chat.callkit.bean.EaseUserAccount
import io.agora.chat.callkit.general.EaseCallKitConfig
import io.agora.chat.callkit.general.EaseCallType
import io.agora.chat.callkit.listener.EaseCallGetUserAccountCallback
import io.agora.chat.callkit.listener.EaseCallKitTokenCallback
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.R
import io.agora.chatdemo.callkit.activity.CallMultipleBaseActivity
import io.agora.chatdemo.callkit.activity.CallSingleBaseActivity
import io.agora.chatdemo.callkit.activity.CallMultipleInviteActivity
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatError
import io.agora.uikit.common.ChatHttpClientManagerBuilder
import io.agora.uikit.common.ChatHttpResponse
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.dialog.SimpleListSheetDialog
import io.agora.uikit.common.dialog.SimpleSheetType
import io.agora.uikit.common.extensions.toUser
import io.agora.uikit.feature.chat.enums.EaseChatType
import io.agora.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.uikit.model.EaseMenuItem
import io.agora.uikit.model.EaseUser
import io.agora.uikit.model.getNickname
import io.agora.uikit.provider.getSyncUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

object CallKitManager {

    /**
     * Whether it is a rtc call.
     */
    var isRtcCall = false

    /**
     * Rtc call type.
     */
    var rtcType = 0

    /**
     * If multiple call, should set groupId.
     */
    var currentCallGroupId: String? = null

    var channelName: String? = null

    private const val TAG = "CallKitManager"
    private const val RESULT_PARAM_TOKEN = "accessToken"
    private const val RESULT_PARAM_UID = "agoraUid"
    private const val RESULT_PARAM_RESULT = "result"
    const val KEY_GROUP_ID = "groupId"
    const val KEY_CALL_TYPE = "easeCallType"
    const val KEY_INVITE_PARAMS = "invite_params"
    const val EXTRA_CONFERENCE_GROUP_ID = "groupId"
    const val EXTRA_CONFERENCE_GROUP_EXIT_MEMBERS = "existMembers"


    fun init(context: Context) {
        EaseCallKitConfig().apply {
            callTimeOut = 30
            agoraAppId = BuildConfig.AGORA_APPID
            isEnableRTCToken = true
            defaultHeadImage = EaseIM.getCurrentUser()?.avatar
            EaseCallKit.getInstance().init(context,this)
        }
        (context.applicationContext as Application).registerActivityLifecycleCallbacks(CallKitActivityLifecycleCallback())
        // Register the activities which you have registered in manifest
        EaseCallKit.getInstance().registerVideoCallClass(CallSingleBaseActivity::class.java)
        EaseCallKit.getInstance().registerMultipleVideoClass(CallMultipleBaseActivity::class.java)
        val callKitListener = DemoCallKitListener(context)
        EaseCallKit.getInstance().setCallKitListener(callKitListener)
    }

    /**
     * Show single chat video call dialog.
     */
    fun showSelectDialog(type: EaseChatType?, context: Context, conversationId: String?) {
        val context = (context as FragmentActivity)
        val mutableListOf = mutableListOf(
            EaseMenuItem(
                menuId = R.id.chat_video_call_voice,
                title = context.getString(R.string.voice_call),
                resourceId = R.drawable.phone_pick,
                titleColor = ContextCompat.getColor(context, R.color.color_primary),
                resourceTintColor = ContextCompat.getColor(context, R.color.color_primary)
            ),
            EaseMenuItem(
                menuId = R.id.chat_video_call_video,
                title = context.getString(R.string.video_call),
                resourceId =  R.drawable.video_camera,
                titleColor = ContextCompat.getColor(context, R.color.color_primary),
                resourceTintColor = ContextCompat.getColor(context, R.color.color_primary)
            ),
        )
        val dialog = SimpleListSheetDialog(
            context = context,
            itemList = mutableListOf,
            type = SimpleSheetType.ITEM_LAYOUT_DIRECTION_START)
        dialog.setSimpleListSheetItemClickListener(object : SimpleListSheetItemClickListener {
            override fun onItemClickListener(position: Int, menu: EaseMenuItem) {
                dialog.dismiss()
                when(menu.menuId){
                    R.id.chat_video_call_voice -> {
                        type?.let {
                            if (it == EaseChatType.SINGLE_CHAT){
                                startSingleAudioCall(conversationId)
                            }else{
                                startConferenceCall(EaseCallType.CONFERENCE_VOICE_CALL,context, conversationId)
                            }
                        }
                    }
                    R.id.chat_video_call_video -> {
                        type?.let {
                            if (it == EaseChatType.SINGLE_CHAT){
                                startSingleVideoCall(conversationId)
                            }else{
                                startConferenceCall(EaseCallType.CONFERENCE_VIDEO_CALL, context, conversationId)
                            }
                        }
                    }
                    else -> {}
                }
            }
        })
        context.supportFragmentManager.let { dialog.show(it,"video_call_dialog") }
    }

    /**
     * Start single audio call.
     */
    fun startSingleAudioCall(conversationId: String?) {
        channelName = ""
        rtcType = EaseCallType.SINGLE_VOICE_CALL.ordinal
        EaseCallKit.getInstance().startSingleCall(
            EaseCallType.SINGLE_VOICE_CALL, conversationId, null,
            CallSingleBaseActivity::class.java
        )
    }

    /**
     * Start single video call.
     */
    fun startSingleVideoCall(conversationId: String?) {
        channelName = ""
        rtcType = EaseCallType.SINGLE_VIDEO_CALL.ordinal
        EaseCallKit.getInstance().startSingleCall(
            EaseCallType.SINGLE_VIDEO_CALL, conversationId, null,
            CallSingleBaseActivity::class.java
        )
    }


    /**
     * Receive call push.
     */
    fun receiveCallPush(context: Context) {
        if (isRtcCall) {
            if (EaseCallType.getfrom(rtcType) != EaseCallType.CONFERENCE_VIDEO_CALL &&  EaseCallType.getfrom(rtcType) !=EaseCallType.CONFERENCE_VOICE_CALL) {
                startVideoCallActivity(context)
            } else {
                startMultipleVideoActivity(context)
            }
            isRtcCall = false
        }
    }

    private fun startVideoCallActivity(context: Context) {
        rtcType = EaseCallType.SINGLE_VIDEO_CALL.ordinal
        Intent(context, CallSingleBaseActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    private fun startMultipleVideoActivity(context: Context) {
        rtcType = EaseCallType.CONFERENCE_VIDEO_CALL.ordinal
        Intent(context, CallMultipleBaseActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(this)
        }
    }

    /**
     * Start conference call.
     */
    fun startConferenceCall(callType:EaseCallType,context: Context, groupId: String?) {
        rtcType = callType.ordinal
        val intent = Intent(context, CallMultipleInviteActivity::class.java)
        intent.putExtra(EXTRA_CONFERENCE_GROUP_ID, groupId)
        context.startActivity(intent)
    }


    /**
     * Get rtc token from server.
     */
    fun getRtcToken(tokenUrl: String, callback: EaseCallKitTokenCallback?) {
        executeGetRequest(tokenUrl) {
            it?.let { response ->
                ChatLog.d(TAG, "getRtcToken: url:$tokenUrl ${response.code}, ${response.content}")
                if (response.code == 200) {
                    response.content?.let { body ->
                        try {
                            val result = JSONObject(body)
                            val token = result.getString(RESULT_PARAM_TOKEN)
                            val uid = result.getInt(RESULT_PARAM_UID)
                            EaseIM.getCurrentUser()?.let { profile->
                                setEaseCallKitUserInfo(profile.id)
                            }
                            callback?.onSetToken(token, uid)
                        } catch (e: Exception) {
                            e.stackTrace
                            callback?.onGetTokenError(ChatError.GENERAL_ERROR, e.message)
                        }
                    }
                } else {
                    callback?.onGetTokenError(response.code, response.content)
                }
            } ?: kotlin.run {
                callback?.onSetToken(null, 0)
            }
        }
    }

    fun getUserIdByAgoraUid(uId:Int,url: String, callback: EaseCallGetUserAccountCallback?) {
        executeGetRequest(url) {
            it?.let { response ->
                ChatLog.d(TAG, "getAllUsersByUid: url:$url ${response.code}, ${response.content}")
                if (response.code == 200) {
                    response.content?.let { body ->
                        try {
                            val result = JSONObject(body)
                            val userList = result.getJSONObject(RESULT_PARAM_RESULT)
                            var account: EaseUserAccount? = null
                            userList.keys().forEach { uIdStr ->
                                val uid = Integer.valueOf(uIdStr)
                                val userId = userList.optString(uIdStr)
                                // Set user info to call kit.
                                CallUserInfo(userId).getUserInfo(currentCallGroupId).parse().apply {
                                    EaseCallKit.getInstance().callKitConfig.setUserInfo(userId, this)
                                }
                                if (uid == uId || uid == 0){
                                    account = EaseUserAccount(uIdStr.toInt(), userId)
                                }
                            }
                            callback?.onUserAccount(account)
                        } catch (e: Exception) {
                            e.stackTrace
                            callback?.onSetUserAccountError(ChatError.GENERAL_ERROR, e.message)
                        }
                    }
                } else {
                    callback?.onSetUserAccountError(response.code, response.content)
                }
            } ?: kotlin.run {
                callback?.onSetUserAccountError(ChatError.GENERAL_ERROR, "response is null")
            }
        }
    }

    /**
     * Base get request.
     */
    private fun executeGetRequest(url: String, callback: (ChatHttpResponse?) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            ChatHttpClientManagerBuilder()
                .get()
                .setUrl(url)
                .withToken(true)
                .execute()?.let { response ->
                    callback(response)
                } ?: kotlin.run {
                callback(null)
            }
        }
    }

    fun setEaseCallKitUserInfo(userName: String) {
        val user: EaseUser? = EaseIM.getUserProvider()?.getSyncUser(userName)?.toUser()
        val userInfo = EaseCallUserInfo()
        user?.let {
            userInfo.nickName = user.getNickname()?:userName
            userInfo.headImage = user.avatar
        }
        EaseCallKit.getInstance().callKitConfig.setUserInfo(userName, userInfo)
    }

    fun checkChannelNameNullOrEmpty():Boolean{
        return channelName.isNullOrEmpty()
    }

}