package io.agora.chatdemo.callkit

import android.util.Log
import io.agora.chat.callkit.bean.EaseCallUserInfo
import io.agora.chat.uikit.ChatUIKitClient
import io.agora.chat.uikit.model.ChatUIKitProfile
import io.agora.chat.uikit.provider.getSyncUser


data class CallUserInfo(
    val userId: String?,
    var nickName: String? = null,
    var headImage: String? = null
)

internal fun CallUserInfo.getUserInfo(groupId: String?): CallUserInfo {
    return if (!groupId.isNullOrEmpty()) {
        ChatUIKitProfile.getGroupMember(groupId, this.userId)?.let {
            this.nickName = it.getNotEmptyName()
            this.headImage = it.avatar
        }
        this
    } else {
        ChatUIKitClient.getUserProvider()?.getSyncUser(this.userId)?.let {
            this.nickName = it.getNotEmptyName()
            this.headImage = it.avatar
        }
        this
    }
}

/**
 * Parse to EaseCallUserInfo.
 */
internal fun CallUserInfo.parse(): EaseCallUserInfo {
    return EaseCallUserInfo(nickName, headImage).let {
        it.userId = this.userId
        it
    }
}
