package io.agora.chatdemo.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.agora.chat.ChatClient
import io.agora.chatdemo.DemoHelper
import io.agora.util.EMLog

class FCMMSGService: FirebaseMessagingService() {
    private val TAG = "FCMMSGService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage.data.isNotEmpty()) {
            val message = remoteMessage.data["alert"]
            EMLog.i(TAG, "onMessageReceived: $message")
            DemoHelper.getInstance().getNotifier()?.notify(message)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        EMLog.i(TAG, "onNewToken: $token")
        ChatClient.getInstance().sendFCMTokenToServer(token)
    }
}