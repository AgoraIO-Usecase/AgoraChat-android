package io.agora.chatdemo.repository

import io.agora.chatdemo.base.BaseRepository
import io.agora.chatdemo.common.suspend.getSilentModeForApp
import io.agora.chatdemo.common.suspend.setSilentModeForApp
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatSilentModeParam
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PushRepository: BaseRepository() {
    private val pushManager = ChatClient.getInstance().pushManager()

    /**
     * Make app interruption-free
     */
    suspend fun setSilentModeForApp(silentModeParam: ChatSilentModeParam) =
        withContext(Dispatchers.IO) {
            pushManager.setSilentModeForApp(silentModeParam)
        }

    /**
     * Get the silent mode for the App.
     */
    suspend fun getSilentModeForApp() =
        withContext(Dispatchers.IO) {
            pushManager.getSilentModeForApp()
        }

}