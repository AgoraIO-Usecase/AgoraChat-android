package io.agora.chatdemo.common.extensions.internal

import io.agora.chat.uikit.common.ChatGroup
import io.agora.chat.uikit.model.ChatUIKitGroupProfile

internal fun ChatGroup.parse(): ChatUIKitGroupProfile {
    return ChatUIKitGroupProfile(groupId, groupName, extension)
}