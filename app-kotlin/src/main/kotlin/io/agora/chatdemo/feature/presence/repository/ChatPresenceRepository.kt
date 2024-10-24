package io.agora.chatdemo.feature.presence.repository

import io.agora.chatdemo.common.suspend.fetchUserPresenceStatus
import io.agora.chatdemo.common.suspend.publishExtPresence
import io.agora.chatdemo.common.suspend.subscribeUsersPresence
import io.agora.chatdemo.common.suspend.unSubscribeUsersPresence
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatPresenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatPresenceRepository(
    private val presenceManager: ChatPresenceManager = ChatClient.getInstance().presenceManager(),
) {

    suspend fun publishPresence(customStatus: String) =
        withContext(Dispatchers.IO) {
            presenceManager.publishExtPresence(customStatus)
        }


    suspend fun subscribePresences(userIds:MutableList<String>,expiry:Long) =
        withContext(Dispatchers.IO) {
            presenceManager.subscribeUsersPresence(userIds,expiry)
        }


    suspend fun unSubscribePresences(userIds:MutableList<String>) =
        withContext(Dispatchers.IO) {
            presenceManager.unSubscribeUsersPresence(userIds)
        }

    suspend fun fetchPresenceStatus(userIds:MutableList<String>) =
        withContext(Dispatchers.IO) {
            presenceManager.fetchUserPresenceStatus(userIds)
        }


}