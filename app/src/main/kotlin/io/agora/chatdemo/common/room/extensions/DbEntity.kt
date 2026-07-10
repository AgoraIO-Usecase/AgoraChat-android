package io.agora.chatdemo.common.room.extensions

import io.agora.chatdemo.common.room.entity.DemoUser
import io.agora.chat.uikit.common.ChatUserInfo
import io.agora.chat.uikit.model.ChatUIKitProfile

internal fun ChatUIKitProfile.parseToDbBean() = DemoUser(id, name, avatar, remark)

internal fun ChatUserInfo.parseToDbBean(): DemoUser {
    return DemoUser(
        userId = userId,
        name = nickname,
        avatar = avatarUrl
    )
}