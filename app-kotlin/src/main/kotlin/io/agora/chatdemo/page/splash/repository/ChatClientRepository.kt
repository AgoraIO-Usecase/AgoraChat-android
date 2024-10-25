package io.agora.chatdemo.page.splash.repository

import android.util.Log
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.base.BaseRepository
import io.agora.chatdemo.bean.LoginResult
import io.agora.chatdemo.common.ErrorCode
import io.agora.chatdemo.common.helper.DeveloperModeHelper
import io.agora.cloud.HttpClientManager
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatError
import io.agora.uikit.common.ChatException
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatValueCallback
import io.agora.uikit.feature.invitation.helper.EaseNotificationMsgManager
import io.agora.uikit.model.EaseProfile
import io.agora.uikit.model.EaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * As the repository of ChatManager, handles ChatManager related logic
 */
class ChatClientRepository: BaseRepository() {

    companion object {
        private const val LOGIN_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_SERVER_URL
    }

    /**
     * 登录过后需要加载的数据
     * @return
     */
    suspend fun loadAllInfoFromAgora(): Boolean =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                ChatLog.e("login info","isLoggedInBefore ${ChatClient.getInstance().isLoggedInBefore} - autoLogin ${ChatClient.getInstance().options.autoLogin}")
                if (ChatClient.getInstance().isLoggedInBefore && ChatClient.getInstance().options.autoLogin) {
                    loadAllConversationsAndGroups()
                    continuation.resume(true)
                } else {
                    continuation.resumeWithException(ChatException(ErrorCode.EM_NOT_LOGIN, ""))
                }
            }
        }


    /**
     * Login to app server and get token.
     */
    suspend fun loginFromServer(userName: String, userPassword: String): LoginResult =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                loginFromAppServer(userName, userPassword, object : ChatValueCallback<LoginResult> {
                    override fun onSuccess(value: LoginResult) {
                        continuation.resume(value)
                    }

                    override fun onError(code: Int, error: String?) {
                        continuation.resumeWithException(ChatException(code, error))
                    }
                })
            }
        }

    /**
     * logout
     * @param unbindDeviceToken
     * @return
     */
    suspend fun logout(unbindDeviceToken: Boolean): Int =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                EaseIM.logout(unbindDeviceToken, onSuccess = {
                    continuation.resume(ChatError.EM_NO_ERROR)
                }, onError = { code, error ->
                    continuation.resumeWithException(ChatException(code, error))
                })
            }
        }

    /**
     * Get all unread message count.
     */
    suspend fun getAllUnreadMessageCount(): Int =
        withContext(Dispatchers.IO) {
            val systemConversation = EaseNotificationMsgManager.getInstance().getConversation()
            val systemUnread = systemConversation.unreadMsgCount
            val allUnread = ChatClient.getInstance().chatManager().unreadMessageCount
            ChatLog.d("ChatClientRepository","getAllUnreadMessageCount ${allUnread - systemUnread}")
            allUnread - systemUnread
        }

    /**
     * Get all unread request count.
     */
    suspend fun getRequestUnreadCount():Int =
        withContext(Dispatchers.IO) {
            val systemConversation = EaseNotificationMsgManager.getInstance().getConversation()
            ChatLog.d("ChatClientRepository","getRequestUnreadCount ${systemConversation.unreadMsgCount}")
            systemConversation.unreadMsgCount
        }

    /**
     * 从本地数据库加载所有的对话及群组
     */
    private fun loadAllConversationsAndGroups() {
        // 从本地数据库加载所有的对话及群组
        ChatClient.getInstance().chatManager().loadAllConversations()
        ChatClient.getInstance().groupManager().loadAllGroups()
    }

    private fun successForCallBack(agoraUid: Int,continuation: Continuation<EaseUser>) {
        DemoHelper.getInstance().getDataModel().setCurrentUserAgoraUid(agoraUid)
        // get current user id
        val currentUser = ChatClient.getInstance().currentUser
        val user = EaseUser(currentUser)
        continuation.resume(user)

        // ** manually load all local groups and conversation
        loadAllConversationsAndGroups()
    }


    private fun loginFromAppServer(
        userName: String,
        token: String,
        callBack: ChatValueCallback<LoginResult>
    ) {
        try {
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            val request = JSONObject()
            request.putOpt("userAccount", userName)
            request.putOpt("userPassword", token)
            val url: String = LOGIN_URL
            ChatLog.d("LoginToAppServer url : ", url)
            val response = HttpClientManager.httpExecute(
                url,
                headers,
                request.toString(),
                HttpClientManager.Method_POST
            )
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                ChatLog.d("LoginToAppServer success : ", responseInfo)
                val `object` = JSONObject(responseInfo)
                val result = LoginResult()
                if (`object`.has("accessToken")){
                    result.accessToken = `object`.getString("accessToken")
                }
                if (`object`.has("token")){
                    result.token = `object`.getString("token")
                }
                if (`object`.has("expireTimestamp")){
                    result.expireTimestamp = `object`.getLong("expireTimestamp")
                }
                if (`object`.has("chatUserName")){
                    result.chatUserName = `object`.getString("chatUserName")
                }
                if (`object`.has("agoraUid")){
                    result.agoraUid = `object`.getInt("agoraUid")
                }
                if (`object`.has("avatarUrl")){
                    result.avatarUrl = `object`.getString("avatarUrl")
                }
                result.code = code
                callBack.onSuccess(result)
            } else {
                var errorInfo: String? = null
                responseInfo?.let {
                    try {
                        val responseObject = JSONObject(responseInfo)
                        Log.e("apex","login response $responseObject")
//                        errorInfo = responseObject.getString("errorInfo")
//                        if (errorInfo.contains("phone number illegal")) {
//                            errorInfo = DemoApplication.getInstance().getString(R.string.em_login_phone_illegal)
//                        } else if (errorInfo.contains("verification code error") || errorInfo.contains(
//                                "send SMS to get mobile phone verification code"
//                            )
//                        ) {
//                            errorInfo = DemoApplication.getInstance().getString(R.string.em_login_illegal_code)
//                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    callBack.onError(code, errorInfo)
                }?:kotlin.run {
                    callBack.onError(code, responseInfo)
                }
            }
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

    /**
     * Login to the server, you can choose password login or token login
     * @param userName
     * @param pwd
     * @param isTokenFlag
     * @return
     */
    suspend fun loginToServer(
        userName: String,
        pwd: String,
        agoraUid:Int,
        isTokenFlag: Boolean
    ): EaseUser =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                if (ChatClient.getInstance().isLoggedIn.not()) {
                    if (DeveloperModeHelper.isCustomSetEnable()) {
                        DemoHelper.getInstance().getDataModel().getCustomAppKey().let {
                            if (it.isNotEmpty()) {
                                ChatClient.getInstance().changeAppkey(it)
                            }else{
                                ChatClient.getInstance().options.enableDNSConfig(true)
                                ChatClient.getInstance().changeAppkey(BuildConfig.AGORA_CHAT_APPKEY)
                            }
                        }
                    } else {
                        ChatClient.getInstance().options.enableDNSConfig(true)
                        ChatClient.getInstance().changeAppkey(BuildConfig.AGORA_CHAT_APPKEY)
                    }
                }
                if (isTokenFlag) {
                    EaseIM.login(EaseProfile(userName), pwd, onSuccess = {
                        successForCallBack(agoraUid,continuation)
                    }, onError = { code, error ->
                        if(code == ChatError.USER_ALREADY_LOGIN){
                            if (EaseIM.getCurrentUser()?.id == userName){
                                successForCallBack(agoraUid,continuation)
                            }else{
                                EaseIM.logout(true)
                                continuation.resumeWithException(ChatException(code, error))
                            }
                        }else{
                            continuation.resumeWithException(ChatException(code, error))
                        }
                    })
                } else {
                    EaseIM.login(userName, pwd, onSuccess = {
                        successForCallBack(agoraUid,continuation)
                    }, onError = { code, error ->
                        continuation.resumeWithException(ChatException(code, error))
                    })
                }
            }
        }
}