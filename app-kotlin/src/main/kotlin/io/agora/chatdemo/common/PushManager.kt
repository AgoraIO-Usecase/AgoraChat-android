package io.agora.chatdemo.common

import android.app.Application
import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import io.agora.chatdemo.DemoHelper
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatPushHelper
import io.agora.uikit.common.ChatPushListener
import io.agora.uikit.common.ChatPushType
import io.agora.uikit.common.PushConfig
import io.agora.uikit.common.extensions.isMainProcess

object PushManager {

    /**
     * Initialize push.
     */
    fun initPush(context: Context) {
        if (context.isMainProcess()) {
            // Register push activity lifecycle callback.
            (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(PushActivityLifecycleCallback())
            // Set pushListener to control the push type.
            ChatPushHelper.getInstance().setPushListener(object : ChatPushListener() {

                override fun onError(pushType: ChatPushType?, errorCode: Long) {
                    // 返回的errorCode仅9xx为环信内部错误，可从EMError中查询，其他错误请根据pushType去相应第三方推送网站查询。
                    ChatLog.e("PushManager", "onError: pushType: $pushType, errorCode: $errorCode")
                }

                override fun isSupportPush(
                    pushType: ChatPushType?,
                    pushConfig: PushConfig?
                ): Boolean {
                    if (pushType == ChatPushType.FCM) {
                        ChatLog.d("FCM",
                            "GooglePlayServiceCode:" + GoogleApiAvailabilityLight.getInstance()
                                .isGooglePlayServicesAvailable(context)
                        )
                        return DemoHelper.getInstance().getDataModel().isUseFCM()
                                && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
                    }
                    return super.isSupportPush(pushType, pushConfig)
                }

            })

        }
    }

    /**
     * Get the push token and send to Chat Server.
     */
    fun getPushTokenAndSend(context: Context) {
        // Get FCM push token.
        getFCMTokenAndSend(context)
    }

    /**
     * Get FCM push token and send to Chat Server.
     */
    private fun getFCMTokenAndSend(context: Context) {
        if (DemoHelper.getInstance().getDataModel().isUseFCM()
            && GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            // Enable FCM automatic initialization
            if (FirebaseMessaging.getInstance().isAutoInitEnabled.not()) {
                FirebaseMessaging.getInstance().isAutoInitEnabled = true
                FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(true)
            }
            // Get FCM push token and send to Chat Server.
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                if (it.isSuccessful.not()) {
                    ChatLog.e("FCM", "get FCM push token failed: ${it.exception}")
                    return@addOnCompleteListener
                }
                val token = it.result
                ChatLog.d("FCM", "get FCM push token: $token")
                ChatClient.getInstance().sendFCMTokenToServer(token)
            }
        }
    }
}