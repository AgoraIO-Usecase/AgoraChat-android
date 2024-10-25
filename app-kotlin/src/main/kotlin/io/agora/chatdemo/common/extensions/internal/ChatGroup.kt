package io.agora.chatdemo.common.extensions.internal

import io.agora.uikit.common.ChatGroup
import io.agora.uikit.model.EaseGroupProfile

internal fun ChatGroup.parse(): EaseGroupProfile {
    return EaseGroupProfile(groupId, groupName, extension)
}