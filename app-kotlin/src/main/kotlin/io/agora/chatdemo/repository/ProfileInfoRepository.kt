package io.agora.chatdemo.repository

import io.agora.CallBack
import io.agora.chat.UserInfo
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.base.BaseRepository
import io.agora.chatdemo.common.suspend.fetUserInfo
import io.agora.chatdemo.common.suspend.updateOwnAttribute
import io.agora.cloud.HttpCallback
import io.agora.cloud.HttpClientManager
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatError
import io.agora.uikit.common.ChatException
import io.agora.uikit.common.ChatHttpClientManagerBuilder
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatUserInfo
import io.agora.uikit.common.ChatUserInfoType
import io.agora.uikit.common.ChatValueCallback
import io.agora.uikit.model.EaseProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProfileInfoRepository: BaseRepository()  {

    companion object {
        private const val UPLOAD_AVATAR_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_USER
        private const val GROUP_AVATAR_URL = BuildConfig.APP_SERVER_PROTOCOL + "://" + BuildConfig.APP_SERVER_DOMAIN +
                BuildConfig.APP_BASE_GROUP
    }

    suspend fun getUserInfoAttribute(userIds: List<String>, attributes: List<ChatUserInfoType>): Map<String, ChatUserInfo> =
        withContext(Dispatchers.IO) {
            ChatClient.getInstance().userInfoManager().fetUserInfo(userIds,attributes)
        }

    suspend fun synchronizeProfile(isSyncFromServer:Boolean): EaseProfile? =
        withContext(Dispatchers.IO) {
            val currentProfile = EaseIM.getCurrentUser()?:EaseProfile(ChatClient.getInstance().currentUser)
            val user = DemoHelper.getInstance().getDataModel().getUser(currentProfile.id)
            ChatLog.e("ProfileInfoRepository","synchronizeProfile $user $isSyncFromServer - $currentProfile")
            suspendCoroutine { continuation ->
                if (user == null || isSyncFromServer){
                    currentProfile.let { profile->
                        val ids = mutableListOf(profile.id)
                        val type = mutableListOf(ChatUserInfoType.NICKNAME,ChatUserInfoType.AVATAR_URL)
                        ChatClient.getInstance().userInfoManager().fetchUserInfoByAttribute(ids.toTypedArray(),type.toTypedArray(),object :
                            ChatValueCallback<MutableMap<String, UserInfo>> {
                            override fun onSuccess(value: MutableMap<String, UserInfo>?) {
                                ChatLog.e("ProfileInfoRepository","fetchUserInfoByAttribute onSuccess ${profile.id} - $value")
                                value?.let { map->
                                    if (value.containsKey(profile.id)){
                                        map[profile.id]?.let {
                                            profile.name = it.nickname
                                            profile.avatar = it.avatarUrl
                                            DemoHelper.getInstance().getDataModel().insertUser(profile)
                                            EaseIM.updateUsersInfo(mutableListOf(profile))
                                            continuation.resume(profile)
                                        }
                                    }
                                }
                            }

                            override fun onError(error: Int, errorMsg: String?) {
                                ChatLog.e("ProfileInfoRepository","fetchUserInfoByAttribute onError$error $errorMsg")
                                continuation.resumeWithException(ChatException(error, errorMsg))
                            }
                        })
                    }
                }else{
                    ChatLog.e("ProfileInfoRepository","fetchLocalUserInfo")
                    currentProfile.let {
                        it.name = user.name
                        it.avatar = user.avatar
                        EaseIM.updateUsersInfo(mutableListOf(it))
                    }
                    continuation.resume(currentProfile)
                }
            }
        }

    suspend fun setUserRemark(username:String,remark:String): Int =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                ChatClient.getInstance().contactManager().asyncSetContactRemark(username,remark,object :
                    CallBack {
                    override fun onSuccess() {
                        continuation.resume(ChatError.EM_NO_ERROR)
                    }

                    override fun onError(code: Int, error: String?) {
                        continuation.resumeWithException(ChatException(code, error))
                    }
                })
            }
        }

    suspend fun getGroupAvatar(groupId:String?): String =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                getGroupAvatarFromServer(groupId,object : ChatValueCallback<String>{
                    override fun onSuccess(value: String?) {
                        value?.let {
                            continuation.resume(it)
                        }
                    }

                    override fun onError(code: Int, errorMsg: String?) {
                        continuation.resumeWithException(ChatException(code, errorMsg))
                    }
                })
            }
        }

    /**
     * Update the nickname of the current user to chat server.
     * @param nickname The new nickname.
     */
    suspend fun updateNickname(nickname: String) =
        withContext(Dispatchers.IO) {
            ChatClient.getInstance().userInfoManager().updateOwnAttribute(ChatUserInfoType.NICKNAME, nickname)
        }

    /**
     * Upload avatar url to chat server.
     * @param remoteUrl The remote url of the avatar
     */
    suspend fun uploadAvatarToChatServer(remoteUrl: String) =
        withContext(Dispatchers.IO) {
            ChatClient.getInstance().userInfoManager().updateOwnAttribute(ChatUserInfoType.AVATAR_URL, remoteUrl)
        }

    /**
     * 上传头像
     * @return
     */
    suspend fun uploadAvatar(filePath: String?): String =
        withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                uploadAvatarToAppServer(filePath,object : ChatValueCallback<String>{
                    override fun onSuccess(value: String?) {
                        value?.let {
                            continuation.resume(it)
                        }
                    }

                    override fun onError(error: Int, errorMsg: String?) {
                        continuation.resumeWithException(ChatException(error, errorMsg))
                    }
                })
            }
        }

    private fun uploadAvatarToAppServer(
        filePath: String?,
        callBack: ChatValueCallback<String>
    ){
        try {
            ChatLog.e("ProfileInfoRepository","uploadAvatarToAppServer $filePath")
            if (filePath.isNullOrEmpty()){
                callBack.onError(ChatError.INVALID_URL," invalid url.")
                return
            }
            ChatLog.e("ProfileInfoRepository","uploadAvatarToAppServer ${UPLOAD_AVATAR_URL +"/${ChatClient.getInstance().currentUser}"+BuildConfig.APP_UPLOAD_AVATAR}")
                ChatHttpClientManagerBuilder()
                    .uploadFile(filePath)
                    .setParam("file",filePath)
                    .setUrl(UPLOAD_AVATAR_URL +"/${ChatClient.getInstance().currentUser}"+BuildConfig.APP_UPLOAD_AVATAR)
                    .execute(object : HttpCallback {
                        override fun onSuccess(result: String?) {
                            result?.let {
                                val url = try {
                                val jsonObject = JSONObject(it)
                                jsonObject.getString("avatarUrl")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                ""
                            }
                                callBack.onSuccess(url)
                            } ?: callBack.onError(ChatError.NETWORK_ERROR,"result url is null.")
                        }

                        override fun onError(code: Int, msg: String?) {
                            callBack.onError(code,msg)
                        }

                        override fun onProgress(total: Long, pos: Long) {

                        }
                    })
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }

    private fun getGroupAvatarFromServer(
        groupId: String?,
        callBack: ChatValueCallback<String>
    ){
        try {
            if (groupId.isNullOrEmpty()){
                callBack.onError(ChatError.GROUP_INVALID_ID, "The group ID is incorrect")
                return
            }
            val headers: MutableMap<String, String> = HashMap()
            headers["Content-Type"] = "application/json"
            val url: String = GROUP_AVATAR_URL + "/$groupId" +BuildConfig.APP_GROUP_AVATAR
            val response = HttpClientManager.httpExecute(
                url,
                headers, "",
                HttpClientManager.Method_GET
            )
            val code = response.code
            val responseInfo = response.content
            if (code == 200) {
                val `object` = JSONObject(responseInfo)
                val avatarUrl = `object`.getString("avatarUrl")
                callBack.onSuccess(avatarUrl)
            } else {
                if (responseInfo != null && responseInfo.isNotEmpty()) {
                    var errorInfo: String? = null
                    try {
                        val responseObject = JSONObject(responseInfo)
                        errorInfo = responseObject.getString("errorInfo")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        errorInfo = responseInfo
                    }
                    callBack.onError(code, errorInfo)
                } else {
                    callBack.onError(code, responseInfo)
                }
            }
        } catch (e: Exception) {
            callBack.onError(ChatError.NETWORK_ERROR, e.message)
        }
    }
}