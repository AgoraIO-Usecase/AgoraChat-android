package io.agora.chatdemo.callkit

import android.util.Log
import io.agora.chat.callkit.bean.EaseCallUserInfo
import io.agora.uikit.EaseIM
import io.agora.uikit.model.EaseProfile
import io.agora.uikit.provider.getSyncUser


data class CallUserInfo(
    val userId: String?,
    var nickName: String? = null,
    var headImage: String? = null
)

internal fun CallUserInfo.getUserInfo(groupId: String?): CallUserInfo {
    return if (!groupId.isNullOrEmpty()) {
        EaseProfile.getGroupMember(groupId, this.userId)?.let {
            this.nickName = it.getNotEmptyName()
            this.headImage = it.avatar
        }
        this
    } else {
        EaseIM.getUserProvider()?.getSyncUser(this.userId)?.let {
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
