package io.agora.chatdemo.common.extensions.internal

import io.agora.chat.uikit.common.ChatUserInfo
import io.agora.chat.uikit.model.ChatUIKitProfile

internal fun ChatUserInfo.toProfile(): ChatUIKitProfile {
    return ChatUIKitProfile(
        id = userId,
        name = nickname,
        avatar = avatarUrl
    )
}