package io.agora.chatdemo.common.room.extensions

import io.agora.chatdemo.common.room.entity.DemoUser
import io.agora.uikit.common.ChatUserInfo
import io.agora.uikit.model.EaseProfile

internal fun EaseProfile.parseToDbBean() = DemoUser(id, name, avatar, remark)

internal fun ChatUserInfo.parseToDbBean(): DemoUser {
    return DemoUser(
        userId = userId,
        name = nickname,
        avatar = avatarUrl
    )
}