package io.agora.chatdemo

import android.content.Context
import android.util.Log
import io.agora.chatdemo.callkit.CallKitManager
import io.agora.chatdemo.common.DemoDataModel
import io.agora.chatdemo.common.ListenersWrapper
import io.agora.chatdemo.common.PushManager
import io.agora.chatdemo.common.extensions.internal.checkAppKey
import io.agora.chatdemo.uikit.UIKitManager
import io.agora.uikit.EaseIM
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatOptions
import io.agora.uikit.common.PushConfigBuilder

class DemoHelper private constructor(){

    private lateinit var dataModel: DemoDataModel
    var hasAppKey = false
    lateinit var context: Context

    @Synchronized
    fun init(context: Context) {
        this.context = context.applicationContext
        dataModel = DemoDataModel(context)
        initSDK()
    }

    fun getDataModel(): DemoDataModel {
        return dataModel
    }

    /**
     * Check if the SDK has been initialized.
     */
    fun isSDKInited(): Boolean {
        return EaseIM.isInited()
    }

    /**
     * Initialize the SDK.
     */
    @Synchronized
    fun initSDK() {
        if (::context.isInitialized.not()) {
            Log.e(TAG, "Please call init method first.")
            return
        }
        initChatOptions(context).apply {
            hasAppKey = checkAppKey(context)
            if (!hasAppKey) {
                Log.e(TAG, "App key is null or empty.")
                return
            }
            // Register necessary listeners
            ListenersWrapper.registerListeners()
            isLoadEmptyConversations = true
            EaseIM.init(context, this)
            if (EaseIM.isInited()) {
                // debug mode, you'd better set it to false, if you want release your App officially.
                ChatClient.getInstance().setDebugMode(true)
                // Initialize push.
                initPush()
                // Set the UIKit options.
                addUIKitSettings()
                // Initialize the callkit module.
                initCallKit()
            }
        }
    }

    private fun addUIKitSettings() {
        UIKitManager.addUIKitSettings(context)
    }

    private fun initPush() {
        PushManager.initPush(context)
    }

    /**
     * Get the notifier.
     */
    fun getNotifier() = EaseIM.getNotifier()

    private fun initCallKit() {
        CallKitManager.init(context)
    }

    /**
     * Set chat options.
     * Note: Developers need to set the options according to needs.
     */
    private fun initChatOptions(context: Context): ChatOptions {
        return ChatOptions().apply {
            // set the appkey
            appKey = BuildConfig.AGORA_CHAT_APPKEY
            // set if accept the invitation automatically, default true
            acceptInvitationAlways = false
            // set if you need read ack
            requireAck = true
            // Set whether the sent message is included in the message listener, default false
            isIncludeSendMessageInMessageListener = true

            getDataModel().setUseFCM(true)

            /**
             * Note: Developers need to apply your own push accounts and replace the following
             */
            pushConfig = PushConfigBuilder(context)
                .enableFCM(BuildConfig.FCM_SENDERID)
                .build()

            if (dataModel.isDeveloperMode()) {

                if (dataModel.getCustomAppKey().isNotEmpty()){
                    dataModel.getCustomAppKey().let {
                        appKey = it
                    }
                }

                if (dataModel.isCustomServerEnable()) {
                    // Turn off DNS configuration
                    enableDNSConfig(false)
                    restServer = dataModel.getRestServer()?.ifEmpty { null }
                    setIMServer(dataModel.getIMServer()?.let {
                        if (it.contains(":")) {
                            imPort = it.split(":")[1].toInt()
                            it.split(":")[0]
                        } else {
                            it.ifEmpty { null }
                        }
                    })
                    val port = dataModel.getIMServerPort()
                    if (port != 0) {
                        imPort = port
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "DemoHelper"
        private var instance: DemoHelper? = null
        fun getInstance(): DemoHelper {
            if (instance == null) {
                synchronized(DemoHelper::class.java) {
                    if (instance == null) {
                        instance = DemoHelper()
                    }
                }
            }
            return instance!!
        }
    }
}