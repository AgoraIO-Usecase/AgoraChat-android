package io.agora.chatdemo.page.group.repository

import io.agora.chatdemo.base.BaseRepository
import io.agora.uikit.common.ChatClient
import io.agora.uikit.common.ChatException
import io.agora.uikit.common.ChatGroup
import io.agora.uikit.common.ChatValueCallback
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class GroupRepository: BaseRepository() {
    val groupManager = ChatClient.getInstance().groupManager()

    /**
     * Suspend method for [ChatGroupManager.asyncGetJoinedGroupsFromServer()]
     */
    suspend fun asyncGetJoinedGroupsFromServer(): List<ChatGroup> {
        return suspendCoroutine { continuation->
            groupManager.asyncGetJoinedGroupsFromServer(object : ChatValueCallback<MutableList<ChatGroup>> {
                override fun onSuccess(value: MutableList<ChatGroup>) {
                    continuation.resume(value)
                }
                override fun onError(error: Int, errorMsg: String?) {
                    continuation.resumeWithException(ChatException(error, errorMsg))
                }
            })
        }
    }


}