package io.agora.chatdemo.common.extensions.internal

import io.agora.uikit.common.ChatUserInfo
import io.agora.uikit.model.EaseProfile

internal fun ChatUserInfo.toProfile(): EaseProfile {
    return EaseProfile(
        id = userId,
        name = nickname,
        avatar = avatarUrl
    )
}