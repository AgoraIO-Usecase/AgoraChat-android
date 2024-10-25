package io.agora.chatdemo.common.suspend

import io.agora.uikit.common.ChatException
import io.agora.uikit.common.ChatPushManager
import io.agora.uikit.common.ChatSilentModeParam
import io.agora.uikit.common.ChatSilentModeResult
import io.agora.uikit.common.impl.ValueCallbackImpl
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Set the silent mode for the App.
 * @param silentModeParam The silent mode param, see [ChatSilentModeParam].
 * @return The result of setting silent mode. See [ChatSilentModeResult].
 */
suspend fun ChatPushManager.setSilentModeForApp(silentModeParam: ChatSilentModeParam): ChatSilentModeResult {
    return suspendCoroutine { continuation ->
        setSilentModeForAll(silentModeParam, ValueCallbackImpl<ChatSilentModeResult>(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { error, errorDescription ->
                continuation.resumeWithException(ChatException(error, errorDescription))
            }
        ))
    }
}

/**
 * Get the silent mode for the App.
 * @return The result of getting silent mode. See [ChatSilentModeResult].
 */
suspend fun ChatPushManager.getSilentModeForApp(): ChatSilentModeResult {
    return suspendCoroutine { continuation ->
        getSilentModeForAll(ValueCallbackImpl<ChatSilentModeResult>(
            onSuccess = {
                continuation.resume(it)
            },
            onError = { error, errorDescription ->
                continuation.resumeWithException(ChatException(error, errorDescription))
            }
        ))
    }
}