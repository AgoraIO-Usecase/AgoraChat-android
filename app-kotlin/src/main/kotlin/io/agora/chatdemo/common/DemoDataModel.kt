package io.agora.chatdemo.common

import android.content.Context
import android.util.Log
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.room.AppDatabase
import io.agora.chatdemo.common.room.dao.DemoUserDao
import io.agora.chatdemo.common.room.entity.DemoUser
import io.agora.chatdemo.common.room.entity.parse
import io.agora.chatdemo.common.room.extensions.parseToDbBean
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatContact
import io.agora.uikit.common.ChatException
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatValueCallback
import io.agora.uikit.common.extensions.toProfile
import io.agora.uikit.common.extensions.toUser
import io.agora.uikit.model.EaseProfile
import io.agora.uikit.model.EaseUser
import java.util.concurrent.ConcurrentHashMap

class DemoDataModel(private val context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context, ChatClient.getInstance().currentUser) }

    private val contactList = ConcurrentHashMap<String, DemoUser>()


    init {
        PreferenceManager.init(context)
    }

    /**
     * Initialize the local database.
     */
    fun initDb() {
        if (EaseIM.isInited().not()) {
            throw IllegalStateException("EaseIM SDK must be inited before using.")
        }
        database
        resetUsersTimes()
        contactList.clear()
        val data = getAllContacts().values.map { it.toProfile() }
        if (data.isNotEmpty()){
            EaseIM.updateUsersInfo(data)
            data.map { CallKitManager.setEaseCallKitUserInfo(it.id)}
        }
    }

    /**
     * Get the user data access object.
     */
    fun getUserDao(): DemoUserDao {
        if (EaseIM.isInited().not()) {
            throw IllegalStateException("EaseIM SDK must be inited before using.")
        }
        return database.userDao()
    }

    /**
     * Get all contacts from cache.
     */
    fun getAllContacts(): Map<String, EaseUser> {
        if (contactList.isEmpty()) {
            loadContactFromDb()
        }
        return contactList.mapValues { it.value.parse().toUser() }
    }

    private fun loadContactFromDb() {
        contactList.clear()
        try {
            val localData = ChatClient.getInstance().contactManager().contactsFromLocal
            getUserDao().getAll().forEach {
                val profile = it.parse()
                localData?.forEach { contact->
                    if (contact.equals(profile.id)){
                        contactList[it.userId] = profile.parseToDbBean()
                    }
                }
            }
        }catch (e:ChatException){
            ChatLog.e("DemoDataModel","loadContactFromDb error ${e.description}")
        }
    }

    /**
     * Get user by userId from local db.
     */
    fun getUser(userId: String?): DemoUser? {
        if (userId.isNullOrEmpty()) {
            return null
        }
        if (contactList.containsKey(userId)) {
            return contactList[userId]
        }
        return getUserDao().getUser(userId)
    }

    /**
     * Insert user to local db.
     */
    fun insertUser(user: EaseProfile,isInsertDb:Boolean = true) {
        if (isInsertDb){
            getUserDao().insertUser(user.parseToDbBean())
        }
        contactList[user.id] = user.parseToDbBean()
    }

    /**
     * Insert users to local db.
     */
    fun insertUsers(users: List<EaseProfile>) {
        getUserDao().insertUsers(users.map { it.parseToDbBean() })
        users.forEach {
            contactList[it.id] = it.parseToDbBean()
        }
    }

    /**
     * Update user update times.
     */
    fun updateUsersTimes(userIds: List<EaseProfile>) {
        if (userIds.isNotEmpty()) {
            userIds?.map { it.id }?.let { userIds ->
                getUserDao().updateUsersTimes(userIds)
                loadContactFromDb()
            }
        }
    }

    private fun resetUsersTimes() {
        getUserDao().resetUsersTimes()
    }

    fun clearCache(){
        contactList.clear()
    }

    /**
     * Update UIKit's user cache.
     */
    fun updateUserCache(userId: String?) {
        if (userId.isNullOrEmpty()) {
            return
        }
        val user = contactList[userId]?.parse() ?: return
        EaseIM.updateUsersInfo(mutableListOf(user))
    }


    /**
     * Set the flag whether to use google push.
     * @param useFCM
     */
    fun setUseFCM(useFCM: Boolean) {
        PreferenceManager.putValue(KEY_PUSH_USE_FCM, useFCM)
    }

    /**
     * Get the flag whether to use google push.
     * @return
     */
    fun isUseFCM(): Boolean {
        return PreferenceManager.getValue(KEY_PUSH_USE_FCM, false)
    }

    /**
     * Set the developer mode.
     * @param isDeveloperMode The developer mode.
     */
    fun setDeveloperMode(isDeveloperMode: Boolean) {
        PreferenceManager.putValue(KEY_DEVELOPER_MODE, isDeveloperMode)
    }

    /**
     * Get the developer mode.
     * @return The developer mode.
     */
    fun isDeveloperMode(): Boolean {
        return PreferenceManager.getValue(KEY_DEVELOPER_MODE, false)
    }

    /**
     * Set the custom appKey.
     * @param appKey
     */
    fun setCustomAppKey(appKey: String?) {
        PreferenceManager.putValue(KEY_CUSTOM_APPKEY, appKey)
    }

    /**
     * Get the custom appKey.
     * @return
     */
    fun getCustomAppKey(): String {
        return PreferenceManager.getValue(KEY_CUSTOM_APPKEY, "")
    }

    /**
     * Get whether the custom configuration is enabled.
     * @return
     */
    fun isCustomSetEnable(): Boolean {
        return PreferenceManager.getValue(KEY_ENABLE_CUSTOM_SET, false)
    }

    /**
     * Set whether the custom configuration is enabled.
     * @param enable
     */
    fun enableCustomSet(enable: Boolean) {
        PreferenceManager.putValue(KEY_ENABLE_CUSTOM_SET, enable)
    }

    /**
     * Get whether the custom server is enabled.
     * @return
     */
    fun isCustomServerEnable(): Boolean {
        return PreferenceManager.getValue(KEY_ENABLE_CUSTOM_SERVER, false)
    }

    /**
     * Set whether the custom server is enabled.
     * @param enable
     */
    fun enableCustomServer(enable: Boolean) {
        PreferenceManager.putValue(KEY_ENABLE_CUSTOM_SERVER, enable)
    }

    /**
     * Set the REST server.
     * @param restServer
     */
    fun setRestServer(restServer: String?) {
        PreferenceManager.putValue(KEY_REST_SERVER, restServer)
    }

    /**
     * Get the REST server.
     * @return
     */
    fun getRestServer(): String? {
        return PreferenceManager.getValue(KEY_REST_SERVER, "")
    }

    /**
     * Set the IM server.
     * @param imServer
     */
    fun setIMServer(imServer: String?) {
        PreferenceManager.putValue(KEY_IM_SERVER, imServer)
    }

    /**
     * Get the IM server.
     * @return
     */
    fun getIMServer(): String? {
        return PreferenceManager.getValue(KEY_IM_SERVER, "")
    }

    /**
     * Set the port of the IM server.
     * @param port
     */
    fun setIMServerPort(port: Int) {
        PreferenceManager.putValue(KEY_IM_SERVER_PORT, port)
    }

    /**
     * Get the port of the IM server.
     */
    fun getIMServerPort(): Int {
        return PreferenceManager.getValue(KEY_IM_SERVER_PORT, 0)
    }

    /**
     * Set the silent mode for the App.
     */
    fun setAppPushSilent(isSilent: Boolean) {
        PreferenceManager.putValue(KEY_PUSH_APP_SILENT_MODEL, isSilent)
    }

    /**
     * Get the silent mode for the App.
     */
    fun isAppPushSilent(): Boolean {
        return PreferenceManager.getValue(KEY_PUSH_APP_SILENT_MODEL, false)
    }

    fun setCurrentUserAgoraUid(agoraUid:Int){
        PreferenceManager.putValue("${BuildConfig.AGORA_CHAT_APPKEY}$SHARED_KEY_CURRENTUSER_AGORAUID",agoraUid)
    }

    fun getCurrentUserAgoraUid():Int{
        return PreferenceManager.getValue("${BuildConfig.AGORA_CHAT_APPKEY}$SHARED_KEY_CURRENTUSER_AGORAUID",0)
    }

    fun putBoolean(key: String, value: Boolean){
        PreferenceManager.putValue(key,value)
    }

    fun getBoolean(key: String,default:Boolean?=false): Boolean {
        return if (default == null){
            PreferenceManager.getValue(key, false)
        }else{
            PreferenceManager.getValue(key, default)
        }
    }

    companion object {
        private const val KEY_DEVELOPER_MODE = "shared_is_developer"
        private const val KEY_AGREE_AGREEMENT = "shared_key_agree_agreement"
        private const val KEY_CUSTOM_APPKEY = "SHARED_KEY_CUSTOM_APPKEY"
        private const val KEY_REST_SERVER = "SHARED_KEY_REST_SERVER"
        private const val KEY_IM_SERVER = "SHARED_KEY_IM_SERVER"
        private const val KEY_IM_SERVER_PORT = "SHARED_KEY_IM_SERVER_PORT"
        private const val KEY_ENABLE_CUSTOM_SERVER = "SHARED_KEY_ENABLE_CUSTOM_SERVER"
        private const val KEY_ENABLE_CUSTOM_SET = "SHARED_KEY_ENABLE_CUSTOM_SET"
        private const val KEY_PUSH_USE_FCM = "shared_key_push_use_fcm"
        private const val KEY_PUSH_APP_SILENT_MODEL = "key_push_app_silent_model"
        private const val SHARED_KEY_CURRENTUSER_AGORAUID = "SHARED_KEY_CURRENTUSER_AGORAUID"
    }

}